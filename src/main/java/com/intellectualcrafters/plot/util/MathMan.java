package com.intellectualcrafters.plot.util;

public class MathMan {
    public static double getMean(int[] array) {
        double count = 0;
        for (int i : array) {
            count += i;
        }
        return count / array.length;
    }
    
    public static double getMean(double[] array) {
        double count = 0;
        for (double i : array) {
            count += i;
        }
        return count / array.length;
    }
    
    public static int getPositiveId(int i) {
        if (i < 0) {
            return -i*2 - 1;
        }
        return i * 2;
    }
    
    public static double getSD(double[] array, double av) {
        double sd = 0;
        for (int i=0; i<array.length;i++)
        {
            sd += Math.pow(Math.abs(array[i] - av), 2);
        }
        return Math.sqrt(sd/array.length);
    }
    
    public static double getSD(int[] array, double av) {
        double sd = 0;
        for (int i=0; i<array.length;i++)
        {
            sd += Math.pow(Math.abs(array[i] - av), 2);
        }
        return Math.sqrt(sd/array.length);
    }
    
    public static int mod(int x, int y) {
        if (isPowerOfTwo(y)) {
            return x & (y - 1);
        }
        return x % y;
    }
    
    public static int unsignedmod(int x, int y) {
        if (isPowerOfTwo(y)) {
            return x & (y - 1);
        }
        return x % y;
    }
    
    public static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }
}
