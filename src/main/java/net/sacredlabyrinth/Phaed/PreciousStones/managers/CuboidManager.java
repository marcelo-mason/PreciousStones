package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;

public class CuboidManager {
    private PreciousStones plugin;
    private HashMap<String, CuboidEntry> openCuboids = new HashMap<String, CuboidEntry>();

    /**
     *
     */
    public CuboidManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Whether the player has a currently open cuboid definition
     *
     * @param player
     * @return
     */
    public boolean hasOpenCuboid(Player player) {
        return openCuboids.containsKey(player.getName());
    }

    /**
     * If the block is a cuboid field or one of its children
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOpenCuboid(Player player, Block block) {
        return isOpenCuboidField(player, block) || isOpenCuboidChild(player, block);
    }

    /**
     * If there is a currently open cuboid definition emanating from the field block
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOpenCuboidField(Player player, Block block) {
        if (block == null) {
            return false;
        }

        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null) {
            Field field = ce.getField();

            if (field != null) {
                if (Helper.isSameBlock(field.getLocation(), block.getLocation())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * If the block belongs to a child field inside an open cuboid definition
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOpenCuboidChild(Player player, Block block) {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null) {
            Field field = ce.getField();

            for (Field child : field.getChildren()) {
                if (Helper.isSameBlock(child.getLocation(), block.getLocation())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the players open cuboid definition if it exists
     *
     * @param player
     * @return
     */
    public CuboidEntry getOpenCuboid(Player player) {
        return openCuboids.get(player.getName());
    }

    /**
     * Adds a single block to the cuboid definition
     *
     * @param player
     * @param block
     */
    public boolean processSelectedBlock(Player player, Block block) {
        CuboidEntry openCuboid = getOpenCuboid(player);

        if (!plugin.getVisualizationManager().isOutlineBlock(player, block)) {
            if (openCuboid.testOverflow(block.getLocation()) || plugin.getPermissionsManager().has(player, "preciousstones.bypass.cuboid")) {
                if (plugin.getPermissionsManager().canBuild(player, block.getLocation())) {
                    if (openCuboids.containsKey(player.getName())) {
                        CuboidEntry ce = openCuboids.get(player.getName());

                        int oldVolume = ce.getAvailableVolume();

                        if (ce.isSelected(block)) {
                            ce.removeSelected(block);
                            plugin.getVisualizationManager().displaySingle(player, block.getType(), block);
                        } else {
                            // find conflicts

                            CuboidEntry clone = ce.Clone();
                            clone.addSelected(block);

                            if (plugin.getForceFieldManager().existsConflict(clone.getMockField(), player)) {
                                ChatHelper.send(player, "cuboidSelectionConflicts");
                                return false;
                            }

                            // add block

                            ce.addSelected(block);
                            plugin.getVisualizationManager().displaySingle(player, plugin.getSettingsManager().getCuboidDefiningType().getMaterial(), block);
                        }

                        int newVolume = ce.getAvailableVolume();

                        if (newVolume != oldVolume) {
                            plugin.getVisualizationManager().displayFieldOutline(player, ce);

                            if (newVolume >= 0) {
                                ChatHelper.send(player, "cuboidAvailableProtection", newVolume);
                            } else {
                                ChatHelper.send(player, "cuboidAvailableProtectionBypass", newVolume);
                            }
                        }
                    }
                    return true;
                } else {
                    ChatHelper.send(player, "cuboidCannotExtendWG");
                }
            } else {
                ChatHelper.send(player, "cuboidExceeds");
            }
        } else {
            ChatHelper.send(player, "cuboidOutline");
        }

        return false;
    }


