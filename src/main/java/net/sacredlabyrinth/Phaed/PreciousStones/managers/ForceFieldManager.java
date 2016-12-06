package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import com.google.common.collect.Maps;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.ResultsFilter;
import net.sacredlabyrinth.Phaed.PreciousStones.blocks.TargetBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.ForesterEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.StackHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.ChunkVec;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.Map.Entry;

/**
 * Handles fields
 *
 * @author Phaed
 */
public final class ForceFieldManager {
    private final Map<FieldFlag, List<Field>> fieldsByFlag = Maps.newHashMap();
    private final Map<String, List<Field>> fieldsByWorld = Maps.newHashMap();
    private final Map<String, Map<BlockTypeEntry, List<Field>>> fieldsByOwnerAndType = Maps.newHashMap();
    private final Map<String, Map<BlockTypeEntry, List<Field>>> fieldsByRenterAndType = Maps.newHashMap();
    private final Map<String, Map<FieldFlag, List<Field>>> fieldsByOwnerAndFlag = Maps.newHashMap();
    private final Map<String, List<Field>> fieldsByOwner = Maps.newHashMap();
    private final Map<String, List<Field>> fieldsByAllowed = Maps.newHashMap();
    private final Map<Vec, Field> fieldsByVec = Maps.newHashMap();

    private final HashMap<ChunkVec, HashMap<FieldFlag, List<Field>>> sourceFields = new HashMap<ChunkVec, HashMap<FieldFlag, List<Field>>>();

    private Queue<Field> deletionQueue = new LinkedList<Field>();
    private PreciousStones plugin;

    /**
     *
     */
    public ForceFieldManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Clear out the fields in memory
     */
    public void clearChunkLists() {
        fieldsByFlag.clear();
        fieldsByWorld.clear();
        fieldsByOwner.clear();
        fieldsByOwnerAndType.clear();
        fieldsByOwnerAndFlag.clear();
        fieldsByVec.clear();
        fieldsByAllowed.clear();
        fieldsByRenterAndType.clear();

        sourceFields.clear();
    }

    /**
     * Add a brand new field
     *
     * @param fieldBlock
     * @param player
     * @param event
     * @return confirmation
     */
    public void add(Block fieldBlock, Player player, BlockPlaceEvent event) {
        boolean notify = true;

        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(fieldBlock);

        if (fs == null) {
            return;
        }

        // deny if world is blacklisted

        if (plugin.getSettingsManager().isBlacklistedWorld(fieldBlock.getWorld())) {
            return;
        }

        // check if in worldguard region

        if (!plugin.getPermissionsManager().canBuildField(player, fieldBlock, fs)) {
            ChatHelper.send(player, "fieldIntersectsWG");
            event.setCancelled(true);
            return;
        }

        // check if the pstone limit has been reached by the player

        if (plugin.getLimitManager().reachedLimit(player, fs)) {
            event.setCancelled(true);
            return;
        }

        // purchase pstone

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.purchase")) {
            if (fs.getPrice() > 0 && !purchase(player, fs.getPrice())) {
                return;
            }
        }

        String owner = fs.hasDefaultFlag(FieldFlag.NO_OWNER) ? "Server" : player.getName();
        boolean isChild = false;
        boolean isImport = false;
        Field field;

        // create field

        if (plugin.getCuboidManager().hasOpenCuboid(player)) {
            CuboidEntry ce = plugin.getCuboidManager().getOpenCuboid(player);

            if ((ce.getField().getSettings().getMixingGroup() != fs.getMixingGroup() || fs.getMixingGroup() == 0) && !ce.getField().getSettings().getTypeEntry().equals(fs.getTypeEntry())) {
                plugin.getCuboidManager().cancelOpenCuboid(player);
                ChatHelper.send(player, "cuboidCannotMix");
                event.setCancelled(true);
                return;
            }

            if (fs.getPrice() > ce.getField().getSettings().getPrice()) {
                plugin.getCuboidManager().cancelOpenCuboid(player);
                ChatHelper.send(player, "cuboidCannotAddProps");
                event.setCancelled(true);
                return;
            }

            field = new Field(fieldBlock, 0, 0, owner);

            // set up parent/child relationship

            ce.getField().addChild(field);
            field.setParent(ce.getField());
            isChild = true;
            notify = false;

            // import field flags

            if (!ce.getField().getTypeEntry().equals(fs.getTypeEntry())) {
                ce.getField().getFlagsModule().importFlags(fs.getDefaultFlags());
                ChatHelper.send(player, "flagsImported", fs.getTitle());
                isImport = true;
                plugin.getStorageManager().offerField(ce.getField());
            }

        } else {
            field = new Field(fieldBlock, fs.getRadius(), fs.getCustomHeight(), owner);
        }

        field.setSettings(fs);

        // add to database (skip foresters and activate them)

        if (field.hasFlag(FieldFlag.FORESTER)) {
            if (!field.hasFlag(FieldFlag.PLACE_DISABLED)) {
                ForesterEntry fe = new ForesterEntry(field, player);
            }
        } else {
            // open cuboid definition

            if (field.hasFlag(FieldFlag.CUBOID) && !isImport) {
                if (isChild) {
                    plugin.getCuboidManager().openChild(player, field);
                }
            }

            // insert the field into database

            plugin.getStorageManager().insertField(field);
        }

        // add to collection

        addToCollection(field);

        // visualize the field

        if (field.hasFlag(FieldFlag.VISUALIZE_ON_PLACE) && !isChild) {
            plugin.getVisualizationManager().visualizeSingleFieldFast(player, field);
        }

        if (notify) {
            if (field.hasFlag(FieldFlag.BREAKABLE)) {
                plugin.getCommunicationManager().notifyPlaceBreakableFF(player, fieldBlock);
            } else {
                plugin.getCommunicationManager().notifyPlaceFF(player, fieldBlock);
            }
        }

        // disable flags

        for (FieldFlag flag : field.getSettings().getDisabledFlags()) {
            field.getFlagsModule().disableFlag(flag.toString(), true);
        }

        // places the field in a disabled state

        if (field.hasFlag(FieldFlag.PLACE_DISABLED)) {
            field.setDisabled(true);
        }

        // sets the initial revert seconds for grief reverts

        if (field.hasFlag(FieldFlag.GRIEF_REVERT)) {
            if (field.getSettings().getGriefRevertInterval() > 0) {
                field.getRevertingModule().setRevertSecs(field.getSettings().getGriefRevertInterval());
                plugin.getGriefUndoManager().register(field);
            }
        }

        // start renter scheduler

        if (field.hasFlag(FieldFlag.RENTABLE) || field.hasFlag(FieldFlag.SHAREABLE)) {
            field.getRentingModule().scheduleNextRentUpdate();
        }

        // add allowed clan

        if (plugin.getSettingsManager().isAutoAddClan()) {
            String clan = plugin.getSimpleClansManager().getClan(player.getName());

            if (clan != null) {
                field.addAllowed("c:" + clan);
            }
        }

        // add allowed team

        if (plugin.getSettingsManager().isAutoAddTeam()) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(player.getName());

