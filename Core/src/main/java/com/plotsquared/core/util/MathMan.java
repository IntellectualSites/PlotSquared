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

public class MathMan {

    private static final int ATAN2_BITS = 7;
    private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
    private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
    private static final int ATAN2_COUNT = ATAN2_MASK + 1;
    private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
    private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
    private static final float[] atan2 = new float[ATAN2_COUNT];
    private static final int[] table =
            {0, 16, 22, 27, 32, 35, 39, 42, 45, 48, 50, 53, 55, 57, 59, 61, 64, 65, 67, 69, 71, 73, 75,
                    76, 78, 80, 81, 83, 84, 86, 87, 89, 90, 91, 93, 94, 96, 97, 98, 99, 101, 102, 103, 104,
                    106, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123,
                    124, 125, 126, 128, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140,
                    141, 142, 143, 144, 144, 145, 146, 147, 148, 149, 150, 150, 151, 152, 153, 154, 155,
                    155, 156, 157, 158, 159, 160, 160, 161, 162, 163, 163, 164, 165, 166, 167, 167, 168,
                    169, 170, 170, 171, 172, 173, 173, 174, 175, 176, 176, 177, 178, 178, 179, 180, 181,
                    181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188, 189, 189, 190, 191, 192, 192,
                    193, 193, 194, 195, 195, 196, 197, 197, 198, 199, 199, 200, 201, 201, 202, 203, 203,
                    204, 204, 205, 206, 206, 207, 208, 208, 209, 209, 210, 211, 211, 212, 212, 213, 214,
                    214, 215, 215, 216, 217, 217, 218, 218, 219, 219, 220, 221, 221, 222, 222, 223, 224,
                    224, 225, 225, 226, 226, 227, 227, 228, 229, 229, 230, 230, 231, 231, 232, 232, 233,
                    234, 234, 235, 235, 236, 236, 237, 237, 238, 238, 239, 240, 240, 241, 241, 242, 242,
                    243, 243, 244, 244, 245, 245, 246, 246, 247, 247, 248, 248, 249, 249, 250, 250, 251,
                    251, 252, 252, 253, 253, 254, 254, 255};

    static {
        for (int i = 0; i < ATAN2_DIM; i++) {
            for (int j = 0; j < ATAN2_DIM; j++) {
                float x0 = (float) i / ATAN2_DIM;
                float y0 = (float) j / ATAN2_DIM;

                atan2[(j * ATAN2_DIM) + i] = (float) Math.atan2(y0, x0);
            }
        }
    }

    public static final int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    public static final int gcd(int[] a) {
        int result = a[0];
        for (int i = 1; i < a.length; i++) {
            result = gcd(result, a[i]);
        }
        return result;
    }

    public static double getMean(int[] array) {
        double count = 0;
        for (int i : array) {
            count += i;
        }
        return count / array.length;
    }

    public static int pair(short x, short y) {
        return (x << 16) | (y & 0xFFFF);
    }

    public static final int average(int a, int b) {
        return (a & b) + (a ^ b) / 2;
    }

    public static int roundInt(double value) {
        return (int) (value < 0 ? (value == (int) value) ? value : value - 1 : value);
    }

    public static int getPositiveId(int i) {
        if (i < 0) {
            return (-i * 2) - 1;
        }
        return i * 2;
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if ((c <= '/') || (c >= ':')) {
                return false;
            }
        }
        return true;
    }

    public static double getSD(int[] array, double av) {
        double sd = 0;
        for (int element : array) {
            sd += Math.pow(Math.abs(element - av), 2);
        }
        return Math.sqrt(sd / array.length);
    }

}
