package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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
            public void run()
            {
                if (processing)
                {
                    return;
                }

                processing = true;

                HashMap<String, EntryFields> e = getEntries();

                for (String playerName : e.keySet())
                {
                    EntryFields ef = e.get(playerName);
                    List<Field> fields = ef.getFields();

                    boolean hasDamage = false;
                    boolean hasHeal = false;
                    boolean hasFeeding = false;
                    boolean hasAir = false;
                    boolean hasRepair = false;

                    for (Field field : fields)
                    {
                        FieldSettings fs = field.getSettings();

                        Player player = Helper.matchSinglePlayer(playerName);

                        if (player == null)
                        {
                            continue;
                        }

                        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.giveair"))
                        {
                            if (!hasAir)
                            {
                                if (fs.hasFlag(FieldFlag.GIVE_AIR))
                                {
                                    if (player.getRemainingAir() < 300)
                                    {
                                        player.setRemainingAir(600);
                                        plugin.getCommunicationManager().showGiveAir(player);
                                        hasAir = true;
                                        continue;
                                    }
                                }
                            }
                        }

                        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.feed"))
                        {
                            if (!hasFeeding)
                            {
                                if (fs.hasFlag(FieldFlag.SLOW_FEEDING))
                                {
                                    int food = player.getFoodLevel();
                                    if (food < 20)
                                    {
                                        player.setFoodLevel(food + 1);
                                        plugin.getCommunicationManager().showSlowFeeding(player);
                                        hasFeeding = true;
                                        continue;
                                    }
                                }
                            }
                        }

                        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.heal"))
                        {
                            if (!hasHeal)
                            {
                                if (fs.hasFlag(FieldFlag.INSTANT_HEAL))
                                {
                                    if (player.getHealth() < 20)
                                    {
                                        player.setHealth(20);
                                        plugin.getCommunicationManager().showInstantHeal(player);
                                        hasHeal = true;
                                        continue;
                                    }
                                }

                                if (fs.hasFlag(FieldFlag.SLOW_HEAL))
                                {
                                    if (player.getHealth() < 20)
                                    {
                                        player.setHealth(healthCheck(player.getHealth() + 1));
                                        plugin.getCommunicationManager().showSlowHeal(player);
                                        hasHeal = true;
                                        continue;
                                    }

                                }
                            }
                        }

                        if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.repair"))
                        {
                            if (!hasRepair)
                            {
                                if (fs.hasFlag(FieldFlag.SLOW_REPAIR))
                                {
                                    boolean updated = false;

                                    ItemStack[] armors = player.getInventory().getArmorContents();
                                    for (ItemStack armor : armors)
                                    {
                                        if (plugin.getSettingsManager().isRepairableItemType(armor.getTypeId()))
                                        {
                                            short dur = armor.getDurability();
                                            if (dur > 0)
                                            {
                                                dur -= 25;
                                                if (dur < 0)
                                                {
                                                    dur = 0;
                                                }
                                                armor.setDurability(dur);
                                                plugin.getCommunicationManager().showSlowRepair(player);
                                                updated = true;
                                                hasRepair = true;
                                                break;
                                            }
                                        }
                                    }

                                    if (updated)
                                    {
                                        continue;
                                    }

                                    ItemStack[] items = player.getInventory().getContents();
                                    for (ItemStack item : items)
                                    {
                                        if (item != null)
                                        {
                                            if (plugin.getSettingsManager().isRepairableItemType(item.getTypeId()))
                                            {
                                                short dur = item.getDurability();
                                                if (dur > 0)
                                                {
                                                    dur -= 25;
                                                    if (dur < 0)
                                                    {
                                                        dur = 0;
                                                    }
                                                    item.setDurability(dur);
                                                    plugin.getCommunicationManager().showSlowRepair(player);
                                                    updated = true;
                                                    hasRepair = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (updated)
                                    {
                                        continue;
                                    }
                                }
                            }
                        }

                        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.damage"))
                        {
                            if (!(plugin.getSettingsManager().isSneakingBypassesDamage() && player.isSneaking()))
                            {
                                if (!plugin.getForceFieldManager().isAllowed(field, playerName))
                                {
                                    if (!hasDamage)
                                    {
                                        if (fs.hasFlag(FieldFlag.SLOW_DAMAGE))
                                        {
                                            if (player.getHealth() > 0)
                                            {
                                                player.setHealth(healthCheck(player.getHealth() - 4));
                                                plugin.getCommunicationManager().showSlowDamage(player);
                                                hasDamage = true;
                                                continue;
                                            }
                                        }

                                        if (fs.hasFlag(FieldFlag.FAST_DAMAGE))
                                        {
                                            if (player.getHealth() > 0)
                                            {
                                                player.setHealth(healthCheck(player.getHealth() - 8));
                                                plugin.getCommunicationManager().showFastDamage(player);
                                                hasDamage = true;
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
     *
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
     *
     * @param player
     * @param entryField
     */
    public void leaveOverlappedArea(Player player, Field entryField)
    {
        FieldSettings fs = entryField.getSettings();

        if (fs.hasFlag(FieldFlag.FAREWELL_MESSAGE) && entryField.getName().length() > 0)
        {
            plugin.getCommunicationManager().showFarewellMessage(player, entryField.getName());
        }
    }

    /**
     * @param entity
     * @param field
     */
    public void enterField(Entity entity, Field field)
    {
        if (!(entity instanceof Player))
        {
            return;
        }

        Player player = (Player) entity;

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
            plugin.getVelocityManager().launchPlayer(player, field);
            plugin.getVelocityManager().shootPlayer(player, field);
        }
        if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.damage"))
        {
            if (!(plugin.getSettingsManager().isSneakingBypassesDamage() && player.isSneaking()))
            {
                plugin.getMineManager().enterMine(player, field);
                plugin.getLightningManager().enterLightning(player, field);
            }
        }
    }

    /**
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
     *
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
     * @param field
     * @return
     */
    public HashSet<String> getInhabitants(Field field)
    {
        HashSet<String> inhabitants = new HashSet<String>();

        synchronized (entries)
        {
            for (String playerName : entries.keySet())
            {
                EntryFields ef = entries.get(playerName);
                List<Field> fields = ef.getFields();

                for (Field testfield : fields)
                {
                    if (field.equals(testfield))
                    {
                        inhabitants.add(playerName);
                    }
                }
            }
        }

        return inhabitants;
    }

    /**
     * Returns players that are standing on Redstone triggerable fields
     *
     * @param block
     * @return
     */
    public Map<String, Field> getTriggerableEntryPlayers(Block block)
    {
        Map<String, Field> players = new HashMap<String, Field>();

        synchronized (entries)
        {
            for (String playerName : entries.keySet())
            {
                EntryFields ef = entries.get(playerName);
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

                    players.put(playerName, field);
                }
            }
        }

        return players;
    }

    /**
     * Whether the redstone source powers the field
     *
     * @param field
     * @param block
     * @return confirmation
     */
    public boolean powersField(Field field, Block block)
    {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP};

        for (BlockFace face : faces)
        {
            Block faceblock = block.getRelative(face);

            if (field.getX() == faceblock.getX() && field.getY() == faceblock.getY() && field.getZ() == faceblock.getZ())
            {
                return true;
            }
        }

        BlockFace[] downfaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

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

    /**
     * @return the entries
     */
    public HashMap<String, EntryFields> getEntries()
    {
        HashMap<String, EntryFields> e = new HashMap<String, EntryFields>();
        synchronized (entries)
        {
            e.putAll(entries);
        }
        return e;
    }
}
