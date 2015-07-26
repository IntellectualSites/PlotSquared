package com.intellectualsites.commands.util;

public class StringUtil {

    public static final String[] emptyArray = new String[0];

    public static boolean startsWith(char c, String s) {
        return !(s == null || s.isEmpty()) && s.toCharArray()[0] == c;
    }

    public static String replaceFirst(char c, String s) {
        if (s == null) {
            return "";
        }
        if (s.isEmpty()) {
            return s;
        }
        char[] chars = s.toCharArray();
        char[] newChars = new char[chars.length];
        int used = 0;
        boolean found = false;
        for (char cc : chars) {
            if (!found && c == cc) {
                found = true;
            } else {
                newChars[used++] = cc;
            }
        }
        if (found) {
            chars = new char[newChars.length - 1];
            System.arraycopy(newChars, 0, chars, 0, chars.length);
            return String.valueOf(chars);
        }
        return s;
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
