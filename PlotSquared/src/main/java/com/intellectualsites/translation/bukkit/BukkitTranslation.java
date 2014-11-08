package com.intellectualsites.translation.bukkit;

import com.intellectualsites.translation.TranslationAsset;
import com.intellectualsites.translation.TranslationLanguage;
import com.intellectualsites.translation.TranslationManager;
import com.intellectualsites.translation.TranslationObject;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author Citymonstret
 */
public class BukkitTranslation {

    /**
     * Get the converted string
     *
     * @param asset asset
     * @return converted asset
     */
    public static String convert(TranslationAsset asset) {
        // In some cases newline can screw stuff up, so I added a new character thing
        // &- = new line
        return asset.getTranslated().replace("&-", "\n").replace('&', '§');
    }

    /**
     * Get the universal parent based on the plugin data folder
     *
     * @param plugin to check
     * @return parent folder
     */
    public static File getParent(JavaPlugin plugin) {
        return new File(plugin.getDataFolder() + File.separator + "translations");
    }

    /**
     * The default translation language
     *
     * @return default translation language
     */
    public TranslationLanguage getDefaultLanguage() {
        return TranslationLanguage.englishAmerican;
    }

    /**
     * Add material names to the translation list
     * Will default to a somewhat friendly name
     */
    public static void addMaterials(TranslationManager manager) {
        for (Material material : Material.values()) {
            manager.addTranslationObject(
                    new TranslationObject(material.name(), material.name().replace("_", " ").toLowerCase(), "Material." + material.toString(), "")
            );
        }
    }
}