            if (offlinePlayer != null) {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard board = manager.getMainScoreboard();

                Team team = board.getPlayerTeam(offlinePlayer);

                if (team != null) {
                    field.addAllowed("t:" + team.getName());
                }
            }
        }

        // generate fence

        if (field.getSettings().getFenceItem() > 0) {
            if (field.getFencingModule().getFencePrice() == 0 || purchase(player, field.getFencingModule().getFencePrice())) {
                field.getFencingModule().generateFence(field.getSettings().getFenceItem());
                ChatHelper.send(player, "fenceGenerated");
            }
        }

        // add metadata

        field.getBlock().setMetadata("Pstone", new FixedMetadataValue(plugin, true));

        // allow all owners of intersecting fields into the field

        addAllowOverlappingOwners(field);

        // start disabling process for auto-disable fields

        field.startDisabler();

        // saves the field on the database

        plugin.getStorageManager().offerField(field);
    }

    public void addToRenterCollection(Field field) {
        List<String> renters = field.getRenters();
        if (renters == null || renters.isEmpty()) return;

        for (String renter : renters) {
            Map<BlockTypeEntry, List<Field>> renterTypes = fieldsByRenterAndType.get(renter.toLowerCase());

            if (renterTypes == null) {
                renterTypes = Maps.newHashMap();
            }

            List<Field> fields = renterTypes.get(field.getTypeEntry());
            if (fields == null) {
                fields = new ArrayList<Field>();
            }

            fields.add(field);
            renterTypes.put(field.getTypeEntry(), fields);
            fieldsByRenterAndType.put(renter, renterTypes);
        }
    }

    /**
     * Add the field to the collection, used by add()
     *
     * @param field
     */
    public void addToCollection(Field field) {
        List<FieldFlag> flags = new ArrayList<FieldFlag>();
        flags.addAll(field.getSettings().getDefaultFlags());
        flags.addAll(field.getFlagsModule().getInsertedFlags());

        // add to flags collection

        for (FieldFlag flag : flags) {
            List<Field> fields = fieldsByFlag.get(flag);

            if (fields == null) {
                fields = new ArrayList<Field>();
            }

            fields.add(field);
            fieldsByFlag.put(flag, fields);
        }

        // add to vec collection

        fieldsByVec.put(field.toVec(), field);

        // add to worlds collection

        List<Field> fields = fieldsByWorld.get(field.getWorld());

        if (fields == null) {
            fields = new ArrayList<Field>();
        }

        fields.add(field);
        fieldsByWorld.put(field.getWorld(), fields);

        // add to owners collection

        fields = getFieldsByOwner().get(field.getOwner().toLowerCase());

        if (fields == null) {
            fields = new ArrayList<Field>();
        }

        fields.add(field);
        getFieldsByOwner().put(field.getOwner().toLowerCase(), fields);

        // add to allowed collection
        List<String> allowed = field.getAllowed();
        for (String allowedPlayer : allowed) {
            fields = fieldsByAllowed.get(allowedPlayer.toLowerCase());

            if (fields == null) {
                fields = new ArrayList<Field>();
            }

            fields.add(field);
            fieldsByAllowed.put(allowedPlayer.toLowerCase(), fields);
        }

        // add to owner and type collection

        Map<BlockTypeEntry, List<Field>> types = fieldsByOwnerAndType.get(field.getOwner().toLowerCase());

        if (types == null) {
            types = Maps.newHashMap();
        }

        fields = types.get(field.getTypeEntry());

        if (fields == null) {
            fields = new ArrayList<Field>();
        }

        fields.add(field);
        types.put(field.getTypeEntry(), fields);
        fieldsByOwnerAndType.put(field.getOwner().toLowerCase(), types);

        // add to renter and type collection
        addToRenterCollection(field);

        // add to owner and flag collection

        Map<FieldFlag, List<Field>> allFlags = fieldsByOwnerAndFlag.get(field.getOwner().toLowerCase());

        if (allFlags == null) {
            allFlags = Maps.newHashMap();
        }

        for (FieldFlag flag : field.getFlagsModule().getFlags()) {
            fields = allFlags.get(flag);

            if (fields == null) {
                fields = new ArrayList<Field>();
            }

            fields.add(field);
            allFlags.put(flag, fields);
        }

        fieldsByOwnerAndFlag.put(field.getOwner().toLowerCase(), allFlags);

        // add to sources collection

        addSourceField(field);
    }

    /**
     * Add a fields envoleped chunks to the source fields collection
     *
     * @param field
     */
    public void addSourceField(Field field) {
        Set<ChunkVec> scvs = field.getEnvelopingChunks();

        for (ChunkVec scv : scvs) {
            HashMap<FieldFlag, List<Field>> sf = sourceFields.get(scv);

            if (sf == null) {
                sf = new HashMap<FieldFlag, List<Field>>();
            }

            List<FieldFlag> flags = new ArrayList<FieldFlag>();
            flags.addAll(field.getSettings().getDefaultFlags());
            flags.addAll(field.getFlagsModule().getInsertedFlags());

            for (FieldFlag flag : flags) {
                List<Field> fields = sf.get(flag);

                if (fields == null) {
                    fields = new ArrayList<Field>();
                }

                if (!fields.contains(field)) {
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
    public void deleteField(final Field field) {
        if (field == null) {
            return;
        }

        // remove from flags collection

        if (field.getSettings() != null) {

            for (Entry<FieldFlag, List<Field>> fieldSetting : fieldsByFlag.entrySet()) {
                List<Field> fields = fieldSetting.getValue();

                if (fields != null) {
                    fields.remove(field);
                }
            }
        }

        // remove from vec collection

        fieldsByVec.remove(field.toVec());

        // remove from owners collection

        List<Field> owned = getFieldsByOwner().get(field.getOwner().toLowerCase());

        if (owned != null) {
            owned.remove(field);
        }

        // remove from allowed collection
        List<String> allowed = field.getAllowed();
        for (String allowedPlayer : allowed) {
            List<Field> allowedFields = fieldsByAllowed.get(allowedPlayer.toLowerCase());
            if (allowedFields != null) {
                allowedFields.remove(field);
                if (allowedFields.isEmpty()) {
                    fieldsByAllowed.remove(allowedPlayer.toLowerCase());
                }
            }
        }

        // remove from worlds collection

        List<Field> fields = fieldsByWorld.get(field.getWorld());

        if (fields != null) {
            fields.remove(field);
        }

        // remove from owner and types collection

        Map<BlockTypeEntry, List<Field>> types = fieldsByOwnerAndType.get(field.getOwner().toLowerCase());

        if (types != null) {
            fields = types.get(field.getTypeEntry());

            if (fields != null) {
                fields.remove(field);
            }
        }

        // Remove all renters
        removeAllRenters(field);

        // remove from owner and flags collection

        Map<FieldFlag, List<Field>> allFlags = fieldsByOwnerAndFlag.get(field.getOwner().toLowerCase());

        if (allFlags != null) {
            for (FieldFlag flag : field.getFlagsModule().getFlags()) {
                fields = allFlags.get(flag);

                if (fields != null) {
                    fields.remove(field);
                }
            }
        }

        // remove from sources collection

        removeSourceField(field);

        FieldSettings fs = field.getSettings();

        if (fs != null) {
            List<FieldFlag> flags = new ArrayList<FieldFlag>();
            flags.addAll(fs.getDefaultFlags());
            flags.addAll(field.getFlagsModule().getInsertedFlags());

            // delete any snitch entries

            if (flags.contains(FieldFlag.SNITCH)) {
                plugin.getStorageManager().deleteSnitchEntries(field);
            }

            // remove from grief-undo and delete any records on the database

            if (flags.contains(FieldFlag.GRIEF_REVERT)) {
                plugin.getGriefUndoManager().remove(field);
                plugin.getStorageManager().deleteBlockGrief(field);
            }
        }

        // remove all people as having entered the field

        plugin.getEntryManager().removeAllPlayers(field);

        // delete siblings and parent if exists

        if (field.isParent()) {
            for (Field c : field.getChildren()) {
                c.clearParent();
                queueRelease(c);
            }
            field.clearChildren();
            flush();
        }

        // if the child's parent is not open, then remove the whole family

        if (field.isChild()) {
            release(field.getParent());
            return;
        }

        // delete from database

        field.markForDeletion();
        plugin.getStorageManager().offerField(field);
    }

    /**
     * Remove all tracked renters from a field
     *
     * @param field
     */
    public void removeAllRenters(Field field) {
        List<String> renters = field.getRenters();
        if (renters != null) {
            for (String renter : renters) {
                removeRenter(field, renter);
            }
        }
    }

    /**
     * Update tracking when a renter has been removed from a field.
     *
     * @param field
     * @param renter
     */
    public void removeRenter(Field field, String renter) {
        Map<BlockTypeEntry, List<Field>> renterTypes = fieldsByRenterAndType.get(renter.toLowerCase());

        if (renterTypes == null) {
            return;
        }

        List<Field> fields = renterTypes.get(field.getTypeEntry());
        if (fields == null) {
            return;
        }

        fields.remove(field);
        if (fields.isEmpty()) {
            renterTypes.put(field.getTypeEntry(), fields);
        } else {
            renterTypes.remove(field.getTypeEntry());
        }
        if (renterTypes.isEmpty()) {
            fieldsByRenterAndType.remove(renter);
        } else {
            fieldsByRenterAndType.put(renter, renterTypes);
        }
    }

    /**
     * Remove a field's enveloped chunks from the source fields collection
     *
     * @param field
     */
    public void removeSourceField(Field field) {
        Set<ChunkVec> scvs = field.getEnvelopingChunks();

        for (ChunkVec scv : scvs) {
            HashMap<FieldFlag, List<Field>> sf = sourceFields.get(scv);

            if (sf != null) {
                List<FieldFlag> flags = new ArrayList<FieldFlag>();
                flags.addAll(field.getSettings().getDefaultFlags());
                flags.addAll(field.getFlagsModule().getInsertedFlags());

                for (FieldFlag flag : flags) {
                    List<Field> fields = sf.get(flag);

                    if (fields != null) {
                        fields.remove(field);

                        if (fields.isEmpty()) {
                            sf.remove(flag);
                        }
                    }
                }

                if (sf.isEmpty()) {
                    sourceFields.remove(scv);
                }
            }
        }
    }


    /**
     * Get all rented or owned fields for a player in all worlds
     *
     * @param owner
     * @return
     */
    public List<Field> getAllPlayerFields(String owner) {
        List<Field> out = new ArrayList<Field>();
        owner = owner.toLowerCase();
        Map<BlockTypeEntry, List<Field>> owned = fieldsByOwnerAndType.get(owner);
        if (owned != null) {
            for (List<Field> fields : owned.values()) {
                out.addAll(fields);
            }
        }
        Map<BlockTypeEntry, List<Field>> rented = fieldsByRenterAndType.get(owner);
        if (rented != null) {
            for (List<Field> fields : rented.values()) {
                out.addAll(fields);
            }
        }
        List<Field> allowed = fieldsByAllowed.get(owner);
        if (allowed != null) {
            out.addAll(allowed);
        }
        return out;
    }

    /**
     * Get all fields a player/g:group/c:clan/* is allowed in for a world
     *
     * @param target
     * @param world
     * @return
     */
    public List<Field> getFields(String target, World world) {
        List<Field> out = new ArrayList<Field>();

        List<Field> fields = fieldsByWorld.get(world.getName());

        if (fields != null) {
            for (Field field : fields) {
                if (target.equals("*")) {
                    out.add(field);
                    continue;
                }

                if (target.contains("g:")) {
                    String group = target.substring(2);

                    if (plugin.getPermissionsManager().inGroup(field.getOwner(), world, group)) {
                        out.add(field);
                    }
                    continue;
                }

                if (target.contains("c:")) {
                    String clan = target.substring(2);

                    if (plugin.getSimpleClansManager().isInClan(field.getOwner(), clan)) {
                        out.add(field);
                    }
                    continue;
                }

                if (target.contains("t:")) {
                    String tm = target.substring(2);

                    OfflinePlayer offlinePlayer = PreciousStones.getInstance().getServer().getOfflinePlayer(field.getOwner());

                    if (offlinePlayer != null) {
                        ScoreboardManager manager = Bukkit.getScoreboardManager();
                        Scoreboard board = manager.getMainScoreboard();

                        Team team = board.getPlayerTeam(offlinePlayer);

                        if (team != null) {
                            if (tm.equalsIgnoreCase(team.getName())) {
                                out.add(field);
                            }
                        }
                    }
                    continue;
                }

                if (field.isOwner(target)) {
                    out.add(field);
                }
            }
        }

        return out;
    }

    /**
     * Gets the field object from a block, if the block is a field
     *
     * @param block the block that is a field
     * @return the field object from the block
     */
    public Field getField(Block block) {
        return getField(block.getLocation());
    }

    /**
     * Gets the field object from a block, if the block is a field
     *
     * @return the field object from the block
     */
    public Field getField(Location location) {
        return fieldsByVec.get(new Vec(location));
    }

    /**
     * Looks for the block in our field collection
     *
     * @param fieldBlock
     * @return confirmation
     */
    public boolean isField(Block fieldBlock) {
        return getField(fieldBlock) != null;
    }

    /**
     * Total number of forcefield stones
     *
     * @return the count
     */
    public int getCount() {
        int size = 0;

        List<World> worlds = plugin.getServer().getWorlds();

        if (worlds != null) {
            for (World world : worlds) {
                List<Field> fields = fieldsByWorld.get(world.getName());

                if (fields != null) {
                    size += fields.size();
                }
            }
        }

        return size;
    }

    /**
     * things to do before shutdown
     */
    public void offerAllDirtyFields() {
        Collection<Field> fields = new ArrayList<>(fieldsByVec.values());

        for (Field field : fields) {
            if (field.isDirty()) {
                plugin.getStorageManager().offerField(field);
            }
        }
    }

    /**
     * Gets field counts for player/g:group/c:clan/*
     *
     * @param target
     * @return
     */
    public HashMap<BlockTypeEntry, Integer> getFieldCounts(String target) {
        HashMap<BlockTypeEntry, Integer> counts = new HashMap<BlockTypeEntry, Integer>();
        List<World> worlds = plugin.getServer().getWorlds();

        for (World world : worlds) {
            List<Field> fields = getFields(target, world);

            for (Field field : fields) {
                if (counts.containsKey(field.getTypeEntry())) {
                    counts.put(field.getTypeEntry(), counts.get(field.getTypeEntry()) + 1);
                } else {
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
    public int cleanOrphans(World world) {
        int cleanedCount = 0;

        List<Field> fields = fieldsByWorld.get(world.getName());

        if (fields != null) {
            for (Field field : fields) {
                if (!field.getHidingModule().isHidden() || field.missingBlock()) {
                    if (!field.matchesBlockType()) {
                        cleanedCount++;
                        queueRelease(field);
                    }
                }
            }
        }

        flush();

        if (cleanedCount != 0) {
            PreciousStones.log("countsOrphan", world.getName(), cleanedCount);
        }
        return cleanedCount;
    }

    /**
     * Revert orphan fields
     *
     * @param world
     * @return
     */
    public int revertOrphans(World world) {
        int revertedCount = 0;
        boolean currentChunkLoaded = false;
        ChunkVec currentChunk = null;

        List<Field> fields = fieldsByWorld.get(world.getName());

        if (fields != null) {
            for (Field field : fields) {
                // ensure chunk is loaded prior to polling

                ChunkVec cv = field.toChunkVec();

                if (!cv.equals(currentChunk)) {
                    if (!currentChunkLoaded) {
                        if (currentChunk != null) {
                            world.unloadChunk(currentChunk.getX(), currentChunk.getZ());
                        }
                    }

                    currentChunkLoaded = world.isChunkLoaded(cv.getX(), cv.getZ());

                    if (!currentChunkLoaded) {
                        world.loadChunk(cv.getX(), cv.getZ());
                    }

                    currentChunk = cv;
                }

                if (!field.getHidingModule().isHidden()) {
                    if (!field.matchesBlockType()) {
                        Block block = field.getBlock();
                        block.setTypeId(field.getTypeId());
                        block.setData((byte) field.getData());
                        revertedCount++;
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
    public Block getBlock(Field field) {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world == null) {
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
    public boolean isRedstoneHookedDisabled(Field field) {
        Block block = getBlock(field);

        if (isAnywayPowered(block)) {
            return false;
        }

        int topId = block.getRelative(BlockFace.UP).getTypeId();

        if (topId == 70 || topId == 72) // plates
        {
            return true;
        }

        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

        for (BlockFace face : faces) {
            Block faceblock = block.getRelative(face);
            int faceId = faceblock.getTypeId();

            if (faceId == 75)  // redstone torch
            {
                return true;
            }

            if (faceId == 77) // stone button
            {
                return true;
            }

            if (faceId == 69 /* lever */ && faceblock.getBlockPower() == 0) {
                return true;
            }

            if (faceId == 55 /* redstone wire */ && faceblock.getBlockPower() == 0) {
                return true;
            }
        }

        BlockFace[] upfaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        Block upblock = block.getRelative(BlockFace.UP);

        for (BlockFace face : upfaces) {
            Block faceblock = upblock.getRelative(face);
            int faceId = faceblock.getTypeId();

            if (faceId == 55 /* redstone wire */ && faceblock.getBlockPower() == 0) {
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
    public boolean powersField(Field field, Block block) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP};

        for (BlockFace face : faces) {
            Block faceblock = block.getRelative(face);

            if (field.getX() == faceblock.getX() && field.getY() == faceblock.getY() && field.getZ() == faceblock.getZ()) {
                return true;
            }
        }

        BlockFace[] downfaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        Block upblock = block.getRelative(BlockFace.DOWN);

        for (BlockFace face : downfaces) {
            Block faceblock = upblock.getRelative(face);

            if (field.getX() == faceblock.getX() && field.getY() == faceblock.getY() && field.getZ() == faceblock.getZ()) {
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
    public boolean isAnywayPowered(Block block) {
        if (block.isBlockIndirectlyPowered() || block.isBlockPowered()) {
            return true;
        }

        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

        for (BlockFace face : faces) {
            Block source = block.getRelative(face);

            if (source.getTypeId() == 55) // redstone wire
            {
                if (source.getBlockPower() > 0) {
                    return true;
                }
            }
        }

        BlockFace[] upfaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        Block upblock = block.getRelative(BlockFace.UP);

        for (BlockFace face : upfaces) {
            Block faceblock = upblock.getRelative(face);

            if (faceblock.getTypeId() == 55) // redstone wire
            {
                if (faceblock.getBlockPower() > 0) {
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
    public void announceAllowedPlayers(Field field, String msg) {
        List<String> allowed = field.getAllAllowed();

        for (String playerName : allowed) {
            Player pl = Bukkit.getServer().getPlayerExact(playerName);

            if (pl != null) {
                ChatHelper.send(pl, "announceToAllowedPlayers", msg);
            }
        }
    }

    /**
     * Whether the block is touching a field block
     *
     * @param block
     * @return the touching block, null if none
     */
    public Block touchingFieldBlock(Block block) {
        if (block == null) {
            return null;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    Block surroundingBlock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);

                    if (isField(surroundingBlock)) {
                        return surroundingBlock;
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
    public boolean cleanSnitchList(Field field) {
        FieldSettings fs = field.getSettings();

        if (fs.hasDefaultFlag(FieldFlag.SNITCH)) {
            field.getSnitchingModule().clearSnitch();
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
    public boolean setNameField(Field field, String name) {
        FieldSettings fs = field.getSettings();

        if ((fs.hasNameableFlag()) && !field.getName().equals(name)) {
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
    public HashSet<String> getWho(Player owner, Field field) {
        HashSet<String> playerNames = plugin.getEntryManager().getInhabitants(field);

        for (Iterator iter = playerNames.iterator(); iter.hasNext(); ) {
            String playerName = (String) iter.next();

            Player player = Bukkit.getServer().getPlayer(playerName);

            if (player != null) {
                if (plugin.getPermissionsManager().isVanished(player)) {
                    iter.remove();
                } else if (!owner.canSee(player)) {
                    iter.remove();
                }
            }
        }

        return playerNames;
    }

    /**
     * Determine whether a player is allowed on a field
     *
     * @param fieldBlock
     * @param playerName
     * @return confirmation
     */
    public boolean isAllowed(Block fieldBlock, String playerName) {
        Field field = getField(fieldBlock);
        return field != null && isAllowed(field, playerName);
    }

    /**
     * Whether the player is allowed in the field
     *
     * @param field
     * @param target
     * @return
     */
    public boolean isAllowed(Field field, String target) {
        if (field == null || target == null) {
            return false;
        }

        Player player = Bukkit.getServer().getPlayerExact(target);

        if (player != null) {
            // allow if admin

            if (plugin.getPermissionsManager().has(player, "preciousstones.admin.allowed")) {
                if (!field.hasFlag(FieldFlag.NO_ALLOWING)) {
                    return true;
                }
            }
        }

        // false if settings missing

        if (field.getSettings() == null) {
            return false;
        }

        // deny if doesn't have the required perms

        if (!field.getSettings().getRequiredPermissionAllow().isEmpty()) {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.required-permission")) {
                if (!plugin.getPermissionsManager().has(player, field.getSettings().getRequiredPermissionAllow())) {
                    return false;
                }
            }
        }

        // allow if in global allowed list

        if (field.getSettings().inAllowedList(target)) {
            return true;
        }

        // allow if in global deny list

        if (field.getSettings().inDeniedList(target)) {
            return false;
        }

        // always allow if in war

        if (plugin.getSettingsManager().isWarAllow()) {
            if (plugin.getSimpleClansManager().inWar(field, target)) {
                return true;
            }
        }

        return field.isAllowed(target);
    }

    /**
     * Whether the field is owned by the player
     *
     * @param field
     * @param player
     * @return
     */
    public boolean isOwned(Field field, Player player) {
        if (field == null || player == null) {
            return false;
        }

        // allow if admin

        if (plugin.getPermissionsManager().has(player, "preciousstones.admin.allowed")) {
            if (!field.hasFlag(FieldFlag.NO_ALLOWING)) {
                return true;
            }
        }

        return field.isOwner(player.getName());
    }

    /**
     * Allow a target (name, g:group, c:clan) into a field
     *
     * @param field
     * @param target
     * @return whether he got allowed
     */
    public boolean addAllowed(Field field, String target, boolean isGuest) {
        if (!field.isInAllowedList(target)) {
            field.addAllowed(target, isGuest);
            plugin.getStorageManager().offerField(field);
            List<Field> allowed = fieldsByAllowed.get(target);
            if (allowed == null) {
                allowed = new ArrayList<Field>();
            }
            allowed.add(field);
            fieldsByAllowed.put(target, allowed);
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
    public boolean removeAllowed(Field field, String target) {
        if (field.isInAllowedList(target)) {
            field.removeAllowed(target);
            plugin.getStorageManager().offerField(field);

            List<Field> allowed = fieldsByAllowed.get(target);
            if (allowed != null) {
                allowed.remove(field);
                if (allowed.isEmpty()) {
                    fieldsByAllowed.remove(target);
                }
            }
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
    public int allowAll(Player player, String allowedName, boolean isGuest) {
        List<Field> fields = getOwnersFields(player, FieldFlag.ALL);

        int allowedCount = 0;
        int notAllowed = 0;

        for (Field field : fields) {
            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled")) {
                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED)) {
                    if (!field.isDisabled()) {
                        notAllowed++;
                        continue;
                    }
                }
            }

            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.no-allowing")) {
                if (field.hasFlag(FieldFlag.NO_ALLOWING)) {
                    continue;
                }
            }

            if (!isAllowed(field, allowedName)) {
                if (addAllowed(field, allowedName, isGuest)) {
                    allowedCount++;
                }
            }
            plugin.getStorageManager().offerField(field);
        }

        if (notAllowed > 0) {
            ChatHelper.send(player, "fieldsSkipped", notAllowed);
        }

        return allowedCount;
    }

    /**
     * Removed intersecting fields owned by player
     *
     * @param field
     * @param allowedName
     * @return
     */
    public int removeConflictingFields(Field field, String allowedName) {
        Set<Field> sources = field.getIntersectingFields();

        int conflicted = 0;

        for (Field source : sources) {
            if (source.hasFlag(FieldFlag.NO_CONFLICT)) {
                continue;
            }

            if (field.getOwner().equalsIgnoreCase(source.getOwner())) {
                continue;
            }

            if (source.isOwner(allowedName)) {
                deleteField(source);
                conflicted++;
            }
        }

        return conflicted;
    }

    /**
     * If the field has any sub-plotted fields
     *
     * @param field
     */
    public boolean hasSubFields(Field field) {
        Set<Field> sources = field.getIntersectingFields();

        for (Field source : sources) {
            if (source.getSettings().isAllowedOnlyInside(field)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove allowed player from all your force fields
     *
     * @param player
     * @param target
     * @return count of fields the player was removed from
     */
    public int removeAll(Player player, String target) {
        List<Field> fields = getOwnersFields(player, FieldFlag.ALL);

        int removedCount = 0;
        int notRemoved = 0;

        for (Field field : fields) {
            if (field.containsPlayer(target)) {
                ChatHelper.send(player, "playerInsideNotRemoved");
                continue;
            }

            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.on-disabled")) {
                if (field.hasFlag(FieldFlag.MODIFY_ON_DISABLED)) {
                    if (!field.isDisabled()) {
                        notRemoved++;
                        continue;
                    }
                }
            }

            /*
            int conflicted = removeConflictingFields(field, target);

            if (conflicted > 0) {
                ChatHelper.send(player, "removedConflictingFields", conflicted, target);
                continue;
            }
            */

            if (isAllowed(field, target)) {
                if (removeAllowed(field, target)) {
                    removedCount++;
                }
            }
            plugin.getStorageManager().offerField(field);
        }

        if (notRemoved > 0) {
            ChatHelper.send(player, "fieldsSkipped", notRemoved);
        }

        return removedCount;
    }

    /**
     * Return the owner of a field
     *
     * @param fieldBlock a block which is a field
     * @return owner's name
     */
    public String getOwner(Block fieldBlock) {
        Field field = getField(fieldBlock);

        if (field != null) {
            return field.getOwner();
        }
        return "";
    }

    /**
     * Get all the fields belonging to players,  you can pass field flags and it will only retrieve those matching the field flags
     *
     * @param player
     * @return the fields
     */
    public List<Field> getOwnersFields(Player player, FieldFlag flag) {
        List<Field> fields = fieldsByFlag.get(flag);
        List<Field> out = new ArrayList<Field>();

        if (fields != null) {
            for (Field field : fields) {
                if (field.isOwner(player.getName())) {
                    out.add(field);
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
    public List<Field> getSourceFieldsInChunk(Chunk chunk, FieldFlag flag, ResultsFilter... filters) {
        return getSourceFieldsInChunk(new ChunkVec(chunk), flag, filters);
    }

    /**
     * Get all fields matching this flag that are touching this chunk
     *
     * @param cv
     * @return
     */
    public List<Field> getSourceFieldsInChunk(ChunkVec cv, FieldFlag flag, ResultsFilter... filters) {
        HashMap<FieldFlag, List<Field>> flagList = sourceFields.get(cv);

        if (flagList != null) {
            List<Field> fields = flagList.get(flag);

            if (fields != null && !fields.isEmpty()) {
                fields = new ArrayList<Field>(fields);

                if (!fields.isEmpty()) {
                    for (Iterator it = fields.iterator(); it.hasNext(); ) {
                        Field field = (Field) it.next();

                        // go through each of the filters
                        // and apply the them

                        for (ResultsFilter filter : filters) {
                            if (!filter.Filter(field)) {
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
    public Field getSmallestSourceFieldInChunk(Chunk chunk, FieldFlag flag, ResultsFilter... filters) {
        ChunkVec cv = new ChunkVec(chunk);

        List<Field> allFields = getSourceFieldsInChunk(cv, FieldFlag.ALL, filters);

        if (allFields.isEmpty()) {
            return null;
        }

        Field absoluteSmallest = getSmallestVolumeField(allFields);

        // find absolute smallest (regardless of whether it contains the queried flag or not)

        if (absoluteSmallest != null) {
            if (absoluteSmallest.hasFlag(FieldFlag.PLOT)) {
                // if it doesn't have the flag don't return anything at all

                if (!absoluteSmallest.hasFlag(flag)) {
                    return null;
                }

                return absoluteSmallest;
            }
        }

        // otherwise return the smallest that matches that flag

        List<Field> fields = getSourceFieldsInChunk(cv, flag, filters);

        Field smallest = getSmallestVolumeField(fields);

        return smallest;
    }

    /**
     * Gets the smallest field from a list of fields
     *
     * @param fields
     * @return
     */
    public Field getSmallestVolumeField(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        // sort fields by volume

        Collections.sort(fields, new Comparator<Field>() {
            public int compare(Field f1, Field f2) {
                Integer o1 = f1.getFlatVolume();
                Integer o2 = f2.getFlatVolume();

                PreciousStones.debug("%s: %s", f1.getType(), o1);
                PreciousStones.debug("%s: %s", f2.getType(), o2);

                return o1.compareTo(o2);
            }
        });

        // return smallest fields where a player can fit

        for (Field smallest : fields) {
            if (smallest.hasFlag(FieldFlag.ANTI_PLOT)) {
                continue;
            }

            if (smallest.getActualVolume() > 1 && smallest.getHeight() > 1) {
                return smallest;
            }
        }

        return fields.get(0);
    }

    /**
     * Returns the fields that the location is in match the field flag(s)
     *
     * @param loc
     * @param flag
     * @return the fields
     */
    public List<Field> getSourceFields(final Location loc, final FieldFlag flag) {
        ResultsFilter envelopsFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return field.envelops(loc);
            }
        };

        return getSourceFieldsInChunk(new ChunkVec(loc.getChunk()), flag, envelopsFilter);
    }

    /**
     * Returns the enabled fields that the location is in match the field flag(s)
     *
     * @param loc
     * @param flag
     * @return the fields
     */
    public List<Field> getEnabledSourceFields(final Location loc, final FieldFlag flag) {
        ResultsFilter envelopsFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return field.envelops(loc);
            }
        };

        ResultsFilter disabledFlagFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return !field.getFlagsModule().hasDisabledFlag(flag);
            }
        };

        ResultsFilter notDisabledFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return !field.isDisabled();
            }
        };

        ResultsFilter disableIfOnlineFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return !field.hasFlag(FieldFlag.DISABLE_WHEN_ONLINE) || !field.hasOnlineAllowed();

            }
        };

        Chunk c = null;
        try {
            c = loc.getChunk();
        } catch (Exception e) {
            // some weird stuffs going on
        }

        if (c == null) {
            return new ArrayList<Field>();
        }

        return getSourceFieldsInChunk(new ChunkVec(c), flag, envelopsFilter, disabledFlagFilter, notDisabledFilter, disableIfOnlineFilter);
    }

    /**
     * Returns a field in the location that matches the field flag(s)
     *
     * @param loc
     * @param flag
     * @return the fields
     */
    public Field getEnabledSourceField(final Location loc, final FieldFlag flag) {
        ResultsFilter envelopsFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return field.envelops(loc);
            }
        };

        ResultsFilter disabledFlagFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return !field.getFlagsModule().hasDisabledFlag(flag);
            }
        };

        ResultsFilter notDisabledFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return !field.isDisabled();
            }
        };

        ResultsFilter disableIfOnlineFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                if (field.hasFlag(FieldFlag.DISABLE_WHEN_ONLINE)) {
                    return !field.hasOnlineAllowed();
                }

                return true;
            }
        };

        return getSmallestSourceFieldInChunk(loc.getChunk(), flag, envelopsFilter, notDisabledFilter, disabledFlagFilter, disableIfOnlineFilter);
    }

    /**
     * Returns the first conflict field found in the location and that the player is not allowed in, optionally with field flags
     *
     * @param loc
     * @param playerName
     * @return the fields
     */
    public Field getConflictSourceField(final Location loc, final String playerName, FieldFlag flag) {
        ResultsFilter envelopsFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return field.envelops(loc);
            }
        };

        ResultsFilter noConflictFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return !field.hasFlag(FieldFlag.NO_CONFLICT);
            }
        };

        ResultsFilter allowedFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return !isAllowed(field, playerName);
            }
        };

        return getSmallestSourceFieldInChunk(loc.getChunk(), flag, allowedFilter, envelopsFilter, noConflictFilter);
    }

    /**
     * If a field in the location that matches the field flag(s)
     *
     * @param loc
     * @param flag
     * @return result
     */
    public boolean hasSourceField(Location loc, FieldFlag flag) {
        return getEnabledSourceField(loc, flag) != null;
    }

    /**
     * Returns the fields in the chunk and adjacent chunks
     *
     * @param loc
     * @param chunkRadius
     * @return the fields
     */
    public Set<Field> getFieldsInCustomArea(final Location loc, int chunkRadius, FieldFlag flag) {
        Set<Field> out = new HashSet<Field>();

        int xlow = (loc.getBlockX() >> 4) - chunkRadius;
        int xhigh = (loc.getBlockX() >> 4) + chunkRadius;
        int zlow = (loc.getBlockZ() >> 4) - chunkRadius;
        int zhigh = (loc.getBlockZ() >> 4) + chunkRadius;

        for (int x = xlow; x <= xhigh; x++) {
            for (int z = zlow; z <= zhigh; z++) {
                ResultsFilter envelopsFilter = new ResultsFilter() {
                    public boolean Filter(Field field) {
                        return field.envelops(loc);
                    }
                };

                List<Field> fields = getSourceFieldsInChunk(new ChunkVec(x, z, loc.getWorld().getName()), flag, envelopsFilter);

                if (fields != null) {
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
    public Set<Field> getFieldsInCustomArea(final Location loc, int chunkradius, FieldFlag flag, Player player) {
        Set<Field> out = new HashSet<Field>();

        int xlow = (loc.getBlockX() >> 4) - chunkradius;
        int xhigh = (loc.getBlockX() >> 4) + chunkradius;
        int zlow = (loc.getBlockZ() >> 4) - chunkradius;
        int zhigh = (loc.getBlockZ() >> 4) + chunkradius;

        for (int x = xlow; x <= xhigh; x++) {
            for (int z = zlow; z <= zhigh; z++) {
                ResultsFilter envelopsFilter = new ResultsFilter() {
                    public boolean Filter(Field field) {
                        return field.envelops(loc);
                    }
                };

                List<Field> fields = getSourceFieldsInChunk(new ChunkVec(x, z, loc.getWorld().getName()), flag, envelopsFilter);

                if (fields != null) {
                    for (Field field : fields) {
                        if (isAllowed(field, player.getName())) {
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
    public Field getPointedField(Player player, boolean allowed) {
        TargetBlock aiming = new TargetBlock(player, plugin.getSettingsManager().getMaxTargetDistance(), 0.2, plugin.getSettingsManager().getThroughFieldsSet());
        Block targetBlock = aiming.getTargetBlock();

        if (targetBlock != null) {
            Field f = getField(targetBlock);

            if (f != null) {
                if (f.isChild()) {
                    f = f.getParent();
                }

                if (!allowed) {
                    return f;
                }

                if (isAllowed(f, player.getName())) {
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
    public Field getOneAllowedField(final Block blockInArea, final Player player, FieldFlag flag) {
        Field pointed = getPointedField(player, true);

        if (pointed != null) {
            return pointed;
        }

        ResultsFilter envelopsFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return field.envelops(blockInArea.getLocation());
            }
        };

        ResultsFilter allowedFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return isAllowed(field, player.getName());
            }
        };

        return getSmallestSourceFieldInChunk(blockInArea.getLocation().getChunk(), flag, envelopsFilter, allowedFilter);
    }


    /**
     * Returns the field if he's standing in at least one owned field, optionally matching field flags
     *
     * @param blockInArea
     * @param player
     * @return the field
     */
    public Field getOneOwnedField(final Block blockInArea, final Player player, FieldFlag flag) {
        Field pointed = getPointedField(player, true);

        if (pointed != null) {
            return pointed;
        }

        ResultsFilter envelopsFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return field.envelops(blockInArea.getLocation());
            }
        };

        ResultsFilter allowedFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return isOwned(field, player);
            }
        };

        return getSmallestSourceFieldInChunk(blockInArea.getLocation().getChunk(), flag, envelopsFilter, allowedFilter);
    }

    /**
     * Returns the field if he's standing in at least one non-owned field, optionally matching field flags
     *
     * @param blockInArea
     * @param player
     * @return the field
     */
    public Field getOneNonOwnedField(final Block blockInArea, final Player player, FieldFlag flag) {
        Field pointed = getPointedField(player, false);

        if (pointed != null) {
            return pointed;
        }

        ResultsFilter envelopsFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return field.envelops(blockInArea.getLocation());
            }
        };

        ResultsFilter allowedFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return !isOwned(field, player);
            }
        };

        return getSmallestSourceFieldInChunk(blockInArea.getLocation().getChunk(), flag, envelopsFilter, allowedFilter);
    }

    /**
     * Returns the field if he's standing in at least one field, optionally matching field flags
     *
     * @param blockInArea
     * @param player
     * @return the field
     */
    public Field getOneField(final Block blockInArea, final Player player, FieldFlag flag) {
        Field pointed = getPointedField(player, true);

        if (pointed != null) {
            return pointed;
        }

        ResultsFilter envelopsFilter = new ResultsFilter() {
            public boolean Filter(Field field) {
                return field.envelops(blockInArea.getLocation());
            }
        };

        return getSmallestSourceFieldInChunk(blockInArea.getLocation().getChunk(), flag, envelopsFilter);
    }

    /**
     * Return the first field that conflicts with the unbreakable
     *
     * @param placedBlock
     * @param placer
     * @return the field, null if none found
     */
    public Field unbreakableConflicts(Block placedBlock, Player placer) {
        List<Field> sources = getSourceFields(placedBlock.getLocation(), FieldFlag.ALL);

        ArrayList<Field> out = new ArrayList<Field>();

        for (Field field : sources) {
            if (field.hasFlag(FieldFlag.NO_CONFLICT)) {
                continue;
            }

            if (isAllowed(field, placer.getName())) {
                continue;
            }

            if (field.envelops(placedBlock)) {
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
    public Field fieldConflicts(Block placedBlock, Player placer) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(placedBlock);

        if (fs == null) {
            return null;
        }

        if (fs.hasDefaultFlag(FieldFlag.NO_CONFLICT)) {
            return null;
        }

        // create throwaway field to test intersection

        Field placedField = new Field(placedBlock, fs.getRadius(), fs.getCustomHeight());

        Set<Field> intersecting = placedField.getIntersectingFields();

        ArrayList<Field> out = new ArrayList<Field>();

        for (Field field : intersecting) {
            if (field.hasFlag(FieldFlag.NO_CONFLICT)) {
                continue;
            }

            if (isAllowed(field, placer.getName())) {
                continue;
            }

            out.add(field);
        }

        return getSmallestVolumeField(out);
    }

    /**
     * Whether the provided field would conflict with existing fields in the area
     *
     * @param mockField
     * @param placer
     * @return
     */
    public boolean existsConflict(Field mockField, Player placer) {
        if (mockField.hasFlag(FieldFlag.NO_CONFLICT)) {
            return false;
        }

        Set<Field> intersecting = mockField.getIntersectingFields();

        for (Field field : intersecting) {
            if (field.hasFlag(FieldFlag.NO_CONFLICT)) {
                continue;
            }

            if (isAllowed(field, placer.getName())) {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * Return the first field that conflicts with the cuboid entry
     *
     * @param ce
     * @param placer
     * @return the field, null if none found
     */
    public Field fieldConflicts(CuboidEntry ce, Player placer) {
        if (ce.getField().hasFlag(FieldFlag.NO_CONFLICT)) {
            return null;
        }

        Set<Field> intersecting = ce.getField().getIntersectingFields();

        ArrayList<Field> out = new ArrayList<Field>();

        for (Field field : intersecting) {
            if (field.hasFlag(FieldFlag.NO_CONFLICT)) {
                continue;
            }

            if (isAllowed(field, placer.getName())) {
                continue;
            }

            out.add(field);
        }

        return getSmallestVolumeField(out);
    }

    /**
     * Allows all owners of fields that are intersecting
     *
     * @param field
     */
    public void addAllowOverlappingOwners(Field field) {
        FieldSettings fs = field.getSettings();

        if (fs == null) {
            return;
        }

        if (fs.hasDefaultFlag(FieldFlag.NO_CONFLICT)) {
            return;
        }

        // create throwaway field to test intersection

        Set<Field> intersecting = field.getIntersectingFields();

        for (Field overlap : intersecting) {
            if (overlap.hasFlag(FieldFlag.NO_CONFLICT)) {
                continue;
            }

            if (overlap.isAllowed(field.getOwner())) {
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
    public int deleteBelonging(String playerName) {
        int deletedFields = 0;

        List<Field> fields = getFieldsByOwner().get(playerName.toLowerCase());

        if (fields != null) {
            for (Field field : fields) {
                if (field.hasFlag(FieldFlag.NO_OWNER)) {
                    continue;
                }

                queueRelease(field);
                deletedFields++;
            }
        }
        flush();

        return deletedFields;
    }

    /**
     * Hide all fields belonging to a player
     *
     * @param playerName the players
     * @return the count of hidden fields
     */
    public int hideBelonging(String playerName) {
        int hiddenFields = 0;

        List<Field> fields = getFieldsByOwner().get(playerName.toLowerCase());

        if (fields != null) {
            for (Field field : fields) {
                if (field.hasFlag(FieldFlag.HIDABLE)) {
                    if (!field.getHidingModule().isHidden()) {
                        if (field.hasFlag(FieldFlag.NO_OWNER)) {
                            continue;
                        }

                        if (!field.matchesBlockType()) {
                            continue;
                        }

                        field.getHidingModule().hide();
                        hiddenFields++;
                    }
                }
            }
        }

        return hiddenFields;
    }

    /**
     * Unhides all fields belonging to a player
     *
     * @param playerName the players
     * @return the count of hidden fields
     */
    public int unhideBelonging(String playerName) {
        int unhiddenFields = 0;

        List<Field> fields = getFieldsByOwner().get(playerName.toLowerCase());

        if (fields != null) {
            for (Field field : fields) {
                if (field.hasFlag(FieldFlag.HIDABLE)) {
                    if (field.getHidingModule().isHidden()) {
                        if (field.hasFlag(FieldFlag.NO_OWNER)) {
                            continue;
                        }

                        field.getHidingModule().unHide();
                        unhiddenFields++;
                    }
                }
            }
        }

        return unhiddenFields;
    }

    /**
     * Deletes a field from the collection
     *
     * @param block
     */
    public void release(Block block) {
        Field field = getField(block);

        if (field != null) {
            release(field);
        }
    }

    /**
     * Deletes a field from the collection
     *
     * @param field
     */
    public void release(Field field) {
        dropField(field);
        deleteField(field);
    }

    /**
     * Deletes a field and wipe it out (set to air)
     *
     * @param field
     */
    public void releaseWipe(Field field) {
        deleteField(field);
        field.getBlock().setType(Material.AIR);
    }

    /**
     * Deletes a field and wipe it out (set to air)
     *
     * @param block
     */
    public void releaseWipe(Block block) {
        releaseWipe(getField(block));
    }

    /**
     * Deletes a field silently (no drop)
     *
     * @param field
     */
    public void releaseNoDrop(Field field) {
        deleteField(field);
    }

    /**
     * Adds a field to deletion queue
     *
     * @param field
     */
    public void queueRelease(Field field) {
        if (!deletionQueue.contains(field)) {
            deletionQueue.add(field);
        }
    }

    /**
     * Delete fields in deletion queue
     */
    public void flush() {
        while (!deletionQueue.isEmpty()) {
            Field pending = deletionQueue.poll();

            dropField(pending);
            deleteField(pending);
        }
    }

    /**
     * Delete fields in deletion queue
     */
    public void flushNoDrop() {
        while (!deletionQueue.isEmpty()) {
            Field pending = deletionQueue.poll();

            Block block = pending.getBlock();
            if (block != null) {
                block.setType(Material.AIR);
            }
            deleteField(pending);
        }
    }

    /**
     * Drops a field
     *
     * @param field
     */
    public void dropField(Field field) {

        // unhide it

        field.getHidingModule().unHide();

        // drop it

        dropBlock(field.getBlock(), field.getTypeEntry(), field.getSettings());
    }

    /**
     * Drop a block with a specific type and metadata (if applicable)
     *
     * @param block
     * @param type
     * @param settings
     */
    public void dropBlock(Block block, BlockTypeEntry type, FieldSettings settings) {
        // build item

        World world = block.getWorld();
        ItemStack is = new ItemStack(type.getTypeId(), 1, (short) 0, type.getData());

        // apply meta name and lore

        if (settings.hasMetaName()) {
            StackHelper.setItemMeta(is, settings);
        }

        // wipe previous block

        block.setType(Material.AIR);

        // drop item

        if (plugin.getSettingsManager().isDropOnDelete()) {
            world.dropItemNaturally(block.getLocation(), is);
        }
    }

    /**
     * Delete fields the the player is standing on
     *
     * @param sourceFields
     * @return count of fields deleted
     */
    public int deleteFields(List<Field> sourceFields) {
        int deletedCount = 0;

        for (Field f : sourceFields) {
            queueRelease(f);
            deletedCount++;
        }

        if (deletedCount > 0) {
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
    public int deleteFieldsOfType(BlockTypeEntry type) {
        int deletedCount = 0;

        Collection<Field> fields = fieldsByVec.values();

        for (Field field : fields) {
            if (field.getTypeEntry().equals(type)) {
                queueRelease(field);
                deletedCount++;
            }
        }

        if (deletedCount > 0) {
            flush();
        }
        return deletedCount;
    }

    /**
     * Delete fields of a certain type from a player
     *
     * @param playerName
     * @param type
     * @return count of fields deleted
     */
    public int deletePlayerFieldsOfType(String playerName, BlockTypeEntry type) {
        int deletedCount = 0;

        Map<BlockTypeEntry, List<Field>> types = fieldsByOwnerAndType.get(playerName.toLowerCase());

        if (types != null) {
            List<Field> fields = types.get(type);

            if (fields != null) {
                for (Field field : fields) {
                    queueRelease(field);
                    deletedCount++;
                }

            }
        }

        if (deletedCount > 0) {
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
    public boolean purchase(Player player, double amount) {
        if (plugin.getPermissionsManager().hasEconomy()) {
            if (PermissionsManager.hasMoney(player, amount)) {
                plugin.getPermissionsManager().playerCharge(player, amount);
            } else {
                ChatHelper.send(player, "economyNotEnoughMoney");
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Credits money back to player's account
     *
     * @param player
     * @param amount
     */
    public void refund(Player player, double amount) {
        if (plugin.getPermissionsManager().hasEconomy()) {
            plugin.getPermissionsManager().playerCredit(player, amount);
            ChatHelper.send(player, "economyAccountCredited");
        }
    }

    /**
     * Refunds a field to a player, accounts for parent/child relationships
     *
     * @param player
     */
    public void refundField(Player player, Field field) {
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.purchase")) {
            if (!plugin.getSettingsManager().isNoRefunds()) {
                int refund = field.getSettings().getRefund();

                if (refund > -1) {

                    if (field.isChild() || field.isParent()) {
                        Field parent = field;

                        if (field.isChild()) {
                            parent = field.getParent();
                        }

                        refund(player, refund);

                        for (Field child : parent.getChildren()) {
                            refund = child.getSettings().getRefund();

                            refund(player, refund);
                        }
                    } else {
                        refund(player, refund);
                    }
                }
            }
        }
    }

    /**
     * check if the area a field may cover has players in it
     *
     * @param block
     */
    public boolean fieldTouchesPlayers(Block block, Player self) {
        FieldSettings fs = plugin.getSettingsManager().getFieldSettings(block);

        if (fs == null) {
            return false;
        }

        // create throwaway field to test intersections

        Field field = new Field(block, fs.getRadius(), fs.getCustomHeight());

        List<Player> players = block.getWorld().getPlayers();

        for (Player player : players) {
            if (player.equals(self)) {
                continue;
            }

            if (isAllowed(field, player.getName())) {
                continue;
            }

            if (field.envelops(player.getLocation())) {
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
    public Set<Player> getFieldInhabitants(Field field) {
        Set<Player> out = new HashSet<Player>();
        List<Player> players = field.getLocation().getWorld().getPlayers();

        for (Player player : players) {
            if (field.envelops(player.getLocation())) {
                out.add(player);
            }
        }

        return out;
    }

    /**
     * Change owner of a field
     *
     * @param field
     * @param owner
     */
    public void changeOwner(Field field, String owner) {
        List<Field> fields = getFieldsByOwner().get(field.getOwner().toLowerCase());

        if (fields != null) {
            fields.remove(field);
            field.setNewOwner(owner);
            fields = getFieldsByOwner().get(owner.toLowerCase());

            if (fields == null) {
                fields = new ArrayList<Field>();
            }

            fields.add(field);

            getFieldsByOwner().put(owner.toLowerCase(), fields);
        }
    }

    /**
     * Returns a random field owned by the same player with the same name that has the TELEPORT_DESTINATION flag
     *
     * @param owner
     * @param sourceField
     * @return
     */
    public Field getDestinationField(String owner, Field sourceField) {
        List<Field> out = new ArrayList<Field>();

        List<Field> fields = getFieldsByOwner().get(owner.toLowerCase());

        if (fields != null) {
            for (Field field : fields) {
                if (field.equals(sourceField)) {
                    continue;
                }

                if (!field.hasFlag(FieldFlag.TELEPORT_DESTINATION)) {
                    continue;
                }

                if (field.getName().equalsIgnoreCase(sourceField.getName())) {
                    out.add(field);
                }
            }
        }

        if (out.isEmpty()) {
            return null;
        }

        if (out.size() == 1) {
            return out.get(0);
        }

        return out.get(new Random().nextInt(out.size()));
    }

    /**
     * Disables all fields belonging to a player that have the DISABLE_ON_LOGOFF flag if he is the last allowed player online
     *
     * @param name
     */
    public void disableFieldsOnLogoff(String name) {
        List<Field> fields = getFieldsByOwner().get(name.toLowerCase());

        if (fields != null) {
            for (Field field : fields) {
                if (field.hasFlag(FieldFlag.DISABLE_ON_LOGOFF)) {
                    List<String> allAllowed = field.getAllAllowed();

                    boolean someoneOnline = false;

                    for (String playerName : allAllowed) {
                        Player allowed = Bukkit.getServer().getPlayerExact(playerName);

                        if (allowed != null) {
                            someoneOnline = true;
                        }
                    }

                    if (!someoneOnline) {
                        field.setDisabled(true);
                    }
                }
            }
        }
    }

    /**
     * Enables all fields belonging to a player that have the ENABLE_ON_LOGON flag if he is the last allowed player online
     *
     * @param name
     */
    public void enableFieldsOnLogon(String name) {
        List<Field> fields = getFieldsByOwner().get(name.toLowerCase());

        if (fields != null) {
            for (Field field : fields) {
                if (field.hasFlag(FieldFlag.ENABLE_ON_LOGON)) {
                    List<String> allAllowed = field.getAllAllowed();

                    boolean someoneOnline = false;

                    for (String playerName : allAllowed) {
                        Player allowed = Bukkit.getServer().getPlayerExact(playerName);

                        if (allowed != null) {
                            someoneOnline = true;
                        }
                    }

                    if (!someoneOnline) {
                        field.setDisabled(false);
                    }
                }
            }
        }
    }

    public int getFieldCount(String playerName, BlockTypeEntry type) {
        Map<BlockTypeEntry, List<Field>> types = fieldsByOwnerAndType.get(playerName.toLowerCase());

        if (types != null) {
            List<Field> fields = types.get(type);

            if (fields != null) {
                return fields.size();
            }
        }

        return 0;
    }

    public int getRentedFieldCount(String playerName, BlockTypeEntry type) {
        Map<BlockTypeEntry, List<Field>> types = fieldsByRenterAndType.get(playerName.toLowerCase());

        if (types != null) {
            List<Field> fields = types.get(type);

            if (fields != null) {
                return fields.size();
            }
        }

        return 0;
    }

    public int getTotalFieldCount(String playerName) {
        List<Field> fields = getFieldsByOwner().get(playerName.toLowerCase());

        if (fields != null) {
            return fields.size();
        }

        return 0;
    }

    public List<Field> getFieldsOwnedBy(String playerName, FieldFlag flag) {
        Map<FieldFlag, List<Field>> flags = fieldsByOwnerAndFlag.get(playerName.toLowerCase());

        if (flags != null) {
            return flags.get(flag);
        }

        return null;
    }

    public void removeFieldsIfNoPermission(String playerName) {
        Player player = Bukkit.getServer().getPlayer(playerName);
        Map<String, Integer> deleted = Maps.newHashMap();

        if (player != null) {
            List<Field> fields = getFieldsOwnedBy(playerName, FieldFlag.DELETE_IF_NO_PERMISSION);

            if (fields != null) {

                for (Field field : fields) {
                    String permission = field.getSettings().getDeleteIfNoPermission();

                    if (!permission.isEmpty()) {
                        if (!plugin.getPermissionsManager().has(player, permission)) {
                            queueRelease(field);

                            int count = 0;
                            if (deleted.containsKey(permission)) {
                                count = deleted.get(permission);
                            }
                            deleted.put(permission, ++count);
                        }
                    }
                }

                if (!deleted.isEmpty()) {
                    flushNoDrop();

                    for (String perm : deleted.keySet()) {
                        int count = deleted.get(perm);
                        ChatHelper.send(player, "notifyDeletedNoPermission", perm, count);
                        PreciousStones.log("logDeletedNoPermission", count, playerName);
                    }
                }
            }
        }
    }

    /**
     * Return all of a player's fields, by type
     *
     * @param playerName
     * @param flag
     * @return
     */
    public List<Field> getPlayerFields(String playerName, FieldFlag flag) {
        Map<FieldFlag, List<Field>> fields = fieldsByOwnerAndFlag.get(playerName.toLowerCase());

        if (fields == null) {
            return null;
        }

        return fields.get(flag);
    }

    /**
     * Changes username of all fields to a new one
     *
     * @param oldName
     * @param newName
     */
    public void migrateUsername(String oldName, String newName) {
        String oldNameLowercase = oldName.toLowerCase();
        String newNameLowercase = newName.toLowerCase();

        List<Field> fields = fieldsByOwner.get(oldNameLowercase);

        if (fields != null) {
            for (Field field : fields) {
                field.setOwner(newName);
                PreciousStones.getInstance().getStorageManager().offerField(field);
            }
            fieldsByOwner.remove(oldNameLowercase);
            fieldsByOwner.put(newNameLowercase, fields);
        }

        Map<BlockTypeEntry, List<Field>> typeMap = fieldsByOwnerAndType.get(oldNameLowercase);
        if (typeMap != null) {
            fieldsByOwnerAndType.put(newNameLowercase, typeMap);
            fieldsByOwnerAndType.remove(oldNameLowercase);
        }

        Map<FieldFlag, List<Field>> flagMap = fieldsByOwnerAndFlag.get(oldNameLowercase);
        if (flagMap != null) {
            fieldsByOwnerAndFlag.put(newNameLowercase, flagMap);
            fieldsByOwnerAndFlag.remove(oldNameLowercase);
        }

        Map<BlockTypeEntry, List<Field>> rentalFields = fieldsByRenterAndType.get(oldNameLowercase);
        if (rentalFields != null) {
            for (List<Field> fieldList : rentalFields.values()) {
                for (Field field : fieldList) {
                    field.getRentingModule().migrateRenters(oldName, newName);
                    PreciousStones.getInstance().getStorageManager().offerField(field);
                }
            }
            fieldsByRenterAndType.remove(oldNameLowercase);
            fieldsByRenterAndType.put(newNameLowercase, rentalFields);
        }

        List<Field> allowedList = fieldsByAllowed.get(oldNameLowercase);
        if (allowedList != null) {
            for (Field field : allowedList) {
                if (field.migrateAllowed(oldName, newName)) {
                    PreciousStones.getInstance().getStorageManager().offerField(field);
                }
            }
        }
        fieldsByAllowed.remove(oldNameLowercase);
        fieldsByAllowed.put(newNameLowercase, allowedList);
    }

    public Map<String, List<Field>> getFieldsByOwner() {
        return fieldsByOwner;
    }

    public void giveField(Player player, FieldSettings settings, int count) {
        // build item

        ItemStack is = new ItemStack(settings.getTypeId(), count, (short) 0, settings.getData());

        // apply meta name and lore

        if (settings.hasMetaName()) {
            StackHelper.setItemMeta(is, settings);
        }

        player.getInventory().addItem(is);
    }

    public void placeField(CommandSender sender, String ownerName, FieldSettings fs, int x, int y, int z, String worldName, int radius, int height) {
        World world = plugin.getServer().getWorld(worldName);

        if (world == null) {
            ChatHelper.send(sender, "worldNotFound");
            return;
        }

        Block fieldBlock = world.getBlockAt(x, y, z);
        BlockTypeEntry type = fs.getTypeEntry();

        // deny if world is blacklisted

        if (plugin.getSettingsManager().isBlacklistedWorld(fieldBlock.getWorld())) {
            return;
        }

        // check if a field exists there

        if (getField(fieldBlock) != null) {
            ChatHelper.send(sender, "fieldExists");
            return;
        }

        // verify owner name

        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(ownerName);

        if (fs.hasDefaultFlag(FieldFlag.NO_OWNER)) {
            ownerName = "Server";
        } else {
            if (owner == null) {
                ChatHelper.send(sender, "playerNotFound", ownerName);
                return;
            }
        }

        // set block

        fieldBlock.setType(type.getMaterial());
        fieldBlock.setData(type.getData());

        // create field

        Field field = new Field(fieldBlock, radius, height, ownerName);
        field.setSettings(fs);

        // add to database (skip foresters and activate them)

        if (field.hasFlag(FieldFlag.FORESTER)) {
            if (!field.hasFlag(FieldFlag.PLACE_DISABLED)) {
                ForesterEntry fe = new ForesterEntry(field, sender);
            }
        } else {

            // insert the field into database
            plugin.getStorageManager().insertField(field);
        }

        // add to collection

        addToCollection(field);

        // disable flags

        for (FieldFlag flag : field.getSettings().getDisabledFlags()) {
            field.getFlagsModule().disableFlag(flag.toString(), true);
        }

        // places the field in a disabled state

        if (field.hasFlag(FieldFlag.PLACE_DISABLED)) {
            field.setDisabled(true);
        }

        // sets the initial revert seconds for grief reverts

        if (field.hasFlag(FieldFlag.GRIEF_REVERT)) {
            if (field.getSettings().getGriefRevertInterval() > 0) {
                field.getRevertingModule().setRevertSecs(field.getSettings().getGriefRevertInterval());
                plugin.getGriefUndoManager().register(field);
            }
        }

        // start renter scheduler

        if (field.hasFlag(FieldFlag.RENTABLE) || field.hasFlag(FieldFlag.SHAREABLE)) {
            field.getRentingModule().scheduleNextRentUpdate();
        }

        // add allowed clan

        if (plugin.getSettingsManager().isAutoAddClan()) {
            String clan = plugin.getSimpleClansManager().getClan(owner.getName());

            if (clan != null) {
                field.addAllowed("c:" + clan);
            }
        }

        // add allowed team

        if (plugin.getSettingsManager().isAutoAddTeam()) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(owner.getName());

            if (offlinePlayer != null) {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard board = manager.getMainScoreboard();

                Team team = board.getPlayerTeam(offlinePlayer);

                if (team != null) {
                    field.addAllowed("t:" + team.getName());
                }
            }
        }

        // generate fence

        if (field.getSettings().getFenceItem() > 0) {
            field.getFencingModule().generateFence(field.getSettings().getFenceItem());
        }

        // add metadata

        field.getBlock().setMetadata("Pstone", new FixedMetadataValue(plugin, true));

        // allow all owners of intersecting fields into the field

        addAllowOverlappingOwners(field);

        // start disabling process for auto-disable fields

        field.startDisabler();

        // saves the field on the database

        plugin.getStorageManager().offerField(field);
    }
}
