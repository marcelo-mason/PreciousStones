
package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

/**
 * @author phaed
 * @author spacemoose
 */
public class PSServerListener extends ServerListener
{

	public static Permission permission = null;

	public static Economy economy = null;

	@Override
	public void onPluginDisable(PluginDisableEvent event)
		{
		}

	@Override
	public void onPluginEnable(PluginEnableEvent event)
		{
			getVault();
			setupEconomy();
			setupPermissions();
			if(economy != null)
				{
					PreciousStones.log("Payment method found.");
				}
			else
				{
					PreciousStones.log("No payment method found: economy disabled!");
				}
		}

	private boolean setupEconomy()
		{
			RegisteredServiceProvider<Economy> economyProvider = getServer()
					.getServicesManager()
						.getRegistration(net.milkbowl.vault.economy.Economy.class);
			if(economyProvider != null)
				{
					economy = economyProvider.getProvider();
				}
			return(economy != null);
		}

	private Boolean setupPermissions()
		{
			RegisteredServiceProvider<Permission> permissionProvider = getServer()
					.getServicesManager()
						.getRegistration(net.milkbowl.vault.permission.Permission.class);
			if(permissionProvider != null)
				{
					permission = permissionProvider.getProvider();
				}
			return(permission != null);
		}

	public void getVault()
		{
			PluginManager vault = getServer().getPluginManager();
			if(vault.getPlugin("Vault") == null)
				{
					try
						{
							File toPut = new File("plugins/Vault.jar");
							download(getServer().getLogger(), new URL("http://ci.milkbowl.net/job/Vault/Recommended%20Build/artifact/target/Vault-0.0.1-SNAPSHOT.jar"), toPut);
							vault.loadPlugin(toPut);
							vault.enablePlugin(vault.getPlugin("Vault"));
						}
					catch(Exception exception)
						{
							PreciousStones.log("Could not download Vault, try again or install manually.");
						}
				}
		}

	public static void download(Logger log, URL url, File file) throws IOException
		{
			if( ! file.getParentFile().exists())
				file.getParentFile().mkdir();
			if(file.exists())
				file.delete();
			file.createNewFile();
			final int size = url.openConnection().getContentLength();
			PreciousStones.log("Downloading " + file.getName() + " (" + size / 1024 + "kb) ...");
			final InputStream in = url.openStream();
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			final byte[] buffer = new byte[1024];
			int len, downloaded = 0, msgs = 0;
			final long start = System.currentTimeMillis();
			while((len = in.read(buffer)) >= 0)
				{
					out.write(buffer, 0, len);
					downloaded += len;
					if((int)((System.currentTimeMillis() - start) / 200) > msgs)
						{
							PreciousStones.log((int)((double)downloaded / (double)size * 100d) + "%");
							msgs ++ ;
						}
				}
			in.close();
			out.close();
			PreciousStones.log("Vault.jar download complete.");
		}
}