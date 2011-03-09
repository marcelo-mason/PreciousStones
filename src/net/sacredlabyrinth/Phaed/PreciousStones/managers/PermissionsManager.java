package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.plugin.Plugin;

import org.bukkit.entity.Player;

public class PermissionsManager
{
    public static PermissionHandler Permissions = null;
    public GroupManager gm;
    private PreciousStones plugin;
    
    public PermissionsManager(PreciousStones plugin)
    {
	this.plugin = plugin;
	
	if (!startGroupManager() && !startPermissions())
	{
	    PreciousStones.log.info("[" + plugin.getDescription().getName() + "] Permission system not found. Disabling plugin.");
	    plugin.getServer().getPluginManager().disablePlugin(plugin);
	}
    }
    
    public boolean hasPermission(Player player, String permission)
    {
	if (player == null)
	    return false;
	
	return (Permissions != null && Permissions.has(player, permission)) || (gm != null && gm.getWorldsHolder().getWorldPermissions(player).has(player, permission));
    }
    
    public boolean startGroupManager()
    {
	Plugin p = plugin.getServer().getPluginManager().getPlugin("GroupManager");
	if (p != null)
	{
	    if (!plugin.getServer().getPluginManager().isPluginEnabled(p))
	    {
		plugin.getServer().getPluginManager().enablePlugin(p);
	    }
	    gm = (GroupManager) p;
	}
	else
	{
	    plugin.getPluginLoader().disablePlugin(plugin);
	}
	
	return false;
    }
    
    @SuppressWarnings("static-access")
    public boolean startPermissions()
    {
	Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");
	
	if (this.Permissions == null)
	{
	    if (test != null)
	    {		
		this.Permissions = ((Permissions) test).getHandler();
		return true;
	    }
	}
	
	return false;
    }
}
