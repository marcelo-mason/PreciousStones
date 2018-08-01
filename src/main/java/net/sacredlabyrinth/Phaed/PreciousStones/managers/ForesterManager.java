package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

/**
 * @author phaed
 */
public final class ForesterManager {
    private PreciousStones plugin;

    public ForesterManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Prepares the land inside a forrester
     *
     * @param field
     * @param world
     */
    public void prepareLand(Field field, World world) {
        int minx = field.getX() - field.getRadius();
        int maxx = field.getX() + field.getRadius();
        int minz = field.getZ() - field.getRadius();
        int maxz = field.getZ() + field.getRadius();
        int miny = field.getY() - (Math.max(field.getHeight() - 1, 0) / 2);
        int maxy = field.getY() + (Math.max(field.getHeight() - 1, 0) / 2);

        for (int x = minx; x < maxx; x += 4) {
            for (int z = minz; z <= maxz; z += 4) {
                for (int y = maxy; y > miny; y--) {
                    Material type = world.getBlockAt(x, y, z).getType();

                    if (!isSeeThrough(type)) {
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
    public int prepareSpot(Field field, World world, int xx, int yy, int zz, int radius) {
        PreciousStones.debug("prepare spot");
        Vec pos = new Vec(xx, yy, zz, world.getName());

        int affected = 0;

        for (int x = 0; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                for (int z = 0; z <= radius; z++) {
                    Vec vec = pos.add(x, y, z);
                    double d = vec.distance(pos);

                    if (vec.getX() == field.getX() && vec.getY() == field.getY() && vec.getZ() == field.getZ()) {
                        continue;
                    }

                    if (d <= radius + 0.5D) {
                        Material type = world.getBlockAt(vec.getX(), vec.getY(), vec.getZ()).getType();

                        if (field.getSettings().isFertileType(new BlockTypeEntry(type))) {
                            Block fertile = world.getBlockAt(vec.getX(), vec.getY(), vec.getZ());
                            fertile.setType(field.getSettings().getGroundBlock().getMaterial());

                            if (!field.getSettings().getShrubTypes().isEmpty()) {
                                Material typeabove = world.getBlockAt(vec.getX(), vec.getY() + 1, vec.getZ()).getType();

                                if (typeabove == Material.AIR) {
                                    Random r = new Random();

                                    int density = 100 - field.getSettings().getShrubDensity();

                                    if (density == 0 || r.nextInt(density) == 0) {
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
    public void generateTree(Field field, Player player, World world) {
        int minx = field.getX() - field.getRadius();
        int maxx = field.getX() + field.getRadius();
        int minz = field.getZ() - field.getRadius();
        int maxz = field.getZ() + field.getRadius();
        int miny = field.getY() - (int) Math.floor(((double) field.getHeight()) / 2.0);
        int maxy = field.getY() + (int) Math.ceil(((double) field.getHeight()) / 2.0);

        Random r = new Random();

        int xr = r.nextInt(maxx - minx) + minx;
        int zr = r.nextInt(maxz - minz) + minz;

        for (int y = maxy; y > miny; y--) {
            Block floor = world.getBlockAt(xr, y, zr);
            Material type = floor.getType();

            if (!isSeeThrough(type)) {
                if (type == field.getSettings().getGroundBlock().getMaterial()) {
                    Block block = world.getBlockAt(xr, y + 1, zr);

                    // do not place next to an existing tree

                    BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

                    for (BlockFace face : faces) {
                        Block rel = block.getRelative(face);

                        if (rel.getType().equals(Material.OAK_LOG)) {
                            return;
                        }
                    }

                    // do not place in protected area

                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.place")) {
                        Field f = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_PLACE);

                        if (f != null) {
                            if (!field.getSettings().inPlaceBlacklist(block)) {
                                if (FieldFlag.PREVENT_PLACE.applies(field, player)) {
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

    public static TreeType getTree(FieldSettings fs) {
        Random r = new Random();

        List<Integer> treeTypes = fs.getTreeTypes();

        if (treeTypes.isEmpty()) {
            return TreeType.TREE;
        }

        int rand = r.nextInt(treeTypes.size());
        int tree = treeTypes.get(rand);

        PreciousStones.debug("tree: " + tree);

        switch (tree) {
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
            case 9:
                return TreeType.SMALL_JUNGLE;
            case 10:
                return TreeType.COCOA_TREE;
            case 11:
                return TreeType.SWAMP;
            case 12:
                return TreeType.ACACIA;
            case 13:
                return TreeType.DARK_OAK;
            case 14:
                return TreeType.MEGA_REDWOOD;
            case 15:
                return TreeType.TALL_BIRCH;
            case 16:
                return TreeType.CHORUS_PLANT;
        }

        return TreeType.TREE;
    }

    /**
     * Gets a random shrub from available types
     *
     * @param fs
     * @param block
     */

    public void setShrub(FieldSettings fs, Block block) {
        Random r = new Random();

        List<Integer> treeTypes = fs.getShrubTypes();

        int rand = r.nextInt(treeTypes.size());

        switch (treeTypes.get(rand)) {
            case 0:
                block.setType(Material.TALL_GRASS, false); // tall grass
                return;
            case 1:
                block.setType(Material.POPPY, false); // tall grass
                return;
            case 2:
                block.setType(Material.OXEYE_DAISY, false); // tall grass
                return;
            case 3:
                block.setType(Material.DANDELION_YELLOW, false); // yellow flower
                return;
            case 4:
                block.setType(Material.ROSE_RED, false); // red flower
                return;
            case 5:
                block.setType(Material.BROWN_MUSHROOM, false); // brown shroom
                return;
            case 6:
                block.setType(Material.RED_MUSHROOM, false); // red shroom
                return;
        }
    }

    private boolean isSeeThrough(Material type) {
        return type == Material.AIR || type == Material.DEAD_BUSH || type == Material.DEAD_BUSH || type == Material.DANDELION || type == Material.POPPY || type == Material.OAK_LEAVES || type == Material.OAK_SAPLING;
    }

    /**
     * Spawn all the creatures
     */
    public void doCreatureSpawns(Field field) {
        int minx = field.getX() - field.getRadius();
        int maxx = field.getX() + field.getRadius();
        int minz = field.getZ() - field.getRadius();
        int maxz = field.getZ() + field.getRadius();
        int miny = field.getY() - (int) Math.floor(((double) field.getHeight()) / 2);
        int maxy = field.getY() + (int) Math.ceil(((double) field.getHeight()) / 2);

        Random r = new Random();
        FieldSettings fs = field.getSettings();

        for (int i = 0; i < fs.getCreatureCount(); i++) {
            int x = r.nextInt(maxx - minx) + minx;
            int z = r.nextInt(maxz - minz) + minz;
            int y = field.getY();
            World world = field.getBlock().getWorld();

            Material floorType = world.getBlockAt(x, y, z).getType();

            while (!plugin.getSettingsManager().isThroughType(floorType) && y < 256) {
                floorType = world.getBlockAt(x, ++y, z).getType();
            }

            EntityType entity = getEntity(fs);

            if (entity == null || !entity.isAlive() || !entity.isSpawnable()) {
                continue;
            }

            world.spawnEntity(new Location(world, x, y, z), entity);
        }
    }

    /**
     * Gets a random tree from available types
     *
     * @param fs
     * @return
     */
    public static EntityType getEntity(FieldSettings fs) {
        Random r = new Random();

        List<String> creatureTypes = fs.getCreatureTypes();

        if (creatureTypes.isEmpty()) {
            return null;
        }

        int rand = r.nextInt(creatureTypes.size());
        String entity = creatureTypes.get(rand);

        PreciousStones.debug("entity: " + entity);

        if (entity.equalsIgnoreCase("None")) {
            return null;
        }

        return EntityType.valueOf(entity.toUpperCase());
    }
}
