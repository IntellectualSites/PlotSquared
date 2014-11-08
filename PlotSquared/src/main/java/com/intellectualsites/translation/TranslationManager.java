package com.intellectualsites.translation;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Translation Manager Main class
 *
 * @author Citymonstret
 */
public class TranslationManager {

    /**
     * The instance
     */
    private TranslationManager instance;

    /**
     * Constructor
     */
    public TranslationManager() {
        this(new TranslationObject[]{});
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
     * Objects
     */
    private LinkedList<TranslationObject> translationObjects;

    /**
     * The translations
     */
    private LinkedHashMap<String, TranslationAsset> translatedObjects;

    /**
     * Get the translation objects
     *
     * @return objects
     */
    public List<TranslationObject> translations() {
        return translationObjects;
    }

    /**
     * Constructor
     *
     * @param translationObjects pre-init
     */
    public TranslationManager(TranslationObject[] translationObjects) {
        this.translationObjects
                = new LinkedList<TranslationObject>(Arrays.asList(translationObjects));
        this.translatedObjects
                = new LinkedHashMap<String, TranslationAsset>();
        instance = this;
    }

    /**
     * Add an object
     *
     * @param t object
     * @return instance
     */
    public TranslationManager addTranslationObject(TranslationObject t) {
        translationObjects.add(t);
        return instance();
    }

    /**
     * Remove an object
     *
     * @param t object
     * @return instance
     */
    public TranslationManager removeTranslationObject(TranslationObject t) {
        translationObjects.remove(t);
        return instance();
    }

    public String getDescription(String key) {
        for (TranslationObject o : translations()) {
            if (o.getKey().equals(key) && !o.getDescription().equals("")) {
                return "# " + o.getDescription();
            }
        }
        return "";
    }

    public TranslationManager addTranslation(TranslationObject t, TranslationAsset a) {
        return addTranslation(t.getKey(), a);
    }

    public TranslationManager addTranslation(String key, TranslationAsset a) {
        String eKey = key + "." + a.getLang().toString();
        eKey = eKey.toLowerCase();
        if (translatedObjects.containsKey(eKey))
            translatedObjects.remove(eKey);
        translatedObjects.put(eKey, a);
        return instance();
    }

    public TranslationAsset getTranslated(String key, String language) {
        String eKey = key + "." + language;
        eKey = eKey.toLowerCase();
        if (!translatedObjects.containsKey(eKey))
            return new TranslationAsset(getDefault(key), getDefault(key).getKey(), TranslationLanguage.englishAmerican);
        return translatedObjects.get(key);
    }

    public TranslationAsset getTranslated(String key, TranslationLanguage language) {
        String eKey = key + "." + language.toString();
        eKey = eKey.toLowerCase();
        if (!translatedObjects.containsKey(eKey)) {
            return new TranslationAsset(getDefault(key), getDefault(key).getDefaultValue(), TranslationLanguage.englishAmerican);
        }
        return translatedObjects.get(eKey);
    }

    public TranslationAsset getTranslated(TranslationObject t, TranslationLanguage l) {
        return getTranslated(t.getKey(), l);
    }

    public String getTranslation(String key, TranslationLanguage l) {
        return getTranslated(key, l).getTranslated();
    }

    public TranslationObject getDefault(String key) {
        for (TranslationObject o : translations())
            if (o.getKey().equals(key.toLowerCase()))
                return o;
        return null;
    }

    public TranslationManager saveAll(TranslationFile file) {
        for (TranslationObject object : translations()) {
            TranslationAsset o = getTranslated(object.getKey(), file.getLanguage());
            file.add(object.getKey(), o.getTranslated());
        }
        return instance();
    }

    public static List<TranslationObject> transformEnum(Object[] os) {
        List<TranslationObject> eList = new ArrayList<TranslationObject>();
        for (Object o : os) {
            eList.add(
                    new TranslationObject(o.toString(), o.toString().toLowerCase().replace("_", " "), "", "")
            );
        }
        return eList;
    }

    public static void scan(Class c, TranslationManager manager) throws IllegalAccessException {
        Field[] fields = c.getDeclaredFields();
        Annotation annotation;
        for (Field field : fields) {
            if (field.getType() != String.class || (annotation = field.getAnnotation(Translation.class)) == null)
                continue;
            Translation t = (Translation) annotation;
            String key = field.getName();
            // Make sure we can get the value
            field.setAccessible(true);
            String defaultValue = (String) field.get(c);
            manager.addTranslationObject(
                    new TranslationObject(
                            key,
                            defaultValue,
                            t.description(),
                            t.creationDescription()
                    )
            );
        }
    }

    public TranslationManager debug(PrintStream out) {
        for (TranslationObject object : translations()) {
            out.println(object.getKey() + ":");
            for (TranslationLanguage language : TranslationLanguage.values()) {
                out.println(language.toString() + ": " + getTranslated(object.getKey(), language).getTranslated());
            }
        }
        return instance();
    }

    public TranslationManager saveFile(TranslationFile file) {
        file.saveFile();
        return instance();
    }
}
