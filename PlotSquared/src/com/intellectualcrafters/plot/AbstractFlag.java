package com.intellectualcrafters.plot;

import org.apache.commons.lang.StringUtils;

/**
 * Created by Citymonstret on 2014-09-23.
 */
public class AbstractFlag {

    private final String key;

    public AbstractFlag(String key) {
        if (!StringUtils.isAlpha(key)) {
            throw new IllegalArgumentException("Flag must be alphabetic characters");
        }
        if (key.length() > 16) {
            throw new IllegalArgumentException("Key must be <= 16 characters");
        }
        this.key = key.toLowerCase();
    }

    public String getKey() {
        return this.key;
    }

}
