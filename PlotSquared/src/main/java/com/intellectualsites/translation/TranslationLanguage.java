package com.intellectualsites.translation;

public class TranslationLanguage {
    public static final TranslationLanguage englishAmerican = (new TranslationLanguage("American English", "us", "en"));
    public static final TranslationLanguage englishBritish = (new TranslationLanguage("British English", "gb", "en"));
    public static final TranslationLanguage swedishSwedish = (new TranslationLanguage("Swedish", "sv", "se"));
    public static final TranslationLanguage russianRussian = (new TranslationLanguage("Russian", "ru", "ru"));
    private final String countryCode;
    private final String languageCode;
    private final String friendlyName;

    public TranslationLanguage(final String friendlyName, final String countryCode, final String languageCode) {
        this.friendlyName = friendlyName;
        this.countryCode = countryCode;
        this.languageCode = languageCode;
    }

    public static TranslationLanguage[] values() {
        return new TranslationLanguage[] { englishAmerican, englishBritish, swedishSwedish };
    }

    public String getName() {
        return this.friendlyName;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    @Override
    public String toString() {
        /* en_US */
        return this.languageCode.toLowerCase() + "_" + this.countryCode.toUpperCase();
    }
}
