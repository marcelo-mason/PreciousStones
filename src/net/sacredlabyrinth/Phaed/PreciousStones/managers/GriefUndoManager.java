package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Rollback;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.GriefBlock;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.HashMap;
import java.util.Queue;

/**
 * @author phaed
 */
public final class GriefUndoManager
{
    private PreciousStones plugin;
    private HashMap<Field, Integer> intervalFields = new HashMap<Field, Integer>();

    /**
     *
     */
    public GriefUndoManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Register an interval field
     *
     * @param field
     */
    public void register(Field field)
    {
        if (field.getRevertSecs() == 0)
        {
            return;
        }

        if (intervalFields.containsKey(field))
        {
            int taskId = intervalFields.get(field);
            plugin.getServer().getScheduler().cancelTask(taskId);
        }

        int taskId = startInterval(field);
        intervalFields.put(field, taskId);
    }

    /**
     * Un-register an interval field
     *
     * @param field
     */
    public void remove(Field field)
    {
        intervalFields.remove(field);
    }

    /**
     * Add grief block to field, accounts for dependents and signs
     *
     * @param field
     * @param block
     */
    public void addBlock(Field field, Block block)
    {
        if (!plugin.getGriefUndoManager().isDependentBlock(block.getTypeId()))
        {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

            for (BlockFace face : faces)
            {
                Block rel = block.getRelative(face);

                if (plugin.getGriefUndoManager().isDependentBlock(rel.getTypeId()))
                {
                    field.addGriefBlock(new GriefBlock(rel.getLocation(), rel.getTypeId(), rel.getData()));
                    rel.setTypeId(0);
                }
            }
        }

        if (block.getType().equals(Material.WOODEN_DOOR) || block.getType().equals(Material.IRON_DOOR))
        {
            // record wood doors in correct order

            if ((block.getData() & 0x8) == 0x8)
            {
                Block bottom = block.getRelative(BlockFace.DOWN);

                field.addGriefBlock(new GriefBlock(bottom));
                field.addGriefBlock(new GriefBlock(block));

                bottom.setTypeId(0);
                block.setTypeId(0);
            }
            else
            {
                Block top = block.getRelative(BlockFace.UP);

                field.addGriefBlock(new GriefBlock(block));
                field.addGriefBlock(new GriefBlock(top));

                block.setTypeId(0);
                top.setTypeId(0);
            }
        }
        else
        {
            GriefBlock gb = new GriefBlock(block);

            if (block.getState() instanceof Sign)
            {
                String signText = "";
                Sign sign = (Sign) block.getState();

                for (String line : sign.getLines())
                {
                    signText += line + "`";
                }

                signText = Helper.stripTrailing(signText, "`");
                gb.setSignText(signText);
            }

            field.addGriefBlock(gb);
        }
    }

    /**
     * Whether the block depends on an adjacent block to be placed
     *
     * @param type
     * @return
     */
    public boolean isDependentBlock(int type)
    {
        if (type == 26 || type == 27 || type == 28 || type == 31 || type == 32 || type == 37 || type == 38 || type == 39 || type == 40 || type == 50 || type == 55 || type == 63 || type == 64 || type == 65 || type == 66 || type == 68 || type == 69 || type == 70 || type == 71 || type == 72 || type == 75 || type == 76 || type == 77 || type == 85 || type == 96)
        {
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
    public int undoGrief(Field field)
    {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null)
        {
            Queue<GriefBlock> gbs = plugin.getStorageManager().retrieveBlockGrief(field);

            if (!gbs.isEmpty())
            {
                plugin.getCommunicationManager().notifyRollBack(field, gbs.size());
                Rollback rollback = new Rollback(gbs, world);
            }
            return gbs.size();
        }

        return 0;
    }

    /**
     * Undo the grief that has not yet been saved to the database from one field
     *
     * @param field
     * @return
     */
    public int undoDirtyGrief(Field field)
    {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null)
        {
            Queue<GriefBlock> gbs = field.getGrief();

            if (!gbs.isEmpty())
            {
                plugin.getCommunicationManager().notifyRollBack(field, gbs.size());
                Rollback rollback = new Rollback(gbs, world);
            }
            return gbs.size();
        }
        field.clearGrief();

        return 0;
    }

    /**
     * @param gb
     * @param world
     */
    public void undoGriefBlock(GriefBlock gb, World world)
    {
        if (gb == null)
        {
            return;
        }

        Block block = world.getBlockAt(gb.getX(), gb.getY(), gb.getZ());

        if (block == null)
        {
            return;
        }

        boolean noConflict = false;

        int[] seeThrough = {0, 6, 8, 31, 32, 37, 38, 39, 40, 9, 10, 11, 12, 51, 59, 83, 81};

        for (int st : seeThrough)
        {
            if (block.getTypeId() == st)
            {
                noConflict = true;

                if (st == 12)
                {
                    for (int count = 1; count < 128; count++)
                    {
                        int type = world.getBlockTypeIdAt(gb.getX(), gb.getY() + count, gb.getZ());

                        if (type == 0 || type == 8 || type == 9 || type == 10 || type == 11)
                        {
                            Block toSand = world.getBlockAt(gb.getX(), gb.getY() + count, gb.getZ());
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
            block.setTypeIdAndData(gb.getTypeId(), gb.getData(), true);

            if (block.getState() instanceof Sign && gb.getSignText().length() > 0)
            {
                Sign sign = (Sign) block.getState();
                String[] lines = gb.getSignText().split("[`]");

                for (int i = 0; i < lines.length; i++)
                {
                    sign.setLine(i, lines[i]);
                }
            }
        }
    }

    private int startInterval(final Field field)
    {
        return plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            public void run()
            {
                undoGrief(field);
            }
        }, field.getRevertSecs() * 20L, field.getRevertSecs() * 20L);
    }
}
