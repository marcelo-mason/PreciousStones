package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

/**
 * @author spacemoose (daniel@errortown.com)
 */

public final class VaultManager
{
    private static Economy economy;

    public boolean hasEconomy()
    {
        if (economy != null && economy.isEnabled())
        {
            return true;
        }
        return false;
    }

    public boolean playerCharge(Player player, double amount)
    {
        return economy.withdrawPlayer(player.getName(), amount).transactionSuccess();
    }

    public boolean playerCredit(Player player, double amount)
    {
        return economy.depositPlayer(player.getName(), amount).transactionSuccess();
    }

    public static boolean hasMoney(Player player, double amount)
    {
        return economy.has(player.getName(), amount);
    }
}
