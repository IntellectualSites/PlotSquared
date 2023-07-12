/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.location;

import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.StringMan;

public record ChunkWrapper(
        String world,
        int x,
        int z
) {

    @Override
    public int hashCode() {
        return MathMan.pair((short) x, (short) z);
    }

    @Override
    public boolean equals(Object obj) {
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

    @Override
    public String toString() {
        return this.world + ":" + this.x + "," + this.z;
    }

}
