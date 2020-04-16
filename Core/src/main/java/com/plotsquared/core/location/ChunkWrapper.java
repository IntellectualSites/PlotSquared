/*
 *
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

import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.StringMan;

public class ChunkWrapper {

    public final int x;
    public final int z;
    public final String world;

    public ChunkWrapper(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override public int hashCode() {
        return MathMan.pair((short) x, (short) z);
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChunkWrapper other = (ChunkWrapper) obj;
        return (this.x == other.x) && (this.z == other.z) && StringMan
            .isEqual(this.world, other.world);
    }

    @Override public String toString() {
        return this.world + ":" + this.x + "," + this.z;
    }
}
