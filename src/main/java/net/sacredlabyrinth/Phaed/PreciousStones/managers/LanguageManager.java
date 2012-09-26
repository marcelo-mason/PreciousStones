package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class LanguageManager
{
    private PreciousStones plugin;
    private File file;
    private HashMap<String, Object> language = new HashMap<String, Object>();

    public LanguageManager()
    {
        plugin = PreciousStones.getInstance();
        file = new File(plugin.getDataFolder() + File.separator + "language.yml");

        check();
    }

    private void check()
    {
        boolean exists = (file).exists();

        if (exists)
        {
            loadDefaults();
            loadFile();
            saveFile();
        }
        else
        {
            copyDefaults();
            loadDefaults();
        }
    }

    private void loadDefaults()
    {
        InputStream defaultLanguage = getClass().getResourceAsStream("/language.yml");
        HashMap<String, Object> objects = (HashMap<String, Object>) new Yaml().load(defaultLanguage);
        if (objects != null)
        {
            language.putAll(objects);
        }
    }

    private void loadFile()
    {
        try
        {
            InputStream fileLanguage = new FileInputStream(file);
            HashMap<String, Object> objects = (HashMap<String, Object>) new Yaml().load(fileLanguage);
            if (objects != null)
            {
                language.putAll(objects);
            }
        }
        catch (FileNotFoundException e)
        {
            // file not found
            copyDefaults();
        }
    }

    private void saveFile()
    {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);

        try
        {
            FileWriter fw = new FileWriter(file);
            StringWriter writer = new StringWriter();
            new Yaml(options).dump(language, writer);
            fw.write(writer.toString());
            fw.close();
        }
        catch (IOException e)
        {
            // could not save
            e.printStackTrace();
        }
    }

    private void copyDefaults()
    {
        try
        {
            InputStream defaultLanguage = getClass().getResourceAsStream("/language.yml");
            OutputStream out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = defaultLanguage.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            out.close();
            defaultLanguage.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String get(String key)
    {
        Object o = language.get(key);

        if (o != null)
        {
            return o.toString();
        }

        return "";
    }
}
