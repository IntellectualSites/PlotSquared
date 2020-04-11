package com.plotsquared.util;

/**
 *
 */
public class StringWrapper {

    public final String value;
    private int hash;

    /**
     * Constructor
     *
     * @param value to wrap
     */
    public StringWrapper(String value) {
        this.value = value;
    }

    /**
     * Check if a wrapped string equals another one
     *
     * @param obj to compare
     * @return true if obj equals the stored value
     */
    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            if (obj.getClass() == String.class) {
                return obj.toString().equalsIgnoreCase(this.value);
            }
            return false;
        }
        if (obj.hashCode() != hashCode()) {
            return false;
        }
        StringWrapper other = (StringWrapper) obj;
        if ((other.value == null) || (this.value == null)) {
            return false;
        }
        return other.value.equalsIgnoreCase(this.value);
    }

    /**
     * Get the string value.
     *
     * @return string value
     */
    @Override public String toString() {
        return this.value;
    }

    /**
     * Get the hash value.
     *
     * @return has value
     */
    @Override public int hashCode() {
        if (this.value == null) {
            return 0;
        }
        if (this.hash == 0) {
            this.hash = this.value.toLowerCase().hashCode();
        }
        return this.hash;
    }
}
