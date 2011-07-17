package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.register.payment.Method.MethodAccount;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;

import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.TargetBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 * Handles fields
 *
 * @author Phaed
 */
public final class ForceFieldManager
{
    private final HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> chunkLists = new HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>>();
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

        if (fieldsettings == null)
        {
            return false;
        }

        Field field = new Field(fieldblock, fieldsettings.radius, fieldsettings.getHeight(), fieldsettings.noOwner ? "Server" : owner.getName());

        // add to database (skip foresters and activate them)

        if (fieldsettings.forester || fieldsettings.foresterShrubs)
        {
            plugin.fm.add(field, owner.getName());
        }
        else
        {
            // if interval field then register it

            if (fieldsettings.griefUndoInterval)
            {
                plugin.gum.add(field);
            }

            // insert the field into database

            plugin.sm.insertField(field);

            // add field to memory
        }

        addToCollection(field);

        // tag the chunk

        plugin.tm.tagChunk(field.toChunkVec());
        return true;
    }

    /**
     * Deletes a field from memory and from the database
     * @param field the field to delete
     */
    public void deleteField(Field field)
    {
        HashMap<ChunkVec, HashMap<Vec, Field>> w = chunkLists.get(field.getWorld());

        if (w != null)
        {
            HashMap<Vec, Field> c = w.get(field.toChunkVec());

            if (c != null)
            {
                c.remove(field.toVec());
            }
        }

        FieldSettings fs = plugin.settings.getFieldSettings(field);

        // remove from forester

        if (fs != null && (fs.forester || fs.foresterShrubs))
        {
            plugin.fm.remove(field);
        }


        // delete any snitch entries

        if (fs != null && fs.snitch)
        {
            plugin.sm.deleteSnitchEntires(field);
        }

        // remove from grief-undo and delete any records on the database

        if (fs != null && (fs.griefUndoRequest || fs.griefUndoInterval))
        {
            plugin.gum.remove(field);
            plugin.sm.deleteBlockGrief(field);
        }

        // delete from database

        field.markForDeletion();
        plugin.sm.offerField(field);

        // see if we need to untag chunk

        plugin.tm.untagChunk(field.toChunkVec());
    }

    /**
     * Add the field to the collection, used by add()
     * @param field
     */
    public void addToCollection(Field field)
    {
        String world = field.getWorld();
        ChunkVec chunkvec = field.toChunkVec();

        HashMap<ChunkVec, HashMap<Vec, Field>> w = chunkLists.get(world);

        if (w != null)
        {
            HashMap<Vec, Field> c = w.get(chunkvec);

            if (c != null)
            {
                c.put(field.toVec(), field);
            }
            else
            {

                HashMap<Vec, Field> newc = new HashMap<Vec, Field>();
                newc.put(field.toVec(), field);
                w.put(chunkvec, newc);
            }
        }
        else
        {
            HashMap<ChunkVec, HashMap<Vec, Field>> _w = new HashMap<ChunkVec, HashMap<Vec, Field>>();
            HashMap<Vec, Field> _c = new HashMap<Vec, Field>();

            _c.put(field.toVec(), field);
            _w.put(chunkvec, _c);
            chunkLists.put(world, _w);
        }
    }

    /**
     * Check if a chunk contains a field
     * @param cv the chunk vec
     * @return whether the chunk contains fields
     */
    public boolean hasField(ChunkVec cv)
    {
        HashMap<ChunkVec, HashMap<Vec, Field>> w = chunkLists.get(cv.getWorld());

        if (w != null)
        {
            if (w.containsKey(cv))
            {
                HashMap<Vec, Field> c = w.get(cv);

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
    public Collection<Field> retrieveFields(ChunkVec cv)
    {
        if (chunkLists.get(cv.getWorld()) == null)
        {
            return null;
        }

        if (chunkLists.get(cv.getWorld()).get(cv) == null)
        {
            return null;
        }

        return chunkLists.get(cv.getWorld()).get(cv).values();
    }

    /**
     * Retrieve all fields on a world
     * @param world the world you want the fields from
     * @return all fields from the database that match the world
     */
    public HashMap<ChunkVec, HashMap<Vec, Field>> retrieveFields(String world)
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
        HashMap<ChunkVec, HashMap<Vec, Field>> w = chunkLists.get(block.getLocation().getWorld().getName());

        if (w != null)
        {
            HashMap<Vec, Field> c = w.get(new ChunkVec(block));

            if (c != null)
            {
                Field field = c.get(new Vec(block));

                if (field != null)
                {
                    if (field.getTypeId() != block.getTypeId())
                    {
                        deleteField(field);
                        return null;
                    }
                }

                return field;
            }
        }
        return null;
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

        for (HashMap<ChunkVec, HashMap<Vec, Field>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (HashMap<Vec, Field> c : w.values())
                {
                    size += c.size();
                }
            }
        }
        return size;
    }

    /**
     * Clean up orphan fields
     * @param world
     * @return
     */
    public int cleanOrphans(World world)
    {
        int cleanedCount = 0;
        boolean currentChunkLoaded = false;
        ChunkVec currentChunk = null;

        HashMap<ChunkVec, HashMap<Vec, Field>> w = retrieveFields(world.getName());

        if (w != null)
        {
            for (HashMap<Vec, Field> fields : w.values())
            {
                for (Field field : fields.values())
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

                    int type = world.getBlockTypeIdAt(field.getX(), field.getY(), field.getZ());

                    if (!plugin.settings.isFieldType(type))
                    {
                        cleanedCount++;
                        queueRelease(field);
                    }
                }
            }
        }
        flush();

        if (cleanedCount != 0)
        {
            PreciousStones.log(Level.INFO, "({0}) orphan-fields: {1}", world.getName(), cleanedCount);
        }
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
        List<String> allowed = field.getAllAllowed();

        for (String ae : allowed)
        {
            if (plugin.helper.matchSinglePlayer(ae) != null)
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

        BlockFace[] faces =
        {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP
        };

        for (BlockFace face : faces)
        {
            Block faceblock = block.getRelative(face);

            if (faceblock.getType().equals(Material.REDSTONE_TORCH_OFF))
            {
                return true;
            }

            if (faceblock.getType().equals(Material.STONE_BUTTON))
            {
                return true;
            }

            if (faceblock.getType().equals(Material.LEVER) && faceblock.getBlockPower() == 0)
            {
                return true;
            }
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

        BlockFace[] faces =
        {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
        };

        for (BlockFace face : faces)
        {
            Block source = block.getRelative(face);

            if (source.getType().equals(Material.REDSTONE_WIRE))
            {
                if (source.getBlockPower() > 0)
                {
                    return true;
                }
            }

        }

        return false;
    }

    /**
     * Send a message to all allowed players of the field
     * @param field
     * @param msg
     */
    public void announceAllowedPlayers(Field field, String msg)
    {
        List<String> allowed = field.getAllAllowed();

        for (String playerName : allowed)
        {
            Player pl = plugin.helper.matchSinglePlayer(playerName);

            if (pl != null)
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + " * " + ChatColor.AQUA + msg);
            }
        }
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

                    if (plugin.ffm.isField(surroundingblock))
                    {
                        return surroundingblock;
                    }
                }
            }
        }

        return null;
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
            field.clearSnitch();
            plugin.sm.deleteSnitchEntires(field);
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

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(f);
                continue;
            }

            if ((fieldsettings.farewellMessage || fieldsettings.welcomeMessage) && !f.getName().equals(name))
            {
                f.setName(name);
                plugin.sm.offerField(f);
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
     * Whether the player is allowed in the field
     * @param field
     * @param playerName
     * @return
     */
    public boolean isAllowed(Field field, String playerName)
    {
        Player player = plugin.helper.matchSinglePlayer(playerName);

        if (player != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.admin.allowed"))
            {
                return true;
            }
        }

        return field.isAllowed(playerName) || plugin.stm.isTeamMate(playerName, field.getOwner());
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

        if (field == null)
        {
            return false;
        }

        return isAllowed(field, playerName);
    }

    /**
     * Get allowed players on the overlapped fields
     * @param player
     * @param field
     * @return allowed entry object per user
     */
    public HashSet<String> getAllowed(Player player, Field field)
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
     * Add allowed player to overlapped fields
     * @param player
     * @param field
     * @param allowedName
     * @return count of fields the player was allowed in
     */
    public int addAllowed(Player player, Field field, String allowedName)
    {
        HashSet<Field> total = getOverlappedFields(player, field);

        int allowedCount = 0;

        for (Field f : total)
        {
            if (!f.isAllowed(allowedName))
            {
                f.addAllowed(allowedName);
                allowedCount++;
            }
            plugin.sm.offerField(field);
        }
        return allowedCount;
    }

    /**
     * Add allowed player to all your force fields
     * @param player
     * @param allowedName
     * @return count of fields allowed
     */
    public int allowAll(Player player, String allowedName)
    {
        List<Field> fields = getOwnersFields(player);

        int allowedCount = 0;

        for (Field field : fields)
        {
            if (!field.isAllowed(allowedName))
            {
                field.addAllowed(allowedName);
                allowedCount++;
            }
            plugin.sm.offerField(field);
        }

        return allowedCount;
    }

    /**
     * Remove allowed player from overlapped fields
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
                f.removeAllowed(allowedName);
                removedCount++;
            }
            plugin.sm.offerField(f);
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
                field.removeAllowed(allowedName);
                removedCount++;
            }
            plugin.sm.offerField(field);
        }

        return removedCount;
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
        List<Field> sourcefields = getSourceFields(blockInArea.getLocation());

        for (Field field : sourcefields)
        {
            return field.getOwner();
        }

        return "";
    }

    /**
     * Get all the fields belonging to player
     * @param player
     * @return the fields
     */
    public List<Field> getOwnersFields(Player player)
    {
        List<Field> out = new LinkedList<Field>();

        for (HashMap<ChunkVec, HashMap<Vec, Field>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (HashMap<Vec, Field> fields : w.values())
                {
                    for (Field field : fields.values())
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
     * Returns the fields in the chunk and adjacent chunks
     * @param loc
     * @return the fields
     */
    public List<Field> getFieldsInArea(Location loc)
    {
        return getFieldsInCustomArea(loc, plugin.settings.chunksInLargestForceFieldArea);
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     * @param field
     * @return the fields
     */
    public List<Field> getFieldsInArea(Field field)
    {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world == null)
        {
            return new LinkedList<Field>();
        }

        Location loc = new Location(world, field.getX(), field.getY(), field.getZ());

        return getFieldsInCustomArea(loc, plugin.settings.chunksInLargestForceFieldArea);
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     * @param loc
     * @param chunkradius
     * @return the fields
     */
    public List<Field> getFieldsInCustomArea(Location loc, int chunkradius)
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
                Collection<Field> fields = retrieveFields(new ChunkVec(x, z, loc.getWorld().getName()));

                if (fields != null)
                {
                    out.addAll(fields);
                }
            }
        }

        return out;
    }

    /**
     * Returns the fields that the location is in
     * @param loc
     * @return the fields
     */
    public List<Field> getSourceFields(Location loc)
    {
        List<Field> fields = new LinkedList<Field>();
        List<Field> fieldsinarea = getFieldsInArea(loc);

        for (Field field : fieldsinarea)
        {
            if (field.envelops(loc))
            {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Returns the fields that the location is in and that the player is allowed in
     * @param loc
     * @param playerName
     * @return the fields
     */
    public List<Field> getAllowedSourceFields(Location loc, String playerName)
    {
        List<Field> fields = new LinkedList<Field>();
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            if (isAllowed(field, playerName))
            {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Returns the fields that the location is in and that the player is not allowed in
     * @param loc
     * @param playerName
     * @return the fields
     */
    public List<Field> getNotAllowedSourceFields(Location loc, String playerName)
    {
        List<Field> fields = new LinkedList<Field>();
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            if (!isAllowed(field, playerName))
            {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Returns the prevent-entry fields that the location is in
     * @param player
     * @return
     */
    public List<Field> getNotAllowedEntryFields(Player player)
    {
        List<Field> out = new LinkedList<Field>();
        List<Field> fields = plugin.ffm.getNotAllowedSourceFields(player.getLocation(), player.getName());

        for (Field field : fields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
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
                if (plugin.ffm.isField(targetblock))
                {
                    Field f = getField(targetblock);

                    if (isAllowed(f, player.getName()))
                    {
                        return f;
                    }
                }
            }
        }

        List<Field> sourcefields = getAllowedSourceFields(blockInArea.getLocation(), player.getName());

        if (!sourcefields.isEmpty())
        {
            return sourcefields.get(0);
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
                if (plugin.ffm.isField(targetblock))
                {
                    Field f = getField(targetblock);

                    if (isAllowed(f, player.getName()))
                    {
                        return f;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns overlapped fields the player is allowed on
     * @param player
     * @param field
     * @return the fields
     */
    public HashSet<Field> getOverlappedFields(Player player, Field field)
    {
        HashSet<Field> total = new HashSet<Field>();
        total.add(field);

        HashSet<Field> newlyfound = new HashSet<Field>();
        newlyfound.add(field);

        while (newlyfound.size() > 0)
        {
            newlyfound = getIntersecting(player, total, field.getTypeId(), true);

            if (newlyfound.isEmpty())
            {
                return total;
            }
            else
            {
                total.addAll(newlyfound);
            }
        }

        return null;
    }

    /**
     * Returns overlapped fields belonging to any player
     * @param player
     * @param field
     * @return the fields
     */
    public HashSet<Field> getAllOverlappedFields(Player player, Field field)
    {
        HashSet<Field> total = new HashSet<Field>();
        total.add(field);

        HashSet<Field> newlyfound = new HashSet<Field>();
        newlyfound.add(field);

        while (newlyfound.size() > 0)
        {
            newlyfound = getIntersecting(player, total, field.getTypeId(), false);

            if (newlyfound.isEmpty())
            {
                return total;
            }
            else
            {
                total.addAll(newlyfound);
            }
        }

        return null;
    }

    /**
     * Gets all fields intersecting to the passed fields
     * @param player
     * @param total
     * @param fieldType
     * @param onlyallowed
     * @return the fields
     */
    public HashSet<Field> getIntersecting(Player player, HashSet<Field> total, int fieldType, boolean onlyallowed)
    {
        HashSet<Field> newlyfound = new HashSet<Field>();
        HashSet<Field> near = new HashSet<Field>();

        for (Field tf : total)
        {
            near.addAll(getFieldsInArea(tf));
        }

        for (Field nearfield : near)
        {
            if (nearfield.getTypeId() != fieldType)
            {
                continue;
            }

            if (total.contains(nearfield))
            {
                continue;
            }

            if (onlyallowed)
            {
                if (!isAllowed(nearfield, player.getName()))
                {
                    continue;
                }
            }

            for (Field foundfield : total)
            {
                if (foundfield.intersects(nearfield))
                {
                    newlyfound.add(nearfield);
                }
            }
        }

        return newlyfound;
    }

    /**
     * Gets snitch fields you're in
     * @param loc
     * @return the fields
     */
    public List<Field> getSnitchFields(Location loc)
    {
        List<Field> out = new LinkedList<Field>();
        List<Field> sourcefields = getSourceFields(loc);

        for (Field f : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(f);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(f);
                continue;
            }

            if (fieldsettings.snitch)
            {
                out.add(f);
            }
        }
        return out;
    }

    /**
     * Whether the location is in a unprotectable prevention field
     * @param loc
     * @return the unprotectable field, null if not near a prevent unprotectable field
     */
    public Field isUprotectableBlockField(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventUnprotectable)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is a grief protected field that the player is not allowed in
     * @param loc
     * @param player
     * @return the field, null if its not
     */
    public Field isGriefProtected(Location loc, Player player)
    {
        List<Field> sourcefields = getNotAllowedSourceFields(loc, player.getName());

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.griefUndoInterval || fieldsettings.griefUndoRequest)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is a grief protected field
     * @param loc
     * @return the field, null if its not
     */
    public Field isGriefProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.griefUndoInterval || fieldsettings.griefUndoRequest)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a build protected area that the player is not allowed on
     * @param loc
     * @param player
     * @return the field, null if its not
     */
    public Field isPlaceProtected(Location loc, Player player)
    {
        List<Field> sourcefields = getNotAllowedSourceFields(loc, player.getName());

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventPlace)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a break protected area that the player is not allowed in
     * @param loc
     * @param player
     * @return the field, null if its not
     */
    public Field isDestroyProtected(Location loc, Player player)
    {
        List<Field> sourcefields = getNotAllowedSourceFields(loc, player.getName());

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                deleteField(field);
                continue;
            }

            if (fieldsettings.preventDestroy)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a break protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isDestroyProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                deleteField(field);
                continue;
            }

            if (fieldsettings.preventDestroy)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a use protected area that the player is not allowed in
     * @param loc
     * @param player
     * @param type_id
     * @return the field, null if its not
     */
    public Field isUseProtected(Location loc, Player player, int type_id)
    {
        List<Field> sourcefields = getNotAllowedSourceFields(loc, player.getName());

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                deleteField(field);
                continue;
            }

            if (!fieldsettings.canUse(type_id))
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a fire protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isFireProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventFire)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a entry protected area that the player is not allowed in
     * @param loc
     * @param player
     * @return
     * @returnthe field, null if its not
     */
    public Field isEntryProtected(Location loc, Player player)
    {
        List<Field> sourcefields = getNotAllowedSourceFields(loc, player.getName());

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventEntry)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a mob damage protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isMobDamageProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventMobDamage)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a mob spawn protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isMobSpawnProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventMobSpawn)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a animal spawn protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isAnimalSpawnProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventAnimalSpawn)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a pvp protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isPvPProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventPvP)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the location is in a flow protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isFlowProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fieldsettings.preventFlow)
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Whether the block is in an explosion protected area
     * @param loc
     * @return the field, null if its not
     */
    public Field isExplosionProtected(Location loc)
    {
        List<Field> sourcefields = getSourceFields(loc);

        for (Field field : sourcefields)
        {
            FieldSettings fieldsettings = plugin.settings.getFieldSettings(field);

            if (fieldsettings == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

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
        List<Field> sourcefields = getSourceFields(placedBlock.getLocation());

        for (Field field : sourcefields)
        {
            FieldSettings fs = plugin.settings.getFieldSettings(field.getTypeId());

            if (fs == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fs.noConflict)
            {
                continue;
            }

            if (isAllowed(field, placer.getName()))
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

        if (fieldsettings == null)
        {
            return null;
        }

        if (fieldsettings.noConflict)
        {
            return null;
        }

        // create throwaway field to test intersection

        Field placedField = new Field(placedBlock, fieldsettings.radius, fieldsettings.getHeight());

        List<Field> fieldsinarea = getFieldsInArea(placedBlock.getLocation());

        for (Field field : fieldsinarea)
        {
            FieldSettings fs = plugin.settings.getFieldSettings(field.getTypeId());

            if (fs == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

            if (fs.noConflict)
            {
                continue;
            }

            if (isAllowed(field, placer.getName()))
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
     * Whether the pstone could be displaced by a piston
     * @param pstone
     * @param placer
     * @return
     */
    public Block getPistonConflictReverse(Block pstone, Player placer)
    {
        for (int x = -15; x <= 15; x++)
        {
            Block block = pstone.getRelative(x, 0, 0);

            if (block.getType().equals(Material.PISTON_BASE) || block.getType().equals(Material.PISTON_STICKY_BASE))
            {
                return block;
            }
        }

        for (int z = -15; z <= +15; z++)
        {
            Block block = pstone.getRelative(0, 0, z);

            if (block.getType().equals(Material.PISTON_BASE) || block.getType().equals(Material.PISTON_STICKY_BASE))
            {
                return block;
            }
        }

        return null;
    }

    /**
     * Whether the piston could displace a pstone
     * @param piston
     * @param placer
     * @return
     */
    public Field getPistonConflict(Block piston, Player placer)
    {
        for (int x = -15; x <= 15; x++)
        {
            Block block = piston.getRelative(x, 0, 0);
            Field field = getField(block);

            if (field != null)
            {
                return field;
            }
        }

        for (int z = -15; z <= +15; z++)
        {
            Block block = piston.getRelative(0, 0, z);
            Field field = getField(block);

            if (field != null)
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

        if (fieldsettings == null)
        {
            return out;
        }

        // create throwaway field to test intersection

        Field placedField = new Field(scopedBlock, fieldsettings.radius, fieldsettings.getHeight());

        List<Field> fieldsinarea = getFieldsInArea(scopedBlock.getLocation());

        for (Field field : fieldsinarea)
        {
            FieldSettings fs = plugin.settings.getFieldSettings(field.getTypeId());

            if (fs == null)
            {
                plugin.ffm.queueRelease(field);
                continue;
            }

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
     * Deletes all fields belonging to a player
     * @param playerName the players
     * @return the count of deleted fields
     */
    public int deleteBelonging(String playerName)
    {
        int deletedFields = 0;

        for (HashMap<ChunkVec, HashMap<Vec, Field>> w : chunkLists.values())
        {
            if (w != null)
            {
                for (HashMap<Vec, Field> fields : w.values())
                {
                    for (Field field : fields.values())
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
        Field field = getField(fieldblock);

        if (!deletionQueue.contains(field))
        {
            deletionQueue.add(field);
        }
    }

    /**
     * Adds a field to deletion queue
     * @param field
     */
    public void queueRelease(Field field)
    {
        if (!deletionQueue.contains(field))
        {
            deletionQueue.add(field);
        }
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
     * Delete fields the overlapping fields the player is standing on
     * @param player
     * @param field
     * @return count of fields deleted
     */
    public int deleteFields(Player player, Field field)
    {
        HashSet<Field> total = getAllOverlappedFields(player, field);

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

        if (plugin.settings.isFieldType(block))
        {
            ItemStack is = new ItemStack(block.getTypeId(), 1);
            block.setType(Material.AIR);
            world.dropItemNaturally(block.getLocation(), is);
        }
    }

    /**
     * Drops a block
     * @param block
     */
    public void dropBlock(Block block)
    {
        World world = block.getWorld();
        ItemStack is = new ItemStack(block.getTypeId(), 1);

        if (plugin.settings.isFieldType(block))
        {
            block.setType(Material.AIR);
            world.dropItemNaturally(block.getLocation(), is);
        }
    }

    /**
     * Removes money from player's account
     * @param player
     * @param price
     * @return
     */
    public boolean purchase(Player player, int price)
    {
        if (plugin.Method != null)
        {
            MethodAccount account = plugin.Method.getAccount(player.getName());

            if (account.hasEnough(price))
            {
                account.subtract(price);
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You do not have sufficient money in your account");
                return false;
            }
        }

        return true;
    }

    /**
     * Credits money back to player's account
     * @param player
     * @param price
     */
    public void refund(Player player, int price)
    {
        if (plugin.Method != null)
        {
            MethodAccount account = plugin.Method.getAccount(player.getName());
            account.add(price);
            player.sendMessage(ChatColor.AQUA + "Your account has been credited");

        }
    }
}
