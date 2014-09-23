package com.intellectualcrafters.plot;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

public class Flag {
    private String key;
    private String value;
    public Flag(String key, String value) {
        if (!StringUtils.isAlphanumeric(key) || !StringUtils.isAlphanumeric(ChatColor.stripColor(value).replace(" ", "")))
            throw new IllegalArgumentException("Flag must be alphanumerical");
        if (key.length()>16)
            throw new IllegalArgumentException("Key must be <= 16 characters");
        if (value.length()>48)
            throw new IllegalArgumentException("Value must be <= 48 characters");
        this.key = key.toLowerCase();
        this.value = value;
    }
    public String getKey() {
        return this.key;
    }
    public String getValue() {
        return this.value;
    }
    @Override
    public String toString() {
        if (this.value.equals("")) {
            return this.key;
        }
        return this.key+":"+this.value;
    }
    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Flag other = (Flag) obj;
        return (this.key==other.key && this.value == other.value);
    }
    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
