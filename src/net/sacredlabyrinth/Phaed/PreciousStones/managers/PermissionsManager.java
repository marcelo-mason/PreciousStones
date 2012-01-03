
package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * 
 * @author phaed
 * @author spacemoose
 */
public final class PermissionsManager
{

	private Permission handler;

	@SuppressWarnings("unused")
    private PreciousStones plugin;


	public PermissionsManager()
		{
			plugin = PreciousStones.getInstance();
/*			detectPermissionsBukkit();
			detectPermissions();*/
		}

	/**
	 * Check whether a player has a permission
	 * 
	 * @param player
	 * @param permission
	 * @return
	 */
	public boolean has(Player player, String permission)
		{
			if(player == null)
				{
					return true;
				}
			if(handler.isEnabled())
				{
					if(handler.has(player, "preciousstones.blacklist") && ! handler.has(player, "preciousstones.admintest"))
						{
							return false;
						}
					return handler.has(player, permission);
				}
			else
				{
					return player.hasPermission(permission);
				}
		}

	/**
	 * Check whether a player belongs to a group
	 * 
	 * @param playerName
	 * @param group
	 * @param world
	 * @return
	 */
	public boolean inGroup(World world, String playerName, String group)
		{
			if(handler != null)
				{
					return(handler.playerInGroup(world, playerName, group));
				}
			return false;
		}

	/**
	 * Get a player's groups
	 * 
	 * @param worldName
	 * @param playerName
	 * @return
	 */
	public String[] getGroups(String worldName, String playerName)
		{
			String[] groups = null;
			if(handler != null)
				{
					groups = handler.getPlayerGroups(worldName, playerName);
				}
			return groups;
		}
	
	/*
	 * private void detectPermissions() { if(handler == null) { Plugin test =
	 * plugin.getServer().getPluginManager().getPlugin("Permissions"); if(test
	 * != null) { handler = ((Permissions)test).getHandler(); } } }
	 * 
	 * private void detectPermissionsBukkit() { if(handler2 == null) { Plugin
	 * test =
	 * plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");
	 * if(test != null) { handler2 = ((PermissionsPlugin)test); } } }
	 */
}
