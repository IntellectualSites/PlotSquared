package com.intellectualsites.translation.bukkit;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualsites.translation.Translation;
import com.intellectualsites.translation.TranslationFile;
import com.intellectualsites.translation.TranslationLanguage;
import com.intellectualsites.translation.TranslationManager;
import com.intellectualsites.translation.YamlTranslationFile;

public class TranslationPlugin extends JavaPlugin {

    @Translation(description = "Printed when the translator is fully loaded")
    private static final String TRANSLATOR_LOADED = "The translator has been loaded";

    @Translation(description = "Printed when the translator has been disabled")
    private static final String TRANSLATOR_DISABLED = "The translator has been disabled";

    private static TranslationManager manager;

    private TranslationFile english;

    @Override
    public void onEnable() {
        // Create a new manager
        manager = new TranslationManager();
        // Scan this file for all @Translations
        try {
            TranslationManager.scan(TranslationPlugin.class, manager);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        // Now let's create some default files :D
        this.english = new YamlTranslationFile(BukkitTranslation.getParent(), TranslationLanguage.englishAmerican, getName(), manager).read().header("Example file", "@author Citymonstret");
        // That created the file, read it, and made a default header
        getLogger().log(Level.INFO, BukkitTranslation.convert(manager.getTranslated("translator_loaded", TranslationLanguage.englishAmerican)));
    }

    @Override
    public void onDisable() {
        // Add all translations and save the file
        manager.saveAll(this.english).saveFile(this.english);
        getLogger().log(Level.INFO, BukkitTranslation.convert(manager.getTranslated("translator_disabled", TranslationLanguage.englishAmerican)));
    }

}
