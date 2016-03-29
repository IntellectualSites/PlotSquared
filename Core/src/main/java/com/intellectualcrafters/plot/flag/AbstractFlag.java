package com.intellectualcrafters.plot.flag;

import com.intellectualcrafters.plot.util.StringMan;

/**
 * Created 2014-09-23 for PlotSquared
 *


 */
public class AbstractFlag {
    public final String key;
    public final FlagValue<?> value;
    
    public AbstractFlag(final String key) {
        this(key, new FlagValue.StringValue());
    }
    
    /**
     * AbstractFlag is a parameter used in creating a new Flag<br>
     * The key must be alphabetical characters and &lt;= 16 characters in length
     * @param key
     */
    public AbstractFlag(final String key, final FlagValue<?> value) {
        if (!StringMan.isAlpha(key.replaceAll("_", "").replaceAll("-", ""))) {
            throw new IllegalArgumentException("Flag must be alphabetic characters");
        }
        if (key.length() > 16) {
            throw new IllegalArgumentException("Key must be <= 16 characters");
        }
        this.key = key.toLowerCase();
        if (value == null) {
            this.value = new FlagValue.StringValue();
        } else {
            this.value = value;
        }
    }
    
    public boolean isList() {
        return value instanceof FlagValue.ListValue;
    }
    
    public Object parseValueRaw(final String value) {
        try {
            return this.value.parse(value);
        } catch (final Exception e) {
            return null;
        }
    }
    
    public String toString(final Object t) {
        return value.toString(t);
    }
    
    public String getValueDesc() {
        return value.getDescription();
    }
    
    /**
     * AbstractFlag key
     *
     * @return String
     */
    public String getKey() {
        return key;
    }
    
    @Override
    public String toString() {
        return key;
    }
    
    @Override
    public int hashCode() {
        return key.hashCode();
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AbstractFlag)) {
            return false;
        }
        final AbstractFlag otherObj = (AbstractFlag) other;
        return otherObj.key.equals(key);
    }
}
