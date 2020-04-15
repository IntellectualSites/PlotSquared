package com.plotsquared.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegExUtil {

    public static Map<String, Pattern> compiledPatterns;

    static {
        compiledPatterns = new HashMap<>();
    }

}
