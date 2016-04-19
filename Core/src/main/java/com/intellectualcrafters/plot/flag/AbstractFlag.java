package com.intellectualcrafters.plot.flag;

/**
 * Created 2014-09-23 for PlotSquared
 *


 */
public class AbstractFlag<T extends FlagValue> {
    public final String key;
    public final T value = null;

    public AbstractFlag(String key) {
        this(key, new FlagValue.StringValue());
    }
    
    /**
     * AbstractFlag is a parameter used in creating a new Flag<br>
     * The key must be alphabetical characters and &lt;= 16 characters in length
     * @param key
     */
    public AbstractFlag(String key, FlagValue<?> value) {
        this.key = key.toLowerCase();
        //if (value == null) {
        //    this.value = new FlagValue.StringValue();
        //} else {
        //    this.value = value;
        //}
    }
    
    public boolean isList() {
        return this.value instanceof FlagValue.ListValue;
    }

    public Object parseValueRaw(String value) {
        try {
            return this.value.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    public String toString(Object t) {
        return this.value.toString(t);
    }

    /**
     * AbstractFlag key
     *
     * @return String
     */
    public String getKey() {
        return this.key;
    }
    
    @Override
    public String toString() {
        return this.key;
    }
    
    @Override
    public int hashCode() {
        return this.key.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AbstractFlag)) {
            return false;
        }
        AbstractFlag otherObj = (AbstractFlag) other;
        return otherObj.key.equals(this.key);
    }
}
