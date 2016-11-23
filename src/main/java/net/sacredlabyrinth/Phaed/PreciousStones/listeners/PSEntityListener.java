package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.SignHelper;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.List;

/**
 * PreciousStones entity listener
 *
 * @author Phaed
 */
public class PSEntityListener implements Listener {
    private final PreciousStones plugin;

    /**
     *
     */
    public PSEntityListener() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getEntity().getLocation().getWorld())) {
            return;
        }

        Entity target = event.getTarget();

        if (target instanceof Player) {
            if (event.getReason().equals(TargetReason.CLOSEST_PLAYER)) {
                if (event.getTarget() instanceof Player) {
                    Player player = (Player) event.getTarget();

                    Field field = plugin.getForceFieldManager().getEnabledSourceField(target.getLocation(), FieldFlag.REMOVE_MOB);

                    if (field != null) {
                        if (FieldFlag.REMOVE_MOB.applies(field, player)) {
                            Entity mob = event.getEntity();

                            if (mob instanceof Monster) {
                                mob.remove();
                            }
                        }
                    }

                    if (plugin.getForceFieldManager().hasSourceField(target.getLocation(), FieldFlag.PREVENT_MOB_DAMAGE)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (player.getFoodLevel() < event.getFoodLevel()) {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.TELEPORT_ON_FEEDING);

                if (field != null) {
                    if (FieldFlag.TELEPORT_ON_FEEDING.applies(field, player)) {
                        event.setCancelled(true);
                        plugin.getTeleportationManager().teleport(player, field, "teleportAnnounceFeeding");
                    }
                }
            }
        }
    }


    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (event.getCause().equals(DamageCause.FALL)) {
                if (plugin.getVelocityManager().isFallDamageImmune(player)) {
                    event.setCancelled(true);
                    plugin.getCommunicationManager().showThump(player);
                    return;
                }

                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.NO_FALL_DAMAGE);

                if (field != null) {
                    if (FieldFlag.NO_FALL_DAMAGE.applies(field, player)) {
                        plugin.getCommunicationManager().showThump(player);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        Location loc = event.getLocation();

        if (plugin.getSettingsManager().isBlacklistedWorld(loc.getWorld())) {
            return;
        }

        if (entity instanceof Monster || entity instanceof Slime || entity instanceof Squid) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(loc, FieldFlag.PREVENT_MOB_SPAWN);
            if (field != null) {
                event.setCancelled(true);
            }
        }

        if (entity instanceof Animals) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(loc, FieldFlag.PREVENT_ANIMAL_SPAWN);
            if (field != null) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getEntity().getLocation().getWorld())) {
            return;
        }

        // prevent explosion if explosion protected

        if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_EXPLOSIONS)) {
            event.setCancelled(true);
        }

        if (event.getEntity() instanceof Creeper) {
            if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_CREEPER_EXPLOSIONS)) {
                event.setCancelled(true);
            }
        }

        if (event.getEntity() instanceof TNTPrimed) {
            if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_TNT_EXPLOSIONS)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final List<BlockEntry> saved = new ArrayList<BlockEntry>();
        final List<BlockEntry> unprotected = new ArrayList<BlockEntry>();
        final List<BlockEntry> revert = new ArrayList<BlockEntry>();
        final List<BlockEntry> tnts = new ArrayList<BlockEntry>();
        Field rollbackField = null;

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getLocation().getWorld())) {
            return;
        }

        List<Block> blocks = event.blockList();

        for (Block block : blocks) {
            // prevent block break if breaking unbreakable

            if (plugin.getSettingsManager().isUnbreakableType(block) && plugin.getUnbreakableManager().isUnbreakable(block)) {
                revert.add(new BlockEntry(block));
                block.setTypeIdAndData(0, (byte) 0, false);
                continue;
            }

            // prevent block break if breaking field

            if (plugin.getForceFieldManager().isField(block)) {
                Field field = plugin.getForceFieldManager().getField(block);

                if (field.hasFlag(FieldFlag.BREAKABLE)) {
                    plugin.getForceFieldManager().deleteField(field);
                    continue;
                }

                revert.add(new BlockEntry(block));
                block.setTypeIdAndData(0, (byte) 0, false);
                continue;
            }

            // prevent explosion if explosion protected

            if (plugin.getForceFieldManager().hasSourceField(block.getLocation(), FieldFlag.PREVENT_EXPLOSIONS)) {
                saved.add(new BlockEntry(block));
                event.setCancelled(true);
                continue;
            }

            // prevent explosion if field sign

            if (SignHelper.cannotBreakFieldSign(block, null)) {
                event.setCancelled(true);
                return;
            }

            if (event.getEntity() instanceof Creeper) {
                if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_CREEPER_EXPLOSIONS)) {
                    saved.add(new BlockEntry(block));
                    event.setCancelled(true);
                    continue;
                }
            }

            if (event.getEntity() instanceof Wither || event.getEntity() instanceof WitherSkull) {
                if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_WITHER_EXPLOSIONS)) {
                    saved.add(new BlockEntry(block));
                    event.setCancelled(true);
                    continue;
                }
            }

            if (event.getEntity() instanceof TNTPrimed) {
                if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_TNT_EXPLOSIONS)) {
                    saved.add(new BlockEntry(block));
                    event.setCancelled(true);
                    continue;
                }
            }

            // capture blocks to roll back in rollback fields

            rollbackField = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.ROLLBACK_EXPLOSIONS);

            if (rollbackField != null) {
                if (block.getTypeId() != 46) {
                    plugin.getGriefUndoManager().addBlock(rollbackField, block, true);
                } else {
                    tnts.add(new BlockEntry(block));
                }
                continue;
            }

            // record the blocks that are in undo fields

            Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

            if (field != null && !field.getSettings().canGrief(new BlockTypeEntry(block.getType()))) {
                if (block.getTypeId() == 46) {
                    // trigger any tnt that exists inside the grief field blast radius

                    tnts.add(new BlockEntry(block));
                    block.setType(Material.AIR);
                } else {
                    // record the griefed block

                    if (!plugin.getSettingsManager().isGriefUndoBlackListType(block.getTypeId())) {
                        saved.add(new BlockEntry(block));
                        plugin.getGriefUndoManager().addBlock(field, block, true);
                    }
                }

                if (!saved.isEmpty()) {
                    plugin.getStorageManager().offerGrief(field);
                }
            } else {
                // record the unprotected block

                unprotected.add(new BlockEntry(block));
            }

            // remove the blocks

            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.TRANSLOCATION);

            if (field != null) {
                if (field.isNamed()) {
                    plugin.getTranslocationManager().removeBlock(field, block);
                }
            }
        }

        // show explosion effect

        if (event.isCancelled()) {
            event.getLocation().getWorld().createExplosion(event.getLocation(), 0.0F, false);
            event.getLocation().getWorld().playEffect(event.getLocation(), Effect.SMOKE, 1);
        }

        // trigger any tnts in the field

        if (!tnts.isEmpty()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    for (BlockEntry db : tnts) {
                        Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

                        if (block != null) {
                            Location midloc = new Location(block.getWorld(), block.getX() + .5, block.getY() + .5, block.getZ() + .5);
                            block.getWorld().spawn(midloc, TNTPrimed.class);
                            block.setTypeId(0);
                        }
                    }
                    tnts.clear();
                }
            }, 10);
        }

        // revert blocks from rollback fields (notice this is running after other tnts have been triggered)

        if (rollbackField != null) {
            final Field field = rollbackField;
            field.setProgress(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    plugin.getGriefUndoManager().undoDirtyGrief(field);
                    field.setProgress(false);
                }
            }, 2);
        }

        // revert any blocks that need reversion

        if (!revert.isEmpty()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    for (BlockEntry db : revert) {
                        Block block = db.getLocation().getBlock();
                        block.setTypeIdAndData(db.getTypeId(), db.getData(), true);
                    }
                    revert.clear();
                }
            }, 3);
        }

        // if some blocks were anti-grief then fake the explosion of the rest

        if (!saved.isEmpty() && !unprotected.isEmpty()) {
            event.setCancelled(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    // remove all blocks and simulate drops for the blocks not in the field

                    for (BlockEntry db : unprotected) {
                        Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

                        if (!plugin.getPermissionsManager().canBuild(null, block.getLocation())) {
                            continue;
                        }

                        block.setTypeId(0);
                    }
                }
            }, 1);
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // capture blocks to roll back in rollback fields

        Field rollbackField = plugin.getForceFieldManager().getEnabledSourceField(event.getLocation(), FieldFlag.ROLLBACK_EXPLOSIONS);

        if (rollbackField != null) {
            if (rollbackField.isProgress()) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getEntity().getLocation().getWorld())) {
            return;
        }

        if (event.getEntity().getType().equals(EntityType.ITEM_FRAME)) {
            Player player = Helper.getDamagingPlayer(event);

            if (player != null && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.item-frame-take")) {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_ITEM_FRAME_TAKE);

                if (field != null) {
                    if (FieldFlag.PREVENT_ITEM_FRAME_TAKE.applies(field, player)) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (event.getEntity().getType().equals(EntityType.ARMOR_STAND)) {
            Player player = Helper.getDamagingPlayer(event);

            if ((player != null && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.armor-stand-take")) || player == null) {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getEntity().getLocation(), FieldFlag.PROTECT_ARMOR_STANDS);

                if (field != null) {
                    if (player != null) {
                        if (FieldFlag.PROTECT_ARMOR_STANDS.applies(field, player)) {
                            event.setCancelled(true);
                        }
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.TELEPORT_ON_DAMAGE);

            if (field != null) {
                if (FieldFlag.TELEPORT_ON_DAMAGE.applies(field, player)) {
                    event.setCancelled(true);
                    plugin.getTeleportationManager().teleport(player, field, "teleportAnnounceDamage");
                }
            }

            if (player.getHealth() - event.getDamage() < 0) {
                field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.TELEPORT_BEFORE_DEATH);

                if (field != null) {
                    if (FieldFlag.TELEPORT_BEFORE_DEATH.applies(field, player)) {
                        event.setCancelled(true);
                        plugin.getTeleportationManager().teleport(player, field, "teleportAnnounceDeath");
                    }
                }
            }
        }

        if (event.getEntity() instanceof Ageable) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getEntity().getLocation(), FieldFlag.PROTECT_ANIMALS);

            if (field != null) {
                if (event instanceof EntityDamageByEntityEvent) {

                    Player player = Helper.getDamagingPlayer(event);
                    if (player != null) {
                        if (FieldFlag.PROTECT_ANIMALS.applies(field, player)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (field.hasFlag(FieldFlag.PROTECT_ANIMALS)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        if (event.getEntity() instanceof Villager) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getEntity().getLocation(), FieldFlag.PROTECT_VILLAGERS);

            if (field != null) {
                if (event instanceof EntityDamageByEntityEvent) {
                    Player player = Helper.getDamagingPlayer(event);

                    if (player != null) {
                        if (FieldFlag.PROTECT_VILLAGERS.applies(field, player)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (field.hasFlag(FieldFlag.PROTECT_VILLAGERS)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        if (event.getEntity() instanceof Monster || event.getEntity() instanceof Golem || event.getEntity() instanceof WaterMob) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getEntity().getLocation(), FieldFlag.PROTECT_MOBS);

            if (field != null) {
                if (event instanceof EntityDamageByEntityEvent) {
                    Player player = Helper.getDamagingPlayer(event);

                    if (player != null) {
                        if (FieldFlag.PROTECT_MOBS.applies(field, player)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        if (field.hasFlag(FieldFlag.PROTECT_MOBS)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
            return;
        }

        // prevent fall damage after cannon throws

        if (event.getCause().equals(DamageCause.FALL)) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();

                if (plugin.getVelocityManager().isFallDamageImmune(player)) {
                    event.setCancelled(true);
                    plugin.getCommunicationManager().showThump(player);
                    return;
                }

                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.NO_FALL_DAMAGE);

                if (field != null) {
                    if (FieldFlag.NO_FALL_DAMAGE.applies(field, player)) {
                        plugin.getCommunicationManager().showThump(player);
                        event.setCancelled(true);
                    }
                }
            }
        }

        // pvp protect against player/mobs

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;

            if (sub.getEntity() instanceof Player) {
                Player victim = (Player) sub.getEntity();
                Player attacker = null;

                if (sub.getDamager() instanceof Player) {
                    attacker = (Player) sub.getDamager();
                } else if (sub.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow) sub.getDamager();

                    if (arrow.getShooter() instanceof Player) {
                        attacker = (Player) arrow.getShooter();
                    }
                }

                if (attacker != null) {
                    Field field = plugin.getForceFieldManager().getEnabledSourceField(victim.getLocation(), FieldFlag.PREVENT_PVP);

                    if (field != null) {
                        if (plugin.getPermissionsManager().has(attacker, "preciousstones.bypass.pvp")) {
                            plugin.getCommunicationManager().warnBypassPvP(attacker, victim, field);
                        } else {
                            //If both the attacker and the victim are in combat, don't cancel it
                            if (((plugin.getCombatTagManager().isInCombat(attacker)) && (plugin.getCombatTagManager().isInCombat(victim)))) {
                                //warn both players
                                plugin.getCommunicationManager().warnBypassPvPDueToCombat(attacker, victim);
                                return;
                            } else {
                                sub.setCancelled(true);
                                plugin.getCommunicationManager().warnPvP(attacker, victim, field);
                                return;
                            }
                        }
                    } else {
                        field = plugin.getForceFieldManager().getEnabledSourceField(attacker.getLocation(), FieldFlag.PREVENT_PVP);

                        if (field != null) {
                            if (plugin.getPermissionsManager().has(attacker, "preciousstones.bypass.pvp")) {
                                plugin.getCommunicationManager().warnBypassPvP(attacker, victim, field);
                            } else {
                                sub.setCancelled(true);
                                plugin.getCommunicationManager().warnPvP(attacker, victim, field);
                                return;
                            }
                        }
                    }

                    // -------------------------------------------------------------------------------- pvp inside a teleport field

                    field = plugin.getForceFieldManager().getEnabledSourceField(victim.getLocation(), FieldFlag.TELEPORT_ON_PVP);

                    if (field != null) {
                        if (FieldFlag.TELEPORT_ON_PVP.applies(field, attacker.getName())) {
                            sub.setCancelled(true);
                            plugin.getTeleportationManager().teleport(attacker, field, "teleportAnnouncePvp");
                        }
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDoorBreak(EntityBreakDoorEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getBlock() == null) {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getBlock().getLocation().getWorld())) {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getBlock().getLocation(), FieldFlag.PREVENT_DESTROY);

        if (field != null) {
            event.setCancelled(true);
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (plugin.getSettingsManager().isBlacklistedWorld(entity.getLocation().getWorld())) {
            return;
        }

        if (entity.getType().equals(EntityType.ARMOR_STAND)) {
            if (player != null && !plugin.getPermissionsManager().has(player, "preciousstones.bypass.armor-stand-take")) {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(entity.getLocation(), FieldFlag.PROTECT_ARMOR_STANDS);

                if (field != null) {
                    if (FieldFlag.PROTECT_ARMOR_STANDS.applies(field, player)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            plugin.getEntryManager().leaveAllFields(player);

            Player killer = event.getEntity().getKiller();

            if (killer != null) {
                plugin.getSnitchManager().recordSnitchPlayerKill(killer, player);
            }
            plugin.getCuboidManager().cancelOpenCuboid(player);
        } else {
            Player killer = event.getEntity().getKiller();

            if (killer != null) {
                plugin.getSnitchManager().recordSnitchEntityKill(killer, event.getEntity());
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        Entity entity = event.getEntity();

        if (entity == null || block == null) {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(block.getLocation().getWorld())) {
            return;
        }

        if (entity instanceof Enderman) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_ENDERMAN_DESTROY);

            if (field != null) {
                event.setCancelled(true);
            }
        } else if (entity instanceof EnderDragon || entity instanceof Monster || entity instanceof Ghast) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PREVENT_DESTROY);

            if (field != null) {
                event.setCancelled(true);
            }
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (plugin.getSettingsManager().isCrop(block)) {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PROTECT_CROPS);

                if (field != null) {
                    if (FieldFlag.PROTECT_CROPS.applies(field, player)) {
                        event.setCancelled(true);
                    }
                }
            }
        } else {
            if (plugin.getSettingsManager().isCrop(block)) {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.PROTECT_CROPS);

                if (field != null) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Hanging entity = event.getEntity();
        Entity remover = event.getRemover();

        if (plugin.getSettingsManager().isBlacklistedWorld(entity.getLocation().getWorld())) {
            return;
        }

        if (remover instanceof Player) {
            Player player = (Player) event.getRemover();

            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy")) {
                Field field = plugin.getForceFieldManager().getEnabledSourceField(entity.getLocation(), FieldFlag.PREVENT_DESTROY);

                if (field != null) {
                    if (FieldFlag.PREVENT_DESTROY.applies(field, player)) {
                        event.setCancelled(true);
                        plugin.getCommunicationManager().warnDestroyHanging(player, entity, field);
                    }
                }
            }
        } else {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(entity.getLocation(), FieldFlag.PREVENT_DESTROY);

            if (field != null) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler
    public void onHangingPlaceEvent(HangingPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Hanging entity = event.getEntity();
        Player player = event.getPlayer();

        if (plugin.getSettingsManager().isBlacklistedWorld(entity.getLocation().getWorld())) {
            return;
        }

        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.place")) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(entity.getLocation(), FieldFlag.PREVENT_PLACE);

            if (field != null) {
                if (FieldFlag.PREVENT_PLACE.applies(field, player)) {
                    event.setCancelled(true);
                    plugin.getCommunicationManager().warnPlaceHanging(player, entity, field);
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler
    public void onHangingBreakEvent(HangingBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Hanging entity = event.getEntity();

        if (plugin.getSettingsManager().isBlacklistedWorld(entity.getLocation().getWorld())) {
            return;
        }

        if (!event.getCause().equals(HangingBreakEvent.RemoveCause.ENTITY)) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(entity.getLocation(), FieldFlag.PREVENT_DESTROY);

            if (field != null) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ThrownPotion potion = event.getPotion();

        ProjectileSource shooter = potion.getShooter();

        if (shooter != null) {
            if (shooter instanceof Player) {
                Player player = (Player) shooter;

                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PREVENT_POTION_SPLASH);

                if (field != null) {
                    if (FieldFlag.PREVENT_POTION_SPLASH.applies(field, player)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileThrow(ProjectileLaunchEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Projectile projectile = event.getEntity();

        ProjectileSource shooter = projectile.getShooter();

        if (shooter != null) {
            if (shooter instanceof Player) {
                Player player = (Player) shooter;

                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.NO_PROJECTILE_THROW);

                if (field != null) {
                    if (FieldFlag.NO_PROJECTILE_THROW.applies(field, player)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PROTECT_ANIMALS);
            if (field != null) {
                if (FieldFlag.PROTECT_ANIMALS.applies(field, player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
        ItemStack is = event.getInventory().getResult();

        if (is == null) {
            return;
        }

        BlockTypeEntry type = new BlockTypeEntry(is.getTypeId(), is.getData().getData());
        FieldSettings settings = plugin.getSettingsManager().getFieldSettings(type);

        if (settings != null && settings.hasMetaName() && settings.isMetaAutoSet()) {
            HumanEntity entity = event.getView().getPlayer();

            if (entity instanceof Player) {
                Player player = (Player) entity;
                PlayerEntry playerEntry = plugin.getPlayerManager().getPlayerEntry(player);
                if (playerEntry.isDisabled()) {
                    return;
                }
            }

            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(settings.getMetaName());
            meta.setLore(settings.getMetaLore());
            is.setItemMeta(meta);
            event.getInventory().setResult(is);
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getPlayer().getLocation(), FieldFlag.NO_DROPPING_ITEMS);

        if (field != null) {
            if (FieldFlag.NO_DROPPING_ITEMS.applies(field, event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreeperPower(CreeperPowerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_CREEPER_EXPLOSIONS);

        if (field != null) {
            event.setCancelled(true);
        }
    }
}
