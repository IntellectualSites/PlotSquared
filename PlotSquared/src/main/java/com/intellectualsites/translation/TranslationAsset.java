package com.intellectualsites.translation;

/**
 * Asset
 *
 * @author Citymonstret
 */
public class TranslationAsset {

    private final TranslationObject   trans;
    private final String              translated;
    private final TranslationLanguage lang;

    public TranslationAsset(final TranslationObject trans, final String translated, final TranslationLanguage lang) {
        this.trans = trans;
        this.translated = translated;
        this.lang = lang;
    }

    public TranslationObject getObject() {
        return this.trans;
    }

    public String getTranslated() {
        return this.translated.replace("\n", "&-");
    }

    public TranslationLanguage getLang() {
        return this.lang;
    }
}
