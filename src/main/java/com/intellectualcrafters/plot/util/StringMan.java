package com.intellectualcrafters.plot.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

public class StringMan {
    public static String replaceFromMap(final String string, final Map<String, String> replacements) {
        final StringBuilder sb = new StringBuilder(string);
        int size = string.length();
        for (final Entry<String, String> entry : replacements.entrySet()) {
            if (size == 0) {
                break;
            }
            final String key = entry.getKey();
            final String value = entry.getValue();
            int start = sb.indexOf(key, 0);
            while (start > -1) {
                final int end = start + key.length();
                final int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                size -= end - start;
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }
    
    public static String getString(final Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        if (obj.getClass().isArray()) {
            String result = "";
            String prefix = "";
            
            for (int i = 0; i < Array.getLength(obj); i++) {
                result += prefix + getString(Array.get(obj, i));
                prefix = ",";
            }
            return "( " + result + " )";
        } else if (obj instanceof Collection<?>) {
            String result = "";
            String prefix = "";
            for (final Object element : (Collection<?>) obj) {
                result += prefix + getString(element);
                prefix = ",";
            }
            return "[ " + result + " ]";
        } else {
            return obj.toString();
        }
    }
    
    public static String replaceFirst(final char c, final String s) {
        if (s == null) {
            return "";
        }
        if (s.isEmpty()) {
            return s;
        }
        char[] chars = s.toCharArray();
        final char[] newChars = new char[chars.length];
        int used = 0;
        boolean found = false;
        for (final char cc : chars) {
            if (!found && (c == cc)) {
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
    
    public static String replaceAll(final String string, final Object... pairs) {
        final StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < pairs.length; i += 2) {
            final String key = pairs[i] + "";
            final String value = pairs[i + 1] + "";
            int start = sb.indexOf(key, 0);
            while (start > -1) {
                final int end = start + key.length();
                final int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }
    
    public static boolean isAlphanumeric(final String str) {
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if ((c < 0x30) || ((c >= 0x3a) && (c <= 0x40)) || ((c > 0x5a) && (c <= 0x60)) || (c > 0x7a)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isAlphanumericUnd(final String str) {
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if ((c < 0x30) || ((c >= 0x3a) && (c <= 0x40)) || ((c > 0x5a) && (c <= 0x60)) || (c > 0x7a) || (c == '_')) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isAlpha(final String str) {
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if ((c <= 0x40) || ((c > 0x5a) && (c <= 0x60)) || (c > 0x7a)) {
                return false;
            }
        }
        return true;
    }
    
    public static String join(final Collection<?> collection, final String delimiter) {
        return join(collection.toArray(), delimiter);
    }
    
    public static String joinOrdered(final Collection<?> collection, final String delimiter) {
        final Object[] array = collection.toArray();
        Arrays.sort(array, new Comparator<Object>() {
            @Override
            public int compare(final Object a, final Object b) {
                return a.hashCode() - b.hashCode();
            }
            
        });
        return join(array, delimiter);
    }
    
    public static String join(final Collection<?> collection, final char delimiter) {
        return join(collection.toArray(), delimiter + "");
    }
    
    public static boolean isAsciiPrintable(final char c) {
        return (c >= ' ') && (c < '');
    }
    
    public static boolean isAsciiPrintable(final String s) {
        for (final char c : s.toCharArray()) {
            if (!isAsciiPrintable(c)) {
                return false;
            }
        }
        return true;
    }
    
    public static int getLevenshteinDistance(String s, String t) {
        int n = s.length();
        int m = t.length();
        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }
        if (n > m) {
            final String tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = t.length();
        }
        int p[] = new int[n + 1];
        int d[] = new int[n + 1];
        int _d[];
        int i;
        int j;
        char t_j;
        int cost;
        for (i = 0; i <= n; i++) {
            p[i] = i;
        }
        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;
            
            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }
            _d = p;
            p = d;
            d = _d;
        }
        return p[n];
    }
    
    public static String join(final Object[] array, final String delimiter) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0, j = array.length; i < j; i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append(array[i]);
        }
        return result.toString();
    }
    
    public static String join(final int[] array, final String delimiter) {
        final Integer[] wrapped = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            wrapped[i] = array[i];
        }
        return join(wrapped, delimiter);
    }
    
    public static boolean isEqualToAny(final String a, final String... args) {
        for (final String arg : args) {
            if (StringMan.isEqual(a, arg)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isEqualIgnoreCaseToAny(final String a, final String... args) {
        for (final String arg : args) {
            if (StringMan.isEqualIgnoreCase(a, arg)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isEqual(final String a, final String b) {
        return ((a == b) || ((a != null) && (b != null) && (a.length() == b.length()) && (a.hashCode() == b.hashCode()) && a.equals(b)));
    }
    
    public static boolean isEqualIgnoreCase(final String a, final String b) {
        return ((a == b) || ((a != null) && (b != null) && (a.length() == b.length()) && a.equalsIgnoreCase(b)));
    }
    
    public static String repeat(final String s, final int n) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
