package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;

/**
 * Handles force-fields
 * 
 * @author Phaed
 */
public class ForceFieldManager
{
    private final HashMap<ChunkVec, LinkedList<Field>> chunkLists = new HashMap<ChunkVec, LinkedList<Field>>();
    
    private Queue<Field> deletionQueue = new LinkedList<Field>();
    private PreciousStones plugin;
    private boolean dirty = false;
    
    public ForceFieldManager(PreciousStones plugin)
    {
	this.plugin = plugin;
    }
    
    /**
     * Retrieve a copy of the chunk list
     */
    public HashMap<ChunkVec, LinkedList<Field>> getChunks()
    {
	HashMap<ChunkVec, LinkedList<Field>> out = new HashMap<ChunkVec, LinkedList<Field>>();
	out.putAll(chunkLists);
	return out;
    }
    
    /**
     * Import chunks to the chunklist
     */
    public void importChunks(HashMap<ChunkVec, LinkedList<Field>> chunks)
    {
	chunkLists.putAll(chunks);
    }
    
    /**
     * Whether we need to save
     */
    public boolean isDirty()
    {
	return dirty;
    }
    
    /**
     * force dirty
     */
    public void setDirty()
    {
	dirty = true;
    }
    
    /**
     * reset dirty
     */
    public void resetDirty()
    {
	dirty = false;
    }
    
    /**
     * Process pending deletions
     */
    public void flush()
    {
	while (deletionQueue.size() > 0)
	{
	    Field pending = deletionQueue.poll();
	    
	    LinkedList<Field> chunkfields = chunkLists.get(pending.getChunkVec());
	    
	    if (chunkfields != null)
	    {
		chunkfields.remove(pending);
		
		if (plugin.settings.dropOnDelete)
		{
		    dropBlock(pending);
		}
	    }
	    setDirty();
	}
    }
    
    /**
     * Total number of forcefield stones
     */
    public int count()
    {
	int size = 0;
	
	for (LinkedList<Field> c : chunkLists.values())
	{
	    size += c.size();
	}
	return size;
    }
    
    /**
     * If its unbreakable or not
     */
    public boolean isBreakable(Block block)
    {
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(block.getTypeId());
	return fieldsettings == null ? false : fieldsettings.breakable;
    }
    
    /**
     * If any of the allowed playes are online
     */
    public boolean allowedAreOnline(Field field)
    {
	ArrayList<String> allowed = field.getAllAllowed();
	
	for (String playername : allowed)
	{
	    if (Helper.matchExactPlayer(plugin, playername) != null)
	    {
		return true;
	    }
	}
	
	return false;
    }
    
    /**
     * Gets the field from field block
     */
    public Field getField(Block fieldblock)
    {
	LinkedList<Field> c = chunkLists.get(new ChunkVec(fieldblock.getChunk()));
	
	if (c != null)
	{
	    int index = c.indexOf(new Vec(fieldblock));
	    
	    if (index > -1)
	    {
		return (c.get(index));
	    }
	}
	return null;
    }
    
    /**
     * Looks for the block in our field collection
     */
    public boolean isField(Block fieldblock)
    {
	return getField(fieldblock) != null;
    }
    
    /**
     * Returns the fields in the chunk and adjacent chunks
     */
    public LinkedList<Field> getFieldsInArea(Block blockInArea, int chunkradius)
    {
	LinkedList<Field> out = new LinkedList<Field>();
	Chunk chunk = blockInArea.getChunk();
	
	int xlow = chunk.getX() - chunkradius;
	int xhigh = chunk.getX() + chunkradius;
	int zlow = chunk.getZ() - chunkradius;
	int zhigh = chunk.getZ() + chunkradius;
	
	for (int x = xlow; x <= xhigh; x++)
	{
	    for (int z = zlow; z <= zhigh; z++)
	    {
		LinkedList<Field> c = chunkLists.get(new ChunkVec(x, z, blockInArea.getWorld().getName()));
		
		if (c != null)
		{
		    out.addAll(c);
		}
	    }
	}
	
	return out;
    }
    
