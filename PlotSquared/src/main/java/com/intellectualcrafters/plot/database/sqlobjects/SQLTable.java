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

package com.intellectualcrafters.plot.database.sqlobjects;

import com.intellectualcrafters.plot.config.Settings;

/**
 * Created by Citymonstret on 2014-10-28.
 */
public abstract class SQLTable {

    private final String name;
    private final SQLField[] fields;

    public SQLTable(final String name, final String primaryKey, final SQLField... fields) {
        this.name = Settings.DB.PREFIX + name;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public SQLField[] getFields() {
        return this.fields;
    }

    public abstract void create();

}
