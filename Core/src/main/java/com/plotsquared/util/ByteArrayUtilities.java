package com.plotsquared.util;

public class ByteArrayUtilities {

    public static boolean bytesToBoolean(byte[] bytes) {
        return bytes[0] == 1;
    }

    public static byte[] booleanToBytes(boolean b) {
        return new byte[] {(byte) (b ? 1 : 0)};
    }
}
