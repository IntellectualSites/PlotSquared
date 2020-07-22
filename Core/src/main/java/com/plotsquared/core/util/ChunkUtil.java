package com.plotsquared.core.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Range;

/**
 * This cache is used for world generation and just saves a bit of calculation time when checking if something is in the plot area.
 */
@UtilityClass
public class ChunkUtil {


    /**
     * Cache of mapping x,y,z coordinates to the chunk array<br>
     * - Used for efficient world generation<br>
     */
    private static short[] x_loc;
    private static short[][] y_loc;
    private static short[] z_loc;
    private static short[][][] CACHE_J = null;

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
}
