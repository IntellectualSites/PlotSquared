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

/**
 * Created by Citymonstret on 2014-10-28.
 */
public enum SQLType {

    INTEGER(0, "integer", Integer.class, 11),
    VARCHAR("", "varchar", String.class, 300),
    BOOL(false, "bool", Boolean.class, 1);

    private Object defaultValue;
    private String sqlName;
    private Class javaClass;
    private int length;

    SQLType(final Object defaultValue, final String sqlName, final Class javaClass, final int length) {
        this.defaultValue = defaultValue;
        this.sqlName = sqlName;
        this.javaClass = javaClass;
        this.length = length;
    }

    public int getLength() {
        return this.length;
    }

    @Override
    public String toString() {
        return this.sqlName;
    }

    public Class getJavaClass() {
        return this.javaClass;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }
}
