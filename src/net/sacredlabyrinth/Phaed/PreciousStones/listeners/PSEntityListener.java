package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.ArrayList;
import java.util.List;
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
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Painting;
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
        CreatureType mob = event.getCreatureType();
        Location loc = event.getLocation();

        if (mob.equals(CreatureType.CREEPER)
                || mob.equals(CreatureType.GHAST)
                || mob.equals(CreatureType.GIANT)
                || mob.equals(CreatureType.MONSTER)
                || mob.equals(CreatureType.PIG_ZOMBIE)
                || mob.equals(CreatureType.SKELETON)
                || mob.equals(CreatureType.SLIME)
                || mob.equals(CreatureType.SPIDER)
                || mob.equals(CreatureType.ZOMBIE))
        {
            if (plugin.ffm.isMobSpawnProtected(loc) != null)
            {
                event.setCancelled(true);
            }
        }

        if (mob.equals(CreatureType.CHICKEN)
                || mob.equals(CreatureType.COW)
                || mob.equals(CreatureType.PIG)
                || mob.equals(CreatureType.SHEEP)
                || mob.equals(CreatureType.SQUID)
                || mob.equals(CreatureType.WOLF))
        {
            if (plugin.ffm.isAnimalSpawnProtected(loc) != null)
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

        // prevent explosion if explosion protected

        Field field = plugin.ffm.isExplosionProtected(event.getEntity().getLocation());

        if (field != null)
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

        final List<Block> blockList = event.blockList();
        final List<Block> griefedBlocks = new ArrayList<Block>();

        for (final Block block : blockList)
        {
            // prevent explosion if breaking unbreakable
            final int type = block.getTypeId();
            final byte data = block.getData();

            if (plugin.settings.isUnbreakableType(block) && plugin.um.isUnbreakable(block))
            {
                block.setTypeIdAndData(0, (byte) 0, false);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        block.setTypeIdAndData(type, data, false);
                    }
                }, 2);
                break;
            }

            // prevent explosion if breaking field

            if (plugin.ffm.isField(block))
            {
                block.setTypeIdAndData(0, (byte) 0, false);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        block.setTypeIdAndData(type, data, false);
                    }
                }, 2);
                break;
            }

            // prevent explosion if explosion protected

            Field field = plugin.ffm.isExplosionProtected(block.getLocation());

            if (field != null)
            {
                event.setCancelled(true);
                break;
            }

            // record the blocks that are in undo fields

            field = plugin.ffm.isGriefProtected(block.getLocation());

            if (field != null)
            {
                if (block.getTypeId() == 46)
                {
                    // trigger any tnt that exists inside the grief field blast radius

                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Location loc = new Location(block.getWorld(), block.getX() + .5, block.getY() + .5, block.getZ() + .5);
                            block.getWorld().spawn(loc, TNTPrimed.class);
                        }
                    }, 3);

                }
                else
                {
                    // record the block

                    if (!plugin.settings.isGriefUndoBlackListType(block.getTypeId()))
                    {
                        plugin.gum.addBlock(field, block);
                        plugin.sm.offerGrief(field);
                        griefedBlocks.add(block);
                        block.setTypeId(0);
                    }
                }
            }
        }

        if (griefedBlocks.size() > 0)
        {
            event.setCancelled(true);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                @Override
                public void run()
                {
                    // then remove the rest of the blocks in the field

                    for (Block block : griefedBlocks)
                    {
                        block.setTypeId(0);
                    }

                    // then remove nad simulate drops for the blocks not in the field

                    for (final Block block : blockList)
                    {
                        if (!griefedBlocks.contains(block))
                        {
                            ItemStack is = new ItemStack(block.getTypeId(), 1);
                            block.setTypeId(0);
                            block.getWorld().dropItemNaturally(block.getLocation(), is);
                        }
                    }
                }
            }, 1);
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

                Field field = plugin.ffm.isPvPProtected(victim.getLocation());

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
                    field = plugin.ffm.isPvPProtected(attacker.getLocation());

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

                Field field = plugin.ffm.isPvPProtected(victim.getLocation());

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
                    field = plugin.ffm.isPvPProtected(attacker.getLocation());

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

                Field field = plugin.ffm.isMobDamageProtected(player.getLocation());

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

        if (cause.equals(RemoveCause.ENTITY))
        {
            PaintingBreakByEntityEvent pre = (PaintingBreakByEntityEvent) event;

            if (pre.getRemover() instanceof Player)
            {
                Player player = (Player) pre.getRemover();

                Field field = plugin.ffm.isDestroyProtected(painting.getLocation(), player);

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

        Field field = plugin.ffm.isPlaceProtected(painting.getLocation(), player);

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
