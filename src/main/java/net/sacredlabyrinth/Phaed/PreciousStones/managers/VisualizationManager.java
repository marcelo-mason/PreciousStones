package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.visualization.Visualization;
import net.sacredlabyrinth.Phaed.PreciousStones.visualization.Visualize;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author phad
 */
public class VisualizationManager {
    private PreciousStones plugin;
    private HashMap<String, Integer> counts = new HashMap<>();
    private HashMap<String, Visualization> visualizations = new HashMap<>();

    /**
     *
     */
    public VisualizationManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Visualize and display a single field
     *
     * @param player
     * @param field
     */
    public void visualizeSingleField(Player player, Field field) {
        addVisualizationField(player, field);
        displayVisualization(player, false);
    }

    /**
     * Visualize and display a single field for 2 seconds
     *
     * @param player
     * @param field
     */
    public void visualizeSingleFieldFast(Player player, Field field) {
        addVisualizationField(player, field);
        displayVisualization(player, false, 2);
    }

    /**
     * If the player is in the middle of a visualization
     *
     * @param player
     * @return
     */
    public boolean pendingVisualization(Player player) {
        return visualizations.containsKey(player.getName());
    }

    /**
     * Reverts all current visualizations
     */
    @SuppressWarnings("deprecation")
    public void revertAll() {
        for (Entry<String, Visualization> visualization : visualizations.entrySet()) {
            Visualization vis = visualization.getValue();
            Player player = Bukkit.getServer().getPlayerExact(visualization.getKey());

            if (player != null) {
                Visualize visualize = new Visualize(vis.getBlocks(), player, true, false, 0);
            }
        }
        visualizations.clear();
        counts.clear();
    }

    /**
     * Adds a fields perimeter to a player's visualization buffer
     *
     * @param player
     * @param field
     */
    public void addVisualizationField(Player player, Field field) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        if (plugin.getCuboidManager().hasOpenCuboid(player)) {
            return;
        }

        PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player);

        if (data.getDensity() == 0) {
            return;
        }

        vis.addField(field);

        Material visualizationType = field.hasFlag(FieldFlag.CUBOID) ? plugin.getSettingsManager().getCuboidVisualizationType().getMaterial() : plugin.getSettingsManager().getVisualizeBlock().getMaterial();
        Material frameType = plugin.getSettingsManager().getVisualizeFrameBlock().getMaterial();

        int minx = field.getX() - field.getRadius() - 1;
        int maxx = field.getX() + field.getRadius() + 1;
        int minz = field.getZ() - field.getRadius() - 1;
        int maxz = field.getZ() + field.getRadius() + 1;
        int miny = field.getY() - (Math.max(field.getHeight() - 1, 0) / 2) - 1;
        int maxy = field.getY() + (Math.max(field.getHeight() - 1, 0) / 2) + 1;

        if (field.hasFlag(FieldFlag.CUBOID)) {
            minx = field.getMinx() - 1;
            maxx = field.getMaxx() + 1;
            minz = field.getMinz() - 1;
            maxz = field.getMaxz() + 1;
            miny = field.getMiny() - 1;
            maxy = field.getMaxy() + 1;
        }

        for (int x = minx; x <= maxx; x++) {
            Location loc = new Location(player.getWorld(), x, miny, maxz);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), x, maxy, minz);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), x, miny, minz);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), x, maxy, maxz);
            vis.addBlock(loc, frameType);
        }

        for (int y = miny; y <= maxy; y++) {
            Location loc = new Location(player.getWorld(), minx, y, maxz);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), maxx, y, minz);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), minx, y, minz);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), maxx, y, maxz);
            vis.addBlock(loc, frameType);
        }

        for (int z = minz; z <= maxz; z++) {
            Location loc = new Location(player.getWorld(), minx, maxy, z);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), maxx, miny, z);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), minx, miny, z);
            vis.addBlock(loc, frameType);

            loc = new Location(player.getWorld(), maxx, maxy, z);
            vis.addBlock(loc, frameType);
        }

        int spacing = ((Math.max(Math.max((maxx - minx), (maxy - miny)), (maxz - minz)) + 2) / data.getDensity()) + 1;

        for (int y = miny; y <= maxy; y++) {
            boolean yTurn = turnCounter(player.getName() + 1, spacing);

            if (maxy - y < spacing) {
                yTurn = false;
            }

            int count = 0;
            for (int z = minz; z <= maxz; z++) {
                if (yTurn || turnCounter(player.getName() + 2, spacing)) {
                    if (maxz - z < spacing && !yTurn) {
                        break;
                    }

                    if (!yTurn && count >= data.getDensity() - 1) {
                        break;
                    }

                    Location loc = new Location(player.getWorld(), minx, y, z);
                    vis.addBlock(loc, visualizationType);

                    loc = new Location(player.getWorld(), maxx, y, z);
                    vis.addBlock(loc, visualizationType);
                    count++;
                }
            }
            counts.put(player.getName() + 2, 0);
        }
        counts.put(player.getName() + 1, 0);


        for (int x = minx; x <= maxx; x++) {
            boolean xTurn = turnCounter(player.getName() + 1, spacing);

            if (maxx - x < spacing) {
                xTurn = false;
            }

            int count = 0;
            for (int z = minz; z <= maxz; z++) {
                if (xTurn || turnCounter(player.getName() + 2, spacing)) {
                    if (maxz - z < spacing && !xTurn) {
                        break;
                    }

                    if (!xTurn && count >= data.getDensity() - 1) {
                        break;
                    }

                    Location loc = new Location(player.getWorld(), x, miny, z);
                    vis.addBlock(loc, visualizationType);

                    loc = new Location(player.getWorld(), x, maxy, z);
                    vis.addBlock(loc, visualizationType);
                    count++;
                }
            }
            counts.put(player.getName() + 2, 0);
        }
        counts.put(player.getName() + 1, 0);


        for (int y = miny; y <= maxy; y++) {
            boolean yTurn = turnCounter(player.getName() + 1, spacing);

            if (maxy - y < spacing) {
                yTurn = false;
            }

            int count = 0;
            for (int x = minx; x <= maxx; x++) {
                if (maxx - x < spacing && !yTurn) {
                    break;
                }

                if (!yTurn && count >= data.getDensity() - 1) {
                    break;
                }

                if (yTurn || turnCounter(player.getName() + 2, spacing)) {
                    Location loc = new Location(player.getWorld(), x, y, minz);
                    vis.addBlock(loc, visualizationType);

                    loc = new Location(player.getWorld(), x, y, maxz);
                    vis.addBlock(loc, visualizationType);
                    count++;
                }
            }
            counts.put(player.getName() + 2, 0);
        }
        counts.put(player.getName() + 1, 0);

        visualizations.put(player.getName(), vis);
    }

    private boolean turnCounter(String name, int size) {
        if (counts.containsKey(name)) {
            int count = counts.get(name);
            count += 1;

            if (count >= size) {
                counts.put(name, 0);
                return true;
            }

            counts.put(name, count);
        } else {
            counts.put(name, 1);
        }

        return false;
    }

    /**
     * Visualizes a single field's outline
     *
     * @param player
     * @param field
     */
    public void visualizeSingleOutline(Player player, Field field, boolean revert) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        // save current outline and clear out the visualization

        List<BlockEntry> newBlocks = new ArrayList<>();

        Material frameType = plugin.getSettingsManager().getVisualizeFrameBlock().getMaterial();

        int minx = field.getX() - field.getRadius() - 1;
        int maxx = field.getX() + field.getRadius() + 1;
        int minz = field.getZ() - field.getRadius() - 1;
        int maxz = field.getZ() + field.getRadius() + 1;
        int miny = field.getY() - (Math.max(field.getHeight() - 1, 0) / 2) - 1;
        int maxy = field.getY() + (Math.max(field.getHeight() - 1, 0) / 2) + 1;

        if (field.hasFlag(FieldFlag.CUBOID)) {
            minx = field.getMinx() - 1;
            maxx = field.getMaxx() + 1;
            minz = field.getMinz() - 1;
            maxz = field.getMaxz() + 1;
            miny = field.getMiny() - 1;
            maxy = field.getMaxy() + 1;
        }

        // add  the blocks for the new outline

        for (int x = minx; x <= maxx; x++) {
            Material frame = (x == minx || x == maxx) ? Material.GLOWSTONE : frameType;

            Location loc = new Location(player.getWorld(), x, miny, maxz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), x, maxy, minz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), x, miny, minz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), x, maxy, maxz);
            newBlocks.add(new BlockEntry(loc, frame));
        }

        for (int y = miny; y <= maxy; y++) {
            Material frame = (y == miny || y == maxy) ? Material.GLOWSTONE : frameType;

            Location loc = new Location(player.getWorld(), minx, y, maxz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), maxx, y, minz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), minx, y, minz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), maxx, y, maxz);
            newBlocks.add(new BlockEntry(loc, frame));
        }

        for (int z = minz; z <= maxz; z++) {
            Material frame = (z == minz || z == maxz) ? Material.GLOWSTONE : frameType;

            Location loc = new Location(player.getWorld(), minx, maxy, z);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), maxx, miny, z);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), minx, miny, z);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), maxx, maxy, z);
            newBlocks.add(new BlockEntry(loc, frame));
        }

        // visualize all the new blocks that are left to visualize

        Visualize visualize = new Visualize(newBlocks, player, false, !revert, plugin.getSettingsManager().getVisualizeSeconds());
        visualizations.put(player.getName(), vis);
    }

    /**
     * Adds a fields outline to a player's visualization buffer
     *
     * @param player
     * @param ce
     */
    public void displayFieldOutline(Player player, CuboidEntry ce) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        // save current outline and clear out the visualization

        List<BlockEntry> oldBlocks = new ArrayList<>(vis.getOutlineBlocks());
        List<BlockEntry> newBlocks = new ArrayList<>();

        Material frameType = plugin.getSettingsManager().getVisualizeFrameBlock().getMaterial();

        int offset = ce.selectedCount() > 1 ? 1 : 0;

        int minx = ce.getMinx() - offset;
        int miny = ce.getMiny() - offset;
        int minz = ce.getMinz() - offset;
        int maxx = ce.getMaxx() + offset;
        int maxy = ce.getMaxy() + offset;
        int maxz = ce.getMaxz() + offset;

        // add  the blocks for the new outline

        for (int x = minx; x <= maxx; x++) {
            Material frame = (x == minx || x == maxx) ? Material.GLOWSTONE : frameType;

            Location loc = new Location(player.getWorld(), x, miny, maxz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), x, maxy, minz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), x, miny, minz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), x, maxy, maxz);
            newBlocks.add(new BlockEntry(loc, frame));
        }

        for (int y = miny; y <= maxy; y++) {
            Material frame = (y == miny || y == maxy) ? Material.GLOWSTONE : frameType;

            Location loc = new Location(player.getWorld(), minx, y, maxz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), maxx, y, minz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), minx, y, minz);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), maxx, y, maxz);
            newBlocks.add(new BlockEntry(loc, frame));
        }

        for (int z = minz; z <= maxz; z++) {
            Material frame = (z == minz || z == maxz) ? Material.GLOWSTONE : frameType;

            Location loc = new Location(player.getWorld(), minx, maxy, z);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), maxx, miny, z);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), minx, miny, z);
            newBlocks.add(new BlockEntry(loc, frame));

            loc = new Location(player.getWorld(), maxx, maxy, z);
            newBlocks.add(new BlockEntry(loc, frame));
        }

        // revert the blocks that are no longer in the new set and should be reverted

        List<BlockEntry> revertible = new ArrayList<>(oldBlocks);
        revertible.removeAll(newBlocks);

        Visualize revert = new Visualize(revertible, player, true, false, plugin.getSettingsManager().getVisualizeSeconds());

        // visualize all the new blocks that are left to visualize

        List<BlockEntry> missing = new ArrayList<>(newBlocks);
        missing.removeAll(oldBlocks);

        Visualize visualize = new Visualize(missing, player, false, true, plugin.getSettingsManager().getVisualizeSeconds());

        vis.setOutlineBlocks(newBlocks);
        visualizations.put(player.getName(), vis);
    }

    /**
     * Whether the block is currently visualized as outline
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOutlineBlock(Player player, Block block) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        return vis.getOutlineBlocks().contains(new BlockEntry(block));
    }

    /**
     * @param player
     * @param field
     */
    public void addFieldMark(Player player, Field field) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        vis.addField(field);

        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null) {
            for (int y = 0; y < 256; y++) {
                Material typeId = world.getBlockAt(field.getX(), y, field.getZ()).getType();

                if (plugin.getSettingsManager().isThroughType(typeId)) {
                    vis.addBlock(new Location(world, field.getX(), y, field.getZ()), plugin.getSettingsManager().getVisualizeMarkBlock().getMaterial());
                }
            }
        }

        visualizations.put(player.getName(), vis);
    }

    /**
     * Adds and displays a visualized block to the player
     *
     * @param player
     * @param material
     * @param block
     */
    @SuppressWarnings("deprecation")
    public void displaySingle(Player player, Material material, Block block) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        vis.addBlock(block);
        visualizations.put(player.getName(), vis);

        player.sendBlockChange(block.getLocation(), material, (byte) 0);
    }

    /**
     * Revert a single a visualized block to the player
     *
     * @param player
     * @param block
     */
    public void revertSingle(Player player, Block block) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        vis.addBlock(block);
        visualizations.put(player.getName(), vis);

        player.sendBlockChange(block.getLocation(), block.getBlockData());
    }

    /**
     * Displays contents of a player's visualization buffer to the player
     *
     * @param player
     * @param minusOverlap
     */
    public void displayVisualization(final Player player, boolean minusOverlap) {
        displayVisualization(player, minusOverlap, plugin.getSettingsManager().getVisualizeSeconds());
    }

    /**
     * Displays contents of a player's visualization buffer to the player
     *
     * @param player
     * @param minusOverlap
     */
    public void displayVisualization(final Player player, boolean minusOverlap, int seconds) {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null) {
            if (minusOverlap) {
                for (Iterator<BlockEntry> iter = vis.getBlocks().iterator(); iter.hasNext(); ) {
                    BlockEntry bd = iter.next();
                    Location loc = bd.getLocation();

                    for (Field field : vis.getFields()) {
                        if (field.envelops(loc)) {
                            iter.remove();
                            break;
                        }
                    }
                }

                Visualize visualize = new Visualize(vis.getBlocks(), player, false, false, seconds);
            } else {
                Visualize visualize = new Visualize(vis.getBlocks(), player, false, false, seconds);
            }
        }
    }

    /**
     * Reverts any player's entire visualization buffer
     *
     * @param player
     */
    public void revert(Player player) {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null) {
            visualizations.remove(player.getName());
            Visualize visualize = new Visualize(vis.getBlocks(), player, true, false, 0);
        }
    }

    /**
     * Reverts the player's outline blocks
     *
     * @param player
     */
    public void revertOutline(Player player) {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null) {
            visualizations.remove(player.getName());
            Visualize visualize = new Visualize(vis.getOutlineBlocks(), player, true, false, 0);
        }
    }
}
