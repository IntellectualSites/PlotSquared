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

/**
 * Created 2014-09-23 for PlotSquared
 *
 * @author Citymonstret
 * @author Empire92
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
        return this.value instanceof FlagValue.ListValue;
    }

    public Object parseValueRaw(final String value) {
        try {
            return this.value.parse(value);
        } catch (final Exception e) {
            return null;
        }
    }

    public String toString(final Object t) {
        return this.value.toString(t);
    }

    public String getValueDesc() {
        return this.value.getDescription();
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
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof AbstractFlag)) {
            return false;
        }
        final AbstractFlag otherObj = (AbstractFlag) other;
        return (otherObj.key.equals(this.key));
    }
}
