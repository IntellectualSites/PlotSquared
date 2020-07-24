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
package com.plotsquared.core.util;

import com.plotsquared.core.location.Location;
import com.sk89q.worldedit.math.BlockVector2;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Range;

import javax.annotation.Nonnull;

/**
 * This cache is used for world generation and just saves a bit of calculation time when checking if something is in the plot area.
 */
@UtilityClass
public class ChunkUtil {


    /**
     * Cache of mapping x,y,z coordinates to the chunk array<br>
     * - Used for efficient world generation<br>
     */
    private static final short[] x_loc;
    private static final short[][] y_loc;
    private static final short[] z_loc;
    private static final short[][][] CACHE_J;

    static {
        x_loc = new short[4096];
        y_loc = new short[16][4096];
        z_loc = new short[4096];
        for (int i = 0; i < 16; i++) {
            int i4 = i << 4;
            for (int j = 0; j < 4096; j++) {
                int y = i4 + (j >> 8);
                int a = j - ((y & 0xF) << 8);
                int z1 = a >> 4;
                int x1 = a - (z1 << 4);
                x_loc[j] = (short) x1;
                y_loc[i][j] = (short) y;
                z_loc[j] = (short) z1;
            }
        }
        CACHE_J = new short[256][16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    short j = (short) ((y & 0xF) << 8 | z << 4 | x);
                    CACHE_J[y][x][z] = j;
                }
            }
        }
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
    public static int getJ(@Range(from = 0, to = 15) int x, @Range(from = 0, to = 255) int y,
        @Range(from = 0, to = 15) int z) {
        return CACHE_J[y][x][z];
    }

    /**
     * Gets the x coordinate for a specific J value for a ChunkSection 16x16x16 xyz Array[4096].
     *
     * @param j Position in the xyz Array[4096].
     * @return x coordinate within the chunk
     */
    public static int getX(@Range(from = 0, to = 4095) int j) {
        return x_loc[j];
    }

    /**
     * Gets the y coordinate for specific I and J values for a Chunk 16x16x16x16 layerxyz Array[16][4096].
     *
     * @param i Relative layer of the position in the layerxyz Array[16][4096].
     * @param j Position in the xyz Array[4096].
     * @return x coordinate within the chunk
     */
    public static int getY(@Range(from = 0, to = 15) int i, @Range(from = 0, to = 4095) int j) {
        return y_loc[i][j];
    }

    /**
     * Gets the z coordinate for a specific J value for a ChunkSection 16x16x16 xyz Array[4096].
     *
     * @param j Position in the xyz Array[4096].
     * @return z coordinate within the chunk
     */
    public static int getZ(@Range(from = 0, to = 4095) int j) {
        return z_loc[j];
    }

    /**
     * Returns true if the region pos1-pos2 contains the chunk
     *
     * @param pos1  Region minimum point
     * @param pos2  Region maximum point
     * @param chunk BlockVector2 of chunk coordinates
     * @return true if the region pos1-pos2 contains the chunk
     */
    public static boolean isWholeChunk(@Nonnull Location pos1, @Nonnull Location pos2, @Nonnull BlockVector2 chunk) {
        int x1 = pos1.getX();
        int z1 = pos1.getZ();
        int x2 = pos2.getX();
        int z2 = pos2.getZ();
        int cx = chunk.getX() << 4;
        int cz = chunk.getZ() << 4;
        return cx > x1 && cz > z1 && cx < x2 && cz < z2;
    }
}
