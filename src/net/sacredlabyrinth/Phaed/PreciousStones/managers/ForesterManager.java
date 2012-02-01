package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.ForesterEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * @author phaed
 */
public final class ForesterManager
{
    private PreciousStones plugin;
    private HashSet<ForesterEntry> foresters = new HashSet<ForesterEntry>();
    private boolean processing = false;

    /**
     * @param plugin
     */
    public ForesterManager()
    {
        plugin = PreciousStones.getInstance();
        scheduler();
    }

    /**
     * Add forester
     *
     * @param field
     */
    public void add(Field field, String playerName)
    {
        foresters.add(new ForesterEntry(field, playerName));
    }

    /**
     * Remove forester
     *
     * @param field
     */
    public void remove(Field field)
    {
        foresters.remove(new ForesterEntry(field, ""));
    }

    private int prepareSpot(Field field, World world, int xx, int yy, int zz, int radius, boolean shrubs)
    {
        Vec pos = new Vec(xx, yy, zz, world.getName());

        int affected = 0;

        for (int x = 0; x <= radius; x++)
        {
            for (int y = 0; y <= radius; y++)
            {
                for (int z = 0; z <= radius; z++)
                {
                    Vec vec = pos.add(x, y, z);
                    double d = vec.distance(pos);

                    if (vec.getX() == field.getX() && vec.getY() == field.getY() && vec.getZ() == field.getZ())
                    {
                        continue;
                    }

                    if (d <= radius + 0.5D)
                    {
                        int type = world.getBlockTypeIdAt(vec.getX(), vec.getY(), vec.getZ());

                        if (plugin.getSettingsManager().isFertileType(type))
                        {
                            Block fertile = world.getBlockAt(vec.getX(), vec.getY(), vec.getZ());
                            fertile.setType(Material.GRASS);

                            if (shrubs)
                            {
                                int typeabove = world.getBlockTypeIdAt(vec.getX(), vec.getY() + 1, vec.getZ());

                                if (typeabove == 0)
                                {
                                    Random r = new Random();

                                    if (r.nextInt(36) == 7)
                                    {
                                        Block blockAbove = world.getBlockAt(vec.getX(), vec.getY() + 1, vec.getZ());
                                        setShrub(blockAbove);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return affected;
    }

    private void prepareLand(Field field, World world)
    {
        FieldSettings fs = field.getSettings();

        int minx = field.getX() - field.getRadius();
        int maxx = field.getX() + field.getRadius();
        int minz = field.getZ() - field.getRadius();
        int maxz = field.getZ() + field.getRadius();
        int miny = field.getY() - (Math.max(field.getHeight() - 1, 0) / 2);
        int maxy = field.getY() + (Math.max(field.getHeight() - 1, 0) / 2);

        for (int x = minx; x < maxx; x += 4)
        {
            for (int z = minz; z <= maxz; z += 4)
            {
                for (int y = maxy; y > miny; y--)
                {
                    int type = world.getBlockTypeIdAt(x, y, z);

                    if (!isSeeThrough(type))
                    {
                        prepareSpot(field, world, x, y, z, 4, field.hasFlag(FieldFlag.FORESTER_SHRUBS));
                    }
                }
            }
        }
    }

    private void generateTree(Field field, Player player, World world)
    {
        int minx = field.getX() - field.getRadius();
        int maxx = field.getX() + field.getRadius();
        int minz = field.getZ() - field.getRadius();
        int maxz = field.getZ() + field.getRadius();
        int miny = field.getY() - (int) Math.floor(((double) field.getHeight()) / 2);
        int maxy = field.getY() + (int) Math.ceil(((double) field.getHeight()) / 2);

        Random r = new Random();

        int xr = r.nextInt(maxx - minx) + minx;
        int zr = r.nextInt(maxz - minz) + minz;

        for (int y = maxy; y > miny; y--)
        {
            Block floor = world.getBlockAt(xr, y, zr);
            int type = floor.getTypeId();

            if (!isSeeThrough(type))
            {
                if (type == 2)
                {
                    Block block = world.getBlockAt(xr, y + 1, zr);

                    // do not place next to an existing tree

                    BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

                    for (BlockFace face : faces)
                    {
                        Block rel = block.getRelative(face);

                        if (rel.getType().equals(Material.LOG))
                        {
                            return;
                        }
                    }

                    // do not place in protected area

                    Field f = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_PLACE);

                    if (f != null)
                    {
                        boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                        if (!allowed)
                        {
                            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
                            {
                                return;
                            }
                        }
                    }

                    // create tree
                    floor.setType(Material.GRASS);
                    block.setType(Material.AIR);
                    world.generateTree(block.getLocation(), getTree());
                }

                return;
            }
        }
    }

    private void scheduler()
    {
        final List<Field> deletion = new ArrayList<Field>();

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            public void run()
            {
                if (processing)
                {
                    return;
                }

                if (foresters.isEmpty())
                {
                    return;
                }

                processing = true;

                for (ForesterEntry fe : foresters)
                {
                    if (fe.isProcessing())
                    {
                        continue;
                    }

                    Field field = fe.getField();
                    fe.setProcessing(true);

                    FieldSettings fs = field.getSettings();

                    World world = plugin.getServer().getWorld(field.getWorld());
                    Player player = plugin.getServer().getPlayer(fe.getPlayerName());

                    if (world == null || player == null)
                    {
                        fe.setProcessing(false);
                        continue;
                    }

                    if (!fe.isLandPrepared())
                    {
                        prepareLand(field, world);
                        fe.setLandPrepared(true);
                    }

                    generateTree(field, player, world);
                    fe.addCount();

                    if (fe.getCount() >= plugin.getSettingsManager().getForesterTrees())
                    {
                        deletion.add(field);
                        break;
                    }

                    fe.setProcessing(false);
                }

                for (Field field : deletion)
                {
                    World world = plugin.getServer().getWorld(field.getWorld());

                    if (world != null)
                    {
                        Block block = world.getBlockAt(field.getX(), field.getY(), field.getZ());
                        block.setTypeId(0, false);
                        world.generateTree(block.getLocation(), TreeType.BIRCH);
                    }

                    foresters.remove(field);
                    plugin.getForceFieldManager().queueRelease(field);
                }

                deletion.clear();
                plugin.getForceFieldManager().flush();
                processing = false;
            }
        }, 20L * plugin.getSettingsManager().getForesterInterval(), 20L * plugin.getSettingsManager().getForesterInterval());
    }

    private static TreeType getTree()
    {
        Random r = new Random();

        switch (r.nextInt(5))
        {
            case 0:
                return TreeType.TREE;
            case 1:
                return TreeType.BIG_TREE;
            case 2:
                return TreeType.REDWOOD;
            case 3:
                return TreeType.TALL_REDWOOD;
            case 4:
                return TreeType.BIRCH;
        }

        return TreeType.TREE;
    }

    private void setShrub(Block block)
    {
        Random r = new Random();

        if (r.nextInt(2) == 0)
        {
            block.setTypeIdAndData(31, (byte) 1, false);
        }
        else
        {
            switch (r.nextInt(3))
            {
                case 0:
                    block.setTypeIdAndData(31, (byte) 1, false);
                    break;
                case 1:
                    block.setTypeIdAndData(31, (byte) 2, false);
                    break;
                case 2:
                    switch (r.nextInt(5))
                    {
                        case 0:
                            block.setTypeId(37, false);
                            break;
                        case 1:
                            block.setTypeId(38, false);
                            break;
                        case 2:
                            block.setTypeId(37, false);
                            break;
                        case 3:
                            block.setTypeId(38, false);
                            break;
                        case 4:
                            switch (r.nextInt(2))
                            {
                                case 0:
                                    block.setTypeId(39, false);
                                    break;
                                case 1:
                                    block.setTypeId(40, false);
                                    break;
                            }
                    }
            }
        }

    }

    private boolean isSeeThrough(int type)
    {
        if (type == 0 || type == 31 || type == 32 || type == 37 || type == 38 || type == 18 || type == 6)
        {
            return true;
        }

        return false;
    }
}
