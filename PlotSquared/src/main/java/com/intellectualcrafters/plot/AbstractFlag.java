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

package com.intellectualcrafters.plot;

import org.apache.commons.lang.StringUtils;

/**
 * Created by Citymonstret on 2014-09-23.
 */
public class AbstractFlag {

    private final String key;

    /**
     * AbstractFlag is a parameter used in creating a new Flag
     *
     * @param key The key must be alphabetical characters and <= 16 characters
     *            in length
     */
    public AbstractFlag(final String key) {
        if (!StringUtils.isAlpha(key.replaceAll("_", "").replaceAll("-", ""))) {
            throw new IllegalArgumentException("Flag must be alphabetic characters");
        }
        if (key.length() > 16) {
            throw new IllegalArgumentException("Key must be <= 16 characters");
        }
        this.key = key.toLowerCase();
    }

    public String parseValue(final String value) {
        return value;
    }

    public String getValueDesc() {
        return "Flag value must be alphanumeric";
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
