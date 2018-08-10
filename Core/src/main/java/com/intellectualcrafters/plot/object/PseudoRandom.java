package com.intellectualcrafters.plot.object;

public class PseudoRandom {

    public static final PseudoRandom random = new PseudoRandom();

    public long state = System.nanoTime();

    public long nextLong() {
        long a = this.state;
        this.state = xorShift64(a);
        return a;
    }

    public long xorShift64(long a) {
        a ^= a << 21;
        a ^= a >>> 35;
        a ^= a << 4;
        return a;
    }

    public int random(int n) {
        if (n == 1) {
            return 0;
        }
        long r = ((nextLong() >>> 32) * n) >> 32;
        return (int) r;
    }
}
