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
package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PS;

/**
 * Created 2014-09-29 for PlotSquared
 *
 * @author Citymonstret
 */
public class PlotSquaredException extends RuntimeException {
    public PlotSquaredException(final PlotError error, final String details) {
        super("PlotError >> " + error.getHeader() + ": " + details);
        PS.log("&cPlotError &6>> &c" + error.getHeader() + ": &6" + details);
    }

    public static enum PlotError {
        PLOTMAIN_NULL("The PlotSquared instance was null"),
        MISSING_DEPENDENCY("Missing Dependency");
        private final String errorHeader;

        PlotError(final String errorHeader) {
            this.errorHeader = errorHeader;
        }

        public String getHeader() {
            return this.errorHeader;
        }

        @Override
        public String toString() {
            return this.getHeader();
        }
    }
}
