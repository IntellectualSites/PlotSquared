package com.intellectualsites.translation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public YamlTranslationFile(final File path, final TranslationLanguage language, final String name, final TranslationManager manager) {
        this.language = language;
        this.name = name;
        this.manager = manager;
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
        return this.instance;
    }
}
