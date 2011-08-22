package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.BlockData;
import net.sacredlabyrinth.Phaed.PreciousStones.DebugTimer;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
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
import org.bukkit.entity.Arrow;
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
    public PSEntityListener()
    {
        plugin = PreciousStones.getInstance();
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

        if (plugin.getSettingsManager().isBlacklistedWorld(loc.getWorld()))
        {
            return;
        }

        if (entity instanceof Monster
                || entity instanceof Slime)
        {
            if (plugin.getForceFieldManager().getSourceField(loc, FieldFlag.PREVENT_MOB_SPAWN) != null)
            {
                event.setCancelled(true);
            }
        }

        if (entity instanceof Animals)
        {
            if (plugin.getForceFieldManager().getSourceField(loc, FieldFlag.PREVENT_ANIMAL_SPAWN) != null)
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

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getLocation().getWorld()))
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

            if (plugin.getSettingsManager().isUnbreakableType(block) && plugin.getUnbreakableManager().isUnbreakable(block))
            {
                block.setTypeIdAndData(0, (byte) 0, false);
                revert.add(new BlockData(block));
                continue;
            }

            // prevent block break if breaking field

            if (plugin.getForceFieldManager().isField(block))
            {
                block.setTypeIdAndData(0, (byte) 0, false);
                revert.add(new BlockData(block));
                continue;
            }

            // prevent explosion if explosion protected

            if (plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.PREVENT_EXPLOSIONS) != null)
            {
                event.setCancelled(true);
                break;
            }

            // record the blocks that are in undo fields

            Field field = plugin.getForceFieldManager().getSourceField(block.getLocation(), FieldFlag.GRIEF_UNDO_INTERVAL, FieldFlag.GRIEF_UNDO_REQUEST);

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
                        griefed.add(new BlockData(block));
                        block.setTypeId(0);
                    }
                }

                if (griefed.size() > 0)
                {
                    plugin.getStorageManager().offerGrief(field);
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

        if (plugin.getSettingsManager().isDebug())
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

        // pvp protect against player

        if (event instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;

            Player victim = (Player) sub.getEntity();
            Player attacker = null;

            if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Player)
            {
                attacker = (Player) sub.getDamager();
            }

            if (sub.getEntity() instanceof Player && sub.getDamager() instanceof Arrow)
            {
                Arrow arrow = (Arrow) sub.getDamager();
                attacker = (Player) arrow.getShooter();
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

        // pvp protect against entity attacks

        if (event.getCause().equals(DamageCause.ENTITY_ATTACK))
        {
            Field field = plugin.getForceFieldManager().getSourceField(event.getEntity().getLocation(), FieldFlag.PREVENT_MOB_DAMAGE);

            if (field != null)
            {
                event.setCancelled(true);
            }
        }

        if (plugin.getSettingsManager().isDebug())
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
     *
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
     *
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
