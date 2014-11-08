package com.intellectualsites.translation;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The YAML implementation of TranslationFile
 * Relies heavily on SnakeYAML
 *
 * @author Citymonstret
 */
public class YamlTranslationFile extends TranslationFile {

    private File path;
    private TranslationLanguage language;
    private String name;
    private File file;
    private HashMap<String, String> map;
    private String[] header;
    private boolean fancyHead = false;
    private YamlTranslationFile instance;
    private TranslationManager manager;

    /**
     * Reload
     */
    public void reload() {
        this.map = new HashMap<String, String>();
        this.read();
    }

    /**
     * Constructor
     *
     * @param path     save path
     * @param language translation language
     * @param name     project name
     */
    public YamlTranslationFile(File path, TranslationLanguage language, String name, TranslationManager manager) {
        this.path = path;
        this.language = language;
        this.name = name;
        this.manager = manager;
        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new RuntimeException("Could not create: " + path.getAbsolutePath());
            }
        }
        this.file = new File(path + File.separator + name + "." + language.toString() + ".yml");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Could not create: " + file.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        instance = this;
        this.instance = this;
    }

    /**
     * Set the header
     *
     * @param header Comment header
     * @return instance
     */
    public YamlTranslationFile header(String... header) {
        this.header = header;
        this.fancyHead = false;
        return instance;
    }

    /**
     * Set a fancy header
     *
     * @param header Comment header
     * @return instance
     */
    public YamlTranslationFile fancyHeader(String... header) {
        final String line = "################################################################################################";
        final int lineLength = line.length();
        List<String> strings = new ArrayList<String>();
        strings.add(line + "\n");
        for (String s : header) {
            s = "# " + s;
            while (s.length() < lineLength - 1) {
                s = s + " ";
            }
            s = s + "#\n";
            strings.add(s);
        }
        strings.add(line + "\n");
        this.header = strings.toArray(new String[strings.size()]);
        this.fancyHead = true;
        return instance;
    }

    /**
     * Add a translation
     *
     * @param key   translation name
     * @param value translation value
     */
    public void add(String key, String value) {
        if (map.containsKey(key))
            return;
        map.put(
                key, value
        );
    }

    /**
     * Get the translation language
     *
     * @return language
     */
    @Override
    public TranslationLanguage getLanguage() {
        return language;
    }

    /**
     * Save the file
     */
    @Override
    public void saveFile() {
        try {
            FileWriter writer = new FileWriter(file);
            //String s = getYaml().dump(map);
            if (header != null && !fancyHead) {
                for (String head : header) {
                    writer.write("# " + head + "\n");
                }
            } else if (header != null && fancyHead) {
                for (String head : header) {
                    writer.write(head);
                }
            }
            int length = map.size();
            int current = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String var = entry.getKey();
                String val = entry.getValue();
                String des = manager.getDescription(var);
                if (des.equals(""))
                    writer.write(var + ": \"" + val + "\"" + (current < length - 1 ? "\n" : ""));
                else
                    writer.write(des + "\n" + var + ": \"" + val + "\"" + (current < length - 1 ? "\n" : ""));
                ++current;
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * YAML Object
     */
    private Yaml yaml;

    /**
     * Get the YAML object
     *
     * @return yaml object with correct settings
     */
    public Yaml getYaml() {
        if (yaml == null) {
            DumperOptions options = new DumperOptions();
            options.setAllowUnicode(true);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            yaml = new Yaml(options);
            yaml.setName(name + "." + language.toString());
        }
        return yaml;
    }

    /**
     * Read the file
     *
     * @return instance
     */
    @Override
    public YamlTranslationFile read() {
        try {
            map = (HashMap<String, String>) getYaml().load(new FileReader(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (map == null) {
            map = new HashMap<String, String>();
            System.out.println("Was null...");
        }
        for (Map.Entry<String, String> objects : map.entrySet()) {
            String key = objects.getKey();
            String val = objects.getValue();
            manager.addTranslation(
                    key,
                    new TranslationAsset(null, val, language)
            );
        }
        return instance;
    }
}
