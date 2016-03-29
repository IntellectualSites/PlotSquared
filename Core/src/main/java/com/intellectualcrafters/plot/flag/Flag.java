////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.flag;

import com.intellectualcrafters.plot.util.StringMan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Flag<T> implements Cloneable {
    private AbstractFlag key;
    private Object value;
    private String name;

    /**
     * Flag object used to store basic information for a Plot. Flags are a
     * key/value pair. For a flag to be usable by a player, you need to
     * register it with PlotSquared.
     *
     * @param key   AbstractFlag
     * @param value Value must be alphanumerical (can have spaces) and be &lt;= 48 characters
     *
     * @throws IllegalArgumentException if you provide inadequate inputs
     */
    public Flag(AbstractFlag key, String value) {
        if (!StringMan.isAsciiPrintable(value)) {
            throw new IllegalArgumentException("Flag must be ascii");
        }
        if (value.length() > 128) {
            throw new IllegalArgumentException("Value must be <= 128 characters");
        }
        this.key = key;
        this.value = key.parseValueRaw(value);
        if (this.value == null) {
            throw new IllegalArgumentException(key.getValueDesc() + " (" + value + ")");
        }
    }
    
    /**
     * Warning: Unchecked
     * @param key
     * @param value
     */
    public Flag(AbstractFlag key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Flag(String name) {
        this.name = name;
    }
    
    /**
     * Get the AbstractFlag used in creating the flag.
     *
     * @return AbstractFlag
     */
    public AbstractFlag getAbstractFlag() {
        return this.key;
    }
    
    /**
     * Get the key for the AbstractFlag.
     *
     * @return String
     */
    public String getKey() {
        return this.key.getKey();
    }

    public void setKey(AbstractFlag key) {
        this.key = key;
        if (this.value instanceof String) {
            this.value = key.parseValueRaw((String) this.value);
        }
    }

    /**
     * Get the value.
     *
     * @return String
     */
    public Object getValue() {
        return this.value;
    }
    
    public String getValueString() {
        return this.key.toString(this.value);
    }
    
    @Override
    public String toString() {
        if ("".equals(this.value)) {
            return this.key.getKey();
        }
        return this.key + ":" + getValueString();
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
        Flag other = (Flag) obj;
        return this.key.getKey().equals(other.key.getKey()) && this.value.equals(other.value);
    }
    
    @Override
    public int hashCode() {
        return this.key.getKey().hashCode();
    }
    
    @Override
    protected Object clone() {
        try {
            if (this.value == null) {
                return super.clone();
            }
            if (this.value instanceof Cloneable) {
                Method method = this.value.getClass().getDeclaredMethod("clone");
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return new Flag(this.key, method.invoke(this.value));
            }
            return new Flag(this.key, this.key.parseValueRaw(this.key.toString(this.value)));
        } catch (CloneNotSupportedException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getName() {
        return this.name;
    }
}
