package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegExUtil {

    public static Map<String, Pattern> compiledPatterns;

    static {
        compiledPatterns = new HashMap<>();
    }

}