    /**
     * Returns the fields in the chunk and adjacent chunks
     */
    public LinkedList<Field> getFieldsInArea(Player player, int chunkradius)
    {
	Block blockInArea = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getFieldsInArea(blockInArea, chunkradius);
    }
    
    /**
     * Returns the fields in the chunk and adjacent chunks
     */
    public LinkedList<Field> getFieldsInArea(Block blockInArea)
    {
	return getFieldsInArea(blockInArea, plugin.settings.chunksInLargestForceFieldArea);
    }
    
    /**
     * Returns all fields of the type
     */
    public LinkedList<Field> getFieldsOfType(int typeid, World world)
    {
	LinkedList<Field> fields = new LinkedList<Field>();
	
	for (LinkedList<Field> c : chunkLists.values())
	{
	    for (Field field : c)
	    {
		if (!field.getWorld().equals(world.getName()))
		{
		    continue;
		}
		
		if (field.getTypeId() == typeid)
		{
		    fields.add(field);
		}
	    }
	}
	return fields;
    }
    
    /**
     * Returns the blocks that is originating the protective field the block is in and that the player is not allowed in
     */
    public LinkedList<Field> getSourceFields(Block blockInArea, String playerName)
    {
	LinkedList<Field> fields = new LinkedList<Field>();
	LinkedList<Field> fieldsinarea = getFieldsInArea(blockInArea);
	
	for (Field field : fieldsinarea)
	{
	    if (field.envelops(blockInArea) && (playerName == null || !field.isAllAllowed(playerName)))
	    {
		if (!plugin.settings.isFieldType(field.getTypeId()))
		{
		    queueRelease(field);
		}
		else
		{
		    fields.add(field);
		}
	    }
	}
	
	// delete pending fields
	flush();
	
	return fields;
    }
    
    /**
     * Returns the blocks that are originating the protective fields the block is in
     */
    public LinkedList<Field> getSourceFields(Block blockInArea)
    {
	return getSourceFields(blockInArea, null);
    }
    
    /**
     * Returns the blocks that are originating the protective fields the player is standing in
     */
    public LinkedList<Field> getSourceFields(Player player)
    {
	Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getSourceFields(block, null);
    }
    
    /**
     * Returns the blocks that are originating the protective fields the player is standing in. That the player is not allowed in
     */
    public LinkedList<Field> getSourceFields(Player player, String playerName)
    {
	Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	return getSourceFields(block, playerName);
    }
    
    /**
     * Returns the field if hes standing in at least one allowed field
     */
    public Field inOneAllowedVector(Block blockInArea, Player player)
    {
	LinkedList<Field> sourcefields = getSourceFields(blockInArea);
	
	for (Field field : sourcefields)
	{
	    if (field.isAllAllowed(player.getName()))
	    {
		return field;
	    }
	}
	
	return null;
    }
    
    /**
     * Gets all fields intersecting to the passed fields
     */
    public HashSet<Field> getIntersecting(HashSet<Field> fields, Player player, HashSet<Field> total)
    {
	HashSet<Field> touching = new HashSet<Field>();
	
	for (LinkedList<Field> c : chunkLists.values())
	{
	    for (Field otherfield : c)
	    {
		for (Field foundfield : fields)
		{
		    if (foundfield.intersects(otherfield))
		    {
			if (!otherfield.isAllAllowed(player.getName()))
			{
			    continue;
			}
			if (total.contains(otherfield))
			{
			    continue;
			}
			touching.add(otherfield);
		    }
		}
	    }
	}
	
	return touching;
    }
    
    /**
     * Renurns all overlapped force-fiels
     */
    public HashSet<Field> getOverlappedFields(Player player, Field field)
    {
	HashSet<Field> total = new HashSet<Field>();
	total.add(field);
	
	HashSet<Field> start = new HashSet<Field>();
	start.add(field);
	
	while (start.size() > 0)
	{
	    start = getIntersecting(start, player, total);
	    
	    if (start.size() == 0)
	    {
		return total;
	    }
	    else
	    {
		total.addAll(start);
	    }
	}
	
	return null;
    }
    
