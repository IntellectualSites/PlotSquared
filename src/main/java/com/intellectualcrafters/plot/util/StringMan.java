package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StringMan {
    public static String replaceFromMap(String string, Map<String, String> replacements) {
        StringBuilder sb = new StringBuilder(string);
        for (Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            int start = sb.indexOf(key, 0);
            while (start > -1) {
                int end = start + key.length();
                int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }
    
    public static String replaceAll(String string, Object... pairs) {
        HashMap<String, String> replacements = new HashMap<>();
        for (int i = 0; i < pairs.length; i+=2) {
            replacements.put(pairs[i] + "", pairs[i+1] + "");
        }
        return replaceFromMap(string, replacements);
    }
}
