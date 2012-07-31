package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

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
    private final HashMap<String, EntryFields> updatableEntries = new HashMap<String, EntryFields>();
    private int updateCount = 0;

    /**
     *
     */
    public EntryManager()
    {
        plugin = PreciousStones.getInstance();
        scheduleNextUpdate();
    }

    private boolean isUpdatable(Field field)
    {
        return field.hasFlag(FieldFlag.DAMAGE) ||
                field.hasFlag(FieldFlag.REPAIR) ||
                field.hasFlag(FieldFlag.HEAL) ||
                field.hasFlag(FieldFlag.FEED) ||
                field.hasFlag(FieldFlag.POTIONS) ||
                field.hasFlag(FieldFlag.NEUTRALIZE_POTIONS) ||
                field.hasFlag(FieldFlag.CONFISCATE_ITEMS) ||
                field.hasFlag(FieldFlag.AIR);
    }

    private void scheduleNextUpdate()
    {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Update(), 20L);
    }

    private class Update implements Runnable
    {
        public void run()
        {
            synchronized (entries)
            {
                doEffects();
            }
            scheduleNextUpdate();

            updateCount++;

            if (updateCount > 214783640)
            {
                updateCount = 0;
            }
        }
    }

    private void doEffects()
    {
        for (String playerName : updatableEntries.keySet())
        {
            Player player = Helper.matchSinglePlayer(playerName);

            if (player == null)
            {
                continue;
            }

            EntryFields ef = updatableEntries.get(playerName);
            List<Field> fields = ef.getFields();

            boolean hasDamage = false;
            boolean hasHeal = false;
            boolean hasFeeding = false;
            boolean hasAir = false;
            boolean hasRepair = false;
            boolean hasPotion = false;

            for (Field field : fields)
            {
                // disabled fields shouldn't be doing things

                if (field.isDisabled())
                {
                    continue;
                }

                boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, playerName);

                // check players inventories for items to confiscate every five seconds

                if (updateCount % 5 == 0)
                {
                    if (field.hasFlag(FieldFlag.CONFISCATE_ITEMS))
                    {
                        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            plugin.getConfiscationManager().confiscateItems(field, player);
                        }
                    }
                }


                if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.giveair"))
                {
                    if (!hasAir)
                    {
                        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            if (field.hasFlag(FieldFlag.AIR))
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
                }

                if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.feed"))
                {
                    if (!hasFeeding)
                    {
                        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            if (field.hasFlag(FieldFlag.FEED))
                            {
                                int food = player.getFoodLevel();
                                if (food < 20)
                                {
                                    player.setFoodLevel(food + field.getSettings().getFeed());
                                    plugin.getCommunicationManager().showSlowFeeding(player);
                                    hasFeeding = true;
                                    continue;
                                }
                            }
                        }
                    }
                }

                if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.heal"))
                {
                    if (!hasHeal)
                    {
                        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            if (field.hasFlag(FieldFlag.HEAL))
                            {
                                if (player.getHealth() < 20 && player.getHealth() > 0)
                                {
                                    player.setHealth(healthCheck(player.getHealth() + field.getSettings().getHeal()));
                                    plugin.getCommunicationManager().showSlowHeal(player);
                                    hasHeal = true;
                                    continue;
                                }

                            }
                        }
                    }
                }

                if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.repair"))
                {
                    if (!hasRepair)
                    {
                        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            if (field.hasFlag(FieldFlag.REPAIR))
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
                                            dur -= field.getSettings().getRepair();
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
                                                dur -= field.getSettings().getRepair();
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
                }

                if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.damage"))
                {
                    if (!(field.hasFlag(FieldFlag.SNEAKING_BYPASS) && player.isSneaking()))
                    {
                        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            if (!hasDamage)
                            {
                                if (field.hasFlag(FieldFlag.DAMAGE))
                                {
                                    if (player.getHealth() > 0)
                                    {
                                        int health = healthCheck(player.getHealth() - field.getSettings().getDamage());
                                        player.setHealth(health);

                                        if (health <= 0)
                                        {
                                            player.playEffect(EntityEffect.DEATH);
                                        }
                                        plugin.getCommunicationManager().showSlowDamage(player);
                                        hasDamage = true;
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }

                if (plugin.getPermissionsManager().has(player, "preciousstones.benefit.potions"))
                {
                    if (!hasPotion)
                    {
                        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                        {
                            if (field.hasFlag(FieldFlag.POTIONS))
                            {
                                plugin.getPotionManager().applyPotions(player, field);
                                hasPotion = true;
                                continue;
                            }
                        }
                    }
                }

                if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
                {
                    if (field.hasFlag(FieldFlag.NEUTRALIZE_POTIONS))
                    {
                        plugin.getPotionManager().neutralizePotions(player, field);
                    }
                }
            }
        }
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
        boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

        if (field.hasFlag(FieldFlag.WELCOME_MESSAGE))
        {
            plugin.getCommunicationManager().showWelcomeMessage(player, field);
        }

        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            if (field.getSettings().getGroupOnEntry() != null)
            {
                plugin.getPermissionsManager().addGroup(player, field.getSettings().getGroupOnEntry());
            }
        }

        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            if (field.hasFlag(FieldFlag.CONFISCATE_ITEMS))
            {
                plugin.getConfiscationManager().confiscateItems(field, player);
            }
        }

        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            if (field.getSettings().getForceEntryGameMode() != null)
            {
                player.setGameMode(field.getSettings().getForceEntryGameMode());
            }

            if (field.hasFlag(FieldFlag.PREVENT_FLIGHT))
            {
                if (plugin.getSettingsManager().isNotifyFlyZones())
                {
                    ChatBlock.sendMessage(player, ChatColor.YELLOW + "Entering no fly zone");
                }

                player.setAllowFlight(false);
            }

            if (field.hasFlag(FieldFlag.ENTRY_ALERT))
            {
                if (!field.hasFlag(FieldFlag.SNEAKING_BYPASS) || !player.isSneaking())
                {
                    plugin.getForceFieldManager().announceAllowedPlayers(field, Helper.capitalize(player.getName()) + " entry at " + field.getName() + " " + ChatColor.DARK_GRAY + field.getCoords());
                }
            }
        }
    }

    /**
     * Runs when a player leaves an overlapped area
     *
     * @param player
     * @param entryField
     */
    public void leaveOverlappedArea(Player player, Field field)
    {
        boolean allowed = plugin.getForceFieldManager().isApplyToAllowed(field, player.getName());

        if (field.hasFlag(FieldFlag.FAREWELL_MESSAGE))
        {
            plugin.getCommunicationManager().showFarewellMessage(player, field);
        }

        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            if (field.hasFlag(FieldFlag.CONFISCATE_ITEMS))
            {
                plugin.getConfiscationManager().returnItems(player);
            }
        }

        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            if (field.getSettings().getGroupOnEntry() != null)
            {
                plugin.getPermissionsManager().removeGroup(player, field.getSettings().getGroupOnEntry());
            }
        }

        if (allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            if (field.hasFlag(FieldFlag.POTIONS))
            {
                HashMap<PotionEffectType, Integer> potions = field.getSettings().getPotions();

                for (PotionEffectType pot : potions.keySet())
                {
                    player.removePotionEffect(pot);
                }
            }
        }

        if (!allowed || field.hasFlag(FieldFlag.APPLY_TO_ALL))
        {
            if (field.getSettings().getForceLeavingGameMode() != null)
            {
                player.setGameMode(field.getSettings().getForceLeavingGameMode());
            }

            if (field.hasFlag(FieldFlag.PREVENT_FLIGHT))
            {
                Field sub = plugin.getForceFieldManager().getEnabledSourceField(player.getLocation(), FieldFlag.PREVENT_FLIGHT);

                if (sub == null)
                {
                    if (plugin.getSettingsManager().isNotifyFlyZones())
                    {
                        ChatBlock.sendMessage(player, ChatColor.YELLOW + "Leaving no fly zone");
                    }

                    player.setAllowFlight(true);
                }
            }
        }
    }

    /**
     * @param entity
     * @param field
     */
    public void enterField(Player player, Field field)
    {
        PreciousStones.debug(player.getName() + " entered a " + field.getSettings().getTitle() + " field");

        EntryFields newEntryField = new EntryFields(field);

        synchronized (entries)
        {
            EntryFields ef = entries.get(player.getName());

            if (ef != null)
            {
                ef.addField(field);
            }
            else
            {
                entries.put(player.getName(), newEntryField);
            }
        }

        if (isUpdatable(field))
        {
            synchronized (entries)
            {
                EntryFields ef = updatableEntries.get(player.getName());

                if (ef != null)
                {
                    ef.addField(field);
                }
                else
                {
                    updatableEntries.put(player.getName(), newEntryField);
                }
            }
        }

        // entry actions

        if (!field.isDisabled())
        {
            plugin.getSnitchManager().recordSnitchEntry(player, field);

            if (!(field.hasFlag(FieldFlag.SNEAKING_BYPASS) && player.isSneaking()))
            {
                plugin.getVelocityManager().launchPlayer(player, field);
                plugin.getVelocityManager().shootPlayer(player, field);
            }

            if (!plugin.getPermissionsManager().has(player, "preciousstones.bypass.damage"))
            {
                if (!(field.hasFlag(FieldFlag.SNEAKING_BYPASS) && player.isSneaking()))
                {
                    plugin.getMineManager().enterMine(player, field);
                    plugin.getLightningManager().enterLightning(player, field);
                }
            }
        }

        if (field.hasFlag(FieldFlag.MASK_ON_DISABLED))
        {
            if (field.isDisabled())
            {
                field.mask(player);
            }
            else
            {
                field.unmask(player);
            }
        }
    }

    /**
     * @param player
     * @param field
     */
    public void leaveField(Player player, Field field)
    {
        if (field == null)
        {
            return;
        }

        PreciousStones.debug(player.getName() + " left a " + field.getSettings().getTitle() + " field");

        synchronized (entries)
        {
            EntryFields ef = entries.get(player.getName());
            ef.removeField(field);

            if (ef.size() == 0)
            {
                entries.remove(player.getName());
            }
        }

        synchronized (entries)
        {
            EntryFields ef = updatableEntries.get(player.getName());
            if (ef != null)
            {
                ef.removeField(field);
                if (ef.size() == 0)
                {
                    updatableEntries.remove(player.getName());
                }
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
        // remove player from all entered fields

        synchronized (entries)
        {
            if (entries.containsKey(player.getName()))
            {
                EntryFields entryFields = entries.get(player.getName());

                for (Field field : entryFields.getFields())
                {
                    leaveOverlappedArea(player, field);
                }

                entries.remove(player.getName());
            }
        }

        synchronized (entries)
        {
            if (updatableEntries.containsKey(player.getName()))
            {
                updatableEntries.remove(player.getName());
            }
        }

        // remove player from all entry groups

        List<String> allEntryGroups = plugin.getSettingsManager().getAllEntryGroups();

        for (String group : allEntryGroups)
        {
            plugin.getPermissionsManager().removeGroup(player, group);
        }
    }

    /**
     * Remove all players from field
     *
     * @param player
     */
    public void removeAllPlayers(Field field)
    {
        synchronized (entries)
        {
            for (String playerName : entries.keySet())
            {
                Player player = Helper.matchSinglePlayer(playerName);

                if (player == null)
                {
                    continue;
                }

                EntryFields ef = entries.get(playerName);
                List<Field> fields = ef.getFields();

                for (Iterator iter = fields.iterator(); iter.hasNext(); )
                {
                    Field testfield = (Field) iter.next();

                    if (field.equals(testfield))
                    {
                        iter.remove();
                        leaveOverlappedArea(player, field);
                    }
                }
            }

            for (String playerName : updatableEntries.keySet())
            {
                Player player = Helper.matchSinglePlayer(playerName);

                if (player == null)
                {
                    continue;
                }

                EntryFields ef = updatableEntries.get(playerName);
                List<Field> fields = ef.getFields();

                for (Iterator iter = fields.iterator(); iter.hasNext(); )
                {
                    Field testfield = (Field) iter.next();

                    if (field.equals(testfield))
                    {
                        iter.remove();
                        leaveOverlappedArea(player, field);
                    }
                }
            }
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
                    if (entryfield.getOwner().equals(field.getOwner()) && entryfield.getName().equals(field.getName()) && entryfield.getType().equals(field.getType()))
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
    public boolean isInhabitant(Field field, String playerName)
    {
        HashSet<String> inhabitants = new HashSet<String>();

        synchronized (entries)
        {
            for (String entrantName : entries.keySet())
            {
                EntryFields ef = entries.get(entrantName);
                List<Field> fields = ef.getFields();

                return fields.contains(field);
            }
        }

        return false;
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
     * @return the entries
     */
    public HashMap<String, EntryFields> getEntries()
    {
        synchronized (entries)
        {
            return entries;
        }
    }
}
