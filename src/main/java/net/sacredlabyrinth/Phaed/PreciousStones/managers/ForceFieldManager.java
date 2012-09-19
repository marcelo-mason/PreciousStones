package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.ForesterEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Handles fields
 *
 * @author Phaed
 */
public final class ForceFieldManager
{
    private final HashMap<FieldFlag, HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>>> fieldLists = new HashMap<FieldFlag, HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>>>();
    private final HashMap<ChunkVec, HashMap<FieldFlag, List<Field>>> sourceFields = new HashMap<ChunkVec, HashMap<FieldFlag, List<Field>>>();

    private Queue<Field> deletionQueue = new LinkedList<Field>();
    private PreciousStones plugin;

    /**
     *
     */
    public ForceFieldManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Clear out the fields in memory
     */
    public void clearChunkLists()
    {
        fieldLists.clear();
        sourceFields.clear();
    }

    /**
     * Prints out a tree of all source fields to the log
     */
    public void drawSourceFields()
    {
        for (ChunkVec cv : sourceFields.keySet())
        {
            PreciousStones.getLog().info(cv.toString());

            HashMap<FieldFlag, List<Field>> flagList = sourceFields.get(cv);

            for (FieldFlag flag : flagList.keySet())
            {
                PreciousStones.getLog().info("   -" + flag);

                List<Field> fields = flagList.get(flag);

                for (Field field : fields)
                {
                    PreciousStones.getLog().info("     -" + Helper.toLocationString(field.getLocation()));
                }
            }
        }
    }

    /**
     * Add a brand new field
     *
     * @param fieldBlock
     * @param player
     * @param event
     * @return confirmation
     */
    public Field add(Block fieldBlock, Player player, BlockPlaceEvent event)
    {
        boolean notify = true;

        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(fieldBlock);

        if (fs == null)
        {
            return null;
        }

        // deny if world is blacklisted

        if (plugin.getSettingsManager().isBlacklistedWorld(fieldBlock.getWorld()))
        {
            return null;
        }

        // check if in worldguard region

        if (!plugin.getWorldGuardManager().canBuildField(player, fieldBlock, fs))
        {
            ChatBlock.sendMessage(player, "The field intersects with a WorldGuard region you are not allowed in.");
            event.setCancelled(true);
            return null;
        }

        // check if the pstone limit has been reached by the player

        if (plugin.getLimitManager().reachedLimit(player, fs))
        {
            event.setCancelled(true);
            return null;
        }

        // purchase pstone

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.purchase"))
        {
            if (fs.getPrice() > 0 && !purchase(player, fs.getPrice()))
            {
                return null;
            }
        }

        String owner = fs.hasDefaultFlag(FieldFlag.NO_OWNER) ? "Server" : player.getName();
        boolean isChild = false;
        boolean isImport = false;
        Field field;

        // create field

        if (plugin.getCuboidManager().hasOpenCuboid(player))
        {
            CuboidEntry ce = plugin.getCuboidManager().getOpenCuboid(player);

            if ((ce.getField().getSettings().getMixingGroup() != fs.getMixingGroup() || fs.getMixingGroup() == 0) && !ce.getField().getSettings().getTypeEntry().equals(fs.getTypeEntry()))
            {
                plugin.getCuboidManager().cancelOpenCuboid(player);
                ChatBlock.sendMessage(player, "Cannot mix fields that are not in the same mixing group.");
                event.setCancelled(true);
                return null;
            }

            if (fs.getPrice() > ce.getField().getSettings().getPrice())
            {
                plugin.getCuboidManager().cancelOpenCuboid(player);
                ChatBlock.sendMessage(player, "Cannot add on properties of more valuable fields");
                event.setCancelled(true);
                return null;
            }

            field = new Field(fieldBlock, 0, 0, owner);

            // set up parent/child relationship

            ce.getField().addChild(field);
            field.setParent(ce.getField());
            isChild = true;
            notify = false;

            // import field flags

            if (!ce.getField().getTypeEntry().equals(fs.getTypeEntry()))
            {
                ce.getField().importFlags(fs.getDefaultFlags());
                ChatBlock.sendMessage(player, ChatColor.YELLOW + Helper.capitalize(fs.getTitle()) + "'s field flags imported");
                isImport = true;
            }

        }
        else
        {
            field = new Field(fieldBlock, fs.getRadius(), fs.getHeight(), owner);
        }

        field.setSettings(fs);

        String clan = plugin.getSimpleClansManager().getClan(player.getName());

        if (clan != null)
        {
            field.addAllowed("c:" + clan);
        }

        // add to database (skip foresters and activate them)

        if (fs.hasDefaultFlag(FieldFlag.FORESTER))
        {
            ForesterEntry fe = new ForesterEntry(field, player.getName());
        }
        else
        {
            // add count to player data

            PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player.getName());
            data.incrementFieldCount(field.getSettings().getTypeEntry());
            plugin.getStorageManager().offerPlayer(player.getName());

            // open cuboid definition

            if (field.hasFlag(FieldFlag.CUBOID) && !isImport)
            {
                if (isChild)
                {
                    plugin.getCuboidManager().openChild(player, field);
                }
            }

            // insert the field into database

            plugin.getStorageManager().insertField(field);
        }

        // add to collection

        addToCollection(field);

        // visualize the field

        if (field.hasFlag(FieldFlag.VISUALIZE_ON_PLACE) && !isChild)
        {
            plugin.getVisualizationManager().visualizeSingleFieldFast(player, field);
        }

        if (notify)
        {
            if (field.hasFlag(FieldFlag.BREAKABLE))
            {
                plugin.getCommunicationManager().notifyPlaceBreakableFF(player, fieldBlock);
            }
            else
            {
                plugin.getCommunicationManager().notifyPlaceFF(player, fieldBlock);
            }
        }

