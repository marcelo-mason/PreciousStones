package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.TreeMap;

@SuppressWarnings("unchecked")
public class LanguageManager {
    private File file;
    private TreeMap<String, Object> language = new TreeMap<String, Object>();
    private String[] comments = new String[]{
            "# Guidelines",
            "\n#",
            "\n# 1. Never change the contents inside the variables { }",
            "\n# 2. You can rearrange the order that the variables appear on the sentences to best suit your language",
            "\n# 3. You can add/remove colors as you please",
            "\n# 4. If you change a command, make sure it's corresponding menu item matches",
            "\n# 5. When new text is added on future versions, they will be added automatically to your language.yml file",
            "\n#",
            "\n# Colors: {aqua}, {black}, {blue}, {white}, {yellow}, {gold}, {gray}, {green}, {red} ",
            "\n#         {dark-aqua}, {dark-blue}, {dark-gray}, {dark-green}, {dark-purple}, {dark-red}, {light-purple}",
            "\n#         {magic}, {bold}, {italic}, {reset}, {strikethrough}, {underline}\n\n",
    };

    public LanguageManager() {
        load();
    }

    public void load() {
        file = new File(PreciousStones.getInstance().getDataFolder() + File.separator + "language.yml");
        check();
    }

    private void check() {
        boolean exists = (file).exists();

        loadDefaults();

        if (exists) {
            loadFile();
        }

        saveFile();
    }

    private void loadDefaults() {
        InputStream defaultLanguage = getClass().getResourceAsStream("/language.yml");
        HashMap<String, Object> objects = (HashMap<String, Object>) new Yaml().load(defaultLanguage);
        if (objects != null) {
            language.putAll(objects);
        }
    }

    private void loadFile() {
        try {
            InputStream fileLanguage = new FileInputStream(file);
            HashMap<String, Object> objects = (HashMap<String, Object>) new Yaml().load(fileLanguage);
            if (objects != null) {
                language.putAll(objects);
            }
        } catch (FileNotFoundException e) {
            // file not found
        }
    }

    private void saveFile() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setWidth(99999999);
        options.setAllowUnicode(true);

        try {
            FileWriter fw = new FileWriter(file);
            StringWriter writer = new StringWriter();
            new Yaml(options).dump(language, writer);

            for (String comment : comments) {
                fw.write(comment);
            }

            fw.write(writer.toString());
            fw.close();
        } catch (IOException e) {
            // could not save
            e.printStackTrace();
        }
    }

    public String get(String key) {
        Object o = language.get(key);

        if (o != null) {
            return o.toString();
        }

        return null;
    }
}
