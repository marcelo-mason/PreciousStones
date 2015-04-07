package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.TranslocationBlock;
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
public final class TranslocationManager
{
    private PreciousStones plugin;

    /**
     *
     */
    public TranslocationManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Add grief block to field, accounts for dependents and signs
     *
     * @param field
     * @param block
     */
    public void addBlock(final Field field, final Block block)
    {
        addBlock(field, block, false);
    }

    /**
     * Add grief block to field, accounts for dependents and signs
     *
     * @param field
     * @param block
     */
    public void addBlock(final Field field, final Block block, final boolean isImport)
    {
        // if its not a dependent block, then look around it for dependents and add those first

        if (!plugin.getSettingsManager().isDependentBlock(block.getTypeId()))
        {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

            for (BlockFace face : faces)
            {
                Block rel = block.getRelative(face);

                if (plugin.getSettingsManager().isDependentBlock(rel.getTypeId()))
                {
                    addBlock(field, rel, isImport);
                }
            }
        }

        // record wood doors in correct order

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                if (Helper.isDoor(block))
                {
                    Block bottom = block.getRelative(BlockFace.DOWN);
                    Block top = block.getRelative(BlockFace.UP);

                    if (Helper.isDoor(bottom))
                    {
                        plugin.getStorageManager().insertTranslocationBlock(field, new TranslocationBlock(field, bottom));
                    }

                    if (Helper.isDoor(top))
                    {
                        plugin.getStorageManager().insertTranslocationBlock(field, new TranslocationBlock(field, top));
                    }

                    if (isImport)
                    {
                        if (Helper.isDoor(block))
                        {
                            block.setTypeIdAndData(0, (byte) 0, true);
                        }

                        if (Helper.isDoor(bottom))
                        {
                            bottom.setTypeIdAndData(0, (byte) 0, true);
                        }

                        if (Helper.isDoor(top))
                        {
                            top.setTypeIdAndData(0, (byte) 0, true);
                        }
                    }

                    return;
                }
            }
        }, 5);

        // record translocation

        TranslocationBlock tb = new TranslocationBlock(field, block);

        if (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN))
        {
            tb.setSignText(getSignText(block));
        }

        if (block.getState() instanceof InventoryHolder)
        {
            InventoryHolder holder = (InventoryHolder) block.getState();
            Inventory inv = holder.getInventory();
            tb.setContents(inv.getContents());
        }

        boolean isApplied = !isImport;

        plugin.getStorageManager().insertTranslocationBlock(field, tb, isApplied);

        if (!isImport)
        {
            int count = plugin.getStorageManager().totalTranslocationCount(field.getName(), field.getOwner());
            field.setTranslocationSize(count);
        }
        else
        {
            if (!Helper.isDoor(block))
            {
                block.setTypeIdAndData(0, (byte) 0, true);
            }
        }
    }

    /**
     * Removes a block from the traslocation
     *
     * @param field
     * @param block
     */
    public void removeBlock(Field field, Block block)
    {
        // sets the relative coords of the new tblock
        // so it can match the one on the db

        TranslocationBlock tb = new TranslocationBlock(block);
        tb.setRelativeCoords(field);

        plugin.getStorageManager().deleteTranslocation(field, tb);
        int count = plugin.getStorageManager().totalTranslocationCount(field.getName(), field.getOwner());
        field.setTranslocationSize(count);
    }

    private String getSignText(Block block)
    {
        String signText = "";
        Sign sign = (Sign) block.getState();

        for (String line : sign.getLines())
        {
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
    public int applyTranslocation(Field field)
    {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null)
        {
            Queue<TranslocationBlock> tbs = plugin.getStorageManager().retrieveTranslocation(field);

            if (!tbs.isEmpty())
            {
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
    public boolean applyTranslocationBlock(TranslocationBlock tb, World world)
    {
        Block block = world.getBlockAt(tb.getX(), tb.getY(), tb.getZ());

        // only apply on air or water

        if (!block.getType().equals(Material.AIR) &&
                !block.getType().equals(Material.STATIONARY_WATER) &&
                block.getType().equals(Material.WATER) &&
                block.getType().equals(Material.STATIONARY_LAVA) &&
                block.getType().equals(Material.LAVA))
        {
            return false;
        }

        // rollback empty blocks straight up

        if (tb.isEmpty())
        {
            block.setTypeIdAndData(tb.getTypeId(), (byte)tb.getData(), true);
            return true;
        }

        boolean noConflict = false;

        // handle sand

        int[] seeThrough = {0, 6, 8, 31, 32, 37, 38, 39, 40, 9, 10, 11, 12, 51, 59, 83, 81};

        for (int st : seeThrough)
        {
            if (block.getTypeId() == st)
            {
                noConflict = true;

                if (st == 12)
                {
                    for (int count = 1; count < 256; count++)
                    {
                        int type = world.getBlockTypeIdAt(tb.getX(), tb.getY() + count, tb.getZ());

                        if (type == 0 || type == 8 || type == 9 || type == 10 || type == 11)
                        {
                            Block toSand = world.getBlockAt(tb.getX(), tb.getY() + count, tb.getZ());
                            toSand.setTypeId(12, false);
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (noConflict)
        {
            block.setTypeIdAndData(tb.getTypeId(), (byte)tb.getData(), true);

            if (block.getState() instanceof Sign && !tb.getSignText().isEmpty())
            {
                Sign sign = (Sign) block.getState();
                String[] lines = tb.getSignText().split("[`]");

                for (int i = 0; i < lines.length; i++)
                {
                    sign.setLine(i, lines[i]);
                    sign.update();
                }
            }

            if (tb.hasItemStacks())
            {
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
    public int clearTranslocation(Field field)
    {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null)
        {
            Queue<TranslocationBlock> tbs = plugin.getStorageManager().retrieveClearTranslocation(field);

            if (!tbs.isEmpty())
            {
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
    public boolean wipeTranslocationBlock(Field field, TranslocationBlock tb)
    {
        Block block = tb.getBlock();

        tb = processBlock(tb);

        if (tb != null)
        {
            // wipe the block
            addBlock(field, block, true);
            block.setTypeIdAndData(0, (byte) 0, true);
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
    public boolean updateTranslationBlock(Field field, TranslocationBlock tb, boolean clear)
    {
        Block block = tb.getBlock();

        tb = processBlock(tb);

        if (tb != null)
        {
            // if the data no longer matches, update the database

            if (block.getData() != tb.getData())
            {
                tb.setData(block.getData());
                plugin.getStorageManager().updateTranslocationBlockData(field, tb);
            }

            // save the contents if it has some

            if (tb.hasItemStacks())
            {
                plugin.getStorageManager().updateTranslocationBlockContents(field, tb);
            }

            // update the sign text

            if (!tb.getSignText().isEmpty())
            {
                plugin.getStorageManager().updateTranslocationSignText(field, tb);
            }

            if (clear)
            {
                block.setTypeIdAndData(0, (byte) 0, true);
            }
            return true;
        }

        return false;
    }

    private TranslocationBlock processBlock(TranslocationBlock tb)
    {
        // if the block changed from the time it was recorded in the database
        // then cancel its clearing

        Block block = tb.getBlock();

        int id1 = block.getTypeId();
        int id2 = tb.getTypeId();

        boolean equal = id1 == id2;

        if (id1 == 3 && id2 == 2 || id1 == 2 && id2 == 3 || id1 == 9 && id2 == 8 || id1 == 8 && id2 == 9 || id1 == 11 && id2 == 10 || id1 == 10 && id2 == 11 || id1 == 73 && id2 == 74 || id1 == 74 && id2 == 73 || id1 == 61 && id2 == 62 || id1 == 62 && id2 == 61)
        {
            equal = true;
        }

        if (!equal)
        {
            PreciousStones.debug("translocation block rejected, it's id changed since it was recorded: " + block.getTypeId() + " " + tb.getTypeId());
            return null;
        }

        if (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN))
        {
            tb.setSignText(getSignText(block));
        }

        // extract the block's contents

        if (block.getState() instanceof InventoryHolder)
        {
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
    public void zeroOutBlock(TranslocationBlock tb)
    {
        final Block block = tb.getBlock().getWorld().getBlockAt(tb.getX(), tb.getY(), tb.getZ());
        block.setTypeIdAndData(0, (byte) 0, true);
    }

    /**
     * Changes the type of the block to glass (client-side) for a second
     *
     * @param field
     * @param player
     */
    public void flashFieldBlock(final Field field, final Player player)
    {
        final Set<Player> inhabitants = plugin.getForceFieldManager().getFieldInhabitants(field);
        inhabitants.add(player);

        for (Player p : inhabitants)
        {
            p.sendBlockChange(field.getLocation(), Material.GLASS, (byte) 0);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                for (Player p : inhabitants)
                {
                    p.sendBlockChange(field.getLocation(), field.getTypeId(), (byte)field.getData());
                }
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
    public void importBlocks(Field field, Player player, List<BlockTypeEntry> entries)
    {
        ProcessResult result = processBlocks(field, player, entries);

        Queue<TranslocationBlock> tbs = result.tbs;
        int notImported = result.notImported;

        int imported = tbs.size();

        if (!tbs.isEmpty())
        {
            TranslocationImporter importer = new TranslocationImporter(field, tbs, player);

            ChatBlock.send(player, "translocationImportingBlocks", imported);

            if (notImported > 0)
            {
                ChatBlock.send(player, "translocationSkippedDueToLimit", notImported);
            }
        }
        else
        {
            ChatBlock.send(player, "nothingToImport");
        }

        field.setDisabled(true);
        field.dirtyFlags("importBlocks");
    }

    /**
     * Imports the contets of the field into the translocation
     *
     * @param field
     * @param player
     * @param entries
     * @return
     */
    public void removeBlocks(Field field, Player player, List<BlockTypeEntry> entries)
    {
        ProcessResult result = processBlocks(field, player, entries);
        Queue<TranslocationBlock> tbs = result.tbs;
        int notImported = result.notImported;

        int imported = tbs.size();

        if (!tbs.isEmpty())
        {
            TranslocationRemover remover = new TranslocationRemover(field, tbs, player);

            ChatBlock.send(player, "translocationRemovingBlocks", imported);

            if (notImported > 0)
            {
                ChatBlock.send(player, "translocationSkippedDueToLimit", notImported);
            }
        }
        else
        {
            ChatBlock.send(player, "nothingToRemove");
        }

        field.setDisabled(true);
        field.dirtyFlags("removeBlocks");
    }

    private final class ProcessResult
    {
        public Queue<TranslocationBlock> tbs;
        public int notImported;

        public ProcessResult(Queue<TranslocationBlock> tbs, int notImported)
        {
            this.tbs = tbs;
            this.notImported = notImported;
        }
    }

    private ProcessResult processBlocks(Field field, Player player, List<BlockTypeEntry> entries)
    {
        int minx = field.getMinx();
        int maxx = field.getMaxx();
        int minz = field.getMinz();
        int maxz = field.getMaxz();
        int miny = field.getMiny();
        int maxy = field.getMaxy();

        int count = plugin.getStorageManager().totalTranslocationCount(field.getName(), field.getOwner());
        int maxCount = plugin.getSettingsManager().getMaxSizeTranslocation();
        int notImported = 0;

        Queue<TranslocationBlock> tbs = new LinkedList<TranslocationBlock>();

        Map<Integer, BlockTypeEntry> map = new HashMap<Integer, BlockTypeEntry>();

        if (entries != null)
        {
            for (BlockTypeEntry e : entries)
            {
                map.put(e.getTypeId(), e);
            }
        }

        for (int x = minx; x <= maxx; x++)
        {
            for (int z = minz; z <= maxz; z++)
            {
                for (int y = miny; y <= maxy; y++)
                {
                    if (field.getX() == x && field.getY() == y && field.getZ() == z)
                    {
                        continue;
                    }

                    if (count <= maxCount)
                    {
                        int id = player.getWorld().getBlockTypeIdAt(x, y, z);

                        if (entries != null)
                        {
                            if (map.containsKey(id))
                            {
                                Block block = player.getWorld().getBlockAt(x, y, z);

                                BlockTypeEntry entry = map.get(id);
                                BlockTypeEntry actual = new BlockTypeEntry(block);

                                if (entry.equals(actual))
                                {
                                    if (field.getSettings().canTranslocate(actual))
                                    {
                                        tbs.add(new TranslocationBlock(field, block));
                                        count++;
                                    }
                                }
                            }
                        }
                        else
                        {
                            if (id != 0)
                            {
                                Block block = player.getWorld().getBlockAt(x, y, z);

                                if (field.getSettings().canTranslocate(new BlockTypeEntry(block)))
                                {
                                    tbs.add(new TranslocationBlock(field, block));
                                    count++;
                                }
                            }
                        }
                    }
                    else
                    {
                        notImported++;
                    }
                }
            }
        }

        return new ProcessResult(tbs, notImported);
    }
}
