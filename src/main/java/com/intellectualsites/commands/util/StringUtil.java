package com.intellectualsites.commands.util;

public class StringUtil {

    public static final String[] emptyArray = new String[0];

    public static boolean startsWith(char c, String s) {
        return !(s == null || s.isEmpty()) && s.toCharArray()[0] == c;
    }

    public static boolean inArray(String s, String[] a, boolean matchCase) {
        for (String aS : a) {
            if (matchCase) {
                if (s.equals(aS)) {
                    return true;
                }
            } else {
                if (s.equalsIgnoreCase(aS)) {
                    return true;
                }
            }
        }
        return false;
    }

}
