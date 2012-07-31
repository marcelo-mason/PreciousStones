package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.TranslocationApplier;
import net.sacredlabyrinth.Phaed.PreciousStones.TranslocationClearer;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.TranslocationBlock;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.Set;

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
    public void addBlock(Field field, Block block)
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
                    addBlock(field, rel);
                }
            }
        }

        // record wood doors in correct order

        if (block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.IRON_DOOR))
        {
            plugin.getStorageManager().insertTranslocatorBlock(field, new TranslocationBlock(field, block));

            Block bottom = block.getRelative(BlockFace.DOWN);
            Block top = block.getRelative(BlockFace.UP);

            if (bottom.getType().equals(Material.WOODEN_DOOR) || bottom.getType().equals(Material.IRON_DOOR))
            {
                plugin.getStorageManager().insertTranslocatorBlock(field, new TranslocationBlock(field, bottom));
            }

            if (top.getType().equals(Material.WOODEN_DOOR) || top.getType().equals(Material.IRON_DOOR))
            {
                plugin.getStorageManager().insertTranslocatorBlock(field, new TranslocationBlock(field, top));
            }

            return;
        }

        // record translocation

        TranslocationBlock tb;

        if (block.getState() instanceof Sign)
        {
            tb = handleSign(block);
            tb.setRelativeCoords(field);
        }
        else
        {
            tb = new TranslocationBlock(field, block);
        }

        plugin.getStorageManager().insertTranslocatorBlock(field, tb);
    }

    /**
     * Removes a block from the traslocation
     *
     * @param field
     * @param tb
     */
    public void removeBlock(Field field, Block block)
    {
        // sets the relative coords of the new tblock
        // so it can match the one on the db

        TranslocationBlock tb = new TranslocationBlock(block);
        tb.setRelativeCoords(field);

        plugin.getStorageManager().deleteTranslocation(field, tb);
    }

    private TranslocationBlock handleSign(Block block)
    {
        TranslocationBlock tb = new TranslocationBlock(block);

        String signText = "";
        Sign sign = (Sign) block.getState();

        for (String line : sign.getLines())
        {
            signText += line + "`";
        }

        signText = Helper.stripTrailing(signText, "`");

        tb.setSignText(signText);

        return tb;
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
                field.setTranslocating(true);
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
            block.setTypeIdAndData(tb.getTypeId(), tb.getData(), true);
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
            block.setTypeIdAndData(tb.getTypeId(), tb.getData(), true);

            if (block.getState() instanceof Sign && tb.getSignText().length() > 0)
            {
                Sign sign = (Sign) block.getState();
                String[] lines = tb.getSignText().split("[`]");

                for (int i = 0; i < lines.length; i++)
                {
                    sign.setLine(i, lines[i]);
                }
            }
        }

        return true;
    }

    /**
     * Undo the grief recorded in one field
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
                field.setTranslocating(true);
                TranslocationClearer rollback = new TranslocationClearer(field, tbs, world);
            }
            return tbs.size();
        }

        return 0;
    }

    /**
     * Clears a single translocation block
     *
     * @param tb
     * @param world
     */
    public boolean clearTranslocationBlock(Field field, TranslocationBlock tb, World world)
    {
        Block block = world.getBlockAt(tb.getX(), tb.getY(), tb.getZ());

        // if the block changed from the time it was recorded in the database
        // then cancel its clearing

        int id1 = block.getTypeId();
        int id2 = tb.getTypeId();

        boolean equal = id1 == id2;

        if (id1 == 8 && id2 == 9 || id1 == 10 && id2 == 11 || id1 == 74 && id2 == 73 || id1 == 61 && id2 == 62 || id1 == 62 && id2 == 61)
        {
            equal = true;
        }

        if (!equal)
        {
            PreciousStones.debug("translocation block rejected, it's id changed since it was recorded: " + block.getTypeId() + " " + tb.getTypeId());
            return false;
        }

        // if the data no longer matches, update the database

        if (block.getData() != tb.getData())
        {
            tb.setData(block.getData());
            plugin.getStorageManager().updateTranslocationBlockData(field, tb);
        }

        block.setTypeIdAndData(0, (byte) 0, true);
        return true;
    }

    public void flashFieldBlock(final Field field, final Player player)
    {
        final Set<Player> inhabitants = plugin.getForceFieldManager().getFieldInhabitants(field);
        inhabitants.add(player);

        for (Player p : inhabitants)
        {
            p.sendBlockChange(field.getLocation(), 79, (byte) 0);
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                for (Player p : inhabitants)
                {
                    p.sendBlockChange(field.getLocation(), field.getTypeId(), field.getData());
                }
            }
        }, 20);
    }
}
