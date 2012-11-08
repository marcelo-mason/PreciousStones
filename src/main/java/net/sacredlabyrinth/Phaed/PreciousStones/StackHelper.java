package net.sacredlabyrinth.Phaed.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockTypeEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PaymentEntry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StackHelper
{
    public static void remove(Player player, BlockTypeEntry item, int amount)
    {
        for (ItemStack stack : makeStacks(item, amount))
        {
            player.getInventory().removeItem(stack);
        }
        player.updateInventory();
    }

    public static PaymentEntry give(Player player, BlockTypeEntry item, int amount)
    {
        List<ItemStack> remainder = new ArrayList<ItemStack>();

        for (ItemStack stack : makeStacks(item, amount))
        {
            HashMap<Integer, ItemStack> rem = player.getInventory().addItem(stack);

            if (rem != null && !rem.isEmpty())
            {
                remainder.addAll(rem.values());
            }
        }

        player.updateInventory();

        if (remainder.isEmpty())
        {
            return null;
        }

        return packRemainder(remainder);
    }

    private static PaymentEntry packRemainder(List<ItemStack> stacks)
    {
        PaymentEntry entry = new PaymentEntry();

        entry.setItem(new BlockTypeEntry(stacks.get(0).getTypeId(), stacks.get(0).getData().getData()));

        int amount = 0;

        for (ItemStack stack : stacks)
        {
            amount += stack.getAmount();
        }

        entry.setAmount(amount);

        return entry;
    }

    public static List<ItemStack> makeStacks(BlockTypeEntry item, int amount)
    {
        List<ItemStack> out = new ArrayList<ItemStack>();

        Material material = Material.getMaterial(item.getTypeId());

        int blocks = amount / 64;

        for (int i = 0; i < blocks; i++)
        {
            ItemStack is = new ItemStack(material, 64);
            is.setDurability(item.getData());
            out.add(is);
        }

        int remainder = amount % 64;

        if (remainder > 0)
        {
            ItemStack is = new ItemStack(material, remainder);
            is.setDurability(item.getData());
            out.add(is);
        }

        return out;
    }

    public static boolean hasItems(Player player, BlockTypeEntry item, int amount)
    {
        for (ItemStack i : player.getInventory())
        {
            if (i == null)
            {
                continue;
            }

            if (i.getTypeId() == item.getTypeId())
            {
                if (item.getData() == 0 || i.getData().getData() == item.getData())
                {
                    amount -= i.getAmount();
                }
            }
        }

        if (amount <= 0)
        {
            return true;
        }

        return false;
    }
}
