package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;

public final class Flags {

    public static final LongFlag TIME = new LongFlag("time");
    public static final StringListFlag BLOCKED_CMDS = new StringListFlag("blocked-cmds");
    public static final Flag<?> KEEP = new Flag(Captions.FLAG_CATEGORY_MIXED, "keep") {
        @Override public String valueToString(Object value) {
            return value.toString();
        }

        @Override public Object parseValue(String value) {
            if (MathMan.isInteger(value)) {
                return Long.parseLong(value);
            }
            switch (value.toLowerCase()) {
                case "true":
                    return true;
                case "false":
                    return false;
                default:
                    return MainUtil.timeToSec(value) * 1000 + System.currentTimeMillis();
            }
        }

        @Override public String getValueDescription() {
            return Captions.FLAG_ERROR_KEEP.getTranslated();
        }
    };

}
