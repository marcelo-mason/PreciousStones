package net.sacredlabyrinth.Phaed.register;

import net.sacredlabyrinth.Phaed.register.payment.Methods;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.File;

public class Register extends JavaPlugin
{
  private Configuration config;
  private String preferred;
  private PluginDescriptionFile info;

  private String getPreferred()
  {
    return config.getString("economy.preferred");
  }

  private void setPreferred(String preferences) {
    config.setProperty("economy.preferred", preferences);
    config.save();
  }

  private boolean hasPreferred() {
    return Methods.setPreferred(getPreferred());
  }

  public void onDisable()
  {
    Methods.reset();

    System.out.println("[" + info.getName() + "] Payment method was disabled. No longer accepting payments.");
  }

  public void onEnable()
  {
    config = new Configuration(new File("bukkit.yml"));
    info = getDescription();
    config.load();

    if (!hasPreferred()) {
      System.out.println("[" + info.getName() + "] Preferred method [" + getPreferred() + "] not found, using first found.");

      Methods.setVersion(info.getVersion());
      Methods.setMethod(getServer().getPluginManager());
    }

    if (Methods.getMethod() == null)
      System.out.println("[" + info.getName() + "] No payment method found, economy based plugins may not work.");
    else {
      System.out.println("[" + info.getName() + "] Payment method found (" + Methods.getMethod().getName() + " version: " + Methods.getMethod().getVersion() + ")");
    }
    System.out.print("[" + info.getName() + "] version " + info.getVersion() + " is enabled.");
  }
}