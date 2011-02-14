package com.bukkit.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.bukkit.Phaed.PreciousStones.PreciousStones;
import com.bukkit.Phaed.PreciousStones.Vector;
import com.bukkit.Phaed.PreciousStones.Field;
import com.bukkit.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;

/**
 * Holds all protected stones
 * 
 * @author Phaed
 */
public class ForceFieldManager
{
    protected final HashMap<Vector, ArrayList<Field>> chunkLists = new HashMap<Vector, ArrayList<Field>>();
    
    private Queue<Field> deletionQueue;
    private transient PreciousStones plugin;
    
    public ForceFieldManager(PreciousStones plugin)
    {
	initiate(plugin);
    }
    
    public void initiate(PreciousStones plugin)
    {
	this.plugin = plugin;
	this.deletionQueue = new LinkedList<Field>();
    }
    
    /**
     * Process pending deletions
     */
    public void flush()
    {
	while (deletionQueue.size() > 0)
	{
	    Field delfield = deletionQueue.poll();
	    
	    for (ArrayList<Field> c : chunkLists.values())
	    {
		for (Field field : c)
		{
		    if (field.getWorldId() == delfield.getWorldId())
			c.remove(field.getVector());
		}
	    }
	}
    }
    
    /**
     * Total number of forcefield stones
     */
    public int count()
    {
	int size = 0;
	
	for (ArrayList<Field> c : chunkLists.values())
	    size += c.size();
	
	return size;
    }
    
    /**
     * Exposes the chunklist
     */
    public HashMap<Vector, ArrayList<Field>> getChunkLists()
    {
	return chunkLists;
    }
    
    /**
     * Returns the settings for a specific pstone block type
     */
    public FieldSettings getFieldSettings(Block block)
    {
	return plugin.settings.getFieldSettings(block);
    }
    
    /**
     * Gets the field from source block
     */
    public FieldSettings getFieldSettings(Field field, World world)
    {
	Vector fieldvec = field.getVector();
	Block fieldblock = world.getBlockAt(fieldvec.getX(), fieldvec.getY(), fieldvec.getZ());
	
	return plugin.ffm.getFieldSettings(fieldblock);
    }
    