        return field;
    }

    /**
     * Add the field to the collection, used by add()
     *
     * @param field
     */
    public void addToCollection(Field field)
    {
        String world = field.getWorld();

        ChunkVec cv = field.toChunkVec();

        List<FieldFlag> flags = new ArrayList<FieldFlag>();
        flags.addAll(field.getSettings().getDefaultFlags());
        flags.addAll(field.getInsertedFlags());

        for (FieldFlag flag : flags)
        {
            // add to fields collection

            HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(flag);

            if (wLists == null)
            {
                wLists = new HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>>();
            }

            HashMap<ChunkVec, HashMap<Vec, Field>> w = wLists.get(world);

            if (w == null)
            {
                w = new HashMap<ChunkVec, HashMap<Vec, Field>>();
            }

            HashMap<Vec, Field> c = w.get(cv);

            if (c == null)
            {
                c = new HashMap<Vec, Field>();
            }

            c.put(field.toVec(), field);
            w.put(cv, c);
            wLists.put(world, w);
            fieldLists.put(flag, wLists);
        }

        // add to sources collection

        addSourceField(field);
    }

    /**
     * Add a fields envoleped chunks to the source fields collection
     *
     * @param field
     */
    public void addSourceField(Field field)
    {
        Set<ChunkVec> scvs = field.getEnvelopingChunks();

        for (ChunkVec scv : scvs)
        {
            HashMap<FieldFlag, List<Field>> sf = sourceFields.get(scv);

            if (sf == null)
            {
                sf = new HashMap<FieldFlag, List<Field>>();
            }

            List<FieldFlag> flags = new ArrayList<FieldFlag>();
            flags.addAll(field.getSettings().getDefaultFlags());
            flags.addAll(field.getInsertedFlags());

            for (FieldFlag flag : flags)
            {
                List<Field> fields = sf.get(flag);

                if (fields == null)
                {
                    fields = new ArrayList<Field>();
                }

                if (!fields.contains(field))
                {
                    fields.add(field);
                }

                sf.put(flag, fields);
            }

            sourceFields.put(scv, sf);
        }
    }

    /**
     * Deletes a field from memory and from the database
     *
     * @param field the field to delete
     */
    public void deleteField(final Field field)
    {
        if (field == null)
        {
            return;
        }

        // remove from fields collection

        if (field.getSettings() != null)
        {
            List<FieldFlag> flags = new ArrayList<FieldFlag>();
            flags.addAll(field.getSettings().getDefaultFlags());
            flags.addAll(field.getInsertedFlags());

            for (FieldFlag flag : flags)
            {
                HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(flag);

                if (wLists == null)
                {
                    return;
                }

                HashMap<ChunkVec, HashMap<Vec, Field>> w = wLists.get(field.getWorld());

                if (w != null)
                {
                    HashMap<Vec, Field> c = w.get(field.toChunkVec());

                    if (c != null)
                    {
                        c.remove(field.toVec());
                    }
                }
            }
        }

        // remove from sources collection

        removeSourceField(field);

        FieldSettings fs = field.getSettings();

        if (fs != null)
        {
            List<FieldFlag> flags = new ArrayList<FieldFlag>();
            flags.addAll(fs.getDefaultFlags());
            flags.addAll(field.getInsertedFlags());

            // delete any snitch entries

            if (flags.contains(FieldFlag.SNITCH))
            {
                plugin.getStorageManager().deleteSnitchEntries(field);
            }

            // remove from grief-undo and delete any records on the database

            if (flags.contains(FieldFlag.GRIEF_REVERT))
            {
                plugin.getGriefUndoManager().remove(field);
                plugin.getStorageManager().deleteBlockGrief(field);
            }
        }

        // remove all people as having entered the field

        plugin.getEntryManager().removeAllPlayers(field);

        // remove the count from the owner

        PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(field.getOwner());
        data.decrementFieldCount(field.getSettings().getTypeEntry());
        plugin.getStorageManager().offerPlayer(field.getOwner());

        // delete siblings and parent if exists

        if (field.isParent())
        {
            for (Field c : field.getChildren())
            {
                c.clearParent();
                queueRelease(c);
            }

            field.clearChildren();
        }

        // if the child's parent is not open, then remove the whole family

        if (field.isChild())
        {
            if (!field.getParent().isOpen())
            {
                queueRelease(field.getParent());
            }
            else
            {
                Field parent = field.getParent();

                for (Field s : parent.getChildren())
                {
                    s.clearParent();
                    queueRelease(s);
                }

                parent.clearChildren();
                queueRelease(parent);
            }
        }

        dropBlock(field);

        // delete from database

        field.markForDeletion();
        plugin.getStorageManager().offerField(field);
    }

    /**
     * Remove a field's enveloped chunks from the source fields collection
     *
     * @param field
     */
    public void removeSourceField(Field field)
    {
        Set<ChunkVec> scvs = field.getEnvelopingChunks();

        for (ChunkVec scv : scvs)
        {
            HashMap<FieldFlag, List<Field>> sf = sourceFields.get(scv);

            if (sf != null)
            {
                List<FieldFlag> flags = new ArrayList<FieldFlag>();
                flags.addAll(field.getSettings().getDefaultFlags());
                flags.addAll(field.getInsertedFlags());

                for (FieldFlag flag : flags)
                {
                    List<Field> fields = sf.get(flag);

                    if (fields != null)
                    {
                        fields.remove(field);

                        if (fields.isEmpty())
                        {
                            sf.remove(flag);
                        }
                    }
                }

                if (sf.isEmpty())
                {
                    sourceFields.remove(scv);
                }
            }
        }
    }

    /**
     * Get all fields a player/g:group/c:clan/* is allowed in for a world
     *
     * @param target
     * @param world
     * @return
     */
    public List<Field> getFields(String target, World world)
    {
        List<Field> out = new ArrayList<Field>();

        HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(FieldFlag.ALL);

        if (wLists != null)
        {
            HashMap<ChunkVec, HashMap<Vec, Field>> w = wLists.get(world.getName());

            if (w != null)
            {
                for (ChunkVec cv : w.keySet())
                {
                    HashMap<Vec, Field> c = w.get(cv);

                    for (Field field : c.values())
                    {
                        if (target.equals("*"))
                        {
                            out.add(field);
                            continue;
                        }

                        if (target.contains("g:"))
                        {
                            String group = target.substring(2);

                            if (plugin.getPermissionsManager().inGroup(field.getOwner(), world, group))
                            {
                                out.add(field);
                            }
                            continue;
                        }

                        if (target.contains("c:"))
                        {
                            String clan = target.substring(2);

                            if (plugin.getSimpleClansManager().isInClan(field.getOwner(), clan))
                            {
                                out.add(field);
                            }
                            continue;
                        }

                        if (field.isOwner(target))
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
     * Get all fields on a world, optionally based on field flags
     *
     * @param worldName
     * @return all fields from the database that match the world
     */
    public HashMap<ChunkVec, HashMap<Vec, Field>> getFields(String worldName, FieldFlag flag)
    {
        HashMap<ChunkVec, HashMap<Vec, Field>> out = new HashMap<ChunkVec, HashMap<Vec, Field>>();

        HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(flag);

        if (wLists == null)
        {
            return null;
        }

        HashMap<ChunkVec, HashMap<Vec, Field>> w = wLists.get(worldName);

        if (w == null)
        {
            return null;
        }

        out.putAll(w);
        return out;
    }

    /**
     * Gets the field object from a block, if the block is a field
     *
     * @param block the block that is a field
     * @return the field object from the block
     */
    public Field getField(Block block)
    {
        return getField(block.getLocation(), block.getTypeId(), block.getData());
    }

    /**
     * Gets the field object from a block, if the block is a field
     *
     * @param type the block type id
     * @param data the data value
     * @return the field object from the block
     */
    public Field getField(Location location, int type, byte data)
    {
        HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(FieldFlag.ALL);

        if (wLists == null)
        {
            return null;
        }

        HashMap<ChunkVec, HashMap<Vec, Field>> w = wLists.get(location.getWorld().getName());

        if (w != null)
        {
            HashMap<Vec, Field> c = w.get(new ChunkVec(location));

            if (c != null)
            {
                Field field = c.get(new Vec(location));

                if (field != null)
                {
                    if (!field.getTypeEntry().equals(new BlockTypeEntry(type, data)))
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
     *
     * @param fieldBlock
     * @return confirmation
     */
    public boolean isField(Block fieldBlock)
    {
        return getField(fieldBlock) != null;
    }

    /**
     * Total number of forcefield stones
     *
     * @return the count
     */
    public int getCount()
    {
        int size = 0;

        HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(FieldFlag.ALL);

        if (wLists == null)
        {
            return 0;
        }

        for (HashMap<ChunkVec, HashMap<Vec, Field>> w : wLists.values())
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
     * things to do before shutdown
     */
    public void finalize()
    {
        HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(FieldFlag.ALL);

        if (wLists == null)
        {
            return;
        }

        for (HashMap<ChunkVec, HashMap<Vec, Field>> w : wLists.values())
        {
            if (w != null)
            {
                for (HashMap<Vec, Field> c : w.values())
                {
                    for (Field f : c.values())
                    {
                        if (f.isDirty())
                        {
                            plugin.getStorageManager().offerField(f);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets field counts for player/g:group/c:clan/*
     *
     * @param target
     * @return
     */
    public HashMap<BlockTypeEntry, Integer> getFieldCounts(String target)
    {
        HashMap<BlockTypeEntry, Integer> counts = new HashMap<BlockTypeEntry, Integer>();
        List<World> worlds = plugin.getServer().getWorlds();

        for (World world : worlds)
        {
            List<Field> fields = getFields(target, world);

            for (Field field : fields)
            {
                if (counts.containsKey(field.getTypeEntry()))
                {
                    counts.put(field.getTypeEntry(), counts.get(field.getTypeEntry()) + 1);
                }
                else
                {
                    counts.put(field.getTypeEntry(), 1);
                }
            }
        }

        return counts;
    }

    /**
     * Clean up orphan fields
     *
     * @param world
     * @return
     */
    public int cleanOrphans(World world)
    {
        int cleanedCount = 0;
        boolean currentChunkLoaded = false;
        ChunkVec currentChunk = null;

        HashMap<ChunkVec, HashMap<Vec, Field>> w = getFields(world.getName(), FieldFlag.ALL);

        if (w != null)
        {
            for (HashMap<Vec, Field> fields : w.values())
            {
                if (fields != null)
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

                        if (type != field.getTypeId())
                        {
                            cleanedCount++;
                            queueRelease(field);
                        }
                    }
                }
            }
        }
        flush();

        if (cleanedCount != 0)
        {
            PreciousStones.log("({0}) orphan-fields: {1}", world.getName(), cleanedCount);
        }
        return cleanedCount;
    }

    /**
     * Revert orphan fields
     *
     * @param world
     * @return
     */
    public int revertOrphans(World world)
    {
        int revertedCount = 0;
        boolean currentChunkLoaded = false;
        ChunkVec currentChunk = null;

        HashMap<ChunkVec, HashMap<Vec, Field>> w = getFields(world.getName(), FieldFlag.ALL);

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

                    Block block = world.getBlockAt(field.getX(), field.getY(), field.getZ());

                    if (!plugin.getSettingsManager().isFieldType(block))
                    {
                        revertedCount++;
                        block.setTypeId(field.getTypeId());
                        block.setData(field.getData());
                    }
                }
            }
        }

        return revertedCount;
    }

    /**
     * Returns the source block for the field
     *
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
     *
     * @param field
     * @return confirmation
     */
    public boolean isRedstoneHookedDisabled(Field field)
    {
        Block block = getBlock(field);

        if (isAnywayPowered(block))
        {
            return false;
        }

        Material topmat = block.getRelative(BlockFace.UP).getType();

        if (topmat.equals(Material.STONE_PLATE) || topmat.equals(Material.WOOD_PLATE))
        {
            return true;
        }

        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

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

            if (faceblock.getType().equals(Material.REDSTONE_WIRE) && faceblock.getBlockPower() == 0)
            {
                return true;
            }
        }

        BlockFace[] upfaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        Block upblock = block.getRelative(BlockFace.UP);

        for (BlockFace face : upfaces)
        {
            Block faceblock = upblock.getRelative(face);

            if (faceblock.getType().equals(Material.REDSTONE_WIRE) && faceblock.getBlockPower() == 0)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether the redstone source powers the field
     *
     * @param field
     * @param block
     * @return confirmation
     */
    public boolean powersField(Field field, Block block)
    {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP};

        for (BlockFace face : faces)
        {
            Block faceblock = block.getRelative(face);

            if (field.getX() == faceblock.getX() && field.getY() == faceblock.getY() && field.getZ() == faceblock.getZ())
            {
                return true;
            }
        }

        BlockFace[] downfaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        Block upblock = block.getRelative(BlockFace.DOWN);

        for (BlockFace face : downfaces)
        {
            Block faceblock = upblock.getRelative(face);

            if (field.getX() == faceblock.getX() && field.getY() == faceblock.getY() && field.getZ() == faceblock.getZ())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether there is current any where around the block
     *
     * @param block
     * @return confirmation
     */
    public boolean isAnywayPowered(Block block)
    {
        if (block.isBlockIndirectlyPowered() || block.isBlockPowered())
        {
            return true;
        }

        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

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

        BlockFace[] upfaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        Block upblock = block.getRelative(BlockFace.UP);

        for (BlockFace face : upfaces)
        {
            Block faceblock = upblock.getRelative(face);

            if (faceblock.getType().equals(Material.REDSTONE_WIRE))
            {
                if (faceblock.getBlockPower() > 0)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Send a message to all allowed players of the field
     *
     * @param field
     * @param msg
     */
    public void announceAllowedPlayers(Field field, String msg)
    {
        List<String> allowed = field.getAllAllowed();

        for (String playerName : allowed)
        {
            Player pl = Helper.matchSinglePlayer(playerName);

            if (pl != null)
            {
                ChatBlock.sendMessage(pl, ChatColor.DARK_GRAY + " * " + ChatColor.AQUA + msg);
            }
        }
    }

    /**
     * Whether the block is touching a field block
     *
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

                    if (plugin.getSettingsManager().isFieldType(surroundingblock))
                    {
                        if (isField(surroundingblock))
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
     * Clean up snitch lists of the field
     *
     * @param field
     * @return confirmation
     */
    public boolean cleanSnitchList(Field field)
    {
        FieldSettings fs = field.getSettings();

        if (fs.hasDefaultFlag(FieldFlag.SNITCH))
        {
            field.clearSnitch();
            plugin.getStorageManager().deleteSnitchEntries(field);
            return true;
        }

        return false;
    }

    /**
     * Sets the name of the field and all intersecting fields
     *
     * @param field
     * @param name
     * @return count of fields set
     */
    public boolean setNameField(Field field, String name)
    {
        FieldSettings fs = field.getSettings();

        if ((fs.hasNameableFlag()) && !field.getName().equals(name))
        {
            field.setName(name);
            plugin.getStorageManager().offerField(field);
            return true;
        }

        return false;
    }

    /**
     * Returns a list of players who are inside the field
     *
     * @param field
     * @return list of player names
     */
    public HashSet<String> getWho(Field field)
    {
        return plugin.getEntryManager().getInhabitants(field);
    }

    /**
     * Whether the player is allowed in the field
     *
     * @param field
     * @param playerName
     * @return
     */
    public boolean isApplyToAllowed(Field field, String playerName)
    {
        Player player = Helper.matchSinglePlayer(playerName);

        if (player != null)
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.allowed"))
            {
                return true;
            }
        }

        // always allow if in war

        if (plugin.getSimpleClansManager().inWar(field, playerName))
        {
            return true;
        }

        // ensure allow of only those with the required permission, fail silently otherwise

        if (field.getSettings().getRequiredPermissionAllow() != null)
        {
            if (!plugin.getPermissionsManager().has(player, field.getSettings().getRequiredPermissionAllow()))
            {
                return false;
            }
        }

        boolean allowed = isAllowed(field, playerName);

        if (field.hasFlag(FieldFlag.APPLY_TO_REVERSE))
        {
            return !allowed;
        }

        return allowed;
    }

    /**
     * Determine whether a player is allowed on a field
     *
     * @param fieldBlock
     * @param playerName
     * @return confirmation
     */
    public boolean isAllowed(Block fieldBlock, String playerName)
    {
        Field field = getField(fieldBlock);

        return field != null && isAllowed(field, playerName);

    }

    /**
     * Whether the player is allowed in the field
     *
     * @param field
     * @param playerName
     * @return
     */
    public boolean isAllowed(Field field, String playerName)
    {
        if (field == null)
        {
            return false;
        }

        Player player = Helper.matchSinglePlayer(playerName);

        if (player != null)
        {
            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.allowed"))
            {
                return true;
            }
        }

        // handle sllowed/denied lists flags

        if (field.getSettings().inAllowedList(playerName))
        {
            return true;
        }

        if (field.getSettings().inDeniedList(playerName))
        {
            return false;
        }

        // always allow if in war

        if (plugin.getSimpleClansManager().inWar(field, playerName))
        {
            return true;
        }

        return field.isAllowed(playerName);
    }

    /**
     * Allow a target (name, g:group, c:clan) into a field
     *
     * @param field
     * @param target
     * @return whether he got allowed
     */
    public boolean addAllowed(Field field, String target)
    {
        if (!field.isInAllowedList(target))
        {
            field.addAllowed(target);
            plugin.getStorageManager().offerField(field);
            return true;
        }

        return false;
    }

    /**
     * Disallow a target (name, g:group, c:clan) from a field
     *
     * @param field
     * @param target
     * @return count of fields the player was removed from
     */
    public boolean removeAllowed(Field field, String target)
    {
        if (field.isInAllowedList(target))
        {
            field.removeAllowed(target);

            plugin.getStorageManager().offerField(field);
            return true;
        }
        return false;
    }

    /**
     * Add allowed player to all your force fields
     *
     * @param player
     * @param allowedName
     * @return count of fields allowed
     */
    public int allowAll(Player player, String allowedName)
    {
        List<Field> fields = getOwnersFields(player, FieldFlag.ALL);

        int allowedCount = 0;
        int notAllowed = 0;

        for (Field field : fields)
        {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled"))
            {
                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                {
                    if (!field.isDisabled())
                    {
                        notAllowed++;
                        continue;
                    }
                }
            }

            if (!isAllowed(field, allowedName))
            {
                field.addAllowed(allowedName);
                allowedCount++;
            }
            plugin.getStorageManager().offerField(field);
        }

        if (notAllowed > 0)
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "" + notAllowed + " fields were skipped that can only be modified while disabled");
        }

        return allowedCount;
    }

    /**
     * Whether another field that is overlapping the field is owned by the allowedName
     *
     * @param field
     * @param allowedName
     * @return
     */
    public boolean conflictOfInterestExists(Field field, String allowedName)
    {
        Set<Field> sources = field.getOverlappingFields();

        for (Field source : sources)
        {
            if (source.hasFlag(FieldFlag.NO_CONFLICT))
            {
                continue;
            }

            if (field.getOwner().equalsIgnoreCase(source.getOwner()))
            {
                continue;
            }

            if (source.isOwner(allowedName))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * If the field has any sub-plotted fields
     *
     * @param field
     */
    public boolean hasSubFields(Field field)
    {
        Set<Field> sources = field.getOverlappingFields();

        for (Field source : sources)
        {
            if (source.getSettings().isAllowedOnlyInside(field))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove allowed player to all your force fields
     *
     * @param player
     * @param allowedName
     * @return count of fields the player was removed from
     */
    public int removeAll(Player player, String allowedName)
    {
        List<Field> fields = getOwnersFields(player, FieldFlag.ALL);

        int removedCount = 0;
        int notRemoved = 0;

        for (Field field : fields)
        {
            if (field.containsPlayer(allowedName))
            {
                ChatBlock.sendMessage(player, ChatColor.RED + "Player could not be removed from a field because he was currently inside of it");
                continue;
            }

            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled"))
            {
                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED))
                {
                    if (!field.isDisabled())
                    {
                        notRemoved++;
                        continue;
                    }
                }
            }

            if (conflictOfInterestExists(field, allowedName))
            {
                ChatBlock.sendMessage(player, ChatColor.RED + Helper.capitalize(allowedName) + " was not disallowed, one of the fields is overlapping one of yours " + field);
                continue;
            }

            if (isAllowed(field, allowedName))
            {
                field.removeAllowed(allowedName);
                removedCount++;
            }
            plugin.getStorageManager().offerField(field);
        }

        if (notRemoved > 0)
        {
            ChatBlock.sendMessage(player, ChatColor.RED + "" + notRemoved + " fields were skipped that can only be modified while disabled");
        }

        return removedCount;
    }

    /**
     * Return the owner of a field
     *
     * @param fieldBlock a block which is a field
     * @return owner's name
     */
    public String getOwner(Block fieldBlock)
    {
        Field field = getField(fieldBlock);

        if (field != null)
        {
            return field.getOwner();
        }
        return "";
    }

    /**
     * Get all the fields belonging to players, optionally you can pass field flags and it will only retrieve those matching the field flags
     *
     * @param player
     * @return the fields
     */
    public List<Field> getOwnersFields(Player player, FieldFlag flag)
    {
        List<Field> out = new ArrayList<Field>();

        HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(flag);

        if (wLists == null)
        {
            return null;
        }

        for (HashMap<ChunkVec, HashMap<Vec, Field>> w : wLists.values())
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
     * Get all fields matching this flag that are touching this chunk
     *
     * @param chunk
     * @param flag
     * @param filters
     * @return
     */
    public List<Field> getSourceFieldsInChunk(Chunk chunk, FieldFlag flag, ResultsFilter... filters)
    {
        return getSourceFieldsInChunk(new ChunkVec(chunk), flag, filters);
    }

    /**
     * Get all fields matching this flag that are touching this chunk
     *
     * @param cv
     * @return
     */
    public List<Field> getSourceFieldsInChunk(ChunkVec cv, FieldFlag flag, ResultsFilter... filters)
    {
        HashMap<FieldFlag, List<Field>> flagList = sourceFields.get(cv);

        if (flagList != null)
        {
            List<Field> fields = flagList.get(flag);

            if (fields != null && fields.size() > 0)
            {
                fields = new ArrayList<Field>(fields);

                if (fields.size() > 0)
                {
                    for (Iterator it = fields.iterator(); it.hasNext(); )
                    {
                        Field field = (Field) it.next();

                        // go through each of the filters
                        // and apply the them

                        for (ResultsFilter filter : filters)
                        {
                            if (!filter.Filter(field))
                            {
                                it.remove();
                                break;
                            }
                        }
                    }

                    return fields;
                }
            }
        }

        return new ArrayList<Field>();
    }

    /**
     * Get the smallest source field in the chunk
     *
     * @param chunk
     * @param flag
     * @param filters
     * @return
     */
    public Field getSmallestSourceFieldInChunk(Chunk chunk, FieldFlag flag, ResultsFilter... filters)
    {
        ChunkVec cv = new ChunkVec(chunk);

        List<Field> allFields = getSourceFieldsInChunk(cv, FieldFlag.ALL, filters);

        if (allFields.size() == 0)
        {
            return null;
        }

        Field absoluteSmallest = getSmallestVolumeField(allFields);

        // find absolute smallest

        if (absoluteSmallest.hasFlag(FieldFlag.PLOT))
        {
            // if it doesn't have the flag don't return anything at all

            if (!absoluteSmallest.hasFlag(flag))
            {
                return null;
            }

            return absoluteSmallest;
        }

        // otherwise return the smallest that matches that flag

        List<Field> fields = getSourceFieldsInChunk(cv, flag, filters);

        Field smallest = getSmallestVolumeField(fields);

        return smallest;
    }

    /**
     * Returns the fields that the location is in match the field flag(s)
     *
     * @param loc
     * @param flag
     * @return the fields
     */
    public List<Field> getSourceFields(final Location loc, final FieldFlag flag)
    {
        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(loc);
            }
        };

        List<Field> sources = getSourceFieldsInChunk(new ChunkVec(loc.getBlock().getChunk()), flag, envelopsFilter);
        return sources;
    }

    /**
     * Returns the enabled fields that the location is in match the field flag(s)
     *
     * @param loc
     * @param flag
     * @return the fields
     */
    public List<Field> getEnabledSourceFields(final Location loc, final FieldFlag flag)
    {
        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(loc);
            }
        };

        ResultsFilter disabledFlagFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.hasDisabledFlag(flag);
            }
        };

        ResultsFilter notDisabledFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.isDisabled();
            }
        };

        List<Field> sources = getSourceFieldsInChunk(new ChunkVec(loc.getBlock().getChunk()), flag, envelopsFilter, disabledFlagFilter, notDisabledFilter);
        return sources;
    }

    /**
     * Returns a field in the location that matches the field flag(s)
     *
     * @param loc
     * @param flag
     * @return the fields
     */
    public Field getEnabledSourceField(final Location loc, final FieldFlag flag)
    {
        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(loc);
            }
        };

        ResultsFilter disabledFlagFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.hasDisabledFlag(flag);
            }
        };

        ResultsFilter notDisabledFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.isDisabled();
            }
        };

        Field field = getSmallestSourceFieldInChunk(loc.getBlock().getChunk(), flag, envelopsFilter, notDisabledFilter, disabledFlagFilter);
        return field;
    }

    /**
     * If a field in the location that matches the field flag(s)
     *
     * @param loc
     * @param flag
     * @return result
     */
    public boolean hasSourceField(Location loc, FieldFlag flag)
    {
        return getEnabledSourceField(loc, flag) != null;
    }

    /**
     * Gets the smallest field from a list of fields
     *
     * @param fields
     * @return
     */
    public Field getSmallestVolumeField(List<Field> fields)
    {
        if (fields == null || fields.isEmpty())
        {
            return null;
        }

        // sort fields by volume

        Collections.sort(fields, new Comparator<Field>()
        {
            public int compare(Field f1, Field f2)
            {
                Integer o1 = f1.getVolume();
                Integer o2 = f2.getVolume();

                return o1.compareTo(o2);
            }
        });

        // return smallest fields where a player can fit

        for (Field smallest : fields)
        {
            if (smallest.getVolume() > 1 && smallest.getComputedHeight() > 1)
            {
                return smallest;
            }
        }

        return fields.get(0);
    }

    /**
     * Returns the first field found in the location and that the player is not allowed in, optionally with field flags
     *
     * @param loc
     * @param playerName
     * @return the fields
     * @deprecated
     */
    public Field getNotAllowedSourceField(final Location loc, final String playerName, final FieldFlag flag)
    {
        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(loc);
            }
        };

        ResultsFilter notAllowedFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !isAllowed(field, playerName);
            }
        };

        ResultsFilter disabledFlagFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.hasDisabledFlag(flag);
            }
        };

        ResultsFilter notDisabledFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.isDisabled();
            }
        };

        Field field = getSmallestSourceFieldInChunk(loc.getBlock().getChunk(), flag, envelopsFilter, notAllowedFilter, notDisabledFilter, disabledFlagFilter);
        return field;
    }

    /**
     * Returns the first conflict field found in the location and that the player is not allowed in, optionally with field flags
     *
     * @param loc
     * @param playerName
     * @return the fields
     */
    public Field getConflictSourceField(final Location loc, final String playerName, FieldFlag flag)
    {
        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(loc);
            }
        };

        ResultsFilter noConflictFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.hasFlag(FieldFlag.NO_CONFLICT);
            }
        };

        Field field = getSmallestSourceFieldInChunk(loc.getBlock().getChunk(), flag, envelopsFilter, noConflictFilter);
        return field;
    }

    /**
     * Returns the fields that the location is in and that the player is allowed in, optionally with field flags
     *
     * @param loc
     * @param playerName
     * @return the fields
     */
    @Deprecated
    public List<Field> getAllowedSourceFields(final Location loc, final String playerName, final FieldFlag flag)
    {
        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(loc);
            }
        };

        ResultsFilter allowedFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return isAllowed(field, playerName);
            }
        };

        ResultsFilter disabledFlagFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.hasDisabledFlag(flag);
            }
        };

        ResultsFilter notDisabledFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.isDisabled();
            }
        };

        List<Field> sources = getSourceFieldsInChunk(loc.getBlock().getChunk(), flag, envelopsFilter, allowedFilter, notDisabledFilter, disabledFlagFilter);
        return sources;
    }

    /**
     * Returns the fields that the location is in and that the player is not allowed in, optionally with field flags
     *
     * @param loc
     * @param playerName
     * @return the fields
     * @deprecated
     */
    @Deprecated
    public List<Field> getNotAllowedSourceFields(final Location loc, final String playerName, final FieldFlag flag)
    {
        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(loc);
            }
        };

        ResultsFilter notAllowedFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !isAllowed(field, playerName);
            }
        };

        ResultsFilter disabledFlagFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.hasDisabledFlag(flag);
            }
        };

        ResultsFilter notDisabledFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return !field.isDisabled();
            }
        };

        List<Field> sources = getSourceFieldsInChunk(loc.getBlock().getChunk(), flag, envelopsFilter, notAllowedFilter, notDisabledFilter);
        return sources;
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     *
     * @param loc
     * @param chunkradius
     * @return the fields
     */
    public Set<Field> getFieldsInCustomArea(final Location loc, int chunkradius, FieldFlag flag)
    {
        Set<Field> out = new HashSet<Field>();

        int xlow = (loc.getBlockX() >> 4) - chunkradius;
        int xhigh = (loc.getBlockX() >> 4) + chunkradius;
        int zlow = (loc.getBlockZ() >> 4) - chunkradius;
        int zhigh = (loc.getBlockZ() >> 4) + chunkradius;

        for (int x = xlow; x <= xhigh; x++)
        {
            for (int z = zlow; z <= zhigh; z++)
            {
                ResultsFilter envelopsFilter = new ResultsFilter()
                {
                    public boolean Filter(Field field)
                    {
                        return field.envelops(loc);
                    }
                };

                List<Field> fields = getSourceFieldsInChunk(new ChunkVec(x, z, loc.getWorld().getName()), flag, envelopsFilter);

                if (fields != null)
                {
                    out.addAll(fields);
                }
            }
        }

        return out;
    }

    /**
     * Returns the fields in the chunk and adjacent chunks that the player is allowe din
     *
     * @param loc
     * @param chunkradius
     * @return the fields
     */
    public Set<Field> getFieldsInCustomArea(final Location loc, int chunkradius, FieldFlag flag, Player player)
    {
        Set<Field> out = new HashSet<Field>();

        int xlow = (loc.getBlockX() >> 4) - chunkradius;
        int xhigh = (loc.getBlockX() >> 4) + chunkradius;
        int zlow = (loc.getBlockZ() >> 4) - chunkradius;
        int zhigh = (loc.getBlockZ() >> 4) + chunkradius;

        for (int x = xlow; x <= xhigh; x++)
        {
            for (int z = zlow; z <= zhigh; z++)
            {
                ResultsFilter envelopsFilter = new ResultsFilter()
                {
                    public boolean Filter(Field field)
                    {
                        return field.envelops(loc);
                    }
                };

                List<Field> fields = getSourceFieldsInChunk(new ChunkVec(x, z, loc.getWorld().getName()), flag, envelopsFilter);

                if (fields != null)
                {
                    for (Field field : fields)
                    {
                        if (isAllowed(field, player.getName()))
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
     * Returns the field pointed at
     *
     * @param player
     * @return the field
     */
    public Field getPointedField(Player player)
    {
        TargetBlock aiming = new TargetBlock(player, 1000, 0.2, plugin.getSettingsManager().getThroughFieldsSet());
        Block targetBlock = aiming.getTargetBlock();

        if (targetBlock != null)
        {
            Field f = getField(targetBlock);

            if (f != null)
            {
                if (f.isChild())
                {
                    f = f.getParent();
                }

                if (isAllowed(f, player.getName()))
                {
                    return f;
                }
            }
        }

        return null;
    }

    /**
     * Returns the field if he's standing in at least one allowed field, optionally matching field flags
     *
     * @param blockInArea
     * @param player
     * @return the field
     */
    public Field getOneAllowedField(final Block blockInArea, final Player player, FieldFlag flag)
    {
        Field pointed = getPointedField(player);

        if (pointed != null)
        {
            return pointed;
        }

        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(blockInArea.getLocation());
            }
        };

        ResultsFilter allowedFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return isAllowed(field, player.getName());
            }
        };

        Field field = getSmallestSourceFieldInChunk(blockInArea.getLocation().getBlock().getChunk(), flag, envelopsFilter, allowedFilter);
        return field;
    }


    /**
     * Returns the field if he's standing in at least one owned field, optionally matching field flags
     *
     * @param blockInArea
     * @param player
     * @return the field
     */
    public Field getOneOwnedField(final Block blockInArea, final Player player, FieldFlag flag)
    {
        Field pointed = getPointedField(player);

        if (pointed != null)
        {
            return pointed;
        }

        ResultsFilter envelopsFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.envelops(blockInArea.getLocation());
            }
        };

        ResultsFilter allowedFilter = new ResultsFilter()
        {
            public boolean Filter(Field field)
            {
                return field.isOwner(player.getName());
            }
        };

        Field field = getSmallestSourceFieldInChunk(blockInArea.getLocation().getBlock().getChunk(), flag, envelopsFilter, allowedFilter);
        return field;
    }

    /**
     * Whether the location is in a use protected area that the player is not allowed in
     *
     * @param loc
     * @param player
     * @param type_id
     * @return the field, null if its not
     */
    public boolean isUseProtected(Location loc, Player player, int type_id)
    {
        return findUseProtected(loc, player, type_id) != null;
    }

    /**
     * Find a use protected area in the location that the player is not allowed in
     *
     * @param loc
     * @param player
     * @param type_id
     * @return the field, null if its not
     */
    public Field findUseProtected(Location loc, Player player, int type_id)
    {
        List<Field> sources = getEnabledSourceFields(loc, FieldFlag.ALL);

        ArrayList<Field> out = new ArrayList<Field>();

        for (Field field : sources)
        {
            boolean allowed = isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                FieldSettings fs = field.getSettings();

                if (!fs.canUse(type_id) && field.hasFlag(FieldFlag.PREVENT_USE))
                {
                    out.add(field);
                }
            }
        }

        return getSmallestVolumeField(out);
    }


    /**
     * Return the first field that conflicts with the unbreakable
     *
     * @param placedBlock
     * @param placer
     * @return the field, null if none found
     */
    public Field unbreakableConflicts(Block placedBlock, Player placer)
    {
        List<Field> sources = getSourceFields(placedBlock.getLocation(), FieldFlag.ALL);

        ArrayList<Field> out = new ArrayList<Field>();

        for (Field field : sources)
        {
            if (field.hasFlag(FieldFlag.NO_CONFLICT))
            {
                continue;
            }

            if (isAllowed(field, placer.getName()))
            {
                continue;
            }

            if (field.envelops(placedBlock))
            {
                out.add(field);
            }
        }

        return getSmallestVolumeField(out);
    }

    /**
     * Return the first field that conflicts with the field block
     *
     * @param placedBlock
     * @param placer
     * @return the field, null if none found
     */
    public Field fieldConflicts(Block placedBlock, Player placer)
    {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(placedBlock);

        if (fs == null)
        {
            return null;
        }

        if (fs.hasDefaultFlag(FieldFlag.NO_CONFLICT))
        {
            return null;
        }

        // create throwaway field to test intersection

        Field placedField = new Field(placedBlock, fs.getRadius(), fs.getHeight());

        Set<Field> overlapping = placedField.getOverlappingFields();

        ArrayList<Field> out = new ArrayList<Field>();

        for (Field field : overlapping)
        {
            if (field.hasFlag(FieldFlag.NO_CONFLICT))
            {
                continue;
            }

            if (isAllowed(field, placer.getName()))
            {
                continue;
            }

            if (field.intersects(placedField))
            {
                out.add(field);
            }
        }

        return getSmallestVolumeField(out);
    }

    /**
     * Return the first field that conflicts with the cuboid entry
     *
     * @param ce
     * @param placer
     * @return the field, null if none found
     */
    public Field fieldConflicts(CuboidEntry ce, Player placer)
    {
        if (ce.getField().hasFlag(FieldFlag.NO_CONFLICT))
        {
            return null;
        }

        // create throwaway field to test intersection

        Set<Field> overlapping = ce.getField().getOverlappingFields();

        ArrayList<Field> out = new ArrayList<Field>();

        for (Field field : overlapping)
        {
            if (field.hasFlag(FieldFlag.NO_CONFLICT))
            {
                continue;
            }

            if (isAllowed(field, placer.getName()))
            {
                continue;
            }

            if (field.intersects(ce.getField()))
            {
                out.add(field);
            }
        }

        return getSmallestVolumeField(out);
    }

    /**
     * Allows all owners of fields that are overlapping
     *
     * @param field
     */
    public void addAllowOverlappingOwners(Field field)
    {
        FieldSettings fs = field.getSettings();

        if (fs == null)
        {
            return;
        }

        if (fs.hasDefaultFlag(FieldFlag.NO_CONFLICT))
        {
            return;
        }

        // create throwaway field to test intersection

        Set<Field> overlapping = field.getOverlappingFields();

        for (Field overlap : overlapping)
        {
            if (overlap.hasFlag(FieldFlag.NO_CONFLICT))
            {
                continue;
            }

            if (overlap.isAllowed(field.getOwner()))
            {
                field.addAllowed(overlap.getOwner());
            }
        }
    }

    /**
     * Deletes all fields belonging to a player
     *
     * @param playerName the players
     * @return the count of deleted fields
     */
    public int deleteBelonging(String playerName)
    {
        int deletedFields = 0;

        HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(FieldFlag.ALL);

        if (wLists == null)
        {
            return 0;
        }

        for (HashMap<ChunkVec, HashMap<Vec, Field>> w : wLists.values())
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
     * Deletes a liquid based field from the collection
     *
     * @param field
     */
    public void releaseLiquid(Field field)
    {
        deleteField(field);
        dropBlockSilent(field);
    }

    /**
     * Deletes a field from the collection
     *
     * @param block
     */
    public void release(Block block)
    {
        Field field = getField(block);

        deleteField(field);
        dropBlock(block);
    }

    /**
     * Deletes a field silently (no drop)
     *
     * @param block
     */
    public void silentRelease(Block block)
    {
        deleteField(getField(block));
    }

    /**
     * Deletes a field silently (no drop)
     *
     * @param field
     */
    public void silentRelease(Field field)
    {
        deleteField(field);
    }

    /**
     * Adds a field to deletion queue
     *
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
            dropBlock(pending);
        }
    }

    /**
     * Delete fields in deletion queue, force a drop
     */
    public void flushDrop()
    {
        while (deletionQueue.size() > 0)
        {
            Field pending = deletionQueue.poll();

            deleteField(pending);
            dropBlock(pending);
        }
    }

    /**
     * Drops a block
     *
     * @param field
     */
    public void dropBlock(Field field)
    {
        if (!plugin.getSettingsManager().isDropOnDelete())
        {
            dropBlock(field.getBlock());
        }
    }

    /**
     * Drops a block
     *
     * @param field
     */
    public void dropBlockSilent(Field field)
    {
        // prevent tekkit blocks from dropping and crashing client

        if (field.getTypeId() > 124)
        {
            return;
        }

        if (!plugin.getSettingsManager().isDropOnDelete())
        {
            World world = field.getLocation().getWorld();
            ItemStack is = new ItemStack(field.getTypeId(), 1, (short) 0, field.getData());

            world.dropItemNaturally(field.getLocation(), is);
        }
    }

    /**
     * Drops a block
     *
     * @param block
     */
    public void dropBlock(Block block)
    {
        // prevent tekkit blocks from dropping and crashing client

        if (block.getTypeId() > 124)
        {
            return;
        }

        if (!plugin.getSettingsManager().isDropOnDelete())
        {
            World world = block.getWorld();
            ItemStack is = new ItemStack(block.getTypeId(), 1, (short) 0, block.getData());

            if (plugin.getSettingsManager().isFieldType(block))
            {
                block.setType(Material.AIR);
                world.dropItemNaturally(block.getLocation(), is);
            }
        }
    }

    /**
     * Delete fields the the player is standing on
     *
     * @param sourceFields
     * @return count of fields deleted
     */
    public int deleteFields(List<Field> sourceFields)
    {
        int deletedCount = 0;

        for (Field f : sourceFields)
        {
            queueRelease(f);
            deletedCount++;
        }

        if (deletedCount > 0)
        {
            flush();
        }
        return deletedCount;
    }

    /**
     * Delete fields of a certain type
     *
     * @param type
     * @return count of fields deleted
     */
    public int deleteFieldsOfType(BlockTypeEntry type)
    {
        int deletedCount = 0;

        HashMap<String, HashMap<ChunkVec, HashMap<Vec, Field>>> wLists = fieldLists.get(FieldFlag.ALL);

        if (wLists != null)
        {
            for (HashMap<ChunkVec, HashMap<Vec, Field>> w : wLists.values())
            {
                if (w != null)
                {
                    for (ChunkVec cv : w.keySet())
                    {
                        HashMap<Vec, Field> c = w.get(cv);

                        for (Field field : c.values())
                        {
                            if (field.getTypeEntry().equals(type))
                            {
                                queueRelease(field);
                                deletedCount++;
                            }
                        }
                    }
                }
            }
        }

        if (deletedCount > 0)
        {
            flush();
        }
        return deletedCount;
    }

    /**
     * Removes money from player's account
     *
     * @param player
     * @param amount
     * @return
     */
    public boolean purchase(Player player, double amount)
    {
        if (plugin.getPermissionsManager().hasEconomy())
        {
            if (PermissionsManager.hasMoney(player, amount))
            {
                plugin.getPermissionsManager().playerCharge(player, amount);
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
     *
     * @param player
     * @param amount
     */
    public void refund(Player player, double amount)
    {
        if (plugin.getPermissionsManager().hasEconomy())
        {
            plugin.getPermissionsManager().playerCredit(player, amount);
            player.sendMessage(ChatColor.AQUA + "Your account has been credited");
        }
    }

    /**
     * check if the area a field may cover has players in it
     *
     * @param block
     */
    public boolean fieldTouchesPlayers(Block block, Player self)
    {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(block);

        if (fs == null)
        {
            return false;
        }

        // create throwaway field to test intersections

        Field field = new Field(block, fs.getRadius(), fs.getHeight());

        List<Player> players = block.getWorld().getPlayers();

        for (Player player : players)
        {
            if (player.equals(self))
            {
                continue;
            }

            if (isAllowed(field, player.getName()))
            {
                continue;
            }

            if (field.envelops(player.getLocation()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns all of a fields inhabitants
     *
     * @param field
     */
    public Set<Player> getFieldInhabitants(Field field)
    {
        Set<Player> out = new HashSet<Player>();
        List<Player> players = field.getLocation().getWorld().getPlayers();

        for (Player player : players)
        {
            if (field.envelops(player.getLocation()))
            {
                out.add(player);
            }
        }

        return out;
    }

}
