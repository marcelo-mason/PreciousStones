package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
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
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingBreakEvent.RemoveCause;
import org.bukkit.event.painting.PaintingPlaceEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * PreciousStones entity listener
 *
 * @author Phaed
 */
public class PSEntityListener implements Listener
{
    private final PreciousStones plugin;

    /**
     *
     */
    public PSEntityListener()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getEntity().getLocation().getWorld()))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onEntityTarget");

        Entity target = event.getTarget();

        if (target instanceof Player)
        {
            if (event.getReason().equals(TargetReason.CLOSEST_PLAYER))
            {
                if (event.getTarget() instanceof Player)
                {
                    Player player = (Player) event.getTarget();

                    Field field = plugin.getForceFieldManager().getEnabledSourceField(target.getLocation(), FieldFlag.REMOVE_MOB);

                    if (field != null)
                    {
                        boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            Entity mob = event.getEntity();

                            if (mob instanceof Monster)
                            {
                                mob.remove();
                            }
                        }
                    }

                    if (plugin.getForceFieldManager().hasSourceField(target.getLocation(), FieldFlag.PREVENT_MOB_DAMAGE))
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event)
    {
        Entity entity = event.getEntity();

        if (entity instanceof Player)
        {
            Player player = (Player) entity;

            if (event.getCause().equals(DamageCause.FALL))
            {
                if (plugin.getVelocityManager().isFallDamageImmune(player))
                {
                    event.setCancelled(true);
                    plugin.getCommunicationManager().showThump(player);
                    return;
                }

                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.NO_FALL_DAMAGE);

                if (field != null)
                {
                    boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
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
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        Entity entity = event.getEntity();
        Location loc = event.getLocation();

        if (plugin.getSettingsManager().isBlacklistedWorld(loc.getWorld()))
        {
            return;
        }

        if (entity instanceof Monster || entity instanceof Slime || entity instanceof Squid)
        {
            if (plugin.getForceFieldManager().getEnabledSourceFields(loc, FieldFlag.PREVENT_MOB_SPAWN).size() > 0)
            {
                event.setCancelled(true);
            }
        }

        if (entity instanceof Animals)
        {
            if (plugin.getForceFieldManager().getEnabledSourceFields(loc, FieldFlag.PREVENT_ANIMAL_SPAWN).size() > 0)
            {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosionPrime(ExplosionPrimeEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getEntity().getLocation().getWorld()))
        {
            return;
        }

        // prevent explosion if explosion protected

        if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_EXPLOSIONS))
        {
            event.setCancelled(true);
        }

        if (event.getEntity() instanceof Creeper)
        {
            if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_CREEPER_EXPLOSIONS))
            {
                event.setCancelled(true);
            }
        }

        if (event.getEntity() instanceof TNTPrimed)
        {
            if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_TNT_EXPLOSIONS))
            {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onEntityExplode");

        final List<BlockEntry> saved = new LinkedList<BlockEntry>();
        final List<BlockEntry> unprotected = new LinkedList<BlockEntry>();
        final List<BlockEntry> revert = new LinkedList<BlockEntry>();
        final List<BlockEntry> tnts = new LinkedList<BlockEntry>();
        Field rollbackField = null;

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getLocation().getWorld()))
        {
            return;
        }

        List<Block> blocks = event.blockList();

        for (Block block : blocks)
        {
            // prevent block break if breaking unbreakable

            if (plugin.getSettingsManager().isUnbreakableType(block) && plugin.getUnbreakableManager().isUnbreakable(block))
            {
                revert.add(new BlockEntry(block));
                block.setTypeIdAndData(0, (byte) 0, false);
                continue;
            }

            // prevent block break if breaking field

            if (plugin.getForceFieldManager().isField(block))
            {
                Field field = plugin.getForceFieldManager().getField(block);

                if (field.hasFlag(FieldFlag.BREAKABLE))
                {
                    plugin.getForceFieldManager().deleteField(field);
                    continue;
                }

                revert.add(new BlockEntry(block));
                block.setTypeIdAndData(0, (byte) 0, false);
                continue;
            }

            // prevent explosion if explosion protected

            if (plugin.getForceFieldManager().hasSourceField(block.getLocation(), FieldFlag.PREVENT_EXPLOSIONS))
            {
                saved.add(new BlockEntry(block));
                event.setCancelled(true);
                continue;
            }

            if (event.getEntity() instanceof Creeper)
            {
                if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_CREEPER_EXPLOSIONS))
                {
                    saved.add(new BlockEntry(block));
                    event.setCancelled(true);
                    continue;
                }
            }

            if (event.getEntity() instanceof TNTPrimed)
            {
                if (plugin.getForceFieldManager().hasSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_TNT_EXPLOSIONS))
                {
                    saved.add(new BlockEntry(block));
                    event.setCancelled(true);
                    continue;
                }
            }

            // capture blocks to roll back in rollback fields

            rollbackField = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.ROLLBACK_EXPLOSIONS);

            if (rollbackField != null)
            {
                if (block.getTypeId() != 46)
                {
                    plugin.getGriefUndoManager().addBlock(rollbackField, block, true);
                }
                else
                {
                    tnts.add(new BlockEntry(block));
                }
                continue;
            }

            // record the blocks that are in undo fields

            Field field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.GRIEF_REVERT);

            if (field != null && !field.getSettings().canGrief(block.getTypeId()))
            {
                if (block.getTypeId() == 46)
                {
                    // trigger any tnt that exists inside the grief field blast radius

                    tnts.add(new BlockEntry(block));
                    block.setType(Material.AIR);
                }
                else
                {
                    // record the griefed block

                    if (!plugin.getSettingsManager().isGriefUndoBlackListType(block.getTypeId()))
                    {
                        saved.add(new BlockEntry(block));
                        plugin.getGriefUndoManager().addBlock(field, block, true);
                    }
                }

                if (saved.size() > 0)
                {
                    plugin.getStorageManager().offerGrief(field);
                }
            }
            else
            {
                // record the unprotected block

                unprotected.add(new BlockEntry(block));
            }

            // remove the blocks

            field = plugin.getForceFieldManager().getEnabledSourceField(block.getLocation(), FieldFlag.TRANSLOCATION);

            if (field != null)
            {
                if (field.isNamed())
                {
                    plugin.getTranslocationManager().removeBlock(field, block);
                }
            }
        }

        // trigger any tnts in the field

        if (!tnts.isEmpty())
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    for (BlockEntry db : tnts)
                    {
                        Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

                        if (block != null)
                        {
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

        if (rollbackField != null)
        {
            final Field field = rollbackField;
            field.setProgress(true);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    plugin.getGriefUndoManager().undoDirtyGrief(field);
                    field.setProgress(false);
                }
            }, 2);
        }

        // revert any blocks that need reversion

        if (!revert.isEmpty())
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    for (BlockEntry db : revert)
                    {
                        Block block = db.getLocation().getBlock();
                        block.setTypeIdAndData(db.getTypeId(), db.getData(), true);
                    }
                    revert.clear();
                }
            }, 3);
        }

        // if some blocks were anti-grief then fake the explosion of the rest

        if (!saved.isEmpty() && !unprotected.isEmpty())
        {
            event.setCancelled(true);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    // remove all blocks and simulate drops for the blocks not in the field

                    for (BlockEntry db : unprotected)
                    {
                        Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

                        if (!plugin.getWorldGuardManager().canBuild(null, block.getLocation()))
                        {
                            continue;
                        }

                        if (block != null)
                        {
                            block.setTypeId(0);
                        }
                    }
                }
            }, 1);
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        // capture blocks to roll back in rollback fields

        Field rollbackField = plugin.getForceFieldManager().getEnabledSourceField(event.getLocation(), FieldFlag.ROLLBACK_EXPLOSIONS);

        if (rollbackField != null)
        {
            if (rollbackField.isProgress())
            {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getEntity().getLocation().getWorld()))
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onEntityDamage");

        if (event.getEntity() instanceof Animals)
        {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getEntity().getLocation(), FieldFlag.PROTECT_ANIMALS);

            if (field != null)
            {
                if (event instanceof EntityDamageByEntityEvent)
                {
                    EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;

                    if (sub.getDamager() instanceof Player)
                    {
                        Player player = (Player) sub.getDamager();

                        boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                event.setCancelled(true);
                return;
            }
        }

        if (event.getEntity() instanceof Monster)
        {
            Field field = plugin.getForceFieldManager().getEnabledSourceField(event.getEntity().getLocation(), FieldFlag.PROTECT_MOBS);

            if (field != null)
            {
                if (event instanceof EntityDamageByEntityEvent)
                {
                    EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;

                    if (sub.getDamager() instanceof Player)
                    {
                        Player player = (Player) sub.getDamager();

                        boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                event.setCancelled(true);
                return;
            }
        }

        // prevent fall damage after cannon throws

        if (event.getCause().equals(DamageCause.FALL))
        {
            if (event.getEntity() instanceof Player)
            {
                Player player = (Player) event.getEntity();

                if (plugin.getVelocityManager().isFallDamageImmune(player))
                {
                    event.setCancelled(true);
                    plugin.getCommunicationManager().showThump(player);
                    return;
                }

                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.NO_FALL_DAMAGE);

                if (field != null)
                {
                    boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        plugin.getCommunicationManager().showThump(player);
                        event.setCancelled(true);
                    }
                }
            }
        }

        // pvp protect against player/mobs

        if (event instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;

            if (sub.getEntity() instanceof Player)
            {
                Player victim = (Player) sub.getEntity();
                Player attacker = null;

                if (sub.getDamager() instanceof Player)
                {
                    attacker = (Player) sub.getDamager();
                }
                else if (sub.getDamager() instanceof Arrow)
                {
                    Arrow arrow = (Arrow) sub.getDamager();

                    if (arrow.getShooter() instanceof Player)
                    {
                        attacker = (Player) arrow.getShooter();
                    }
                }

                if (attacker != null)
                {
                    Field field = plugin.getForceFieldManager().getEnabledSourceField(victim.getLocation(), FieldFlag.PREVENT_PVP);

                    if (field != null)
                    {
                        if (plugin.getPermissionsManager().has(attacker, "preciousstones.bypass.pvp"))
                        {
                            plugin.getCommunicationManager().warnBypassPvP(attacker, victim, field);
                        }
                        else
                        {
                            //If both the attacker and the victim are in combat, don't cancel it
                            if (((plugin.getCombatTagManager().isInCombat(attacker)) && (plugin.getCombatTagManager().isInCombat(victim))))
                            {
                                //warn both players
                                plugin.getCommunicationManager().warnBypassPvPDueToCombat(attacker, victim);
                                return;
                            }
                            else
                            {
                                sub.setCancelled(true);
                                plugin.getCommunicationManager().warnPvP(attacker, victim, field);
                                return;
                            }
                        }
                    }
                    else
                    {
                        field = plugin.getForceFieldManager().getEnabledSourceField(attacker.getLocation(), FieldFlag.PREVENT_PVP);

                        if (field != null)
                        {
                            if (plugin.getPermissionsManager().has(attacker, "preciousstones.bypass.pvp"))
                            {
                                plugin.getCommunicationManager().warnBypassPvP(attacker, victim, field);
                            }
                            else
                            {
                                sub.setCancelled(true);
                                plugin.getCommunicationManager().warnPvP(attacker, victim, field);
                                return;
                            }
                        }
                    }
                }
            }
        }

        if (plugin.getSettingsManager().isDebug())
        {
            dt.logProcessTime();
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();
            plugin.getEntryManager().leaveAllFields(player);
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPaintingBreak(PaintingBreakEvent event)
    {
        Painting painting = event.getPainting();
        RemoveCause cause = event.getCause();

        if (plugin.getSettingsManager().isBlacklistedWorld(painting.getLocation().getWorld()))
        {
            return;
        }

        if (cause.equals(RemoveCause.ENTITY))
        {
            PaintingBreakByEntityEvent pre = (PaintingBreakByEntityEvent) event;

            if (pre.getRemover() instanceof Player)
            {
                Player player = (Player) pre.getRemover();

                Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PREVENT_DESTROY);

                if (field != null)
                {
                    boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

                    if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                    {
                        if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.destroy"))
                        {
                            return;
                        }
                        else
                        {
                            event.setCancelled(true);
                            return;
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
    public void onPaintingPlace(PaintingPlaceEvent event)
    {
        Painting painting = event.getPainting();
        Player player = event.getPlayer();

        if (plugin.getSettingsManager().isBlacklistedWorld(painting.getLocation().getWorld()))
        {
            return;
        }
        Field field = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PREVENT_PLACE);

        if (field != null)
        {
            boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

            if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
            {
                if (plugin.getPermissionsManager().has(player, "preciousstones.bypass.place"))
                {
                    plugin.getCommunicationManager().notifyPaintingBypassPlace(player, painting.getLocation(), field);
                }
                else
                {
                    event.setCancelled(true);
                }
            }
        }
    }
}
