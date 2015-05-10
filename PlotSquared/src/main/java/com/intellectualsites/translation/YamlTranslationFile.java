package com.intellectualsites.translation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;

import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * The YAML implementation of TranslationFile Relies heavily on SnakeYAML
 *
 * @author Citymonstret
 */
public class YamlTranslationFile extends TranslationFile {
    final private TranslationLanguage language;
    final private String name;
    final private TranslationManager manager;
    private final File file;
    private HashMap<String, String> map;
    private String[] header;
    private boolean fancyHead = false;
    private boolean isC = false;
    private YamlTranslationFile instance;
    /**
     * YAML Object
     */
    private Yaml yaml;

    /**
     * Constructor
     *
     * @param path     save path
     * @param language translation language
     * @param name     project name
     */
    public YamlTranslationFile(final File path, final TranslationLanguage language, final String name, final TranslationManager manager, final boolean isC) {
        this.language = language;
        this.name = name;
        this.manager = manager;
        this.isC  = isC;
        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new RuntimeException("Could not create: " + path.getAbsolutePath());
            }
        }
        this.file = new File(path + File.separator + name + "." + language.toString() + ".yml");
        if (!this.file.exists()) {
            try {
                if (!this.file.createNewFile()) {
                    throw new RuntimeException("Could not create: " + this.file.getName());
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        this.instance = this;
        this.instance = this;
    }

    /**
     * Reload
     */
    public void reload() {
        this.map = new HashMap<String, String>();
        this.read();
    }

    /**
     * Set the header
     *
     * @param header Comment header
     *
     * @return instance
     */
    public YamlTranslationFile header(final String... header) {
        this.header = header;
        this.fancyHead = false;
        return this.instance;
    }

    /**
     * Set a fancy header
     *
     * @param header Comment header
     *
     * @return instance
     */
    public YamlTranslationFile fancyHeader(final String... header) {
        final String line = "################################################################################################";
        final int lineLength = line.length();
        final List<String> strings = new ArrayList<String>();
        strings.add(line + "\n");
        for (String s : header) {
            s = "# " + s;
            while (s.length() < (lineLength - 1)) {
                s = s + " ";
            }
            s = s + "#\n";
            strings.add(s);
        }
        strings.add(line + "\n");
        this.header = strings.toArray(new String[strings.size()]);
        this.fancyHead = true;
        return this.instance;
    }

    /**
     * Add a translation
     *
     * @param key   translation name
     * @param value translation value
     */
    @Override
    public void add(final String key, final String value) {
        if (this.map.containsKey(key)) {
            return;
        }
        this.map.put(key, value);
    }

    /**
     * Get the translation language
     *
     * @return language
     */
    @Override
    public TranslationLanguage getLanguage() {
        return this.language;
    }

    /**
     * Save the file
     */
    @Override
    public void saveFile() {
        if (this.isC()) {
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(this.file);
            for (String key : this.map.keySet()) {
                C c = C.valueOf(key.toUpperCase());
                conf.set("locale." + c.getCat() + "." + key, this.map.get(key));
                conf.set(key, null);
            }
            try {
                conf.save(this.file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if (!this.file.exists()) {
                    this.file.getParentFile().mkdirs();
                    this.file.createNewFile();
                }
                final FileWriter writer = new FileWriter(this.file);
                // String s = getYaml().dump(map);
                if ((this.header != null) && !this.fancyHead) {
                    for (final String head : this.header) {
                        writer.write("# " + head + "\n");
                    }
                } else if ((this.header != null) && this.fancyHead) {
                    for (final String head : this.header) {
                        writer.write(head);
                    }
                }
                final int length = this.map.size();
                int current = 0;
                for (final Map.Entry<String, String> entry : this.map.entrySet()) {
                    final String var = entry.getKey();
                    final String val = entry.getValue();
                    final String des = this.manager.getDescription(var);
                    if (des.equals("")) {
                        writer.write(var + ": \"" + val + "\"" + (current < (length - 1) ? "\n" : ""));
                    } else {
                        writer.write(des + "\n" + var + ": \"" + val + "\"" + (current < (length - 1) ? "\n" : ""));
                    }
                    ++current;
                }
                writer.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Get the YAML object
     *
     * @return yaml object with correct settings
     */
    public Yaml getYaml() {
        if (this.yaml == null) {
            final DumperOptions options = new DumperOptions();
            options.setAllowUnicode(true);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            this.yaml = new Yaml(options);
            this.yaml.setName(this.name + "." + this.language.toString());
        }
        return this.yaml;
    }

    /**
     * Read the file
     *
     * @return instance
     */
    @Override
    public YamlTranslationFile read() {
        if (this.isC()) {
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(this.file);
            if (conf.get("locale") == null) {
                try {
                    this.map = (HashMap<String, String>) getYaml().load(new FileReader(this.file));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                if (this.map == null) {
                    this.map = new HashMap<String, String>();
                }
                for (final Map.Entry<String, String> objects : this.map.entrySet()) {
                    final String key = objects.getKey();
                    final String val = objects.getValue();
                    this.manager.addTranslation(key, new TranslationAsset(null, val, this.language));
                }
                this.saveFile();
            } else {
                if (this.map == null) {
                    this.map = new HashMap<String, String>();
                }
                for (String label : conf.getConfigurationSection("locale").getKeys(false)) {
                    for (String key : conf.getConfigurationSection("locale." + label).getKeys(false)) {
                        String val = conf.getString("locale." + label + "." + key);
                        this.map.put(key, val);
                        this.manager.addTranslation(key, new TranslationAsset(null, val, this.language));
                    }
                }
            }
        } else {
            try {
                this.map = (HashMap<String, String>) getYaml().load(new FileReader(this.file));
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if (this.map == null) {
                this.map = new HashMap<String, String>();
            }
            for (final Map.Entry<String, String> objects : this.map.entrySet()) {
                final String key = objects.getKey();
                final String val = objects.getValue();
                this.manager.addTranslation(key, new TranslationAsset(null, val, this.language));
            }
        }
        return this.instance;
    }

    public boolean isC() {
        return isC;
    }

    public void setIsC(boolean isC) {
        this.isC = isC;
    }
}