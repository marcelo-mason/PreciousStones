package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.TranslocationClearRollback;
import net.sacredlabyrinth.Phaed.PreciousStones.TranslocationRollback;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.TranslocationBlock;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.Queue;

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
     * @param state
     */
    public void addBlock(Field field, Block block)
    {
        TranslocationBlock tb = new TranslocationBlock(block);
        field.addTranslocationBlock(tb);
    }

    /**
     * Add grief block to field, accounts for dependents and signs
     *
     * @param field
     * @param block
     */
    public void addBlock(Field field, Block block, boolean clear)
    {
        // if its not a dependent block, then look around it for dependents and add those first

        if (!isDependentBlock(block.getTypeId()))
        {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

            for (BlockFace face : faces)
            {
                Block rel = block.getRelative(face);

                if (plugin.getGriefUndoManager().isDependentBlock(rel.getTypeId()))
                {
                    addBlock(field, rel, clear);
                }
            }
        }

        // record wood doors in correct order

        if (block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.IRON_DOOR))
        {
            field.addTranslocationBlock(new TranslocationBlock(block));

            Block bottom = block.getRelative(BlockFace.DOWN);
            Block top = block.getRelative(BlockFace.UP);

            if (bottom.getType().equals(Material.WOODEN_DOOR) || bottom.getType().equals(Material.IRON_DOOR))
            {
                field.addTranslocationBlock(new TranslocationBlock(bottom));
                if (clear)
                {
                    bottom.setTypeId(0);
                    block.setTypeId(0);
                }
            }

            if (top.getType().equals(Material.WOODEN_DOOR) || top.getType().equals(Material.IRON_DOOR))
            {
                field.addTranslocationBlock(new TranslocationBlock(top));
                if (clear)
                {
                    top.setTypeId(0);
                    block.setTypeId(0);
                }
            }

            return;
        }

        // record grief

        if (block.getState() instanceof Sign)
        {
            field.addTranslocationBlock(handleSign(block));
        }
        else
        {
            field.addTranslocationBlock(new TranslocationBlock(block));
        }
        if (clear)
        {
            block.setTypeId(0);
        }
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
     * Whether the block depends on an adjacent block to be placed
     *
     * @param type
     * @return
     */
    public boolean isDependentBlock(int type)
    {
        if (type == 26 || type == 27 || type == 28 || type == 30 || type == 31 || type == 32 || type == 37 || type == 38 || type == 39 || type == 40 || type == 50 || type == 55 || type == 63 || type == 64 || type == 65 || type == 66 || type == 68 || type == 69 || type == 70 || type == 71 || type == 72 || type == 75 || type == 76 || type == 77 || type == 78 || type == 85 || type == 96 || type == 99 || type == 100 || type == 101 || type == 102 || type == 104 || type == 105 || type == 106 || type == 107 || type == 111 || type == 113 || type == 115 || type == 119 || type == 127 || type == 131 || type == 132)        {
            return true;
        }

        return false;
    }

    /**
     * Undo the grief recorded in one field
     *
     * @param field
     * @return
     */
    public int revertTranslocation(Field field)
    {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null)
        {
            Queue<TranslocationBlock> tbs = plugin.getStorageManager().retrieveTranslocation(field);

            if (!tbs.isEmpty())
            {
                TranslocationRollback rollback = new TranslocationRollback(tbs, world);
            }
            return tbs.size();
        }

        return 0;
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
                TranslocationClearRollback rollback = new TranslocationClearRollback(tbs, world);
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
    public void undoTranslocationBlock(TranslocationBlock tb, World world)
    {
        if (tb == null)
        {
            return;
        }

        Block block = world.getBlockAt(tb.getX(), tb.getY(), tb.getZ());

        if (block == null)
        {
            return;
        }

        // rollback empty blocks straight up

        if (tb.isEmpty())
        {
            block.setTypeIdAndData(tb.getTypeId(), tb.getData(), false);
            return;
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
            block.setTypeIdAndData(tb.getTypeId(), tb.getData(), false);

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
    }

    /**
     * Clears a single translocation block
     *
     * @param tb
     * @param world
     */
    public void clearTranslocationBlock(TranslocationBlock tb, World world)
    {
        if (tb == null)
        {
            return;
        }

        Block block = world.getBlockAt(tb.getX(), tb.getY(), tb.getZ());

        if (block == null)
        {
            return;
        }

        block.setTypeIdAndData(0, (byte)0, false);
    }
}
