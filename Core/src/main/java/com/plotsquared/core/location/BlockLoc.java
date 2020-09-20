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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.location;

public class BlockLoc {

    private final int x;
    private final int y;
    private final int z;

    private final float yaw;
    private final float pitch;

    public BlockLoc(int x, int y, int z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public BlockLoc(int x, int y, int z) {
        this(x, y, z, 0f, 0f);
    }

    public static BlockLoc fromString(String string) {
        String[] parts = string.split(",");

        float yaw;
        float pitch;
        if (parts.length == 5) {
            yaw = Float.parseFloat(parts[3]);
            pitch = Float.parseFloat(parts[4]);
        } else {
            return new BlockLoc(0, 0, 0);
        }
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);

        return new BlockLoc(x, y, z, yaw, pitch);
    }

    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.getX();
        result = prime * result + this.getY();
        result = prime * result + this.getZ();
        return result;
    }

    @Override public boolean equals(Object obj) {
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
            .getZ();
    }

    @Override public String toString() {
        if (this.getX() == 0 && this.getY() == 0 && this.getZ() == 0) {
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
