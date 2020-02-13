package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.LinkedHashMap;
import java.util.Map;

public class CaptionUtility {

    public static String format(String message, Object... args) {
        if (args.length == 0) {
            return message;
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = args.length - 1; i >= 0; i--) {
            String arg = "" + args[i];
            if (arg.isEmpty()) {
                map.put("%s" + i, "");
            } else {
                arg = Captions.color(arg);
                map.put("%s" + i, arg);
            }
            if (i == 0) {
                map.put("%s", arg);
            }
        }
        message = StringMan.replaceFromMap(message, map);
        return message;
    }

    public static String format(Caption caption, Object... args) {
        if (caption.usePrefix() && caption.getTranslated().length() > 0) {
            return Captions.PREFIX.getTranslated() + format(caption.getTranslated(), args);
        } else {
            return format(caption.getTranslated(), args);
        }
    }

}
