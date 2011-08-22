package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.EntryFields;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

/**
 * Handles what happens inside fields
 *
 * @author Phaed
 */
public final class EntryManager
{
    private PreciousStones plugin;
    private final HashMap<String, EntryFields> entries = new HashMap<String, EntryFields>();
    private boolean processing = false;

    /**
     *
     * @param plugin
     */
    public EntryManager()
    {
        plugin = PreciousStones.getInstance();

        startScheduler();
    }

    private void startScheduler()
    {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                if (processing)
                {
                    return;
                }

                processing = true;

                synchronized (entries)
                {
                    for (String playername : entries.keySet())
                    {
                        EntryFields ef = entries.get(playername);
                        List<Field> fields = ef.getFields();

                        for (Field field : fields)
                        {
                            FieldSettings fs = field.getSettings();

                            Player player = Helper.matchSinglePlayer(playername);

                            if (player == null)
                            {
                                continue;
                            }

                            if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.benefit.giveair"))
                            {
                                if (fs.hasFlag(FieldFlag.GIVE_AIR))
                                {
                                    if (player.getRemainingAir() < 300)
                                    {
                                        player.setRemainingAir(600);
                                        plugin.getCommunicationManager().showGiveAir(player);
                                        continue;
                                    }
                                }
                            }

                            if (plugin.getPermissionsManager().hasPermission(player, "preciousstones.benefit.heal"))
                            {
                                if (fs.hasFlag(FieldFlag.INSTANT_HEAL))
                                {
                                    if (player.getHealth() < 20)
                                    {
                                        player.setHealth(20);
                                        plugin.getCommunicationManager().showInstantHeal(player);
                                        continue;
                                    }
                                }

                                if (fs.hasFlag(FieldFlag.SLOW_HEAL))
                                {
                                    if (player.getHealth() < 20)
                                    {
                                        player.setHealth(healthCheck(player.getHealth() + 1));
                                        plugin.getCommunicationManager().showSlowHeal(player);
                                        continue;
                                    }

                                }
                            }

                            if (!plugin.getPermissionsManager().hasPermission(player, "preciousstones.bypass.damage"))
                            {
                                if (!(plugin.getSettingsManager().isSneakingBypassesDamage() && player.isSneaking()))
                                {
                                    if (!plugin.getForceFieldManager().isAllowed(field, playername))
                                    {
                                        if (fs.hasFlag(FieldFlag.SLOW_DAMAGE))
                                        {
                                            if (player.getHealth() > 0)
                                            {
                                                player.setHealth(healthCheck(player.getHealth() - 1));
                                                plugin.getCommunicationManager().showSlowDamage(player);
                                                continue;
                                            }
                                        }

                                        if (fs.hasFlag(FieldFlag.FAST_DAMAGE))
                                        {
                                            if (player.getHealth() > 0)
                                            {
                                                player.setHealth(healthCheck(player.getHealth() - 4));
                                                plugin.getCommunicationManager().showFastDamage(player);
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                processing = false;
            }
        }, 0, 20L);
    }

    /**
     *
     * @param player
     * @return
     */
    public List<Field> getPlayerEntryFields(Player player)
    {
        synchronized (entries)
        {
            EntryFields ef = entries.get(player.getName());

            if (ef != null)
            {
                List<Field> e = new ArrayList<Field>();
                e.addAll(ef.getFields());
                return e;
            }
        }

        return null;
    }

    /**
     * Runs when a player enters an overlapped area
     * @param player
     * @param field
     */
    public void enterOverlappedArea(Player player, Field field)
    {
        FieldSettings fs = field.getSettings();

        if (fs.hasFlag(FieldFlag.WELCOME_MESSAGE) && field.getName().length() > 0)
        {
            plugin.getCommunicationManager().showWelcomeMessage(player, field.getName());
        }

        if (fs.hasFlag(FieldFlag.ENTRY_ALERT))
        {
            if (!plugin.getForceFieldManager().isAllowed(field, player.getName()))
            {
                plugin.getForceFieldManager().announceAllowedPlayers(field, Helper.capitalize(player.getName()) + " has triggered an entry alert at " + field.getName() + " " + ChatColor.DARK_GRAY + field.getCoords());
            }
        }
    }

    /**
     * Runs when a player leaves an overlapped area
     * @param player
     * @param entryField
     */
    public void leaveOverlappedArea(Player player, Field entryField)
    {
        FieldSettings fs = entryField.getSettings();

        if (fs.hasFlag(FieldFlag.WELCOME_MESSAGE) && entryField.getName().length() > 0)
        {
            plugin.getCommunicationManager().showFarewellMessage(player, entryField.getName());
        }
    }

    /**
     *
     * @param entity
     * @param field
     */
    public void enterField(Entity entity, Field field)
    {
        Player player = null;
        Vehicle vehicle = null;

        if (entity instanceof Player)
        {
            player = (Player) entity;
        }

        if (entity instanceof Vehicle)
        {
            vehicle = (Vehicle) entity;
            Entity e = vehicle.getPassenger();

            if (e instanceof Player)
            {
                player = (Player) e;
            }
        }

        if (player == null)
        {
            return;
        }

        synchronized (entries)
        {
            EntryFields ef = entries.get(player.getName());

            if (ef != null)
            {
                ef.addField(field);
            }
            else
            {
                entries.put(player.getName(), new EntryFields(field));
            }
        }

        // entry actions

        plugin.getSnitchManager().recordSnitchEntry(player, field);

        if (!plugin.getForceFieldManager().isRedstoneHookedDisabled(field))
        {
            if (vehicle != null)
            {
                plugin.getVelocityManager().launchPlayer(vehicle, field);
                plugin.getVelocityManager().shootPlayer(vehicle, field);
            }
            else
            {
                plugin.getVelocityManager().launchPlayer(player, field);
                plugin.getVelocityManager().shootPlayer(player, field);
            }
        }

        plugin.getMineManager().enterMine(player, field);
        plugin.getLightningManager().enterLightning(player, field);
    }

    /**
     *
     * @param player
     * @param field
     */
    public void leaveField(Player player, Field field)
    {
        synchronized (entries)
        {
            EntryFields ef = entries.get(player.getName());
            ef.removeField(field);

            if (ef.size() == 0)
            {
                entries.remove(player.getName());
            }
        }
    }

    /**
     * Remove a player from all fields (used on death)
     * @param player
     */
    public void leaveAllFields(Player player)
    {
        synchronized (entries)
        {
            entries.remove(player.getName());
        }
    }

    /**
     *
     * @param player
     * @param field
     * @return
     */
    public boolean enteredField(Player player, Field field)
    {
        synchronized (entries)
        {
            EntryFields ef = entries.get(player.getName());

            if (ef == null)
            {
                return false;
            }

            return ef.containsField(field);
        }
    }

    /**
     *
     * @param player
     * @param field
     * @return
     */
    public boolean containsSameNameOwnedField(Player player, Field field)
    {
        synchronized (entries)
        {
            EntryFields ef = entries.get(player.getName());

            if (ef != null)
            {
                List<Field> entryfields = ef.getFields();

                for (Field entryfield : entryfields)
                {
                    if (entryfield.getOwner().equals(field.getOwner()) && entryfield.getName().equals(field.getName()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int healthCheck(int health)
    {
        if (health < 0)
        {
            return 0;
        }

        if (health > 20)
        {
            return 20;
        }

        return health;
    }

    /**
     *
     * @param field
     * @return
     */
    public HashSet<String> getInhabitants(Field field)
    {
        HashSet<String> inhabitants = new HashSet<String>();

        synchronized (entries)
        {
            for (String playername : entries.keySet())
            {
                EntryFields ef = entries.get(playername);
                List<Field> fields = ef.getFields();

                for (Field testfield : fields)
                {
                    if (field.equals(testfield))
                    {
                        inhabitants.add(playername);
                    }
                }
            }
        }

        return inhabitants;
    }

    /**
     * Returns players that are standing on Redstone triggerable fields
     * @param player
     * @return
     */
    public Map<String, Field> getTriggerableEntryPlayers(Block block)
    {
        Map<String, Field> players = new HashMap<String, Field>();

        synchronized (entries)
        {
            for (String playername : entries.keySet())
            {
                EntryFields ef = entries.get(playername);
                List<Field> fields = ef.getFields();

                for (Field field : fields)
                {
                    FieldSettings fs = field.getSettings();

                    if (fs.hasVeocityFlag())
                    {
                        continue;
                    }

                    if (!powersField(field, block))
                    {
                        continue;
                    }

                    players.put(playername, field);
                }
            }
        }

        return players;
    }

    /**
     * Whether the redstone source powers the field
     * @param block
     * @return confirmation
     */
    public boolean powersField(Field field, Block block)
    {
        BlockFace[] faces =
        {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP
        };

        for (BlockFace face : faces)
        {
            Block faceblock = block.getRelative(face);

            if (field.getX() == faceblock.getX() && field.getY() == faceblock.getY() && field.getZ() == faceblock.getZ())
            {
                return true;
            }
        }

        BlockFace[] downfaces =
        {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
        };

        Block upblock = block.getRelative(BlockFace.DOWN);

        for (BlockFace face : downfaces)
        {
            Block faceblock = upblock.getRelative(face);

            if (field.getX() == faceblock.getX() && field.getY() == faceblock.getY() && field.getZ() == faceblock.getZ())
            {
                return true;
            }
        }

        return false;
    }
}
