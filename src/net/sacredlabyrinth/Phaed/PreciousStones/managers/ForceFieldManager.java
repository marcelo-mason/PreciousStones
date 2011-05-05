package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.AllowedEntry;

import org.bukkit.entity.Player;
import org.bukkit.Chunk;
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

import org.bukkit.entity.Vehicle;

/**
 * Handles force-fields
 *
 * @author Phaed
 */
public class ForceFieldManager
{
    private Queue<Field> deletionQueue = new LinkedList<Field>();
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
     *
     * @param cv
     * @return all fields from database that match the chunkvec
     */
    public List<Field> retrieveFields(ChunkVec cv)
    {
        return plugin.getDatabase().find(Field.class).where().eq("chunkX", cv.getX()).eq("chunkZ", cv.getZ()).ieq("world", cv.getWorld()).findList();
    }

    /**
     *
     * @param world
     * @return all fields from the database that match the world
     */
    public List<Field> retrieveFields(String world)
    {
        return plugin.getDatabase().find(Field.class).where().ieq("world", world).findList();
    }

    /**
     *
     * @return all fields from the database
     */
    public List<Field> retrieveFields()
    {
        return plugin.getDatabase().find(Field.class).orderBy("chunkX").orderBy("chunkZ").findList();
    }

    /**
     * Gets the field from field block
     * @param block
     * @return
     */
    public Field getField(Block block)
    {
        return plugin.getDatabase().find(Field.class).where().eq("x", block.getX()).eq("y", block.getY()).eq("z", block.getZ()).ieq("world", block.getWorld().getName()).findUnique();
    }

    /**
     * Check if a field exists in our list
     * @param field
     * @return
     */
    public boolean existsField(Field field)
    {
        return plugin.getDatabase().find(Field.class).where().eq("id", field.getId()).findRowCount() > 0;
    }

