package com.intellectualsites.translation;

/**
 * Abstract TranslationFile
 *
 * @author Citymonstret
 */
public abstract class TranslationFile {

    /**
     * A method used to get the language of the file
     *
     * @return language
     */
    public abstract TranslationLanguage getLanguage();

    /**
     * Save the file
     */
    public abstract void saveFile();

    /**
     * Read from the file
     *
     * @return instance
     */
    public abstract TranslationFile read();

    /**
     * Add a value
     *
     * @param key
     *            name
     * @param value
     *            value
     */
    public abstract void add(String key, String value);

}
