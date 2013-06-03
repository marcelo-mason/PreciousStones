package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Locale;
import java.util.logging.Level;

@SuppressWarnings("unchecked")
public class LanguageManager
{
    private static final String I18N = "language";
    private final transient String defaultLocale = Locale.getDefault().toString();
    private transient String currentLocale = defaultLocale;

    private TreeMap<String, Object> language = new TreeMap<String, Object>();

    public LanguageManager(String theLocale)
    {
        currentLocale = theLocale;
        load();
    }

    public void load()
    {
        PreciousStones.getLog().log(Level.INFO, "[PreciousStones] Using locale %s", currentLocale);
        // load default as base
        tryLoad(I18N + ".yml");
        // load custom as append
        String[] codes = currentLocale.split("_");
        if (codes.length > 1) tryLoad(I18N + "_" + currentLocale + ".yml");
        if (codes.length > 0) tryLoad(I18N + "_" + codes[0] + ".yml");
    }

    private boolean tryLoad(String resname)
    {
        InputStream inputStream = getClass().getResourceAsStream("/" + resname);
        if (inputStream != null)
        {
            language.putAll((Map<? extends String, ?>) new Yaml().load(inputStream));
            return true;
        }
        return false;
    }

    public String get(String key)
    {
        Object o = language.get(key);

        if (o != null)
        {
            return o.toString();
        }

        return null;
    }
}
