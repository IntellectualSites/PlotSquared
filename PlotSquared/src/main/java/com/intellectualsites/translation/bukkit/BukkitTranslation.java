package com.intellectualsites.translation.bukkit;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualsites.translation.TranslationAsset;
import com.intellectualsites.translation.TranslationLanguage;
import com.intellectualsites.translation.TranslationManager;
import com.intellectualsites.translation.TranslationObject;
import org.bukkit.Material;

import java.io.File;

/**
 * @author Citymonstret
 */
public class BukkitTranslation {
    /**
     * Get the converted string
     *
     * @param asset asset
     *
     * @return converted asset
     */
    public static String convert(final TranslationAsset asset) {
        // In some cases newline can screw stuff up, so I added a new character
        // thing
        // &- = new line
        return asset.getTranslated().replace("&-", "\n").replace('&', '\u00A7');
    }

    /**
     * Get the universal parent based on the plugin data folder
     *
     * @return parent folder
     */
    public static File getParent() {
        return new File(PlotSquared.getInstance().IMP.getDirectory() + File.separator + "translations");
    }

    /**
     * Add material names to the translation list Will default to a somewhat friendly name
     */
    public static void addMaterials(final TranslationManager manager) {
        for (final Material material : Material.values()) {
            manager.addTranslationObject(new TranslationObject(material.name(), material.name().replace("_", " ").toLowerCase(), "Material." + material.toString(), ""));
        }
    }

    /**
     * The default translation language
     *
     * @return default translation language
     */
    public TranslationLanguage getDefaultLanguage() {
        return TranslationLanguage.englishAmerican;
    }
}
