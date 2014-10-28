package com.intellectualcrafters.plot;

/**
 * @author Empire92
 */
public class StringWrapper {
    public String value;

    /**
     * Constructor
     * @param value to wrap
     */
    public StringWrapper(String value) {
        this.value = value;
    }

    /**
     * Check if a wrapped string equals another one
     * @param obj to compare
     * @return true if obj equals the stored value
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StringWrapper other = (StringWrapper) obj;
        return other.value.toLowerCase().equals(this.value.toLowerCase());
    }

    /**
     * Get the string value
     * @return string value
     */
    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Get the hash value
     * @return has value
     */
    @Override
    public int hashCode() {
        return this.value.toLowerCase().hashCode();
    }
}