    /**
     * Initiates a cuboid definition
     *
     * @param player
     * @param field
     */
    public void openCuboid(final Player player, final Field field) {
        final CuboidEntry ce = new CuboidEntry(field);
        openCuboids.put(player.getName(), ce);

        field.setOpen(true);
        plugin.getVisualizationManager().revert(player);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                ce.addSelected(field.getBlock());

                for (Field child : field.getChildren()) {
                    ce.addSelected(child.getBlock());
                }

                plugin.getVisualizationManager().displayFieldOutline(player, ce);

                ChatHelper.send(player, "cuboidDrawingMode");
                ChatHelper.send(player, "cuboidAvailableProtection", ce.getAvailableVolume());
            }
        }, 1L);
    }

    /**
     * Adds a child field to the currently open cuboid definition
     *
     * @param player
     * @param field
     */
    public void openChild(final Player player, final Field field) {
        final CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    ce.addSelected(field.getBlock());
                    PreciousStones.getInstance().getForceFieldManager().addSourceField(field);
                    ChatHelper.send(player, "cuboidAvailableProtection", ce.getAvailableVolume());
                }
            }, 1L);
        }
    }

    /**
     * End the open cuboid definition cleanly
     *
     * @param player
     * @return
     */
    public boolean closeCuboid(final Player player) {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null) {
            final Field field = ce.getField();

            if (ce.isExceeded()) {
                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.cuboid")) {
                    ChatHelper.send(player, "cuboidExceedsMax");
                    cancelOpenCuboid(player);
                    return false;
                }
            }

            if (plugin.getForceFieldManager().fieldConflicts(ce, player) != null) {
                ChatHelper.send(player, "cuboidConflicts");
                cancelOpenCuboid(player);
                return false;
            }

            List<Vector> corners = field.getCorners();
            corners.add(field.getLocation().toVector());

            for (Vector corner : corners) {
                Location location = corner.toLocation(player.getWorld());

                if (!plugin.getPermissionsManager().canBuild(player, location)) {
                    ChatHelper.send(player, "cuboidConflictsWG");
                    cancelOpenCuboid(player);
                    return false;
                }
            }

            if (field.hasFlag(FieldFlag.PREVENT_UNPROTECTABLE)) {
                Block foundBlock = plugin.getUnprotectableManager().existsUnprotectableBlock(field);

                if (foundBlock != null) {
                    if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.unprotectable")) {
                        plugin.getCommunicationManager().warnPlaceFieldInUnprotectable(player, foundBlock, field.getBlock());
                        cancelOpenCuboid(player);
                        return false;
                    }
                }
            }

            plugin.getVisualizationManager().revert(player);
            plugin.getVisualizationManager().revertOutline(player);
            plugin.getForceFieldManager().removeSourceField(field);

            ce.finalizeField();
            field.setOpen(false);

            openCuboids.remove(player.getName());

            plugin.getForceFieldManager().addSourceField(field);
            plugin.getForceFieldManager().addAllowOverlappingOwners(field);
            plugin.getVisualizationManager().visualizeSingleField(player, field);
            plugin.getStorageManager().offerField(field);
            plugin.getCommunicationManager().notifyPlaceCuboid(player, field);
            return true;
        }

        return false;
    }

    /**
     * Remove a child block from an open field
     *
     * @param player
     * @param block
     */
    public void removeChild(Player player, Block block) {
        CuboidEntry ce = openCuboids.get(player.getName());
        Field child = plugin.getForceFieldManager().getField(block);

        if (ce != null && child != null) {
            ce.getField().getChildren().remove(child);
        }
    }

    /**
     * Cancels a currently open cuboid definition if one of the field blocks are broken
     *
     * @param block the block that is being broken
     */
    public void cancelOpenCuboid(Player player, Block block) {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null) {
            Field field = ce.getField();

            if (Helper.isSameBlock(field.getLocation(), block.getLocation())) {
                cancelOpenCuboid(player);
            }

            for (Field child : field.getChildren()) {
                if (Helper.isSameBlock(child.getLocation(), block.getLocation())) {
                    cancelOpenCuboid(player);
                }
            }
        }
    }

    /**
     * Cancel an open cuboid and revert all visualizations
     *
     * @param player
     */
    public void cancelOpenCuboid(Player player) {
        plugin.getVisualizationManager().revert(player);
        plugin.getVisualizationManager().revertOutline(player);

        if (openCuboids.containsKey(player.getName())) {
            openCuboids.remove(player.getName());
            ChatHelper.send(player, "cuboidCancelled");
        }
    }

    /**
     * Revert the last selected block
     *
     * @param player
     */
    public void revertLastSelection(Player player) {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null) {
            BlockEntry selected = ce.getLastSelected();

            if (selected != null) {
                ce.revertLastSelected();
                plugin.getVisualizationManager().revertSingle(player, selected.getBlock());
                plugin.getVisualizationManager().displayFieldOutline(player, ce);
                ChatHelper.send(player, "cuboidReverted");
            }
        }
    }

    /**
     * Expand the cuboid in the direction the player is facing by one block
     *
     * @param player
     */
    public void expandDirection(Player player) {
        CuboidEntry ce = openCuboids.get(player.getName());

        if (ce != null) {
            Block block = ce.getExpandedBlock(player);

            if (block != null) {
                if (!processSelectedBlock(player, block)) {
                    return;
                }
            }

            plugin.getVisualizationManager().displayFieldOutline(player, ce);
        }
    }
}
