package com.intellectualsites.translation;

import com.sun.istack.internal.NotNull;

public class TranslationObject {

    // can include a suffix | not null
    private String key;
    //The default ("no-translation" value) | not null
    private String defaultValue;
    // ... "Join message" ... | can be null
    private String description;
    // Like a plugin name for example | can be null
    private String creationDescription;

    public TranslationObject(@NotNull String key, @NotNull String defaultValue, String description, String creationDescription) {
        if (description == null) {
            description = "";
        }
        if (creationDescription == null) {
            creationDescription = "";
        }
        for (char c : key.toCharArray()) {
            if (!Character.isDigit(c) && !Character.isAlphabetic(c) && c != '_' && c != '&' && c != '\u00A7' && c != ':') {
                throw new RuntimeException(
                        String.format("Translation: '%s' is invalid (Character: '%s') - Only alphanumeric + (\\, _, &, §, :) charcters are allowed",
                                key, c + ""
                        )
                );
            }
        }
        this.key = key.toLowerCase();
        this.defaultValue = defaultValue.replace("\n", "&-");
        this.description = description;
        this.creationDescription = creationDescription;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public String getCreationDescription() {
        return creationDescription;
    }

}
