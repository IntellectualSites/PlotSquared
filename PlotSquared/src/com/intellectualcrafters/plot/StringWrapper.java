package com.intellectualcrafters.plot;

public class StringWrapper {
    public String value;
    
    public StringWrapper(String value) {
        this.value = value;
    }
    
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

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.toLowerCase().hashCode();
    }
}
