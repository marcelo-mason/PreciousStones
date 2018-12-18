package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.blocks.TranslocationBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.translocation.TranslocationApplier;
import net.sacredlabyrinth.Phaed.PreciousStones.translocation.TranslocationImporter;
import net.sacredlabyrinth.Phaed.PreciousStones.translocation.TranslocationRemover;
import net.sacredlabyrinth.Phaed.PreciousStones.translocation.TranslocationUpdater;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

/**
 * @author phaed
 */
public final class TranslocationManager {
    private PreciousStones plugin;

    /**
     *
     */
    public TranslocationManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Add grief block to field, accounts for dependents and signs
     *
     * @param field
     * @param block
     */
    public void addBlock(final Field field, final Block block) {
        addBlock(field, block, false);
    }

    /**
     * Add grief block to field, accounts for dependents and signs
     *
     * @param field
     * @param block
     */
    public void addBlock(final Field field, final Block block, final boolean isImport) {
        // if its not a dependent block, then look around it for dependents and add those first

        if (!plugin.getSettingsManager().isDependentBlock(block.getType())) {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

            for (BlockFace face : faces) {
                Block rel = block.getRelative(face);

                if (plugin.getSettingsManager().isDependentBlock(rel.getType())) {
                    addBlock(field, rel, isImport);
                }
            }
        }

        // record wood doors in correct order

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (Helper.isDoor(block)) {
                Block bottom = block.getRelative(BlockFace.DOWN);
                Block top = block.getRelative(BlockFace.UP);

                if (Helper.isDoor(bottom)) {
                    plugin.getStorageManager().insertTranslocationBlock(field, new TranslocationBlock(field, bottom));
                }

                if (Helper.isDoor(top)) {
                    plugin.getStorageManager().insertTranslocationBlock(field, new TranslocationBlock(field, top));
                }

                if (isImport) {
                    if (Helper.isDoor(block)) {
                        block.setType(Material.AIR, true);
                    }

                    if (Helper.isDoor(bottom)) {
                        bottom.setType(Material.AIR, true);
                    }

                    if (Helper.isDoor(top)) {
                        top.setType(Material.AIR, true);
                    }
                }

                return;
            }
        }, 5);

        // record translocation

        TranslocationBlock tb = new TranslocationBlock(field, block);

        if (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN)) {
            tb.setSignText(getSignText(block));
        }

        if (block.getState() instanceof InventoryHolder) {
            InventoryHolder holder = (InventoryHolder) block.getState();
            Inventory inv = holder.getInventory();
            tb.setContents(inv.getContents());
        }

        boolean isApplied = !isImport;

        plugin.getStorageManager().insertTranslocationBlock(field, tb, isApplied);

        if (!isImport) {
            int count = plugin.getStorageManager().totalTranslocationCount(field.getName(), field.getOwner());
            field.getTranslocatingModule().setTranslocationSize(count);
        } else {
            if (!Helper.isDoor(block)) {
                block.setType(Material.AIR, true);
            }
        }
    }

    /**
     * Removes a block from the traslocation
     *
     * @param field
     * @param block
     */
    public void removeBlock(Field field, Block block) {
        // sets the relative coords of the new tblock
        // so it can match the one on the db

        TranslocationBlock tb = new TranslocationBlock(block);
        tb.setRelativeCoords(field);

        plugin.getStorageManager().deleteTranslocation(field, tb);
        int count = plugin.getStorageManager().totalTranslocationCount(field.getName(), field.getOwner());
        field.getTranslocatingModule().setTranslocationSize(count);
    }

    private String getSignText(Block block) {
        String signText = "";
        Sign sign = (Sign) block.getState();

        for (String line : sign.getLines()) {
            signText += line + "`";
        }

        signText = Helper.stripTrailing(signText, "`");

        return signText;
    }

    /**
     * Undo the grief recorded in one field
     *
     * @param field
     * @return
     */
    public int applyTranslocation(Field field) {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null) {
            Queue<TranslocationBlock> tbs = plugin.getStorageManager().retrieveTranslocation(field);

            if (!tbs.isEmpty()) {
                TranslocationApplier rollback = new TranslocationApplier(field, tbs, world);
            }
            return tbs.size();
        }

        return 0;
    }

    /**
     * Reverts a single translocation block
     *
     * @param tb
     * @param world
     */
    public boolean applyTranslocationBlock(TranslocationBlock tb, World world) {
        Block block = world.getBlockAt(tb.getX(), tb.getY(), tb.getZ());

        // only apply on air or water

        if (!block.getType().equals(Material.AIR) &&
                !block.getType().equals(Material.WATER) &&
                !block.getType().equals(Material.LAVA)) {
            return false;
        }

        // rollback empty blocks straight up

        if (tb.isEmpty()) {
            block.setType(tb.getType(), true);
            return true;
        }

        boolean noConflict = false;

        // handle sand

        Material[] seeThrough = {Material.AIR, Material.OAK_SAPLING, Material.WATER, Material.DEAD_BUSH, Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.WATER, Material.LAVA, Material.LAVA, Material.SAND, Material.FIRE, Material.WHEAT, Material.SUGAR_CANE, Material.CACTUS};

        for (Material st : seeThrough) {
            if (block.getType() == st) {
                noConflict = true;

                if (st == Material.SAND) {
                    for (int count = 1; count < 256; count++) {
                        Material type = world.getBlockAt(tb.getX(), tb.getY() + count, tb.getZ()).getType();

                        if (type == Material.AIR || type == Material.WATER || type == Material.LAVA) {
                            Block toSand = world.getBlockAt(tb.getX(), tb.getY() + count, tb.getZ());
                            toSand.setType(Material.SAND, false);
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (noConflict) {
            block.setType(tb.getType(), true);

            if (block.getState() instanceof Sign && !tb.getSignText().isEmpty()) {
                Sign sign = (Sign) block.getState();
                String[] lines = tb.getSignText().split("[`]");

                for (int i = 0; i < lines.length; i++) {
                    sign.setLine(i, lines[i]);
                    sign.update();
                }
            }

            if (tb.hasItemStacks()) {
                InventoryHolder holder = (InventoryHolder) block.getState();
                Inventory inv = holder.getInventory();
                inv.setContents(tb.getItemStacks());
            }
        }

        return true;
    }

    /**
     * Clears all saved translocated blocks form the world
     *
     * @param field
     * @return
     */
    public int clearTranslocation(Field field) {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null) {
            Queue<TranslocationBlock> tbs = plugin.getStorageManager().retrieveClearTranslocation(field);

            if (!tbs.isEmpty()) {
                TranslocationUpdater rollback = new TranslocationUpdater(field, tbs, world);
            }
            return tbs.size();
        }

        return 0;
    }

    /**
     * Wipes a single translocation block from the world
     *
     * @param field
     * @param tb
     */
    public boolean wipeTranslocationBlock(Field field, TranslocationBlock tb) {
        Block block = tb.getBlock();

        tb = processBlock(tb);

        if (tb != null) {
            // wipe the block
            addBlock(field, block, true);
            block.setType(Material.AIR, true);
            return true;
        }

        return false;
    }

    /**
     * Clears a single translocation block from the world
     *
     * @param field
     * @param tb
     * @param clear
     */
    public boolean updateTranslationBlock(Field field, TranslocationBlock tb, boolean clear) {
        Block block = tb.getBlock();

        tb = processBlock(tb);

        if (tb != null) {
            // save the contents if it has some

            if (tb.hasItemStacks()) {
                plugin.getStorageManager().updateTranslocationBlockContents(field, tb);
            }

            // update the sign text

            if (!tb.getSignText().isEmpty()) {
                plugin.getStorageManager().updateTranslocationSignText(field, tb);
            }

            if (clear) {
                block.setType(Material.AIR, true);
            }
            return true;
        }

        return false;
    }

    private TranslocationBlock processBlock(TranslocationBlock tb) {
        // if the block changed from the time it was recorded in the database
        // then cancel its clearing

        Block block = tb.getBlock();

        Material id1 = block.getType();
        Material id2 = tb.getType();

        boolean equal = id1 == id2;

        if (id1 == Material.DIRT && id2 == Material.GRASS_BLOCK || id1 == Material.GRASS_BLOCK && id2 == Material.DIRT) {
            equal = true;
        }

        if (!equal) {
            PreciousStones.debug("translocation block rejected, it's id changed since it was recorded: " + block.getType() + " " + tb.getType());
            return null;
        }

        if (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN)) {
            tb.setSignText(getSignText(block));
        }

        // extract the block's contents

        if (block.getState() instanceof InventoryHolder) {
            InventoryHolder holder = (InventoryHolder) block.getState();
            Inventory inv = holder.getInventory();
            tb.setContents(inv.getContents());
            inv.clear();
        }

        return tb;
    }

    /**
     * Clears a single translocation block from the world
     *
     * @param tb
     */
    public void zeroOutBlock(TranslocationBlock tb) {
        final Block block = tb.getBlock().getWorld().getBlockAt(tb.getX(), tb.getY(), tb.getZ());
        block.setType(Material.AIR, true);
    }

    /**
     * Changes the type of the block to glass (client-side) for a second
     *
     * @param field
     * @param player
     */
    @SuppressWarnings("deprecation")
    public void flashFieldBlock(final Field field, final Player player) {
        final Set<Player> inhabitants = plugin.getForceFieldManager().getFieldInhabitants(field);
        inhabitants.add(player);

        for (Player p : inhabitants) {
            p.sendBlockChange(field.getLocation(), Material.GLASS, (byte) 0);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player p : inhabitants) {
                p.sendBlockChange(field.getLocation(), field.getMaterial(), (byte)0);
            }
        }, 20);
    }

    /**
     * Imports the contets of the field into the translocation
     *
     * @param field
     * @param player
     * @param entries
     * @return
     */
    public void importBlocks(Field field, Player player, List<BlockTypeEntry> entries) {
        ProcessResult result = processBlocks(field, player, entries);

        Queue<TranslocationBlock> tbs = result.tbs;
        int notImported = result.notImported;

        int imported = tbs.size();

        if (!tbs.isEmpty()) {
            TranslocationImporter importer = new TranslocationImporter(field, tbs, player);

            ChatHelper.send(player, "translocationImportingBlocks", imported);

            if (notImported > 0) {
                ChatHelper.send(player, "translocationSkippedDueToLimit", notImported);
            }
        } else {
            ChatHelper.send(player, "nothingToImport");
        }

        field.setDisabled(true);
        field.getFlagsModule().dirtyFlags("importBlocks");
    }

    /**
     * Imports the contets of the field into the translocation
     *
     * @param field
     * @param player
     * @param entries
     * @return
     */
    public void removeBlocks(Field field, Player player, List<BlockTypeEntry> entries) {
        ProcessResult result = processBlocks(field, player, entries);
        Queue<TranslocationBlock> tbs = result.tbs;
        int notImported = result.notImported;

        int imported = tbs.size();

        if (!tbs.isEmpty()) {
            TranslocationRemover remover = new TranslocationRemover(field, tbs, player);

            ChatHelper.send(player, "translocationRemovingBlocks", imported);

            if (notImported > 0) {
                ChatHelper.send(player, "translocationSkippedDueToLimit", notImported);
            }
        } else {
            ChatHelper.send(player, "nothingToRemove");
        }

        field.setDisabled(true);
        field.getFlagsModule().dirtyFlags("removeBlocks");
    }

    private final class ProcessResult {
        public Queue<TranslocationBlock> tbs;
        public int notImported;

        public ProcessResult(Queue<TranslocationBlock> tbs, int notImported) {
            this.tbs = tbs;
            this.notImported = notImported;
        }
    }

    private ProcessResult processBlocks(Field field, Player player, List<BlockTypeEntry> entries) {
        int minx = field.getMinx();
        int maxx = field.getMaxx();
        int minz = field.getMinz();
        int maxz = field.getMaxz();
        int miny = field.getMiny();
        int maxy = field.getMaxy();

        int count = plugin.getStorageManager().totalTranslocationCount(field.getName(), field.getOwner());
        int maxCount = plugin.getSettingsManager().getMaxSizeTranslocation();
        int notImported = 0;

        Queue<TranslocationBlock> tbs = new LinkedList<>();

        Map<Material, BlockTypeEntry> map = new HashMap<>();

        if (entries != null) {
            for (BlockTypeEntry e : entries) {
                map.put(e.getMaterial(), e);
            }
        }

        for (int x = minx; x <= maxx; x++) {
            for (int z = minz; z <= maxz; z++) {
                for (int y = miny; y <= maxy; y++) {
                    if (field.getX() == x && field.getY() == y && field.getZ() == z) {
                        continue;
                    }

                    if (count <= maxCount) {
                        Material id = player.getWorld().getBlockAt(x, y, z).getType();

                        if (entries != null) {
                            if (map.containsKey(id)) {
                                Block block = player.getWorld().getBlockAt(x, y, z);

                                BlockTypeEntry entry = map.get(id);
                                BlockTypeEntry actual = new BlockTypeEntry(block);

                                if (entry.equals(actual)) {
                                    if (field.getSettings().canTranslocate(actual)) {
                                        tbs.add(new TranslocationBlock(field, block));
                                        count++;
                                    }
                                }
                            }
                        } else {
                            if (id != Material.AIR) {
                                Block block = player.getWorld().getBlockAt(x, y, z);

                                if (field.getSettings().canTranslocate(new BlockTypeEntry(block))) {
                                    tbs.add(new TranslocationBlock(field, block));
                                    count++;
                                }
                            }
                        }
                    } else {
                        notImported++;
                    }
                }
            }
        }

        return new ProcessResult(tbs, notImported);
    }
}