    /**
     * If its unbreakable or not
     */
    public boolean isBreakable(Block block)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(block);
	return fieldsettings == null ? false : fieldsettings.breakable;
    }
    
    /**
     * If any of the allowed playes are online
     */
    public boolean allowedAreOnline(Field field)
    {
	for (String playername : field.getAllAllowed())
	{
	    List<Player> players = plugin.getServer().matchPlayer(playername);
	    
	    for(Player player : players)
	    {
		if(player.getName().equals(playername))
		    return true;
	    }
	}
	
	return false;
    }
    
    /**
     * Gets the field from source block
     */
    public Field getField(Block fieldblock)
    {
	ArrayList<Field> c = chunkLists.get(new Vector(fieldblock.getChunk()));
	
	if (c != null)
	{
	    for (Field field : c)
	    {
		Vector blockvec = new Vector(fieldblock.getLocation());
		
		if (blockvec.equals(field.getVector()))
		{
		    if (field.getWorldId() == fieldblock.getWorld().getId())
			return field;
		}
	    }
	}
	return null;
    }
    
    /**
     * Looks for the block in our stone collection
     */
    public boolean isField(Block fieldblock)
    {
	return getField(fieldblock) != null;
    }
    
    /**
     * Returns the fields in the chunk and adjacent chunks
     */
    public ArrayList<Field> getFieldsInArea(Block blockInArea, int chunkradius)
    {
	ArrayList<Field> out = new ArrayList<Field>();
	Chunk chunk = blockInArea.getChunk();
	
	int xlow = chunk.getX() - chunkradius;
	int xhigh = chunk.getX() + chunkradius;
	int zlow = chunk.getZ() - chunkradius;
	int zhigh = chunk.getZ() + chunkradius;
	
	for (int x = xlow; x <= xhigh; x++)
	{
	    for (int z = zlow; z <= zhigh; z++)
	    {
		Chunk chnk = blockInArea.getWorld().getChunkAt(x, z);
		if (chunk != null)
		{
		    ArrayList<Field> c = chunkLists.get(new Vector(chnk));
		    
		    if (c != null)
		    {
			for (Field field : c)
			{
			    if (field.getWorldId() == blockInArea.getWorld().getId())
				out.add(field);
			}
		    }
		}
	    }
	}
	
	return out;
    }
    
    /**
     * Returns the fields in the chunk and adjacent chunks
     */
    public ArrayList<Field> getFieldsInArea(Player player, int chunkradius)
    {
	Block blockInArea = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getFieldsInArea(blockInArea, chunkradius);
    }
    
    /**
     * Returns the fields in the chunk and adjacent chunks
     */
    public ArrayList<Field> getFieldsInArea(Block blockInArea)
    {
	return getFieldsInArea(blockInArea, plugin.settings.chunksInLargestForceFieldArea);
    }
    
    /**
     * Returns all fields of the type
     */
    public ArrayList<Field> getFieldsOfType(int typeid, World world)
    {
	ArrayList<Field> fields = new ArrayList<Field>();
	
	for (ArrayList<Field> c : chunkLists.values())
	{
	    for (Field field : c)
	    {
		Block block = world.getBlockAt(field.getVector().getX(), field.getVector().getY(), field.getVector().getZ());
		
		if (field.getWorldId() != block.getWorld().getId())
		    continue;
		
		if (block.getTypeId() == typeid)
		    fields.add(field);
	    }
	}
	return fields;
    }
    
    /**
     * Returns the blocks that is originating the protective field the block is in. That the player is not allowed in
     */
    public ArrayList<Field> getSourceFields(Block blockInArea, String playerName)
    {
	if (blockInArea == null)
	    return null;
	
	ArrayList<Field> fields = new ArrayList<Field>();
	
	// look to see if the block is in a protected zone we are not allowed in
	
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNear(blockInArea) && (playerName == null || !field.isAllowed(playerName)))
	    {
		Block source = blockInArea.getWorld().getBlockAt(field.getVector().getX(), field.getVector().getY(), field.getVector().getZ());
		
		// if the forcefield's source is not a proper source type then delete the field
		
		if (!plugin.settings.isFieldType(source))
		    queueRelease(source);
		else
		    fields.add(field);
	    }
	}
	
	// delete pending fields
	flush();
	
	return fields;
    }
    
    /**
     * Returns the blocks that is originating the protective field the block is in
     */
    public ArrayList<Field> getSourceFields(Block blockInArea)
    {
	return getSourceFields(blockInArea, null);
    }
    
    /**
     * Returns the blocks that is originating the protective field the player is standing in
     */
    public ArrayList<Field> getSourceFields(Player player)
    {
	Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getSourceFields(block, null);
    }
    
    /**
     * Returns the blocks that is originating the protective field the player is standing in. That the player is not allowed in
     */
    public ArrayList<Field> getSourceFields(Player player, String playerName)
    {
	Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getSourceFields(block, playerName);
    }
    
    /**
     * Whether one of the fields the player is standing in is his
     */
    public boolean inOwnVector(Block blockInArea, String playerName)
    {
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNear(blockInArea) && field.isOwner(playerName))
		return true;
	}
	
	return false;
    }
    
    /**
     * Add allowed player to protected area
     */
    public boolean addAllowed(Block blockInArea, String owner, String allowedName)
    {
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNear(blockInArea))
	    {
		if (!field.isOwner(owner))
		    continue;
		
		return field.addAllowed(allowedName);
	    }
	}
	
	return false;
    }
    
    /**
     * Add allowed player to protected area
     */
    public int addAllowedAll(String owner, String allowedName)
    {
	int count = 0;
	
	for (ArrayList<Field> c : chunkLists.values())
	{
	    for (Field field : c)
	    {
		if (!field.isOwner(owner))
		    continue;
		
		if (field.addAllowed(allowedName))
		    count++;
	    }
	}
	
	return count;
    }
    
    /**
     * Remove allowed player from protected area
     */
    public boolean removeAllowed(Block blockInArea, String owner, String allowedName)
    {
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNear(blockInArea))
	    {
		if (!field.isOwner(owner))
		    continue;
		
		return field.removeAllowed(allowedName);
	    }
	}
	
	return false;
    }
    
    /**
     * Add allowed player to protected area
     */
    public int removeAllowedAll(String owner, String allowedName)
    {
	int count = 0;
	
	for (ArrayList<Field> c : chunkLists.values())
	{
	    for (Field field : c)
	    {
		if (!field.isOwner(owner))
		    continue;
		
		if (field.removeAllowed(allowedName))
		    count++;
	    }
	}
	
	return count;
    }
    
     /**
     * Determine whether a player is allowed on a field
     */
    public boolean isAllowed(Block fieldblock, String playerName)
    {
	Field field = getField(fieldblock);
	
	if (field != null)
	    return field.isAllowed(playerName);
	
	return false;
    }
    
    /**
     * Determine whether a player is the owner of the field
     */
    public boolean isOwner(Block fieldblock, String playerName)
    {
	Field field = getField(fieldblock);
	
	if (field != null)
	    return field.isOwner(playerName);
	
	return false;
    }
    
    /**
     * Return the owner of a field
     */
    public String getOwner(Block fieldblock)
    {
	Field field = getField(fieldblock);
	
	if (field != null)
	    return field.getOwner();
	
	return "";
    }
    
    /**
     * Return the owner of a field by passign a block in the area
     */
    public String getAreaOwner(Block blockInArea)
    {
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNear(blockInArea))
		return field.getOwner();
	}
	
	return "";
    }
    
    /**
     * If the block is touching a pstone block
     */
    public boolean touchingFieldBlock(Block block)
    {
	if (block == null)
	    return false;
	
	for (int x = -1; x <= 1; x++)
	{
	    for (int z = -1; z <= 1; z++)
	    {
		for (int y = -1; y <= 1; y++)
		{
		    if (x == 0 && y == 0 && z == 0)
			continue;
		    
		    Block surroundingblock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
		    
		    if (plugin.settings.isFieldType(surroundingblock))
			return true;
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Whether the block is in a build proteced area owned by someone else
     */
    public Field isPlaceProtected(Block blockInArea, Player player)
    {
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNear(blockInArea) && !field.isAllowed(player.getName()))
	    {
		FieldSettings fieldsettings = getFieldSettings(field, blockInArea.getWorld());
		if (fieldsettings != null)
		{
		    if (fieldsettings.guarddogMode && allowedAreOnline(field))
		    {
			plugin.cm.notifyGuardDog(player, field, "block placement");
			continue;
		    }
		    
		    if (fieldsettings.preventPlace)
			return field;
		}
	    }
	}
	
	return null;
    }
    
    /**
     * Whether the block is in a break protected area belonging to somebody else (not playerName)
     */
    public Field isDestroyProtected(Block blockInArea, Player player)
    {	
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNear(blockInArea) && (player == null || !field.isAllowed(player.getName())))
	    {
		FieldSettings fieldsettings = getFieldSettings(field, blockInArea.getWorld());
		if (fieldsettings != null)
		{
		    if (fieldsettings.guarddogMode && allowedAreOnline(field))
		    {
			plugin.cm.notifyGuardDog(player, field, "block destruction");
			continue;
		    }
		    
		    if (fieldsettings.preventDestroy)
			return field;
		}
	    }
	}
	
	return null;
    }
    
    /**
     * Whether the block is in a fire protected area belonging to somebody else (not playerName)
     */
    public Field isFireProtected(Block blockInArea, Player player)
    {
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNear(blockInArea) && (player == null || !field.isAllowed(player.getName())))
	    {
		FieldSettings fieldsettings = getFieldSettings(field, blockInArea.getWorld());
		if (fieldsettings != null)
		{
		    if (fieldsettings.guarddogMode && allowedAreOnline(field))
		    {
			plugin.cm.notifyGuardDog(player, field, "fire placement");
			continue;
		    }
		    
		    if (fieldsettings.preventFire)
			return field;
		}
	    }
	}
	
	return null;
    }
    
    /**
     * Whether the block is in a entry protected area belonging to somebody else (not playerName) Expands the protected area by one to more acurately predict block entry
     */
    public Field isEntryProtected(Block blockInArea, Player player)
    {
	for (Field field : getFieldsInArea(blockInArea))
	{
	    if (field.getVector().isNearPlusOne(blockInArea) && (player == null || !field.isAllowed(player.getName())))
	    {
		FieldSettings fieldsettings = getFieldSettings(field, blockInArea.getWorld());
		if (fieldsettings != null)
		{
		    if (fieldsettings.guarddogMode && allowedAreOnline(field))
		    {
			plugin.cm.notifyGuardDog(player, field, "fire");
			continue;
		    }
		    
		    return field;
		}
	    }
	}
	
	return null;
    }
    
    /**
     * Whether the protective block will cover someone elses unbreakable or forcefield blocks
     */
    public Field isInConflict(Block block, String placer)
    {
	if (plugin.settings.isFieldType(block))
	{
	    FieldSettings fieldsettings = getFieldSettings(block);
	    
	    Vector placedBlockVec = new Vector(block.getLocation(), fieldsettings.radius, fieldsettings.getHeight());
	    
	    for (Field field : getFieldsInArea(block))
	    {
		// if allowed, continue to check other fields
		
		if (field.isAllowed(placer))
		    continue;
		
		// check to see if any of our stones live whithin the newly placed
		// protective block's area
		
		if (placedBlockVec.isNear(field.getVector()))
		    return field;
		
		// check to see if were placing this stone whithin the area
		// of an already placed stone
		
		if (field.getVector().isNear(placedBlockVec))
		    return field;
	    }
	}
	else
	{
	    Vector placedBlockVec = new Vector(block.getLocation());
	    
	    for (Field field : getFieldsInArea(block))
	    {
		// if allowed, continue to check other fields
		
		if (field.isAllowed(placer))
		    continue;
		
		// check to see if were placing this stone whithin the area
		// of an already placed stone
		
		if (field.getVector().isNear(placedBlockVec))
		    return field;
	    }
	    
	}
	
	return null;
    }
    
    /**
     * Add stone to the collection
     */
    public void add(Block fieldblock, String owner)
    {
	FieldSettings fieldsettings = getFieldSettings(fieldblock);
	
	Vector chunkvec = new Vector(fieldblock.getChunk());
	Vector fieldvec = new Vector(fieldblock.getLocation(), fieldsettings.radius, fieldsettings.getHeight());
	
	ArrayList<Field> c = chunkLists.get(chunkvec);
	
	if (c != null)
	{
	    c.add(new Field(fieldvec, owner, new ArrayList<String>(), fieldblock.getWorld().getId()));
	}
	else
	{
	    ArrayList<Field> newc = new ArrayList<Field>();
	    newc.add(new Field(fieldvec, owner, new ArrayList<String>(), fieldblock.getWorld().getId()));
	    
	    chunkLists.put(chunkvec, newc);
	}
    }
    
    /**
     * Remove stones from the collection
     */
    public void release(Block fieldblock)
    {
	for (ArrayList<Field> c : chunkLists.values())
	{
	    Vector vec = new Vector(fieldblock.getLocation());
	    
	    for (Field field : c)
	    {
		if (field.getVector().equals(vec))
		{
		    if (field.getWorldId() == fieldblock.getWorld().getId())
			c.remove(vec);
		}
	    }
	}
    }
    
    /**
     * Remove stones from the collection
     */
    public void release(Vector vec, World world)
    {
	for (ArrayList<Field> c : chunkLists.values())
	{
	    for (Field field : c)
	    {
		if (field.getVector().equals(vec))
		{
		    if (field.getWorldId() == world.getId())
			c.remove(vec);
		}
	    }
	}
    }
    
    /**
     * Adds to deletion queue
     */
    public void queueRelease(Block fieldblock)
    {
	deletionQueue.add(new Field(new Vector(fieldblock.getLocation()), null, null, fieldblock.getWorld().getId()));
    }
}
