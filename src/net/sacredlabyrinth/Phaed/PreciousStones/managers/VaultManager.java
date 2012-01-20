package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author spacemoose (daniel@errortown.com)
 */

public final class VaultManager
{
    private PreciousStones plugin;
    private static Economy economy = null;

    /**
     *
     */
    public VaultManager()
    {
        plugin = PreciousStones.getInstance();

        try
        {
            Class.forName("net.milkbowl.vault.economy.Economy");
            setupEconomy();
        }
        catch (ClassNotFoundException e)
        {
        }

        if (economy != null)
        {
            PreciousStones.log("Payment method found");
        }
    }

    public boolean hasEconomy()
    {
        if (economy != null && economy.isEnabled())
        {
            return true;
        }
        return false;
    }

    private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (economyProvider != null)
        {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
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
