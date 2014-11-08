package com.intellectualsites.translation;

public class TranslationLanguage {

    private String countryCode;
    private String languageCode;
    private String friendlyName;

    public TranslationLanguage(String friendlyName, String countryCode, String languageCode) {
        this.friendlyName = friendlyName;
        this.countryCode = countryCode;
        this.languageCode = languageCode;
    }

    public String getName() {
        return friendlyName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    @Override
    public String toString() {
        /* en_US */
        return languageCode.toLowerCase() + "_" + countryCode.toUpperCase();
    }

    public static final TranslationLanguage englishAmerican
            = (new TranslationLanguage("American English", "us", "en"));
    public static final TranslationLanguage englishBritish
            = (new TranslationLanguage("British English", "gb", "en"));
    public static final TranslationLanguage swedishSwedish
            = (new TranslationLanguage("Swedish", "sv", "se"));
    public static final TranslationLanguage russianRussian
            = (new TranslationLanguage("Russian", "ru", "ru"));


    public static TranslationLanguage[] values() {
        return new TranslationLanguage[]{
                englishAmerican,
                englishBritish,
                swedishSwedish
        };
    }
}
