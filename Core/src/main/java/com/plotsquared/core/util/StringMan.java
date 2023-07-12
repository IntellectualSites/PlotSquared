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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class StringMan {

    // Stolen from https://stackoverflow.com/a/366532/12620913 | Debug: https://regex101.com/r/DudJLb/1
    private static final Pattern STRING_SPLIT_PATTERN = Pattern.compile("[^\\s\"]+|\"([^\"]*)\"");

    public static int intersection(Set<String> options, String[] toCheck) {
        int count = 0;
        for (String check : toCheck) {
            if (options.contains(check)) {
                count++;
            }
        }
        return count;
    }

    public static boolean isAlphanumericUnd(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x30 || (c >= 0x3a) && (c <= 0x40) || (c > 0x5a) && (c <= 0x60) || (c > 0x7a)) {
                return false;
            }
        }
        return true;
    }

    public static String join(Collection<?> collection, String delimiter) {
        return join(collection.toArray(), delimiter);
    }

    public static String joinOrdered(Collection<?> collection, String delimiter) {
        Object[] array = collection.toArray();
        Arrays.sort(array, Comparator.comparingInt(Object::hashCode));
        return join(array, delimiter);
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
            String tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = t.length();
        }
        int[] p = new int[n + 1];
        int[] d = new int[n + 1];
        int i;
        for (i = 0; i <= n; i++) {
            p[i] = i;
        }
        for (int j = 1; j <= m; j++) {
            char t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                int cost = s.charAt(i - 1) == t_j ? 0 : 1;
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }
            int[] _d = p;
            p = d;
            d = _d;
        }
        return p[n];
    }

    public static String join(Object[] array, String delimiter) {
        StringBuilder result = new StringBuilder();
        for (int i = 0, j = array.length; i < j; i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append(array[i]);
        }
        return result.toString();
    }

    public static boolean isEqualIgnoreCaseToAny(@NonNull String a, String... args) {
        for (String arg : args) {
            if (a.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEqual(String a, String b) {
        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        } else if (a == null /* implies that b is null */) {
            return false;
        }
        return a.equals(b);
    }

    public static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(s).repeat(Math.max(0, n)));
        return sb.toString();
    }

    /**
     * @param message an input string
     * @return a list of strings
     * @since 6.4.0
     *
     *         <table border="1">
     *         <caption>Converts multiple quoted and single strings into a list of strings</caption>
     *         <thead>
     *           <tr>
     *             <th>Input</th>
     *             <th>Output</th>
     *           </tr>
     *         </thead>
     *         <tbody>
     *           <tr>
     *             <td>title "sub title"</td>
     *             <td>["title", "sub title"]</td>
     *           </tr>
     *           <tr>
     *             <td>"a title" subtitle</td>
     *             <td>["a title", "subtitle"]</td>
     *           </tr>
     *           <tr>
     *             <td>"title" "subtitle"</td>
     *             <td>["title", "subtitle"]</td>
     *           </tr>
     *           <tr>
     *             <td>"PlotSquared is going well" the authors "and many contributors"</td>
     *             <td>["PlotSquared is going well", "the", "authors", "and many contributors"]</td>
     *           </tr>
     *         </tbody>
     *         </table>
     */
    public static @NonNull List<String> splitMessage(@NonNull String message) {
        var matcher = StringMan.STRING_SPLIT_PATTERN.matcher(message);
        List<String> splitMessages = new ArrayList<>();
        while (matcher.find()) {
            splitMessages.add(matcher.group(matcher.groupCount() - 1).replaceAll("\"", ""));
        }
        return splitMessages;
    }

}
