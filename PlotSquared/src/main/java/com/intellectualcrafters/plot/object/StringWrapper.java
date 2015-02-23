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
package com.intellectualcrafters.plot.object;

/**
 * @author Empire92
 */
public class StringWrapper {
    public final String value;

    /**
     * Constructor
     *
     * @param value to wrap
     */
    public StringWrapper(final String value) {
        this.value = value;
    }

    /**
     * Check if a wrapped string equals another one
     *
     * @param obj to compare
     *
     * @return true if obj equals the stored value
     */
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
        final StringWrapper other = (StringWrapper) obj;
        if ((other.value == null) || (this.value == null)) {
            return false;
        }
        return other.value.toLowerCase().equals(this.value.toLowerCase());
    }

    /**
     * Get the string value
     *
     * @return string value
     */
    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Get the hash value
     *
     * @return has value
     */
    @Override
    public int hashCode() {
        if (this.value == null) {
            return 0;
        }
        return this.value.toLowerCase().hashCode();
    }
}
