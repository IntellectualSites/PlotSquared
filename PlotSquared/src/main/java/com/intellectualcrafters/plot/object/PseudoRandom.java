package com.intellectualcrafters.plot.object;

public class PseudoRandom {
    private static long state = 1;

    public static long nextLong() {
        final long a = state;
        state = xorShift64(a);
        return a;
    }

    public static long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public static int random(final int n) {
        if (n == 1) {
            return 0;
        }
        final long r = ((nextLong() >>> 32) * n) >> 32;
        return (int) r;
    }
}
