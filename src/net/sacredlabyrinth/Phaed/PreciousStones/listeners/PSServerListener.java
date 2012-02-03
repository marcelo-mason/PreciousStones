package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;

import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

/**
 * @author phaed, spacemoose (daniel@errortown.com)
 */
public class PSServerListener implements Listener
{
    private final PreciousStones plugin;

    /**
     *
     */
    public PSServerListener()
    {
        plugin = PreciousStones.getInstance();
    }

    @EventHandler(event = PluginEnableEvent.class, priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event)
    {
        if (plugin.getSettingsManager().isAutoDownloadVault())
        {
            getVault();
        }
    }

    public void getVault()
    {
        PluginManager vault = getServer().getPluginManager();

        if (vault.getPlugin("Vault") == null)
        {
            try
            {
                File toPut = new File("plugins/Vault.jar");
                download(getServer().getLogger(), new URL("http://ci.milkbowl.net/job/Vault/lastStableBuild/artifact/target/Vault-0.0.1-SNAPSHOT.jar"), toPut);
                vault.loadPlugin(toPut);
                vault.enablePlugin(vault.getPlugin("Vault"));
            }
            catch (Exception exception)
            {
                PreciousStones.log("Could not download Vault, try again or install manually.");
            }
        }
    }

    public static void download(Logger log, URL url, File file) throws IOException
    {
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdir();
        }

        if (file.exists())
        {
            file.delete();
        }

        file.createNewFile();
        final int size = url.openConnection().getContentLength();
        log.info("Downloading " + file.getName() + " (" + size / 1024 + "kb) ...");
        final InputStream in = url.openStream();
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        final byte[] buffer = new byte[1024];
        int len, downloaded = 0, msgs = 0;
        final long start = System.currentTimeMillis();
        while ((len = in.read(buffer)) >= 0)
        {
            out.write(buffer, 0, len);
            downloaded += len;
            if ((int) ((System.currentTimeMillis() - start) / 500) > msgs)
            {
                log.info((int) ((double) downloaded / (double) size * 100d) + "%");
                msgs++;
            }
        }
        in.close();
        out.close();
        log.info("Download finished");
    }
}