package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.BlockData;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingBreakEvent.RemoveCause;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

/**
 * PreciousStones entity listener
 *
 * @author Phaed
 */
public class PSEntityListener extends EntityListener
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
    @Override
    public void onEndermanPickup(EndermanPickupEvent event)
    {
        if (plugin.getForceFieldManager().hasSourceField(event.getBlock().getLocation(), FieldFlag.PREVENT_DESTROY))
        {
            event.setCancelled(true);
        }

        if (plugin.getForceFieldManager().hasSourceField(event.getBlock().getLocation(), FieldFlag.GRIEF_UNDO_INTERVAL))
        {
            event.setCancelled(true);
        }

        if (plugin.getForceFieldManager().hasSourceField(event.getBlock().getLocation(), FieldFlag.GRIEF_UNDO_REQUEST))
        {
            event.setCancelled(true);
        }
    }

    /**
     * @param event
     */
    @Override
    public void onEndermanPlace(EndermanPlaceEvent event)
    {
        if (plugin.getForceFieldManager().hasSourceField(event.getLocation(), FieldFlag.PREVENT_PLACE))
        {
            event.setCancelled(true);
        }

        if (plugin.getForceFieldManager().hasSourceField(event.getLocation(), FieldFlag.GRIEF_UNDO_INTERVAL))
        {
            event.setCancelled(true);
        }

        if (plugin.getForceFieldManager().hasSourceField(event.getLocation(), FieldFlag.GRIEF_UNDO_REQUEST))
        {
            event.setCancelled(true);
        }
    }


    /**
     * @param event
     */
    @Override
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
        	if(event.getReason() == TargetReason.CLOSEST_PLAYER)
            {                      
        		if(plugin.getForceFieldManager().hasSourceField(target.getLocation(), FieldFlag.REMOVE_MOB))
        		{
        			Entity mob = event.getEntity();
        			if(mob instanceof Monster)
        			{
        				plugin.getCommunicationManager().debug("Removed a scary monster");
        				mob.remove();
        			}
        		}

        		if (plugin.getForceFieldManager().hasSourceField(target.getLocation(), FieldFlag.PREVENT_MOB_DAMAGE))
        		{
        			event.setCancelled(true);
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
    @Override
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
            if (plugin.getForceFieldManager().hasSourceField(loc, FieldFlag.PREVENT_MOB_SPAWN))
            {
                event.setCancelled(true);
            }
        }

        if (entity instanceof Animals)
        {
            if (plugin.getForceFieldManager().hasSourceField(loc, FieldFlag.PREVENT_ANIMAL_SPAWN))
            {
                event.setCancelled(true);
            }
        }
    }

    /**
     * @param event
     */
    @Override
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

        if (plugin.getForceFieldManager().getSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_EXPLOSIONS) != null)
        {
            event.setCancelled(true);
        }
    }

    /**
     * @param event
     */
    @Override
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        DebugTimer dt = new DebugTimer("onEntityExplode");

        final List<BlockData> saved = new LinkedList<BlockData>();
        final List<BlockData> unprotected = new LinkedList<BlockData>();
        final List<BlockData> revert = new LinkedList<BlockData>();
        final List<BlockData> tnts = new LinkedList<BlockData>();
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
                revert.add(new BlockData(block));
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

                revert.add(new BlockData(block));
                block.setTypeIdAndData(0, (byte) 0, false);
                continue;
            }

            // prevent explosion if explosion protected

            if (plugin.getForceFieldManager().hasSourceField(block.getLocation(), FieldFlag.PREVENT_EXPLOSIONS))
            {
                saved.add(new BlockData(block));
                event.setCancelled(true);
                continue;
            }

            // capture blocks to roll back in rollback fields

            rollbackField = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.ROLLBACK_EXPLOSIONS);

            if (rollbackField != null)
            {
                if (block.getTypeId() != 46)
                {
                    plugin.getGriefUndoManager().addBlock(rollbackField, block);
                }
                else
                {
                    tnts.add(new BlockData(block));
                }
                continue;
            }

            // record the blocks that are in undo fields

            Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.GRIEF_UNDO_REQUEST);

            if (field == null)
            {
                field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.GRIEF_UNDO_INTERVAL);
            }

            if (field != null)
            {
                if (block.getTypeId() == 46)
                {
                    // trigger any tnt that exists inside the grief field blast radius

                    tnts.add(new BlockData(block));
                }
                else
                {
                    // record the griefed block

                    if (!plugin.getSettingsManager().isGriefUndoBlackListType(block.getTypeId()))
                    {
                        plugin.getGriefUndoManager().addBlock(field, block);
                        saved.add(new BlockData(block));
                        block.setTypeId(0);
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

                unprotected.add(new BlockData(block));
            }
        }

        // trigger any tnts in the field

        if (!tnts.isEmpty())
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run()
                {
                    for (BlockData db : tnts)
                    {
                        Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

                        if (block != null)
                        {
                            Location midloc = new Location(block.getWorld(), block.getX() + .5, block.getY() + .5, block.getZ() + .5);
                            block.getWorld().spawn(midloc, TNTPrimed.class);
                        }
                    }
                    tnts.clear();
                }
            }, 10);
        }

        // revert blocks from rollback fields

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
                    for (BlockData db : revert)
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

                    for (BlockData db : unprotected)
                    {
                        Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

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
    @Override
    public void onItemSpawn(ItemSpawnEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        // capture blocks to roll back in rollback fields

        Field rollbackField = plugin.getForceFieldManager().getSourceField(event.getLocation(), FieldFlag.ROLLBACK_EXPLOSIONS);

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
    @Override
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
                    Field field = plugin.getForceFieldManager().getSourceField(victim.getLocation(), FieldFlag.PREVENT_PVP);

                    if (field != null)
                    {
                        if (plugin.getPermissionsManager().hasPermission(attacker, "preciousstones.bypass.pvp"))
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
                    else
                    {
                        field = plugin.getForceFieldManager().getSourceField(attacker.getLocation(), FieldFlag.PREVENT_PVP);

                        if (field != null)
                        {
                            if (plugin.getPermissionsManager().hasPermission(attacker, "preciousstones.bypass.pvp"))
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
    @Override
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(event.getEntity().getLocation().getWorld()))
        {
            return;
        }

        if (event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();

            plugin.getEntryManager().leaveAllFields(player);
        }
    }

    /**
     * @param event
     */
    @Override
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

                Field field = plugin.getForceFieldManager().getNotAllowedSourceField(player.getLocation(), player.getName(), FieldFlag.PREVENT_DESTROY);

                if (field != null)
                {
                    if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.destroy"))
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

    /**
     * @param event
     */
    @Override
    public void onPaintingPlace(PaintingPlaceEvent event)
    {
        Painting painting = event.getPainting();
        Player player = event.getPlayer();

        if (plugin.getSettingsManager().isBlacklistedWorld(painting.getLocation().getWorld()))
        {
            return;
        }
        Field field = plugin.getForceFieldManager().getNotAllowedSourceField(player.getLocation(), player.getName(), FieldFlag.PREVENT_PLACE);

        if (field != null)
        {
            if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.place"))
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
