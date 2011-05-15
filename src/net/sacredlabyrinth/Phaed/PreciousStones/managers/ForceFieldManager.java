package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.avaje.ebean.Page;
import com.avaje.ebean.PagingList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.AllowedEntry;

import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.SnitchEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.TargetBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.Location;

import org.bukkit.entity.Vehicle;

/**
 * Handles force-fields
 *
 * @author Phaed
 */
public class ForceFieldManager
{
    private final HashMap<String, HashMap<ChunkVec, LinkedList<Field>>> chunkLists = new HashMap<String, HashMap<ChunkVec, LinkedList<Field>>>();
    private Queue<Field> deletionQueue = new LinkedList<Field>();
    private Queue<Field> replacementQueue = new LinkedList<Field>();
    private PreciousStones plugin;

    /**
     *
     * @param plugin
     */
    public ForceFieldManager(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Loads all fields for a specific world into memory
     * @param world the world to load
     */
    public void loadWorld(String world)
    {
        int orphans = cleanOrphans(world);

        if (orphans > 0)
        {
            PreciousStones.log(Level.INFO, "{0} orphan fields: {1}", world, orphans);
        }

        int fields = importFromDatabase(world);

        PreciousStones.log(Level.INFO, "{0} fields: {1}", world, fields);
    }

    private int importFromDatabase(String world)
    {
        PagingList<Field> pages = plugin.getDatabase().find(Field.class).where().ieq("world", world).orderBy("chunk_x").orderBy("chunk_z").findPagingList(999999);

        for (int i = 0; i < pages.getTotalPageCount(); i++)
        {
            List<Field> fields = pages.getPage(i).getList();

            for (Field field : fields)
            {
                addToCollection(field);
            }
        }

        return pages.getTotalRowCount();
    }

    /**
     * Save field to database
     * @param field the field to save
     */
    public void saveField(Field field)
    {
        try
        {
            if (field.isDirty())
            {
                plugin.getDatabase().save(field);
                Field newfield = plugin.getDatabase().find(Field.class).where().eq("id", field.getId()).findUnique();
                replacementQueue.add(newfield);
            }
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Saves all fields to the database
     */
    public void saveAll()
    {
        for (HashMap<ChunkVec, LinkedList<Field>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (LinkedList<Field> fields : w.values())
                {
                    for (Field field : fields)
                    {
                        saveField(field);
                    }
                }
            }
        }
        processReplacementQueue();
    }

    /**
     * Replaces outdated references in memory with the new db references
     */
    public void processReplacementQueue()
    {
        for (Field field : replacementQueue)
        {
            addToCollection(field);
        }
    }

    /**
     * Check if a chunk contains a field
     * @param cv the chunk vec
     * @return whether the chunk contains fields
     */
    public boolean hasField(ChunkVec cv)
    {
        HashMap<ChunkVec, LinkedList<Field>> w = chunkLists.get(cv.getWorld());

        if (w != null)
        {
            if (w.containsKey(cv))
            {
                LinkedList<Field> c = w.get(cv);

                if (!c.isEmpty())
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieve all fields in a chunk
     * @param cv the chunk vector you want the fields from
     * @return all fields from database that match the chunkvec
     */
    public List<Field> retrieveFields(ChunkVec cv)
    {
        if (chunkLists.get(cv.getWorld()) == null)
        {
            return null;
        }

        return chunkLists.get(cv.getWorld()).get(cv);
    }

    /**
     * Retrieve all fields on a world
     * @param world the world you want the fields from
     * @return all fields from the database that match the world
     */
    public HashMap<ChunkVec, LinkedList<Field>> retrieveFields(String world)
    {
        return chunkLists.get(world);
    }

    /**
     * Gets the field object from a block, if the block is a field
     * @param block the block that is a field
     * @return the field object from the block
     */
    public Field getField(Block block)
    {
        HashMap<ChunkVec, LinkedList<Field>> w = chunkLists.get(block.getLocation().getWorld().getName());

        if (w != null)
        {
            LinkedList<Field> c = w.get(new ChunkVec(block));

            if (c != null)
            {
                int index = c.indexOf(new Vec(block));

                if (index > -1)
                {
                    return (c.get(index));
                }
            }
        }
        return null;
    }

    /**
     * Check if a field exists in memory
     * @param field
     * @return confirmation
     */
    public boolean existsField(Field field)
    {
        HashMap<ChunkVec, LinkedList<Field>> w = chunkLists.get(field.getWorld());

        if (w != null)
        {
            LinkedList<Field> c = w.get(field.toChunkVec());

            if (c != null)
            {
                int index = c.indexOf(field);

                if (index > -1)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Looks for the block in our field collection
     * @param fieldblock
     * @return confirmation
     */
    public boolean isField(Block fieldblock)
    {
        return getField(fieldblock) != null;
    }

    /**
     * Total number of forcefield stones
     * @return the count
     */
    public int getCount()
    {
        int size = 0;

        for (HashMap<ChunkVec, LinkedList<Field>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (LinkedList<Field> c : w.values())
                {
                    size += c.size();
                }
            }
        }
        return size;
    }

    /**
     * Clean up orphan fields
     * @param worldName
     * @return count of cleaned orphans
     */
    public int cleanOrphans(String worldName)
    {
        int cleanedCount = 0;
        boolean currentChunkLoaded = false;
        ChunkVec currentChunk = null;

        World world = plugin.getServer().getWorld(worldName);

        if (world == null)
        {
            return 0;
        }

        HashMap<ChunkVec, LinkedList<Field>> w = retrieveFields(worldName);

        if (w != null)
        {
            for (LinkedList<Field> fields : w.values())
            {
                for (Field field : fields)
                {
                    // ensure chunk is loaded prior to polling

                    ChunkVec cv = field.toChunkVec();

                    if (!cv.equals(currentChunk))
                    {
                        if (!currentChunkLoaded)
                        {
                            if (currentChunk != null)
                            {
                                world.unloadChunk(currentChunk.getX(), currentChunk.getZ());
                            }
                        }

                        currentChunkLoaded = world.isChunkLoaded(cv.getX(), cv.getZ());

                        if (!currentChunkLoaded)
                        {
                            world.loadChunk(cv.getX(), cv.getZ());
                        }

                        currentChunk = cv;
                    }

                    // do the deed

                    int type = world.getBlockTypeIdAt(field.getX(), field.getY(), field.getZ());

                    if (!plugin.settings.isFieldType(type) && !(plugin.settings.isCloakableType(type) && (plugin.settings.isCloakType(type) || plugin.settings.isCloakableType(type))))
                    {
                        cleanedCount++;
                        queueRelease(field);
                    }
                }
            }
        }

        flush();

        return cleanedCount;
    }

    /**
     * Whether the block is an unbreakable field
     * @param block
     * @return confirmation
     */
    public boolean isBreakable(Block block)
    {
        FieldSettings fieldsettings = plugin.settings.getFieldSettings(block.getTypeId());
        return fieldsettings == null ? false : fieldsettings.breakable;
    }

    /**
     * Whether any of the allowed players are online
     * @param field
     * @return confirmation
     */
    public boolean allowedAreOnline(Field field)
    {
        List<AllowedEntry> allowed = field.getAllAllowed();

        for (AllowedEntry ae : allowed)
        {
            if (Helper.matchExactPlayer(plugin, ae.getName()) != null)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the source block for the field
     * @param field
     * @return the source block
     */
    public Block getBlock(Field field)
    {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world == null)
        {
            return null;
        }

        return world.getBlockAt(field.getX(), field.getY(), field.getZ());
    }

    /**
     * Whether a Redstone hooked field is in a disabled state
     * @param field
     * @return confirmation
     */
    public boolean isRedstoneHookedDisabled(Field field)
    {
        Block block = plugin.ffm.getBlock(field);

        if (isAnywayPowered(block))
        {
            return false;
        }

        Material topmat = block.getRelative(BlockFace.UP).getType();

        if (topmat.equals(Material.STONE_PLATE) || topmat.equals(Material.WOOD_PLATE))
        {
            return true;
        }

        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    if (x == 0 && y == 0 && z == 0)
                    {
                        continue;
                    }

                    Block source = block.getRelative(x, y, z);

                    if ((source.getType().equals(Material.REDSTONE_WIRE) && source.getBlockPower() == 0))
                    {
                        return true;
                    }
                }
            }
        }

        Block up = block.getRelative(BlockFace.UP);
        Block down = block.getRelative(BlockFace.DOWN);
        Block west = block.getRelative(BlockFace.WEST);
        Block east = block.getRelative(BlockFace.EAST);
        Block north = block.getRelative(BlockFace.NORTH);
        Block south = block.getRelative(BlockFace.SOUTH);

        if (up.getType().equals(Material.REDSTONE_TORCH_OFF) || down.getType().equals(Material.REDSTONE_TORCH_OFF) || east.getType().equals(Material.REDSTONE_TORCH_OFF) || west.getType().equals(Material.REDSTONE_TORCH_OFF) || north.getType().equals(Material.REDSTONE_TORCH_OFF) || south.getType().equals(Material.REDSTONE_TORCH_OFF))
        {
            return true;
        }

        if (up.getType().equals(Material.STONE_BUTTON) || down.getType().equals(Material.STONE_BUTTON) || east.getType().equals(Material.STONE_BUTTON) || west.getType().equals(Material.STONE_BUTTON) || north.getType().equals(Material.STONE_BUTTON) || south.getType().equals(Material.STONE_BUTTON))
        {
            return true;
        }

        if (up.getType().equals(Material.LEVER) && up.getBlockPower() == 0 || down.getType().equals(Material.LEVER) && up.getBlockPower() == 0 || east.getType().equals(Material.LEVER) && up.getBlockPower() == 0 || west.getType().equals(Material.LEVER) && up.getBlockPower() == 0 || north.getType().equals(Material.LEVER) && up.getBlockPower() == 0 || south.getType().equals(Material.LEVER) && up.getBlockPower() == 0)
        {
            return true;
        }

        return false;
    }

    /**
     * Whether there is current any where around the block
     * @param block
     * @return confirmation
     */
    public boolean isAnywayPowered(Block block)
    {
        if (block.isBlockIndirectlyPowered() || block.isBlockPowered())
        {
            return true;
        }

        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    if (x == 0 && y == 0 && z == 0)
                    {
                        continue;
                    }

                    Block source = block.getRelative(x, y, z);

                    if (source.getType().equals(Material.REDSTONE_WIRE))
                    {
                        if (source.getBlockPower() > 0)
                        {
                            return true;
                        }
                    }

                }
            }
        }

        return false;
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     * @param loc
     * @param chunkradius
     * @return the fields
     */
    public List<Field> getFieldsInArea(Location loc, int chunkradius)
    {
        List<Field> out = new LinkedList<Field>();

        int xlow = (loc.getBlockX() >> 4) - chunkradius;
        int xhigh = (loc.getBlockX() >> 4) + chunkradius;
        int zlow = (loc.getBlockZ() >> 4) - chunkradius;
        int zhigh = (loc.getBlockZ() >> 4) + chunkradius;

        for (int x = xlow; x <= xhigh; x++)
        {
            for (int z = zlow; z <= zhigh; z++)
            {
                List<Field> fields = retrieveFields(new ChunkVec(x, z, loc.getWorld().getName()));

                if (fields != null)
                {
                    out.addAll(fields);
                }
            }
        }

        return out;
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     * @param player
     * @param chunkradius
     * @return the fields
     */
    public List<Field> getFieldsInArea(Player player, int chunkradius)
    {
        return getFieldsInArea(player.getLocation(), chunkradius);
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     * @param loc
     * @return the fields
     */
    public List<Field> getFieldsInArea(Location loc)
    {
        return getFieldsInArea(loc, plugin.settings.chunksInLargestForceFieldArea);
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     * @param blockInArea
     * @return the fields
     */
    public List<Field> getFieldsInArea(Block blockInArea)
    {
        return getFieldsInArea(blockInArea.getLocation(), plugin.settings.chunksInLargestForceFieldArea);
    }

    /**
     * Returns the blocks that is originating the protective field the block is in and that the player is not allowed in
     * @param loc
     * @param playerName
     * @return the fields
     */
    public List<Field> getSourceFields(Location loc, String playerName)
    {
        List<Field> fields = new LinkedList<Field>();
        List<Field> fieldsinarea = getFieldsInArea(loc);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(loc) && (playerName == null || !field.isAllowed(playerName)))
            {
                fields.add(field);
            }
        }

        flush();

        return fields;
    }

    /**
     * Returns the blocks that are originating the protective fields the block is in
     * @param loc
     * @return the fields
     */
    public List<Field> getSourceFields(Location loc)
    {
        return getSourceFields(loc, null);
    }

    /**
     * Returns the blocks that are originating the protective fields the block is in
     * @param vehicle
     * @return the fields
     */
    public List<Field> getSourceFields(Vehicle vehicle)
    {
        return getSourceFields(vehicle.getLocation(), null);
    }

    /**
     * Returns the blocks that are originating the protective fields the block is in
     * @param blockInArea
     * @return the fields
     */
    public List<Field> getSourceFields(Block blockInArea)
    {
        return getSourceFields(blockInArea.getLocation(), null);
    }

    /**
     * Returns the blocks that are originating the protective fields the player is standing in
     * @param player
     * @return the fields
     */
    public List<Field> getSourceFields(Player player)
    {
        return getSourceFields(player.getLocation(), null);
    }

    /**
     * Returns the blocks that are originating the protective fields the player is standing in. That the player is not allowed in
     * @param player
     * @param playerName
     * @return the fields
     */
    public List<Field> getSourceFields(Player player, String playerName)
    {
        return getSourceFields(player.getLocation(), playerName);
    }

    /**
     * Returns the blocks that are originating prevent entry fields in the players area
     * @param player
     * @return
     */
    public List<Field> getSourceEntryFields(Player player)
    {
        List<Field> out = new LinkedList<Field>();
        List<Field> fields = plugin.ffm.getSourceFields(player);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventEntry)
            {
                out.add(field);
            }
        }

        return out;
    }

    /**
     * Returns the field if he's standing in at least one allowed field
     * @param blockInArea
     * @param player
     * @return the field
     */
    public Field getOneAllowedField(Block blockInArea, Player player)
    {
        TargetBlock tb = new TargetBlock(player, 100, 0.2, plugin.settings.throughFields);

        if (tb != null)
        {
            Block targetblock = tb.getTargetBlock();

            if (targetblock != null)
            {
                if ((plugin.settings.isFieldType(targetblock) || plugin.settings.isCloakableType(targetblock)) && plugin.ffm.isField(targetblock))
                {
                    Field f = getField(targetblock);

                    if (f.isAllowed(player.getName()))
                    {
                        return f;
                    }
                }
            }
        }

        List<Field> sourcefields = getSourceFields(blockInArea);

        for (Field field : sourcefields)
        {
            if (field.isAllowed(player.getName()))
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Returns the field pointed at
     * @param blockInArea
     * @param player
     * @return the field
     */
    public Field getPointedField(Block blockInArea, Player player)
    {
        TargetBlock tb = new TargetBlock(player, 100, 0.2, plugin.settings.throughFields);

        if (tb != null)
        {
            Block targetblock = tb.getTargetBlock();

            if (targetblock != null)
            {
                if ((plugin.settings.isFieldType(targetblock) || plugin.settings.isCloakableType(targetblock)) && plugin.ffm.isField(targetblock))
                {
                    Field f = getField(targetblock);

                    if (f.isAllowed(player.getName()))
                    {
                        return f;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets all fields intersecting to the passed fields
     * @param fields
     * @param player
     * @param total
     * @return the fields
     */
    public HashSet<Field> getIntersecting(HashSet<Field> fields, Player player, HashSet<Field> total)
    {
        HashSet<Field> touching = new HashSet<Field>();

        HashMap<ChunkVec, LinkedList<Field>> w = chunkLists.get(player.getLocation().getWorld().getName());

        if (w != null)
        {
            for (LinkedList<Field> all : w.values())
            {
                for (Field otherfield : all)
                {
                    for (Field foundfield : fields)
                    {
                        if (foundfield.intersects(otherfield))
                        {
                            if (player != null && !otherfield.isAllowed(player.getName()))
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
        }
        return touching;
    }

    /**
     * Returns all overlapped force-fields
     * @param player
     * @param field
     * @return the fields
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

            if (start.isEmpty())
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
     * Get first snitch fields you're standing on that you're allowed on
     * @param block
     * @return the fields
     */
    public List<Field> getSnitchFields(Block block)
    {
        List<Field> out = new LinkedList<Field>();
        List<Field> total = getSourceFields(block);

        for (Field f : total)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(f);

            if (fieldsettings.snitch)
            {
                out.add(f);
            }
        }
        return out;
    }

    /**
     * Clean up snitch lists of the field
     * @param player
     * @param field
     * @return confirmation
     */
    public boolean cleanSnitchList(Player player, Field field)
    {
        FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

        if (fieldsettings.snitch)
        {
            List<SnitchEntry> ses = field.getSnitchList();

            for (SnitchEntry se : ses)
            {
                try
                {
                    plugin.snm.deleteSnitchEntry(se);
                }
                catch (Exception ex)
                {
                }
            }

            field.cleanSnitchList();
            return true;
        }

        return false;
    }

    /**
     * Sets the name of the field and all intersecting fields
     * @param player
     * @param field
     * @param name
     * @return count of fields set
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
                f.setFieldName(name);
                renamedCount++;
            }
        }
        return renamedCount;
    }

    /**
     * Returns a list of players who are inside he overlapped fields
     * @param player
     * @param field
     * @return list of player names
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
     * @param player
     * @param field
     * @return allowed entry object per user
     */
    public HashSet<AllowedEntry> getAllowed(Player player, Field field)
    {
        HashSet<AllowedEntry> allowed = new HashSet<AllowedEntry>();
        HashSet<Field> total = getOverlappedFields(player, field);

        for (Field f : total)
        {
            allowed.addAll(f.getAllAllowed());
        }

        return allowed;
    }

    /**
     * Add allowed player to overlapped force-fields
     * @param player
     * @param field
     * @param allowedName
     * @param perm
     * @return count of fields the player was allowed in
     */
    public int addAllowed(Player player, Field field, String allowedName, String perm)
    {
        HashSet<Field> total = getOverlappedFields(player, field);

        int allowedCount = 0;

        for (Field f : total)
        {
            if (!f.isAllowed(allowedName))
            {
                f.addAllowed(allowedName, perm);
                allowedCount++;
            }
        }
        return allowedCount;
    }

    /**
     * Get all the fields belonging to player
     * @param player
     * @return the fields
     */
    public List<Field> getOwnersFields(Player player)
    {
        List<Field> out = new LinkedList<Field>();

        for (HashMap<ChunkVec, LinkedList<Field>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (LinkedList<Field> fields : w.values())
                {
                    for (Field field : fields)
                    {
                        if (field.isOwner(player.getName()))
                        {
                            out.add(field);
                        }
                    }
                }
            }
        }
        return out;
    }

    /**
     * Add allowed player to all your force fields
     * @param player
     * @param allowedName
     * @param perm
     * @return count of fields allowed
     */
    public int allowAll(Player player, String allowedName, String perm)
    {
        List<Field> fields = getOwnersFields(player);

        int allowedCount = 0;

        for (Field field : fields)
        {
            if (!field.isAllowed(allowedName))
            {
                field.addAllowed(allowedName, perm);
                allowedCount++;
            }
        }

        return allowedCount;
    }

    /**
     * Remove allowed player from overlapped force-fields
     * @param player
     * @param allowedName
     * @param field
     * @return count of fields the player was removed from
     */
    public int removeAllowed(Player player, Field field, String allowedName)
    {
        HashSet<Field> total = getOverlappedFields(player, field);
        int removedCount = 0;

        for (Field f : total)
        {
            if (f.isAllowed(allowedName))
            {
                AllowedEntry ae = f.removeAllowed(allowedName);



                if (ae != null)
                {
                    try
                    {
                        plugin.getDatabase().delete(AllowedEntry.class, ae.getId());
                    }
                    catch (Exception ex)
                    {
                    }
                }

                removedCount++;
            }
        }

        return removedCount;
    }

    /**
     * Remove allowed player to all your force fields
     * @param player
     * @param allowedName
     * @return count of fields the player was removed from
     */
    public int removeAll(Player player, String allowedName)
    {
        List<Field> fields = getOwnersFields(player);

        int removedCount = 0;

        for (Field field : fields)
        {
            if (field.isAllowed(allowedName))
            {
                AllowedEntry ae = field.removeAllowed(allowedName);



                if (ae != null)
                {
                    try
                    {
                        plugin.getDatabase().delete(AllowedEntry.class, ae.getId());
                    }
                    catch (Exception ex)
                    {
                    }
                }

                removedCount++;
            }
        }

        return removedCount;
    }

    /**
     * Determine whether a player is allowed on a field
     * @param fieldblock
     * @param playerName
     * @return confirmation
     */
    public boolean isAllowed(Block fieldblock, String playerName)
    {
        Field field = getField(fieldblock);

        if (field != null)
        {
            return field.isAllowed(playerName);
        }
        return false;
    }

    /**
     * Determine whether a player is the owner of the field
     * @param fieldblock
     * @param playerName
     * @return confirmation
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
     * @param fieldblock a block which is a field
     * @return owner's name
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
     * Return the owner of a field by passing a block in the area
     * @param blockInArea a block inside a field
     * @return owner's name
     */
    public String getAreaOwner(Block blockInArea)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

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
     * Whether the block is touching a field block
     * @param block
     * @return the touching block, null if none
     */
    public Block touchingFieldBlock(Block block)
    {
        if (block == null)
        {
            return null;
        }

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

                    if (plugin.settings.isFieldType(surroundingblock) || plugin.settings.isCloakableType(surroundingblock))
                    {
                        if (plugin.ffm.isField(surroundingblock))
                        {
                            return surroundingblock;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Whether the block is in a unprotectable prevention field
     * @param blockInArea block placed
     * @return the unprotectable field, null if not near a prevent unprotectable field
     */
    public Field isUprotectableBlockField(Block blockInArea)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

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
     * Whether the block is in a build protected area owned by someone else, exclude unprotected guarddog fields
     * @param blockInArea
     * @param player
     * @return the field, null if its not
     */
    public Field isPlaceProtected(Block blockInArea, Player player)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(blockInArea) && (player == null || !field.isAllowed(player.getName())))
            {
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

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
     * @param blockInArea
     * @param player
     * @return the field, null if its not
     */
    public Field isDestroyProtected(Block blockInArea, Player player)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(blockInArea) && (player == null || !field.isAllowed(player.getName())))
            {
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

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
     * @param blockInArea
     * @param player
     * @return the field, null if its not
     */
    public Field isFireProtected(Block blockInArea, Player player)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(blockInArea) && (player == null || !field.isAllowed(player.getName())))
            {
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

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
     * @param loc
     * @param player
     * @return
     * @returnthe field, null if its not
     */
    public Field isEntryProtected(Location loc, Player player)
    {
        List<Field> fieldsinarea = getFieldsInArea(loc);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(loc) && (player == null || !field.isAllowed(player.getName())))
            {
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                if (fieldsettings.preventEntry)
                {
                    return field;
                }
            }
        }

        return null;
    }

    /**
     * Whether the player is in a mob damage protected area
     * @param player
     * @return the field, null if its not
     */
    public Field isMobDamageProtected(Player player)
    {
        List<Field> fields = getSourceFields(player);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventMobDamage)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the player is in a mob spawn protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isMobSpawnProtected(Location loc)
    {
        List<Field> fields = getSourceFields(loc);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventMobSpawn)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the player is in a animal spawn protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isAnimalSpawnProtected(Location loc)
    {
        List<Field> fields = getSourceFields(loc);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventAnimalSpawn)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the player is in a pvp protected area
     * @param player
     * @return the field, null if its not
     */
    public Field isPvPProtected(Player player)
    {
        List<Field> fields = getSourceFields(player);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventPvP)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the block is in a pvp protected area
     * @param block
     * @return the field, null if its not
     */
    public Field isPvPProtected(Block block)
    {
        List<Field> fields = getSourceFields(block);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventPvP)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the block is in a flow protected area
     * @param block
     * @return the field, null if its not
     */
    public Field isFlowProtected(Block block)
    {
        List<Field> fields = getSourceFields(block);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventFlow)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the block is in an explosion protected area
     * @param placedBlock
     * @return the field, null if its not
     */
    public Field isExplosionProtected(Block placedBlock)
    {
        List<Field> fields = getSourceFields(placedBlock);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventExplosions)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Return the first field that conflicts with the unbreakable
     * @param placedBlock
     * @param placer
     * @return the field, null if none found
     */
    public Field unbreakableConflicts(Block placedBlock, Player placer)
    {
        List<Field> fieldsinarea = getFieldsInArea(placedBlock);

        for (Field field : fieldsinarea)
        {
            FieldSettings fs = plugin.settings.getFieldSettings(field.getTypeId());

            if (fs.noConflict)
            {
                continue;
            }

            if (field.isAllowed(placer.getName()))
            {
                continue;
            }

            if (field.envelops(placedBlock))
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Return the first field that conflicts with the field block
     * @param placedBlock
     * @param placer
     * @return the field, null if none found
     */
    public Field fieldConflicts(Block placedBlock, Player placer)
    {
        FieldSettings fieldsettings = plugin.settings.getFieldSettings(placedBlock.getTypeId());

        if (fieldsettings.noConflict)
        {
            return null;
        }

        Field placedField = new Field(placedBlock, fieldsettings.radius, fieldsettings.getHeight());

        List<Field> fieldsinarea = getFieldsInArea(placedBlock);

        for (Field field : fieldsinarea)
        {
            FieldSettings fs = plugin.settings.getFieldSettings(field.getTypeId());

            if (fs.noConflict)
            {
                continue;
            }

            if (field.isAllowed(placer.getName()))
            {
                continue;
            }

            if (field.intersects(placedField))
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Get all touching fields
     * @param scopedBlock
     * @param materialInHand
     * @return the fields
     */
    public HashSet<Field> getTouchingFields(Block scopedBlock, Material materialInHand)
    {
        HashSet<Field> out = new HashSet<Field>();

        FieldSettings fieldsettings = plugin.settings.getFieldSettings(materialInHand.getId());

        Field placedField = new Field(scopedBlock, fieldsettings.radius, fieldsettings.getHeight());

        List<Field> fieldsinarea = getFieldsInArea(scopedBlock);

        for (Field field : fieldsinarea)
        {
            FieldSettings fs = plugin.settings.getFieldSettings(field.getTypeId());

            if (fs.noConflict)
            {
                continue;
            }

            if (field.intersects(placedField))
            {
                out.add(field);
            }
        }

        return out;
    }

    /**
     * Add a brand new field
     * @param fieldblock
     * @param owner
     * @return confirmation
     */
    public boolean add(Block fieldblock, Player owner)
    {
        if (plugin.plm.isDisabled(owner))
        {
            return false;
        }

        FieldSettings fieldsettings = plugin.settings.getFieldSettings(fieldblock.getTypeId());
        Field field = new Field(fieldblock, fieldsettings.radius, fieldsettings.getHeight(), owner.getName(), "");

        addToCollection(field);

        plugin.tm.tagChunk(field.toChunkVec());
        return true;
    }

    /**
     * Add the field to the collection, used by add()
     * @param field
     */
    public void addToCollection(Field field)
    {
        String world = field.getWorld();
        ChunkVec chunkvec = field.toChunkVec();

        HashMap<ChunkVec, LinkedList<Field>> w = chunkLists.get(world);

        if (w != null)
        {
            LinkedList<Field> c = w.get(chunkvec);

            if (c != null)
            {
                c.remove(field);
                c.add(field);
            }
            else
            {
                LinkedList<Field> newc = new LinkedList<Field>();
                newc.add(field);
                w.put(chunkvec, newc);
            }
        }
        else
        {
            HashMap<ChunkVec, LinkedList<Field>> _w = new HashMap<ChunkVec, LinkedList<Field>>();
            LinkedList<Field> _c = new LinkedList<Field>();

            _c.add(field);
            _w.put(chunkvec, _c);
            chunkLists.put(world, _w);
        }
    }

    /**
     * Deletes all fields belonging to a player
     * @param playerName the players
     * @return the count of deleted fields
     */
    public int deleteBelonging(String playerName)
    {
        int deletedFields = 0;

        for (HashMap<ChunkVec, LinkedList<Field>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (LinkedList<Field> fields : w.values())
                {
                    for (Field field : fields)
                    {
                        if (field.getOwner().equalsIgnoreCase(playerName))
                        {
                            queueRelease(field);
                            deletedFields++;
                        }
                    }
                }
            }
        }

        flush();

        return deletedFields;
    }

    /**
     * Deletes a field from the collection
     * @param block
     */
    public void release(Block block)
    {
        Field field = getField(block);

        deleteField(field);

        if (plugin.settings.dropOnDelete)
        {
            dropBlock(block);
        }
    }

    /**
     * Deletes a field from the collection
     * @param field
     */
    public void release(Field field)
    {
        deleteField(field);

        if (plugin.settings.dropOnDelete)
        {
            dropBlock(field);
        }
    }

    /**
     * Deletes a field silently (no drop)
     * @param field
     */
    public void silentRelease(Field field)
    {
        deleteField(field);
    }

    /**
     * Adds a field to deletion queue
     * @param fieldblock
     */
    public void queueRelease(Block fieldblock)
    {
        deletionQueue.add(getField(fieldblock));
    }

    /**
     * Adds a field to deletion queue
     * @param field
     */
    public void queueRelease(Field field)
    {
        deletionQueue.add(field);
    }

    /**
     * Delete fields in deletion queue
     */
    public void flush()
    {
        while (deletionQueue.size() > 0)
        {
            Field pending = deletionQueue.poll();

            deleteField(pending);

            if (plugin.settings.dropOnDelete)
            {
                dropBlock(pending);
            }
        }
    }

    /**
     * Saves a field from memory and from the database
     * @param field the field to delete
     */
    public void deleteField(Field field)
    {
        HashMap<ChunkVec, LinkedList<Field>> w = chunkLists.get(field.getWorld());

        if (w != null)
        {
            LinkedList<Field> c = w.get(field.toChunkVec());

            if (c != null)
            {
                c.remove(field);


            }
        }

        try
        {
            plugin.getDatabase().delete(Field.class, field.getId());
        }
        catch (Exception ex)
        {
        }

        plugin.tm.untagChunk(field.toChunkVec());
    }

    /**
     * Delete fields the overlapping fields the player is standing on
     * @param player
     * @param field
     * @return count of fields deleted
     */
    public int deleteFields(Player player, Field field)
    {
        HashSet<Field> total = getOverlappedFields(player, field);

        int deletedCount = 0;

        for (Field f : total)
        {
            plugin.ffm.queueRelease(f);
            deletedCount++;
        }

        if (deletedCount > 0)
        {
            flush();
        }
        return deletedCount;
    }

    /**
     * Drops a block
     * @param field
     */
    public void dropBlock(Field field)
    {
        World world = plugin.getServer().getWorld(field.getWorld());
        Block block = world.getBlockAt(field.getX(), field.getY(), field.getZ());

        if (plugin.settings.isFieldType(block) || plugin.settings.isCloakableType(block))
        {
            ItemStack is = new ItemStack(block.getTypeId(), 1);
            block.setType(Material.AIR);
            world.dropItemNaturally(block.getLocation(), is);
        }
    }

    /**
     *
     * Drops a block
     * @param block
     */
    public void dropBlock(Block block)
    {
        World world = block.getWorld();
        ItemStack is = new ItemStack(block.getTypeId(), 1);

        if (plugin.settings.isFieldType(block) || plugin.settings.isCloakableType(block))
        {
            block.setType(Material.AIR);
            world.dropItemNaturally(block.getLocation(), is);
        }
    }
}
