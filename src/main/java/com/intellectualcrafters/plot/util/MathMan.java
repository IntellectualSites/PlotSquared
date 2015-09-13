package com.intellectualcrafters.plot.util;

public class MathMan {
    public static double getMean(final int[] array) {
        double count = 0;
        for (final int i : array) {
            count += i;
        }
        return count / array.length;
    }
    
    public static double getMean(final double[] array) {
        double count = 0;
        for (final double i : array) {
            count += i;
        }
        return count / array.length;
    }
    
    /**
     * Returns [x, y, z]
     * @param yaw
     * @param pitch
     * @return
     */
    public static float[] getDirection(final float yaw, final float pitch) {
        final double pitch_sin = Math.sin(pitch);
        return new float[] { (float) (pitch_sin * Math.cos(yaw)), (float) (pitch_sin * Math.sin(yaw)), (float) Math.cos(pitch) };
    }
    
    public static int roundInt(final double value) {
        return (int) (value < 0 ? (value == (int) value) ? value : value - 1 : value);
    }
    
    /**
     * Returns [ pitch, yaw ]
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static float[] getPitchAndYaw(final float x, final float y, final float z) {
        final float distance = sqrtApprox((z * z) + (x * x));
        return new float[] { atan2(y, distance), atan2(x, z) };
    }
    
    private static final int ATAN2_BITS = 7;
    
    private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
    private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
    private static final int ATAN2_COUNT = ATAN2_MASK + 1;
    private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
    
    private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
    
    private static final float[] atan2 = new float[ATAN2_COUNT];
    
    static {
        for (int i = 0; i < ATAN2_DIM; i++) {
            for (int j = 0; j < ATAN2_DIM; j++) {
                final float x0 = (float) i / ATAN2_DIM;
                final float y0 = (float) j / ATAN2_DIM;
                
                atan2[(j * ATAN2_DIM) + i] = (float) Math.atan2(y0, x0);
            }
        }
    }
    
    public static final float atan2(float y, float x) {
        float add, mul;
        
        if (x < 0.0f) {
            if (y < 0.0f) {
                x = -x;
                y = -y;
                
                mul = 1.0f;
            } else {
                x = -x;
                mul = -1.0f;
            }
            
            add = -3.141592653f;
        } else {
            if (y < 0.0f) {
                y = -y;
                mul = -1.0f;
            } else {
                mul = 1.0f;
            }
            
            add = 0.0f;
        }
        
        final float invDiv = 1.0f / (((x < y) ? y : x) * INV_ATAN2_DIM_MINUS_1);
        
        final int xi = (int) (x * invDiv);
        final int yi = (int) (y * invDiv);
        
        return (atan2[(yi * ATAN2_DIM) + xi] + add) * mul;
    }
    
    public static float sqrtApprox(final float f) {
        return f * Float.intBitsToFloat(0x5f375a86 - (Float.floatToIntBits(f) >> 1));
    }
    
    public static double sqrtApprox(final double d) {
        return Double.longBitsToDouble(((Double.doubleToLongBits(d) - (1l << 52)) >> 1) + (1l << 61));
    }
    
    public static float invSqrt(float x) {
        final float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - (xhalf * x * x));
        return x;
    }
    
    public static int getPositiveId(final int i) {
        if (i < 0) {
            return (-i * 2) - 1;
        }
        return i * 2;
    }
    
    public static boolean isInteger(final String str) {
        if (str == null) {
            return false;
        }
        final int length = str.length();
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
            final char c = str.charAt(i);
            if ((c <= '/') || (c >= ':')) {
                return false;
            }
        }
        return true;
    }
    
    public static double getSD(final double[] array, final double av) {
        double sd = 0;
        for (final double element : array) {
            sd += Math.pow(Math.abs(element - av), 2);
        }
        return Math.sqrt(sd / array.length);
    }
    
    public static double getSD(final int[] array, final double av) {
        double sd = 0;
        for (final int element : array) {
            sd += Math.pow(Math.abs(element - av), 2);
        }
        return Math.sqrt(sd / array.length);
    }
    
    public static int mod(final int x, final int y) {
        if (isPowerOfTwo(y)) {
            return x & (y - 1);
        }
        return x % y;
    }
    
    public static int unsignedmod(final int x, final int y) {
        if (isPowerOfTwo(y)) {
            return x & (y - 1);
        }
        return x % y;
    }
    
    public static boolean isPowerOfTwo(final int x) {
        return (x & (x - 1)) == 0;
    }
}
