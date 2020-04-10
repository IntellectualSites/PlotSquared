/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.plot.object;

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
