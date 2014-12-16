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

import org.apache.commons.lang.StringUtils;

public class Flag {
    private final AbstractFlag key;
    private final String       value;

    /**
     * Flag object used to store basic information for a Plot. Flags are a
     * key/value pair. For a flag to be usable by a player, you need to register
     * it with PlotSquared.
     *
     * @param key
     *            AbstractFlag
     * @param value
     *            Value must be alphanumerical (can have spaces) and be <= 48
     *            characters
     * @throws IllegalArgumentException
     *             if you provide inadequate inputs
     */
    public Flag(final AbstractFlag key, final String value) {
        final char[] allowedCharacters = new char[] { '[', ']', '(', ')', ',', '_', '-', '.', ',', '?', '!', '&', ':', '\u00A7' };
        String tempValue = value;
        for (final char c : allowedCharacters) {
            tempValue = tempValue.replace(c, 'c');
        }
        if (!StringUtils.isAlphanumericSpace(tempValue)) {
            throw new IllegalArgumentException("Flag must be alphanumerical (colours and some special characters are allowed)");
        }
        if (value.length() > 48) {
            throw new IllegalArgumentException("Value must be <= 48 characters");
        }
        this.key = key;
        this.value = key.parseValue(value);
        if (this.value == null) {
            throw new IllegalArgumentException(key.getValueDesc());
        }
    }

    /**
     * Get the AbstractFlag used in creating the flag
     *
     * @return AbstractFlag
     */
    public AbstractFlag getAbstractFlag() {
        return this.key;
    }

    /**
     * Get the key for the AbstractFlag
     *
     * @return String
     */
    public String getKey() {
        return this.key.getKey();
    }

    /**
     * Get the value
     *
     * @return String
     */
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        if (this.value.equals("")) {
            return this.key.getKey();
        }
        return this.key + ":" + this.value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Flag other = (Flag) obj;
        return (this.key.getKey().equals(other.key.getKey()) && this.value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return this.key.getKey().hashCode();
    }
}