    /**
     * Sets the name of the field and all interseting fields
     */
    public int setNameFields(Player player, Field field, String name)
    {
	HashSet<Field> total = getOverlappedFields(player, field);
	
	int renamedCount = 0;
	
	for (Field f : total)
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(f);
	    
	    if (fieldsettings.nameable && !f.getName().equals(name))
	    {
		f.setName(name);
		renamedCount++;
	    }
	}
	
	if (renamedCount > 0)
	{
	    setDirty();
	}
	return renamedCount;
    }
    
    /**
     * Delete fields
     */
    public int deleteFields(Player player, Field field)
    {
	HashSet<Field> total = getOverlappedFields(player, field);
	
	int deletedCount = 0;
	
	for (Field f : total)
	{
	    plugin.ffm.release(f);
	    deletedCount++;
	}
	
	if (deletedCount > 0)
	{
	    setDirty();
	}
	return deletedCount;
    }
    
    /**
     * Returns a list of players who are inside he overlapped fields
     */
    public HashSet<String> getWho(Player player, Field field)
    {
	HashSet<Field> total = getOverlappedFields(player, field);
	HashSet<String> inhabitants = new HashSet<String>();
	
	for (Field f : total)
	{
	    HashSet<String> someInhabitants = plugin.em.getInhabitants(f);
	    inhabitants.addAll(someInhabitants);
	}
	
	return inhabitants;
    }
    
    /**
     * Get allowed players on the overlapped force-fields
     */
    public HashSet<String> getAllAllowed(Player player, Field field)
    {
	HashSet<String> allowed = new HashSet<String>();
	HashSet<Field> total = getOverlappedFields(player, field);
	
	for (Field f : total)
	{
	    allowed.addAll(f.getAllAllowed());
	}

	return allowed;
    }
    
    /**
     * Add allowed player to overlapped force-fields
     */
    public int addAllowed(Player player, Field field, String allowedName)
    {
	HashSet<Field> total = getOverlappedFields(player, field);
	
	int allowedCount = 0;
	
	for (Field f : total)
	{
	    if (!f.isAllAllowed(allowedName))
	    {
		f.addAllowed(allowedName);
		allowedCount++;
	    }
	}
	
	if (allowedCount > 0)
	{
	    setDirty();
	}
	return allowedCount;
    }
    
    /**
     * Remove allowed player from overlapped force-fields
     */
    public int removeAllowed(Player player, Field field, String allowedName)
    {
	HashSet<Field> total = getOverlappedFields(player, field);
	int removedCount = 0;
	
	for (Field f : total)
	{
	    if (f.isAllAllowed(allowedName))
	    {
		f.removeAllowed(allowedName);
		removedCount++;
	    }
	}
	
	if (removedCount > 0)
	{
	    setDirty();
	}
	return removedCount;
    }
    
    /**
     * Get all the fields belonging to player
     */
    public LinkedList<Field> getOwnersFields(Player player)
    {
	LinkedList<Field> out = new LinkedList<Field>();
	
	for (LinkedList<Field> fields : plugin.ffm.chunkLists.values())
	{
	    for (Field field : fields)
	    {
		if (field.isOwner(player.getName()))
		{
		    out.add(field);
		}
	    }
	}
	
	return out;
    }
    
    /**
     * Add allowed player to all your force fields
     */
    public int allowAll(Player player, String allowedName)
    {
	LinkedList<Field> fields = getOwnersFields(player);
	
	int allowedCount = 0;
	
	for (Field field : fields)
	{
	    if (!field.isAllowed(allowedName))
	    {
		field.addAllowed(allowedName);
		allowedCount++;
	    }
	}
	
	return allowedCount;
    }
    
    /**
     * Remove allowed player to all your force fields
     */
    public int removeAll(Player player, String allowedName)
    {
	LinkedList<Field> fields = getOwnersFields(player);
	
	int removedCount = 0;
	
	for (Field field : fields)
	{
	    if (field.isAllowed(allowedName))
	    {
		field.removeAllowed(allowedName);
		removedCount++;
	    }
	}
	
	return removedCount;
    }
    
    /**
     * Determine whether a player is allowed on a field
     */
    public boolean isAllowed(Block fieldblock, String playerName)
    {
	Field field = getField(fieldblock);
	
	if (field != null)
	{
	    return field.isAllAllowed(playerName);
	}
	return false;
    }
    
    /**
     * Determine whether a player is the owner of the field
     */
    public boolean isOwner(Block fieldblock, String playerName)
    {
	Field field = getField(fieldblock);
	
	if (field != null)
	{
	    return field.isOwner(playerName);
	}
	return false;
    }
    
    /**
     * Return the owner of a field
     */
    public String getOwner(Block fieldblock)
    {
	Field field = getField(fieldblock);
	
	if (field != null)
	{
	    return field.getOwner();
	}
	return "";
    }
    
    /**
     * Return the owner of a field by passign a block in the area
     */
    public String getAreaOwner(Block blockInArea)
    {
	LinkedList<Field> fieldsinarea = getFieldsInArea(blockInArea);
	
	for (Field field : fieldsinarea)
	{
	    if (field.envelops(blockInArea))
	    {
		return field.getOwner();
	    }
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
		    {
			continue;
		    }
		    
		    Block surroundingblock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
		    
		    if (plugin.settings.isFieldType(surroundingblock))
		    {
			return true;
		    }
		}
	    }
	}
	
	return false;
    }
    
    /**
     * Whether the block is in a unprotectable prevention area
     */
    public Field isUprotectableBlockField(Block blockInArea)
    {
	LinkedList<Field> fieldsinarea = getFieldsInArea(blockInArea);
	
	for (Field field : fieldsinarea)
	{
	    if (field.envelops(blockInArea))
	    {
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		
		if (fieldsettings.preventUnprotectable)
		{
		    return field;
		}
	    }
	}
	
	return null;
    }
    
    /**
     * Whether the block is in a build proteced area owned by someone else, exclude unprotected guarddogfields
     */
    public Field isPlaceProtected(Block blockInArea, Player player)
    {
	LinkedList<Field> fieldsinarea = getFieldsInArea(blockInArea);
	
	for (Field field : fieldsinarea)
	{
	    if (field.envelops(blockInArea) && !field.isAllAllowed(player.getName()))
	    {
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		
		if (fieldsettings.guarddogMode && allowedAreOnline(field))
		{
		    plugin.cm.notifyGuardDog(player, field, "block placement");
		    {
			continue;
		    }
		}
		
		if (fieldsettings.preventPlace)
		{
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
	LinkedList<Field> fieldsinarea = getFieldsInArea(blockInArea);
	
	for (Field field : fieldsinarea)
	{
	    if (field.envelops(blockInArea) && (player == null || !field.isAllAllowed(player.getName())))
	    {
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		
		if (fieldsettings.guarddogMode && allowedAreOnline(field))
		{
		    plugin.cm.notifyGuardDog(player, field, "block destruction");
		    continue;
		}
		
		if (fieldsettings.preventDestroy)
		{
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
	LinkedList<Field> fieldsinarea = getFieldsInArea(blockInArea);
	
	for (Field field : fieldsinarea)
	{
	    if (field.envelops(blockInArea) && (player == null || !field.isAllAllowed(player.getName())))
	    {
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		
		if (fieldsettings.guarddogMode && allowedAreOnline(field))
		{
		    plugin.cm.notifyGuardDog(player, field, "fire placement");
		    continue;
		}
		
		if (fieldsettings.preventFire)
		{
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
	LinkedList<Field> fieldsinarea = getFieldsInArea(blockInArea);
	
	for (Field field : fieldsinarea)
	{
	    if (field.envelops(blockInArea) && (player == null || !field.isAllAllowed(player.getName())))
	    {
		FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);
		
		if (fieldsettings.guarddogMode && allowedAreOnline(field))
		{
		    plugin.cm.notifyGuardDog(player, field, "fire");
		    continue;
		}
		return field;
	    }
	}
	
	return null;
    }
    
    /**
     * Whether the protective block will cover someone elses unbreakable or forcefield blocks
     */
    public Field isInConflict(Block placedBlock, String placer)
    {
	if (plugin.settings.isFieldType(placedBlock))
	{
	    FieldSettings fieldsettings = plugin.settings.getFieldSettings(placedBlock.getTypeId());
	    
	    Field placedField = new Field(placedBlock, fieldsettings.radius, fieldsettings.getHeight());
	    
	    LinkedList<Field> fieldsinarea = getFieldsInArea(placedBlock);
	    
	    for (Field field : fieldsinarea)
	    {
		if (field.isAllAllowed(placer))
		{
		    continue;
		}
		
		if (field.intersects(placedField))
		{
		    return field;
		}
	    }
	}
	else
	{
	    LinkedList<Field> fieldsinarea = getFieldsInArea(placedBlock);
	    
	    for (Field field : fieldsinarea)
	    {
		if (field.isAllAllowed(placer))
		{
		    continue;
		}
		
		if (field.envelops(placedBlock))
		{
		    return field;
		}
	    }
	}
	
	return null;
    }
    
    /**
     * Add stone to the collection
     */
    public boolean add(Block fieldblock, Player owner)
    {
	if(plugin.plm.isDisabled(owner))
	{
	    return false;
	}
	
	ChunkVec chunkvec = new ChunkVec(fieldblock.getChunk());
	FieldSettings fieldsettings = plugin.settings.getFieldSettings(fieldblock.getTypeId());
	Field field = new Field(fieldblock, fieldsettings.radius, fieldsettings.getHeight(), owner.getName(), new ArrayList<String>(), "");
	
	LinkedList<Field> c = chunkLists.get(new ChunkVec(fieldblock.getChunk()));
	
	if (c != null)
	{
	    if (!c.contains(field))
	    {
		c.add(field);
	    }
	}
	else
	{
	    LinkedList<Field> newc = new LinkedList<Field>();
	    newc.add(field);
	    chunkLists.put(chunkvec, newc);
	}
	
	setDirty();
	return true;
    }
    
    /**
     * Remove stones from the collection
     */
    public void release(Block fieldblock)
    {
	LinkedList<Field> c = chunkLists.get(new ChunkVec(fieldblock.getChunk()));
	
	c.remove(new Vec(fieldblock));
	
	if (plugin.settings.dropOnDelete)
	{
	    dropBlock(fieldblock);
	}
	
	setDirty();
    }
    
    /**
     * Remove stones from the collection
     */
    public void release(Field field)
    {
	LinkedList<Field> c = chunkLists.get(field.getChunkVec());
	
	c.remove(field);
	
	if (plugin.settings.dropOnDelete)
	{
	    dropBlock(field);
	}
	
	setDirty();
    }
    
    /**
     * Adds to deletion queue
     */
    public void queueRelease(Block fieldblock)
    {
	deletionQueue.add(new Field(fieldblock));
    }
    
    /**
     * Adds to deletion queue
     */
    public void queueRelease(Field field)
    {
	deletionQueue.add(field);
    }
    
    /**
     * Drop block
     */
    public void dropBlock(Field field)
    {
	World world = plugin.getServer().getWorld(field.getWorld());
	Block block = world.getBlockAt(field.getX(), field.getY(), field.getZ());
	ItemStack is = new ItemStack(block.getTypeId(), 1);
	
	block.setType(Material.AIR);
	world.dropItemNaturally(block.getLocation(), is);
    }
    
    /**
     * Drop block
     */
    public void dropBlock(Block block)
    {
	World world = block.getWorld();
	ItemStack is = new ItemStack(block.getTypeId(), 1);
	
	block.setType(Material.AIR);
	world.dropItemNaturally(block.getLocation(), is);
    }
}