    /**
     *
     * @param field
     */
    public void saveField(Field field)
    {
        try
        {
            plugin.getDatabase().save(field);
        }
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Error saving field: {0}", ex.getMessage());
        }
    }

    /**
     *
     * @param field
     */
    public void deleteField(Field field)
    {
        try
        {
            plugin.getDatabase().delete(field);
        }
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Error saving field: {0}", ex.getMessage());
        }
    }

    /**
     * Process pending deletions
     */
    public void flush()
    {
        while (deletionQueue.size() > 0)
        {
            Field pending = deletionQueue.poll();

            if (plugin.settings.dropOnDelete)
            {
                dropBlock(pending);
            }

            deleteField(pending);
        }
    }

    /**
     * Total number of forcefield stones
     * @return
     */
    public int getCount()
    {
        return plugin.getDatabase().find(Field.class).findRowCount();
    }

    /**
     * Clean up orphan fields
     * @param worldName
     * @return
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

        List<Field> fields = plugin.ffm.retrieveFields(world.getName());

        for (Field field : fields)
        {
            // ensure chunk is loaded prior to polling

            ChunkVec cv = field.getChunkVec();

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

            Block block = world.getBlockAt(field.getX(), field.getY(), field.getZ());

            if (!plugin.settings.isFieldType(block) && !(plugin.settings.isCloakableType(field.getTypeId()) && (plugin.settings.isCloakType(block) || plugin.settings.isCloakableType(block))))
            {
                cleanedCount++;
                queueRelease(field);
            }
        }

        flush();

        return cleanedCount;
    }

    /**
     * If its unbreakable or not
     * @param block
     * @return
     */
    public boolean isBreakable(Block block)
    {
        FieldSettings fieldsettings = plugin.settings.getFieldSettings(block.getTypeId());
        return fieldsettings == null ? false : fieldsettings.breakable;
    }

    /**
     * If any of the allowed players are online
     * @param field
     * @return
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
     * @return
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
     * Looks for the block in our field collection
     * @param fieldblock
     * @return
     */
    public boolean isField(Block fieldblock)
    {
        return getField(fieldblock) != null;
    }

    /**
     * Whether a Redstone hooked field is in a disabled state
     * @param field
     * @return
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
     * If there is current any where around the block
     * @param block
     * @return
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
     * @param blockInArea
     * @param chunkradius
     * @return
     */
    public List<Field> getFieldsInArea(Block blockInArea, int chunkradius)
    {
        List<Field> out = new LinkedList<Field>();
        Chunk chunk = blockInArea.getChunk();

        int xlow = chunk.getX() - chunkradius;
        int xhigh = chunk.getX() + chunkradius;
        int zlow = chunk.getZ() - chunkradius;
        int zhigh = chunk.getZ() + chunkradius;

        for (int x = xlow; x <= xhigh; x++)
        {
            for (int z = zlow; z <= zhigh; z++)
            {
                List<Field> fields = retrieveFields(new ChunkVec(x, z, blockInArea.getWorld().getName()));

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
     * @return
     */
    public List<Field> getFieldsInArea(Player player, int chunkradius)
    {
        Block blockInArea = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        return getFieldsInArea(blockInArea, chunkradius);
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     * @param blockInArea
     * @return
     */
    public List<Field> getFieldsInArea(Block blockInArea)
    {
        return getFieldsInArea(blockInArea, plugin.settings.chunksInLargestForceFieldArea);
    }

    /**
     * Returns all fields of the type
     * @param typeid
     * @param world
     * @return
     */
    public List<Field> getFieldsOfType(int typeid, World world)
    {
        List<Field> out = new LinkedList<Field>();
        List<Field> fields = retrieveFields();

        for (Field field : fields)
        {
            if (!field.getWorld().equals(world.getName()))
            {
                continue;
            }

            if (field.getTypeId() == typeid)
            {
                out.add(field);
            }
        }
        return out;
    }

    /**
     * Returns the blocks that is originating the protective field the block is in and that the player is not allowed in
     * @param blockInArea
     * @param playerName
     * @return
     */
    public List<Field> getSourceFields(Block blockInArea, String playerName)
    {
        List<Field> fields = new LinkedList<Field>();
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(blockInArea) && (playerName == null || !field.isAllowed(playerName)))
            {
                if (!plugin.settings.isFieldType(field.getTypeId()) && !plugin.settings.isCloakableType(field.getTypeId()))
                {
                    queueRelease(field);
                }
                else
                {
                    fields.add(field);
                }
            }
        }

        flush();

        return fields;
    }

    /**
     * Returns the blocks that are originating the protective fields the block is in
     * @param vehicle
     * @return
     */
    public List<Field> getSourceFields(Vehicle vehicle)
    {
        Block block = vehicle.getWorld().getBlockAt(vehicle.getLocation().getBlockX(), vehicle.getLocation().getBlockY(), vehicle.getLocation().getBlockZ());
        return getSourceFields(block, null);
    }

    /**
     * Returns the blocks that are originating the protective fields the block is in
     * @param blockInArea
     * @return
     */
    public List<Field> getSourceFields(Block blockInArea)
    {
        return getSourceFields(blockInArea, null);
    }

    /**
     * Returns the blocks that are originating the protective fields the player is standing in
     * @param player
     * @return
     */
    public List<Field> getSourceFields(Player player)
    {
        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        return getSourceFields(block, null);
    }

    /**
     * Returns the blocks that are originating the protective fields the player is standing in. That the player is not allowed in
     * @param player
     * @param playerName
     * @return
     */
    public List<Field> getSourceFields(Player player, String playerName)
    {
        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        return getSourceFields(block, playerName);
    }

    /*
     * Returns the blocks that are originating prevent entry fields in the players area
     */
    /**
     *
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

            if (fieldsettings.guarddogMode && allowedAreOnline(field))
            {
                plugin.cm.notifyGuardDog(player, field, "entry attempt");
                continue;
            }

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
     * @return
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
     * @return
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
     * @return
     */
    public HashSet<Field> getIntersecting(HashSet<Field> fields, Player player, HashSet<Field> total)
    {
        HashSet<Field> touching = new HashSet<Field>();

        List<Field> all = retrieveFields();

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

        return touching;
    }

    /**
     * Returns all overlapped force-fields
     * @param player
     * @param field
     * @return
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
     * @return
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
     * @return
     */
    public boolean cleanSnitchList(Player player, Field field)
    {
        FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

        if (fieldsettings.snitch)
        {
            List<SnitchEntry> ses = field.getSnitchList();

            for (SnitchEntry se : ses)
            {
                plugin.snm.deleteSnitchEntry(se);
            }

            field.cleanSnitchList();
            saveField(field);

            return true;
        }

        return false;
    }

    /**
     * Sets the name of the field and all intersecting fields
     * @param player
     * @param field
     * @param name
     * @return
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
                saveField(f);
                renamedCount++;
            }
        }
        return renamedCount;
    }

    /**
     * Delete fields
     * @param player
     * @param field
     * @return
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
     * Returns a list of players who are inside he overlapped fields
     * @param player
     * @param field
     * @return
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
     * @return
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
     * @return
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
                saveField(f);
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
     * @return
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
                    plugin.getDatabase().delete(ae);
                }

                saveField(f);
                removedCount++;
            }
        }

        return removedCount;
    }

    /**
     * Get all the fields belonging to player
     * @param player
     * @return
     */
    public List<Field> getOwnersFields(Player player)
    {
        List<Field> out = new LinkedList<Field>();
        List<Field> fields = retrieveFields();

        for (Field field : fields)
        {
            if (field.isOwner(player.getName()))
            {
                out.add(field);
            }
        }

        return out;
    }

    /**
     * Add allowed player to all your force fields
     * @param player
     * @param allowedName
     * @return
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
                saveField(field);
                allowedCount++;
            }
        }

        return allowedCount;
    }

    /**
     * Remove allowed player to all your force fields
     * @param player
     * @param allowedName
     * @return
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
                    plugin.getDatabase().delete(ae);
                }

                saveField(field);
                removedCount++;
            }
        }

        return removedCount;
    }

    /**
     * Determine whether a player is allowed on a field
     * @param fieldblock
     * @param playerName
     * @return
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
     * @return
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
     * @param fieldblock
     * @return
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
     * @param blockInArea
     * @return
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
     * If the block is touching a plugin block
     * @param block
     * @return
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
     * Whether the block is in a unprotectable prevention area
     * @param blockInArea
     * @return
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
     * @return
     */
    public Field isPlaceProtected(Block blockInArea, Player player)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(blockInArea) && (player == null || !field.isAllowed(player.getName())))
            {
                FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

                if (fieldsettings.guarddogMode && allowedAreOnline(field))
                {
                    plugin.cm.notifyGuardDog(player, field, "block placement");
                    continue;
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
     * @param blockInArea
     * @param player
     * @return
     */
    public Field isDestroyProtected(Block blockInArea, Player player)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(blockInArea) && (player == null || !field.isAllowed(player.getName())))
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
     * @param blockInArea
     * @param player
     * @return
     */
    public Field isFireProtected(Block blockInArea, Player player)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(blockInArea) && (player == null || !field.isAllowed(player.getName())))
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
     * @param blockInArea
     * @param player
     * @return
     */
    public Field isEntryProtected(Block blockInArea, Player player)
    {
        List<Field> fieldsinarea = getFieldsInArea(blockInArea);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(blockInArea) && (player == null || !field.isAllowed(player.getName())))
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
     * Whether the player is in a pvp protected area
     * @param player
     * @return
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
     * Whether the player is in a pvp protected area
     * @param player
     * @return
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
     * @return
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
     * Whether the block is in an explosion protected area
     * @param placedBlock
     * @return
     */
    public Field isExplosionProtected(Block placedBlock)
    {
        List<Field> fields = getSourceFields(placedBlock);

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings.preventExplosions)
            {
                if (fieldsettings.guarddogMode && allowedAreOnline(field))
                {
                    plugin.cm.notifyGuardDog(null, field, "creeper explosion");
                    continue;
                }
                return field;
            }
        }

        return null;
    }

    /**
     * Return the first field that conflicts with the unbreakable
     * @param placedBlock
     * @param placer
     * @return
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
     * @return
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
     * @return
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
     * Add stone to the collection
     * @param fieldblock
     * @param owner
     * @return
     */
    public boolean add(Block fieldblock, Player owner)
    {
        if (plugin.plm.isDisabled(owner))
        {
            return false;
        }

        FieldSettings fieldsettings = plugin.settings.getFieldSettings(fieldblock.getTypeId());
        Field field = new Field(fieldblock, fieldsettings.radius, fieldsettings.getHeight(), owner.getName(), "");

        saveField(field);
        return true;
    }

    /**
     * Remove stones from the collection
     * @param block
     */
    public void release(Block block)
    {
        Field field = getField(block);

        if (plugin.settings.dropOnDelete)
        {
            dropBlock(block);
        }

        deleteField(field);
    }

    /**
     * Remove stones from the collection
     * @param field
     */
    public void release(Field field)
    {
        if (plugin.settings.dropOnDelete)
        {
            dropBlock(field);
        }

        deleteField(field);
    }

    /**
     * Remove stones from the collection
     * @param field
     */
    public void silentRelease(Field field)
    {
        deleteField(field);
    }

    /**
     * Adds to deletion queue
     * @param fieldblock
     */
    public void queueRelease(Block fieldblock)
    {
        deletionQueue.add(getField(fieldblock));
    }

    /**
     * Adds to deletion queue
     * @param field
     */
    public void queueRelease(Field field)
    {
        deletionQueue.add(field);
    }

    /**
     * Drop block
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
     * Drop block
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
