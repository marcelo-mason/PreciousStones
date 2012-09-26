/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.ChatBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.ItemStackEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author telaeris
 */
public class ConfiscationManager
{
    private PreciousStones plugin;

    /**
     *
     */
    public ConfiscationManager()
    {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Confiscates items from a player and places them on a chest on top of the field
     *
     * @param player
     */
    public void confiscateItems(Field field, Player player)
    {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();

        ItemStackEntry helmet = null;
        ItemStackEntry chestplate = null;
        ItemStackEntry leggings = null;
        ItemStackEntry boots = null;

        List<ItemStackEntry> confiscated = new ArrayList<ItemStackEntry>();

        for (ItemStack stack : contents)
        {
            if (stack != null && stack.getTypeId() > 0)
            {
                if (!field.getSettings().canCarry(stack.getTypeId(), stack.getData().getData()))
                {
                    // remove item from inventory

                    inventory.removeItem(stack);

                    // add item to confiscated list

                    confiscated.add(new ItemStackEntry(stack));
                }
            }
        }

        ItemStack item = inventory.getHelmet();

        if (item != null)
        {
            if (!field.getSettings().canCarry(item.getTypeId(), item.getData().getData()))
            {
                // add item to confiscated list
                helmet = new ItemStackEntry(item);

                // remove item
                inventory.setHelmet(new ItemStack(0));
            }
        }

        item = inventory.getChestplate();

        if (item != null)
        {
            if (!field.getSettings().canCarry(item.getTypeId(), item.getData().getData()))
            {
                // add item to confiscated list
                chestplate = new ItemStackEntry(item);

                // remove item
                inventory.setChestplate(new ItemStack(0));
            }
        }

        item = inventory.getLeggings();

        if (item != null)
        {
            if (!field.getSettings().canCarry(item.getTypeId(), item.getData().getData()))
            {
                // add item to confiscated list
                leggings = new ItemStackEntry(item);

                // remove item
                inventory.setLeggings(new ItemStack(0));
            }
        }

        item = inventory.getBoots();

        if (item != null)
        {
            if (!field.getSettings().canCarry(item.getTypeId(), item.getData().getData()))
            {
                // add item to confiscated list
                boots = new ItemStackEntry(item);

                // remove item
                inventory.setBoots(new ItemStack(0));
            }
        }

        if (!confiscated.isEmpty() || helmet != null || chestplate != null || leggings != null || boots != null)
        {
            PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player.getName());
            entry.confiscate(confiscated, helmet, chestplate, leggings, boots);
            plugin.getStorageManager().updatePlayer(player.getName());
            player.updateInventory();

            String msg = "";

            for (ItemStackEntry e : confiscated)
            {
                msg += e.getAmount() + " " + Helper.friendlyBlockType(Material.getMaterial(e.getTypeId()).name()) + ", ";
            }

            if (helmet != null)
            {
                msg += "1 " + Helper.friendlyBlockType(Material.getMaterial(helmet.getTypeId()).name()) + ", ";
            }

            if (chestplate != null)
            {
                msg += "1 " + Helper.friendlyBlockType(Material.getMaterial(chestplate.getTypeId()).name()) + ", ";
            }

            if (leggings != null)
            {
                msg += "1 " + Helper.friendlyBlockType(Material.getMaterial(leggings.getTypeId()).name()) + ", ";
            }

            if (boots != null)
            {
                msg += "1 " + Helper.friendlyBlockType(Material.getMaterial(boots.getTypeId()).name()) + ", ";
            }

            msg = Helper.stripTrailing(msg, ", ");

            PreciousStones.log("Confiscated {1.items} from {2.player} at {3.coords}", msg, player.getName(), field.toString());
            ChatBlock.send(player, "{aqua}Confiscated: {white}{1.image}", msg);
        }
    }

    /**
     * Returns confiscated items to the player
     *
     * @param player
     */
    public void returnItems(Player player)
    {
        PlayerInventory inventory = player.getInventory();

        if (inventory == null)
        {
            return;
        }

        PlayerEntry entry = plugin.getPlayerManager().getPlayerEntry(player.getName());

        List<ItemStackEntry> confiscated = entry.returnInventory();
        ItemStackEntry helmet = entry.returnHelmet();
        ItemStackEntry chestplate = entry.returnChestplate();
        ItemStackEntry leggings = entry.returnLeggings();
        ItemStackEntry boots = entry.returnBoots();

        plugin.getStorageManager().updatePlayer(player.getName());
        player.updateInventory();

        if (helmet != null)
        {
            inventory.setHelmet(helmet.toItemStack());
        }

        if (chestplate != null)
        {
            inventory.setChestplate(chestplate.toItemStack());
        }

        if (leggings != null)
        {
            inventory.setLeggings(leggings.toItemStack());
        }

        if (boots != null)
        {
            inventory.setBoots(boots.toItemStack());
        }

        for (ItemStackEntry item : confiscated)
        {
            inventory.addItem(item.toItemStack());
        }

        if (!confiscated.isEmpty() || helmet != null || chestplate != null || leggings != null || boots != null)
        {
            String msg = "";

            for (ItemStackEntry e : confiscated)
            {
                msg += e.getAmount() + " " + Helper.friendlyBlockType(Material.getMaterial(e.getTypeId()).name()) + ", ";
            }

            if (helmet != null)
            {
                msg += "1 " + Helper.friendlyBlockType(Material.getMaterial(helmet.getTypeId()).name()) + ", ";
            }

            if (chestplate != null)
            {
                msg += "1 " + Helper.friendlyBlockType(Material.getMaterial(chestplate.getTypeId()).name()) + ", ";
            }

            if (leggings != null)
            {
                msg += "1 " + Helper.friendlyBlockType(Material.getMaterial(leggings.getTypeId()).name()) + ", ";
            }

            if (boots != null)
            {
                msg += "1 " + Helper.friendlyBlockType(Material.getMaterial(boots.getTypeId()).name()) + ", ";
            }

            msg = Helper.stripTrailing(msg, ", ");

            PreciousStones.log("Returned {1.items} to {2.player}", msg, player.getName());
            ChatBlock.send(player, "{aqua}Returned: {white}{1.items}", msg);
        }
    }
}
