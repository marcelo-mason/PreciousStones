package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import java.io.*;

public class LanguageManager
{
    private PreciousStones plugin;
    private File language;

    public LanguageManager()
    {
        plugin = PreciousStones.getInstance();
        language = new File(plugin.getDataFolder() + File.separator + "language.yml");
        check();
    }

    private void check()
    {
        boolean exists = (language).exists();

        if (!exists)
        {
            copyFile();
        }

        load();
    }

    private void load()
    {

    }

    private void copyFile()
    {
        try
        {
            InputStream stream = getClass().getResourceAsStream("/language.yml");
            OutputStream out = new FileOutputStream(language);
            byte buf[] = new byte[1024];
            int len;
            while ((len = stream.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            out.close();
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
