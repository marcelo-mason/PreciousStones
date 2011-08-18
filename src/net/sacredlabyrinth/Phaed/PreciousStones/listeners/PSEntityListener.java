package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.BlockData;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingBreakEvent.RemoveCause;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.inventory.ItemStack;

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
     * @param plugin
     */
    public PSEntityListener(PreciousStones plugin)
    {
        this.plugin = plugin;
    }

    /**
     *
     * @param event
     */
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        Entity entity = event.getEntity();
        Location loc = event.getLocation();

        if (plugin.settings.isBlacklistedWorld(loc.getWorld()))
        {
            return;
        }

        if (entity instanceof Monster
                || entity instanceof Slime)
        {
            if (plugin.ffm.isMobSpawnProtected(loc))
            {
                event.setCancelled(true);
            }
        }

        if (entity instanceof Animals)
        {
            if (plugin.ffm.isAnimalSpawnProtected(loc))
            {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onExplosionPrime(ExplosionPrimeEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.settings.isBlacklistedWorld(event.getEntity().getLocation().getWorld()))
        {
            return;
        }

        // prevent explosion if explosion protected

        if (plugin.ffm.isExplosionProtected(event.getEntity().getLocation()))
        {
            event.setCancelled(true);
        }
    }

    /**
     *
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

        final List<BlockData> griefed = new LinkedList<BlockData>();
        final List<BlockData> nonGriefed = new LinkedList<BlockData>();
        final List<BlockData> revert = new LinkedList<BlockData>();
        final List<BlockData> tnts = new LinkedList<BlockData>();

        if (plugin.settings.isBlacklistedWorld(event.getEntity().getLocation().getWorld()))
        {
            return;
        }

        for (final Block block : event.blockList())
        {
            // prevent block break if breaking unbreakable

            if (block.getTypeId() == 0)
            {
                continue;
            }

            if (plugin.settings.isUnbreakableType(block) && plugin.um.isUnbreakable(block))
            {
                block.setTypeIdAndData(0, (byte) 0, false);
                revert.add(new BlockData(block));
                continue;
            }

            // prevent block break if breaking field

            if (plugin.ffm.isField(block))
            {
                block.setTypeIdAndData(0, (byte) 0, false);
                revert.add(new BlockData(block));
                continue;
            }

            // prevent explosion if explosion protected

            if (plugin.ffm.isExplosionProtected(block.getLocation()))
            {
                event.setCancelled(true);
                break;
            }

            // record the blocks that are in undo fields

            Field field = plugin.ffm.findGriefProtected(block.getLocation());

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

                    if (!plugin.settings.isGriefUndoBlackListType(block.getTypeId()))
                    {
                        plugin.gum.addBlock(field, block);
                        griefed.add(new BlockData(block));
                        block.setTypeId(0);
                    }
                }

                if (griefed.size() > 0)
                {
                    plugin.sm.offerGrief(field);
                }
            }
            else
            {
                // record the non-griefed block

                if (block.getTypeId() != 46)
                {
                    nonGriefed.add(new BlockData(block));
                }
            }
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                for (BlockData db : revert)
                {
                    Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

                    block.setTypeIdAndData(db.getTypeId(), db.getData(), false);
                }
                revert.clear();

                for (BlockData db : tnts)
                {
                    Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

                    Location midloc = new Location(block.getWorld(), block.getX() + .5, block.getY() + .5, block.getZ() + .5);
                    block.getWorld().spawn(midloc, TNTPrimed.class);
                }
                tnts.clear();
            }
        }, 4);

        // if some blocks were anti-grief then fake the explosion of the rest

        if (griefed.size() > 0)
        {
            event.setCancelled(true);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                @Override
                public void run()
                {
                    // remove all blocks and simulate drops for the blocks not in the field

                    for (BlockData db : nonGriefed)
                    {
                        Block block = db.getLocation().getWorld().getBlockAt(db.getLocation());

                        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getTypeId(), 1));
                        block.setTypeId(0);
                    }
                }
            }, 2);
        }

        if (plugin.settings.debug)
        {
            dt.logProcessTime();
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.settings.isBlacklistedWorld(event.getEntity().getLocation().getWorld()))
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

                if (plugin.vm.isFallDamageImmune(player))
                {
                    event.setCancelled(true);
                    plugin.cm.showThump(player);
                }
            }
        }

        // pvp protect against player

        if (event instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;

            if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
            {
                Player attacker = (Player) sub.getDamager();
                Player victim = (Player) sub.getEntity();

                Field field = plugin.ffm.findPvPProtected(victim.getLocation());

                if (field != null)
                {
                    if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
                    {
                        plugin.cm.warnBypassPvP(attacker, victim, field);
                    }
                    else
                    {
                        sub.setCancelled(true);
                        plugin.cm.warnPvP(attacker, victim, field);
                    }
                }
                else
                {
                    field = plugin.ffm.findPvPProtected(attacker.getLocation());

                    if (field != null)
                    {
                        if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
                        {
                            plugin.cm.warnBypassPvP(attacker, victim, field);
                        }
                        else
                        {
                            sub.setCancelled(true);
                            plugin.cm.warnPvP(attacker, victim, field);
                        }
                    }
                }
            }
        }

        // pvp protect against projectile

        if (event instanceof EntityDamageByProjectileEvent)
        {
            EntityDamageByProjectileEvent sub = (EntityDamageByProjectileEvent) event;

            if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
            {
                Player attacker = (Player) sub.getDamager();
                Player victim = (Player) sub.getEntity();

                Field field = plugin.ffm.findPvPProtected(victim.getLocation());

                if (field != null)
                {
                    if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
                    {
                        plugin.cm.warnBypassPvP(attacker, victim, field);
                    }
                    else
                    {
                        sub.setCancelled(true);
                        plugin.cm.warnPvP(attacker, victim, field);
                    }
                }
                else
                {
                    field = plugin.ffm.findPvPProtected(attacker.getLocation());

                    if (field != null)
                    {
                        if (plugin.pm.hasPermission(attacker, "preciousstones.bypass.pvp"))
                        {
                            plugin.cm.warnBypassPvP(attacker, victim, field);
                        }
                        else
                        {
                            sub.setCancelled(true);
                            plugin.cm.warnPvP(attacker, victim, field);
                        }
                    }
                }
            }
        }

        // pvp protect against any other player attack

        if (event.getCause().equals(DamageCause.ENTITY_ATTACK))
        {
            if (event.getEntity() instanceof Player)
            {
                Player player = (Player) event.getEntity();

                Field field = plugin.ffm.findMobDamageProtected(player.getLocation());

                if (field != null)
                {
                    event.setCancelled(true);
                }
            }
        }

        if (plugin.settings.debug)
        {
            dt.logProcessTime();
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (plugin.settings.isBlacklistedWorld(event.getEntity().getLocation().getWorld()))
        {
            return;
        }

        if (event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();

            plugin.em.leaveAllFields(player);
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void onPaintingBreak(PaintingBreakEvent event)
    {
        Painting painting = event.getPainting();
        RemoveCause cause = event.getCause();

        if (plugin.settings.isBlacklistedWorld(painting.getLocation().getWorld()))
        {
            return;
        }

        if (cause.equals(RemoveCause.ENTITY))
        {
            PaintingBreakByEntityEvent pre = (PaintingBreakByEntityEvent) event;

            if (pre.getRemover() instanceof Player)
            {
                Player player = (Player) pre.getRemover();

                Field field = plugin.ffm.findDestroyProtected(painting.getLocation(), player);

                if (field != null)
                {
                    if (plugin.pm.hasPermission(player, "preciousstones.bypass.destroy"))
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
     *
     * @param event
     */
    @Override
    public void onPaintingPlace(PaintingPlaceEvent event)
    {
        Painting painting = event.getPainting();
        Player player = event.getPlayer();

        if (plugin.settings.isBlacklistedWorld(painting.getLocation().getWorld()))
        {
            return;
        }

        Field field = plugin.ffm.findPlaceProtected(painting.getLocation(), player);

        if (field != null)
        {
            if (plugin.pm.hasPermission(player, "preciousstones.bypass.place"))
            {
                plugin.cm.notifyBypassPlace(player, field);
            }
            else
            {
                event.setCancelled(true);
            }
        }
    }
}
