package com.intellectualsites.translation;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Translation Manager Main class
 *
 * @author Citymonstret
 */
public class TranslationManager {

    /**
     * Objects
     */
    private final LinkedList<TranslationObject>           translationObjects;
    /**
     * The translations
     */
    private final LinkedHashMap<String, TranslationAsset> translatedObjects;

    /**
     * Constructor
     */
    public TranslationManager() {
        this(new TranslationObject[] {});
    }

    /**
     * Constructor
     *
     * @param translationObjects
     *            pre-init
     */
    public TranslationManager(final TranslationObject[] translationObjects) {
        this.translationObjects = new LinkedList<TranslationObject>(Arrays.asList(translationObjects));
        this.translatedObjects = new LinkedHashMap<String, TranslationAsset>();
    }

    public static List<TranslationObject> transformEnum(final Object[] os) {
        final List<TranslationObject> eList = new ArrayList<TranslationObject>();
        for (final Object o : os) {
            eList.add(new TranslationObject(o.toString(), o.toString().toLowerCase().replace("_", " "), "", ""));
        }
        return eList;
    }

    public static void scan(final Class c, final TranslationManager manager) throws IllegalAccessException {
        final Field[] fields = c.getDeclaredFields();
        Annotation annotation;
        for (final Field field : fields) {
            if ((field.getType() != String.class) || ((annotation = field.getAnnotation(Translation.class)) == null)) {
                continue;
            }
            final Translation t = (Translation) annotation;
            final String key = field.getName();
            // Make sure we can get the value
            field.setAccessible(true);
            final String defaultValue = (String) field.get(c);
            manager.addTranslationObject(new TranslationObject(key, defaultValue, t.description(), t.creationDescription()));
        }
    }

    /**
     * Don't use this!
     *
     * @return this
     */
    public TranslationManager instance() {
        return this;
    }

    /**
     * Get the translation objects
     *
     * @return objects
     */
    public List<TranslationObject> translations() {
        return this.translationObjects;
    }

    /**
     * Add an object
     *
     * @param t
     *            object
     * @return instance
     */
    public TranslationManager addTranslationObject(final TranslationObject t) {
        this.translationObjects.add(t);
        return instance();
    }

    /**
     * Remove an object
     *
     * @param t
     *            object
     * @return instance
     */
    public TranslationManager removeTranslationObject(final TranslationObject t) {
        this.translationObjects.remove(t);
        return instance();
    }

    public String getDescription(final String key) {
        for (final TranslationObject o : translations()) {
            if (o.getKey().equals(key) && !o.getDescription().equals("")) {
                return "# " + o.getDescription();
            }
        }
        return "";
    }

    public TranslationManager addTranslation(final TranslationObject t, final TranslationAsset a) {
        return addTranslation(t.getKey(), a);
    }

    public TranslationManager addTranslation(final String key, final TranslationAsset a) {
        String eKey = key + "." + a.getLang().toString();
        eKey = eKey.toLowerCase();
        if (this.translatedObjects.containsKey(eKey)) {
            this.translatedObjects.remove(eKey);
        }
        this.translatedObjects.put(eKey, a);
        return instance();
    }

    public TranslationAsset getTranslated(final String key, final String language) {
        String eKey = key + "." + language;
        eKey = eKey.toLowerCase();
        if (!this.translatedObjects.containsKey(eKey)) {
            return new TranslationAsset(getDefault(key), getDefault(key).getKey(), TranslationLanguage.englishAmerican);
        }
        return this.translatedObjects.get(key);
    }

    public TranslationAsset getTranslated(final String key, final TranslationLanguage language) {
        String eKey = key + "." + language.toString();
        eKey = eKey.toLowerCase();
        if (!this.translatedObjects.containsKey(eKey)) {
            return new TranslationAsset(getDefault(key), getDefault(key).getDefaultValue(), TranslationLanguage.englishAmerican);
        }
        return this.translatedObjects.get(eKey);
    }

    public TranslationAsset getTranslated(final TranslationObject t, final TranslationLanguage l) {
        return getTranslated(t.getKey(), l);
    }

    public String getTranslation(final String key, final TranslationLanguage l) {
        return getTranslated(key, l).getTranslated();
    }

    public TranslationObject getDefault(final String key) {
        for (final TranslationObject o : translations()) {
            if (o.getKey().equals(key.toLowerCase())) {
                return o;
            }
        }
        return null;
    }

    public TranslationManager saveAll(final TranslationFile file) {
        for (final TranslationObject object : translations()) {
            final TranslationAsset o = getTranslated(object.getKey(), file.getLanguage());
            file.add(object.getKey(), o.getTranslated());
        }
        return instance();
    }

    public TranslationManager debug(final PrintStream out) {
        for (final TranslationObject object : translations()) {
            out.println(object.getKey() + ":");
            for (final TranslationLanguage language : TranslationLanguage.values()) {
                out.println(language.toString() + ": " + getTranslated(object.getKey(), language).getTranslated());
            }
        }
        return instance();
    }

    public TranslationManager saveFile(final TranslationFile file) {
        file.saveFile();
        return instance();
    }
}
