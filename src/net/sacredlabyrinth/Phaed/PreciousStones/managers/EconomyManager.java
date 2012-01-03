
package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

/**
 * @author spacemoose
 * 
 */

public class EconomyManager
{

	private Economy handler;

	@SuppressWarnings("unused")
	private PreciousStones plugin;

	public EconomyManager()
		{
			plugin = PreciousStones.getInstance();
		}

	public boolean hasEconomy()
		{
			return(handler.isEnabled());
		}

	public boolean hasFunds(String name, Double amount)
		{
			if(handler.has(name, amount))
				{
					return true;
				}
			Bukkit.getPlayer(name).sendMessage(ChatColor.RED + "You do not have sufficient money in your account");
			return false;
		}

	public void chargePlayer(String name, Double amount)
		{
			handler.withdrawPlayer(name, amount);
			Bukkit.getPlayer(name).sendMessage(ChatColor.RED + amount.toString() + " was withdrawn from your account.");
		}

	public void creditPlayer(String name, Double amount)
		{
			handler.depositPlayer(name, amount);
			Bukkit.getPlayer(name).sendMessage(ChatColor.AQUA + "Your account has been credited " 
					+ ChatColor.GREEN + amount.toString());
		}
}
