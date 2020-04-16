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
package com.plotsquared.core.location;

public enum Direction {
    ALL(-1, "all"), NORTH(0, "north"), EAST(1, "east"), SOUTH(2, "south"), WEST(3,
        "west"), NORTHEAST(4, "northeast"), SOUTHEAST(5, "southeast"), SOUTHWEST(6,
        "southwest"), NORTHWEST(7, "northwest"),
    ;


    private int index;
    private String name;

    Direction(int index, String name) {

        this.index = index;
        this.name = name;
    }

    public static Direction getFromIndex(int index) {
        for (Direction value : values()) {
            if (value.getIndex() == index) {
                return value;
            }
        }
        return NORTH;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
