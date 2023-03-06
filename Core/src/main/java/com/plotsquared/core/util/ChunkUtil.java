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
package com.plotsquared.core.util;

/**
 * This cache is used for world generation and just saves a bit of calculation time when checking if something is in the plot area.
 */
public class ChunkUtil {

    /**
     * Cache of mapping x,y,z coordinates to the chunk array<br>
     * - Used for efficient world generation<br>
     */
    private static final short[] x_loc;
    private static final short[] y_loc;
    private static final short[] z_loc;
    private static final short[][][] CACHE_J;

    static {
        x_loc = new short[4096];
        y_loc = new short[4096];
        z_loc = new short[4096];
        for (int j = 0; j < 4096; j++) {
            int y = j >> 8;
            int a = j - ((y & 0xF) << 8);
            int z1 = a >> 4;
            int x1 = a - (z1 << 4);
            x_loc[j] = (short) x1;
            y_loc[j] = (short) y;
            z_loc[j] = (short) z1;
        }
        CACHE_J = new short[16][16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    short j = (short) ((y & 0xF) << 8 | z << 4 | x);
                    CACHE_J[y][x][z] = j;
                }
            }
        }
    }

    private ChunkUtil() {
    }

    /**
     * Get the J value for Chunk block storage from the chunk xyz coordinates.
     * J is in the range 0 to 4095 where it represents a position in an array of 16x16x16 xyz (ChunkSection  Array[4096]).
     *
     * @param x Relative x coordinate
     * @param y Relative y coordinate
     * @param z Relative z coordinate
     * @return J value for xyz position in Array[4096].
     */
    public static int getJ(int x, int y, int z) {
        return CACHE_J[y & 15][x & 15][z & 15];
    }

    /**
     * Gets the x coordinate for a specific J value for a ChunkSection 16x16x16 xyz Array[4096].
     *
     * @param j Position in the xyz Array[4096].
     * @return x coordinate within the chunk
     */
    public static int getX(int j) {
        return x_loc[j];
    }

    /**
     * Gets the y coordinate for specific I and J values for a Chunk Nx16x16x16 layerxyz Array[N][4096].
     *
     * @param i Relative layer of the position in the layerxyz Array[16][4096]. May be negative.
     * @param j Position in the xyz Array[4096].
     * @return x coordinate within the chunk
     */
    public static int getY(int i, int j) {
        return (i << 4) + y_loc[j];
    }

    /**
     * Gets the z coordinate for a specific J value for a ChunkSection 16x16x16 xyz Array[4096].
     *
     * @param j Position in the xyz Array[4096].
     * @return z coordinate within the chunk
     */
    public static int getZ(int j) {
        return z_loc[j];
    }

}
