package net.sacredlabyrinth.Phaed.PreciousStones.managers;


import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.blocks.RelativeBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.TeleportEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeleportationManager {
    private PreciousStones plugin;

    public TeleportationManager() {
        plugin = PreciousStones.getInstance();
    }

    public boolean teleport(Entity entity, Field sourceField) {
        return teleport(entity, sourceField, "");
    }

    public boolean teleport(Entity entity, Field sourceField, String announce) {
        Field destinationField = plugin.getForceFieldManager().getDestinationField(sourceField.getOwner(), sourceField);

        if (destinationField != null) {
            if (sourceField.getSettings().getTeleportMaxDistance() > 0) {
                if (sourceField.getLocation().distance(destinationField.getLocation()) > sourceField.getSettings().getTeleportMaxDistance()) {
                    Player player = Bukkit.getServer().getPlayerExact(sourceField.getOwner());

                    if (player != null) {
                        ChatHelper.send(player, "teleportMaxDistance", sourceField.getSettings().getTeleportMaxDistance());
                    }
                    return false;
                }
            }

            if (sourceField.getSettings().getTeleportCost() > 0) {
                if (plugin.getPermissionsManager().hasEconomy()) {
                    if (PermissionsManager.hasMoney(Bukkit.getServer().getPlayerExact(sourceField.getOwner()), sourceField.getSettings().getTeleportCost())) {
                        plugin.getPermissionsManager().playerCharge(Bukkit.getServer().getPlayerExact(sourceField.getOwner()), sourceField.getSettings().getTeleportCost());
                    } else {
                        Player player = Bukkit.getServer().getPlayerExact(sourceField.getOwner());

                        if (player != null) {
                            ChatHelper.send(player, "economyNotEnoughMoney");
                        }
                        return false;
                    }
                }
            }

            if (sourceField.hasFlag(FieldFlag.TELEPORT_RELATIVELY)) {
                return plugin.getTeleportationManager().teleport(new TeleportEntry(entity, new RelativeBlock(sourceField.toVec(), new Vec(entity.getLocation())), sourceField, destinationField, announce));
            } else {
                return plugin.getTeleportationManager().teleport(new TeleportEntry(entity, sourceField, destinationField, announce));
            }
        }

        return false;
    }

    public boolean teleport(TeleportEntry entry) {
        List<TeleportEntry> entries = new ArrayList<TeleportEntry>();
        entries.add(entry);
        return teleport(entries);
    }

    public boolean teleport(List<TeleportEntry> entries) {
        for (TeleportEntry entry : entries) {
            Entity entity = entry.getEntity();
            Location destination = entry.getDestination();
            Field sourceField = entry.getSourceField();
            Vec currentPosition = null;

            if (entity instanceof Player) {
                Player player = (Player) entity;

                plugin.getPlayerManager().getPlayerEntry(player).setTeleporting(false);

                // done teleport players with bypass permission

                if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.teleport")) {
                    continue;
                }

                // don't teleport if sneaking bypasses

                if (sourceField.hasFlag(FieldFlag.SNEAKING_BYPASS) && !sourceField.hasFlag(FieldFlag.TELEPORT_ON_SNEAK)) {
                    if (player.isSneaking()) {
                        continue;
                    }
                }

                currentPosition = new Vec(player.getLocation());
            }

            // prepare teleport destination

            World world = destination.getWorld();

            Vec safe = findSafeLocation(destination);
            double x = safe.getX() + .5D;
            double y = safe.getY();
            double z = safe.getZ() + .5D;

            if (y == -1) {
                continue;
            }

            if (!world.isChunkLoaded(destination.getBlockX() >> 4, destination.getBlockZ() >> 4)) {
                world.loadChunk(destination.getBlockX() >> 4, destination.getBlockZ() >> 4);
            }

            Location loc = new Location(world, x, y, z, entity.getLocation().getYaw(), entity.getLocation().getPitch());

            // teleport the player

            if (sourceField.hasFlag(FieldFlag.TELEPORT_EXPLOSION_EFFECT)) {
                world.createExplosion(entity.getLocation(), -1);
            }

            entity.teleport(loc);

            if (sourceField.hasFlag(FieldFlag.TELEPORT_EXPLOSION_EFFECT)) {
                world.createExplosion(loc, -1);
            }

            if (entity instanceof Player) {
                Player player = (Player) entity;

                if (sourceField.hasFlag(FieldFlag.TELEPORT_ANNOUNCE)) {
                    if (!entry.getAnnounce().isEmpty()) {
                        ChatHelper.send(player, entry.getAnnounce());
                    }
                }

                // start teleport back countdown

                if (sourceField.getSettings().getTeleportBackAfterSeconds() > 0) {
                    if (sourceField.hasFlag(FieldFlag.TELEPORT_ANNOUNCE)) {
                        ChatHelper.send(player, "teleportAnnounceBack", sourceField.getSettings().getTeleportBackAfterSeconds());
                    }

                    PlayerEntry playerEntry = plugin.getPlayerManager().getPlayerEntry(player);

                    playerEntry.setTeleportSecondsRemaining(sourceField.getSettings().getTeleportBackAfterSeconds());
                    playerEntry.setTeleportVec(currentPosition);
                    playerEntry.startTeleportCountDown();
                    plugin.getStorageManager().offerPlayer(player.getName());
                }
            }
        }

        return true;
    }

    public void teleportAway(Player player) {
        // prepare teleport destination

        Location destination = player.getLocation();
        World world = destination.getWorld();

        Vec safe = findSafeLocation(destination);
        double x = safe.getX() + .5D;
        double y = safe.getY();
        double z = safe.getZ() + .5D;

        if (y == -1) {
            return;
        }

        if (!world.isChunkLoaded(destination.getBlockX() >> 4, destination.getBlockZ() >> 4)) {
            world.loadChunk(destination.getBlockX() >> 4, destination.getBlockZ() >> 4);
        }

        Location loc = new Location(world, x, y, z, destination.getYaw(), destination.getPitch());

        // teleport the player

        player.teleport(loc);
    }

    private Vec findSafeLocation(Location dest) {
        Vec d = new Vec(dest);

        if (blockIsSafe(d)) {
            return d;
        }
        if (blockIsSafe(d.add(0, 1, 0))) {
            return d.add(0, 1, 0);
        }
        if (blockIsSafe(d.add(0, 2, 0))) {
            return d.add(0, 2, 0);
        }
        if (blockIsSafe(d.add(1, 0, 0))) {
            return d.add(1, 0, 0);
        }
        if (blockIsSafe(d.add(-1, 0, 0))) {
            return d.add(-1, 0, 0);
        }
        if (blockIsSafe(d.add(0, 0, 1))) {
            return d.add(0, 0, 1);
        }
        if (blockIsSafe(d.add(0, 0, -1))) {
            return d.add(0, 0, -1);
        }
        if (blockIsSafe(d.add(1, 0, 1))) {
            return d.add(1, 0, 1);
        }
        if (blockIsSafe(d.add(-1, 0, 1))) {
            return d.add(-1, 0, 1);
        }
        if (blockIsSafe(d.add(1, 0, -1))) {
            return d.add(1, 0, -1);
        }
        if (blockIsSafe(d.add(-1, 0, -1))) {
            return d.add(-1, 0, -1);
        }

        return new Vec(d.getX(), findSafeHeight(dest), d.getZ(), d.getWorld());
    }

    private int findSafeHeight(Location dest) {
        int y = dest.getBlockY();

        while (!blockIsSafe(dest.getWorld(), dest.getBlockX(), y, dest.getBlockZ())) {
            y += 1;

            if (y >= 255) {
                return -1;
            }
        }

        return y;
    }

    private boolean blockIsSafe(Vec vec) {
        return blockIsSafe(vec.toWorld(), vec.getX(), vec.getY(), vec.getZ());
    }

    private boolean blockIsSafe(World world, int x, int y, int z) {
        int head = world.getBlockTypeIdAt(x, y + 1, z);
        int feet = world.getBlockTypeIdAt(x, y, z);
        return (plugin.getSettingsManager().isThroughType(head)) && (plugin.getSettingsManager().isThroughType((feet)));
    }
}
