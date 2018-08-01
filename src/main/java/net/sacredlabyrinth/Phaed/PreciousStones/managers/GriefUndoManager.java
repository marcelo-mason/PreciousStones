package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.Rollback;
import net.sacredlabyrinth.Phaed.PreciousStones.blocks.GriefBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.util.HashMap;
import java.util.Queue;

/**
 * @author phaed
 */
public final class GriefUndoManager {
    private PreciousStones plugin;
    private HashMap<Field, Integer> intervalFields = new HashMap<>();

    /**
     *
     */
    public GriefUndoManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Register an interval field
     *
     * @param field
     */
    public void register(Field field) {
        if (field.getRevertingModule().getRevertSecs() == 0) {
            return;
        }

        if (intervalFields.containsKey(field)) {
            int taskId = intervalFields.get(field);
            Bukkit.getScheduler().cancelTask(taskId);
        }

        int taskId = startInterval(field);
        intervalFields.put(field, taskId);
    }

    /**
     * Un-register an interval field
     *
     * @param field
     */
    public void remove(Field field) {
        intervalFields.remove(field);
    }

    /**
     * Add grief block to field, accounts for dependents and signs
     *
     * @param field
     * @param state
     */
    public void addBlock(Field field, BlockState state) {
        GriefBlock gb = new GriefBlock(state);
        field.getRevertingModule().addGriefBlock(gb);
    }

    /**
     * Add grief block to field, accounts for dependents and signs
     *
     * @param field
     * @param block
     */
    public void addBlock(Field field, Block block, boolean clear) {
        // if its not a dependent block, then look around it for dependents and add those first

        if (!plugin.getSettingsManager().isDependentBlock(block.getType())) {
            PreciousStones.debug("not depenedent");

            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

            for (BlockFace face : faces) {
                Block rel = block.getRelative(face);

                if (plugin.getSettingsManager().isDependentBlock(rel.getType())) {
                    addBlock(field, rel, clear);
                    PreciousStones.debug("+found dependent");
                }
            }
        }

        // record wood doors in correct order

        if (block.getType() == Material.OAK_DOOR || block.getType() == Material.IRON_DOOR || block.getType() == Material.IRON_DOOR) // doors
        {
            field.getRevertingModule().addGriefBlock(new GriefBlock(block));

            Block bottom = block.getRelative(BlockFace.DOWN);
            Block top = block.getRelative(BlockFace.UP);

            if (bottom.getType() == Material.OAK_DOOR || bottom.getType() == Material.IRON_DOOR || bottom.getType() == Material.IRON_DOOR) // doors
            {
                field.getRevertingModule().addGriefBlock(new GriefBlock(bottom));
                if (clear) {
                    bottom.setType(Material.AIR);
                    block.setType(Material.AIR);
                }
            }

            if (top.getType() == Material.OAK_DOOR || top.getType() == Material.IRON_DOOR || top.getType() == Material.IRON_DOOR) // doors
            {
                field.getRevertingModule().addGriefBlock(new GriefBlock(top));
                if (clear) {
                    top.setType(Material.AIR);
                    block.setType(Material.AIR);
                }
            }

            return;
        }

        // record grief

        if (block.getState() instanceof Sign) {
            field.getRevertingModule().addGriefBlock(handleSign(block));
        } else {
            PreciousStones.debug("added grief to field");
            field.getRevertingModule().addGriefBlock(new GriefBlock(block));
        }
        if (clear) {
            block.setType(Material.AIR);
        }
    }

    private GriefBlock handleSign(Block block) {
        GriefBlock gb = new GriefBlock(block);

        String signText = "";
        Sign sign = (Sign) block.getState();

        for (String line : sign.getLines()) {
            signText += line + "`";
        }

        signText = Helper.stripTrailing(signText, "`");

        gb.setSignText(signText);

        return gb;
    }

    /**
     * /**
     * Undo the grief recorded in one field
     *
     * @param field
     * @return
     */
    public int undoGrief(Field field) {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null) {
            PreciousStones.debug("Retrieving block grief");

            Queue<GriefBlock> gbs = plugin.getStorageManager().retrieveBlockGrief(field);

            if (!gbs.isEmpty()) {
                PreciousStones.debug("Rolling back %s griefed blocks", gbs.size());
                plugin.getCommunicationManager().notifyRollBack(field, gbs.size());
                Rollback rollback = new Rollback(gbs, world, field);
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
    public int undoDirtyGrief(Field field) {
        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null) {
            PreciousStones.debug("Retrieving dirty grief");

            Queue<GriefBlock> gbs = field.getRevertingModule().getGrief();

            if (!gbs.isEmpty()) {
                PreciousStones.debug("Rolling back %s dirty griefed blocks", gbs.size());
                plugin.getCommunicationManager().notifyRollBack(field, gbs.size());
                Rollback rollback = new Rollback(gbs, world, field);
            }
            return gbs.size();
        }
        return 0;
    }

    /**
     * @param gb
     * @param world
     */
    public void undoGriefBlock(GriefBlock gb, World world) {
        if (gb == null) {
            return;
        }

        Block block = world.getBlockAt(gb.getX(), gb.getY(), gb.getZ());

        if (block == null) {
            return;
        }

        // rollback empty blocks straight up

        if (gb.isEmpty()) {
            block.setType(gb.getType(), true);
            return;
        }

        boolean noConflict = false;


        // handle sand

        Material[] seeThrough = {Material.AIR, Material.OAK_SAPLING, Material.WATER, Material.DEAD_BUSH, Material.DEAD_BUSH, Material.DANDELION, Material.POPPY, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.WATER, Material.LAVA, Material.LAVA, Material.SAND, Material.FIRE, Material.WHEAT, Material.SUGAR_CANE, Material.CACTUS};

        for (Material st : seeThrough) {
            if (block.getType() == st) {
                noConflict = true;

                if (st == Material.SAND) {
                    for (int count = 1; count < 256; count++) {
                        Material type = world.getBlockAt(gb.getX(), gb.getY() + count, gb.getZ()).getType();

                        if (type == Material.AIR || type == Material.WATER || type == Material.WATER || type == Material.LAVA || type == Material.LAVA) {
                            Block toSand = world.getBlockAt(gb.getX(), gb.getY() + count, gb.getZ());
                            toSand.setType(Material.SAND, false);
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (noConflict) {
            block.setType(gb.getType(), true);

            if (block.getState() instanceof Sign && gb.getSignText().length() > 0) {
                Sign sign = (Sign) block.getState();
                String[] lines = gb.getSignText().split("[`]");

                for (int i = 0; i < lines.length; i++) {
                    sign.setLine(i, lines[i]);
                    sign.update();
                }
            }
        }
    }

    private int startInterval(final Field field) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> undoGrief(field), field.getRevertingModule().getRevertSecs() * 20L, field.getRevertingModule().getRevertSecs() * 20L);
    }
}
