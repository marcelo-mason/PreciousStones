package net.sacredlabyrinth.Phaed.register.payment;

import net.sacredlabyrinth.Phaed.register.payment.methods.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.HashSet;
import java.util.Set;

public class Methods
{
  private static String version = null;
  private static boolean self = false;
  private static Method Method = null;
  private static String preferred = "";
  private static Set<Method> Methods = new HashSet();
  private static Set<String> Dependencies = new HashSet();
  private static Set<Method> Attachables = new HashSet();

  private static void _init()
  {
    addMethod("iConomy", new iCo6());
    addMethod("iConomy", new iCo5());
    addMethod("iConomy", new iCo4());
    addMethod("BOSEconomy", new BOSE6());
    addMethod("BOSEconomy", new BOSE7());
    addMethod("Essentials", new EE17());
    addMethod("Currency", new MCUR());
    Dependencies.add("MultiCurrency");
  }

  public static void setVersion(String v)
  {
    version = v;
  }

  public static void reset()
  {
    version = null;
    self = false;
    Method = null;
    preferred = "";
    Attachables.clear();
  }

  public static String getVersion()
  {
    return version;
  }

  public static Set<String> getDependencies()
  {
    return Dependencies;
  }

  public static Method createMethod(Plugin plugin)
  {
    for (Method method : Methods) {
      if (method.isCompatible(plugin)) {
        method.setPlugin(plugin);
        return method;
      }
    }
    return null;
  }

  private static void addMethod(String name, Method method) {
    Dependencies.add(name);
    Methods.add(method);
  }

  public static boolean hasMethod()
  {
    return Method != null;
  }

  public static boolean setMethod(PluginManager manager)
  {
    if (hasMethod()) {
      return true;
    }
    if (self) {
      self = false;
      return false;
    }

    int count = 0;
    boolean match = false;
    Plugin plugin = null;

    for (String name : getDependencies()) {
      if (hasMethod()) {
        break;
      }
      plugin = manager.getPlugin(name);
      if (plugin == null) {
        continue;
      }
      Method current = createMethod(plugin);
      if (current == null) {
        continue;
      }
      if (preferred.isEmpty())
        Method = current;
      else {
        Attachables.add(current);
      }
    }
    if (!preferred.isEmpty()) {
      do
        if (hasMethod()) {
          match = true;
        } else {
          for (Method attached : Attachables) {
            if (attached == null) {
              continue;
            }
            if (hasMethod()) {
              match = true;
              break;
            }

            if (preferred.isEmpty()) {
              Method = attached;
            }
            if (count == 0) {
              if (preferred.equalsIgnoreCase(attached.getName())) {
                Method = attached;
              }
              else
                Method = attached;
            }
          }
          count++;
        }
      while (!match);
    }

    return hasMethod();
  }

  public static boolean setPreferred(String check)
  {
    if (getDependencies().contains(check)) {
      preferred = check;
      return true;
    }

    return false;
  }

  public static Method getMethod()
  {
    return Method;
  }

  public static boolean checkDisabled(Plugin method)
  {
    if (!hasMethod()) {
      return true;
    }
    if (Method.isCompatible(method)) {
      Method = null;
    }
    return Method == null;
  }

  static
  {
    _init();
  }
}