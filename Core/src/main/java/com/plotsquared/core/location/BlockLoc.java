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

import com.plotsquared.core.util.StringMan;

public class BlockLoc {

    public static final BlockLoc ZERO = new BlockLoc(0, 0, 0);
    public static final BlockLoc MINY = new BlockLoc(0, Integer.MIN_VALUE, 0);
    private static final BlockLoc MIDDLE = new BlockLoc(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

    private final int x;
    private final int y;
    private final int z;

    private final float yaw;
    private final float pitch;

    public BlockLoc(int x, int y, int z) {
        this(x, y, z, 0f, 0f);
    }

    public BlockLoc(int x, int y, int z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static BlockLoc fromString(String string) {
        if (string == null || "side".equalsIgnoreCase(string)) {
            return null;
        } else if (StringMan.isEqualIgnoreCaseToAny(string, "center", "middle", "centre")) {
            return MIDDLE;
        } else {
            String[] parts = string.split(",");

            float yaw;
            float pitch;
            if (parts.length == 5) {
                yaw = Float.parseFloat(parts[3]);
                pitch = Float.parseFloat(parts[4]);
            } else if (parts.length == 3) {
                yaw = 0;
                pitch = 0;
            } else {
                return ZERO;
            }
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            return new BlockLoc(x, y, z, yaw, pitch);
        }
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.getX();
        result = prime * result + this.getY();
        result = prime * result + this.getZ();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return this.getX() == 0 && this.getY() == 0 && this.getZ() == 0;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BlockLoc other = (BlockLoc) obj;
        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other
                .getZ() && this.getYaw() == other.getYaw() && this.getPitch() == other.getPitch();
    }

    @Override
    public String toString() {
        if (this.getX() == 0 && this.getY() == 0 && this.getZ() == 0 && this.getYaw() == 0 && this.getPitch() == 0) {
            return "";
        }
        return this.getX() + "," + this.getY() + ',' + this.getZ() + ',' + this.getYaw() + ','
                + this.getPitch();

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

}
