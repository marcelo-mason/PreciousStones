package net.sacredlabyrinth.Phaed.PreciousStones.vault;

import net.milkbowl.vault.economy.*;
import org.bukkit.entity.Player;

/**
 * 
 * @author spacemoose (daniel@errortown.com)
 */

public final class Vault
{
	private static Economy economy;

	public boolean hasEconomy()
		{
			if (economy.isEnabled())
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
