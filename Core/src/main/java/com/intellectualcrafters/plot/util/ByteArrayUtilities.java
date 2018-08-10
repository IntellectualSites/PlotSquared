package com.intellectualcrafters.plot.util;

public class ByteArrayUtilities {

    public static byte[] integerToBytes(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (i >> 24);
        bytes[1] = (byte) (i >> 16);
        bytes[2] = (byte) (i >> 8);
        bytes[3] = (byte) (i);
        return bytes;
    }

    public static int bytesToInteger(byte[] bytes) {
        return (bytes[0] << 24) & 0xff000000 | (bytes[1] << 16) & 0x00ff0000
            | (bytes[2] << 8) & 0x0000ff00 | (bytes[3]) & 0x000000ff;
    }

    public static boolean bytesToBoolean(byte[] bytes) {
        return bytes[0] == 1;
    }

    public static byte[] booleanToBytes(boolean b) {
        return new byte[] {(byte) (b ? 1 : 0)};
    }
}
