package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

/**
 * @author phaed
 */
public final class ForesterManager
{
    private PreciousStones plugin;

    public ForesterManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Prepares the land inside a forrester
     *
     * @param field
     * @param world
     */
    public void prepareLand(Field field, World world)
    {
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
                        prepareSpot(field, world, x, y, z, 4);
                    }
                }
            }
        }
    }


    /**
     * Prepares a circular spot inside a forrester
     *
     * @param field
     * @param world
     * @param xx
     * @param yy
     * @param zz
     * @param radius
     * @return
     */
    public int prepareSpot(Field field, World world, int xx, int yy, int zz, int radius)
    {
        PreciousStones.debug("prepare spot");
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

                        if (field.getSettings().isFertileType(type))
                        {
                            Block fertile = world.getBlockAt(vec.getX(), vec.getY(), vec.getZ());
                            fertile.setTypeId(field.getSettings().getGroundBlock());

                            if (!field.getSettings().getShrubTypes().isEmpty())
                            {
                                int typeabove = world.getBlockTypeIdAt(vec.getX(), vec.getY() + 1, vec.getZ());

                                if (typeabove == 0)
                                {
                                    Random r = new Random();

                                    int density = 100 - field.getSettings().getShrubDensity();

                                    if (density == 0 || r.nextInt(density) == 0)
                                    {
                                        Block blockAbove = world.getBlockAt(vec.getX(), vec.getY() + 1, vec.getZ());
                                        setShrub(field.getSettings(), blockAbove);
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

    /**
     * Generates a tree
     *
     * @param field
     * @param player
     * @param world
     */
    public void generateTree(Field field, Player player, World world)
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
                if (type == field.getSettings().getGroundBlock())
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

                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
                    {
                        Field f = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_PLACE);

                        if (f != null)
                        {
                            if (!field.getSettings().inPlaceBlacklist(block))
                            {
                                if (FieldFlag.PREVENT_PLACE.applies(field, player))
                                {
                                    return;
                                }
                            }
                        }
                    }

                    // create tree
                    floor.setType(Material.GRASS);
                    block.setType(Material.AIR);
                    world.generateTree(block.getLocation(), getTree(field.getSettings()));
                }

                return;
            }
        }
    }

    /**
     * Gets a random tree from available types
     *
     * @param fs
     * @return
     */

    public static TreeType getTree(FieldSettings fs)
    {
        Random r = new Random();

        List<Integer> treeTypes = fs.getTreeTypes();

        if (treeTypes.isEmpty())
        {
            return TreeType.TREE;
        }

        int rand = r.nextInt(treeTypes.size());
        int tree = treeTypes.get(rand);

        PreciousStones.debug("tree: " + tree);

        switch (tree)
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
            case 5:
                return TreeType.RED_MUSHROOM;
            case 6:
                return TreeType.BROWN_MUSHROOM;
            case 7:
                return TreeType.JUNGLE;
            case 8:
                return TreeType.JUNGLE_BUSH;
        }

        return TreeType.TREE;
    }

    /**
     * Gets a random shrub from available types
     *
     * @param fs
     * @param block
     */

    public void setShrub(FieldSettings fs, Block block)
    {
        Random r = new Random();

        List<Integer> treeTypes = fs.getShrubTypes();

        int rand = r.nextInt(treeTypes.size());

        switch (treeTypes.get(rand))
        {
            case 0:
                block.setTypeIdAndData(31, (byte) 0, false);
                return;
            case 1:
                block.setTypeIdAndData(31, (byte) 1, false);
                return;
            case 2:
                block.setTypeIdAndData(31, (byte) 2, false);
                return;
            case 3:
                block.setTypeId(37, false);
                return;
            case 4:
                block.setTypeId(38, false);
                return;
            case 5:
                block.setTypeId(40, false);
                return;
            case 6:
                block.setTypeId(39, false);
                return;
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

    /**
     * Spawn all the creatures
     */
    public void doCreatureSpawns(Field field)
    {
        int minx = field.getX() - field.getRadius();
        int maxx = field.getX() + field.getRadius();
        int minz = field.getZ() - field.getRadius();
        int maxz = field.getZ() + field.getRadius();
        int miny = field.getY() - (int) Math.floor(((double) field.getHeight()) / 2);
        int maxy = field.getY() + (int) Math.ceil(((double) field.getHeight()) / 2);

        Random r = new Random();
        FieldSettings fs = field.getSettings();

        for (int i = 0; i < fs.getCreatureCount(); i++)
        {
            int x = r.nextInt(maxx - minx) + minx;
            int z = r.nextInt(maxz - minz) + minz;
            int y = field.getY();
            World world = field.getBlock().getWorld();

            int floorType = world.getBlockTypeIdAt(x, y, z);

            while (!plugin.getSettingsManager().isThroughType(floorType) && y < 256)
            {
                floorType = world.getBlockTypeIdAt(x, ++y, z);
            }

            EntityType entity = getEntity(fs);

            if (entity == null || !entity.isAlive() || !entity.isSpawnable())
            {
                continue;
            }

            world.spawnCreature(new Location(world, x, y, z), entity);
        }
    }

    /**
     * Gets a random tree from available types
     *
     * @param fs
     * @return
     */
    public static EntityType getEntity(FieldSettings fs)
    {
        Random r = new Random();

        List<String> creatureTypes = fs.getCreatureTypes();

        if (creatureTypes.isEmpty())
        {
            return null;
        }

        int rand = r.nextInt(creatureTypes.size());
        String entity = creatureTypes.get(rand);

        PreciousStones.debug("entity: " + entity);

        if (entity.equalsIgnoreCase("None"))
        {
            return null;
        }

        return EntityType.fromName(entity);
    }
}
