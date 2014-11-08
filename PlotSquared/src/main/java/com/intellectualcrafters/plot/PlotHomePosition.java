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

/**
 * Created by Citymonstret on 2014-08-05.
 */
public enum PlotHomePosition {
    CENTER("Center", 'c'),
    DEFAULT("Default", 'd');

    private String string;
    private char ch;

    PlotHomePosition(final String string, final char ch) {
        this.string = string;
        this.ch = ch;
    }

    public boolean isMatching(final String string) {
        if ((string.length() < 2) && (string.charAt(0) == this.ch)) {
            return true;
        }
        if (string.equalsIgnoreCase(this.string)) {
            return true;
        }
        return false;
    }

}
