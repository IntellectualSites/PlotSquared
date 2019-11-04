package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;

public class LongFlag extends Flag<Long> {

    public LongFlag(String name) {
        super(Captions.FLAG_CATEGORY_INTEGERS, name);
    }

    @Override public Long parseValue(String value) {
        try {
            return Long.parseLong(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override public String valueToString(Object value) {
        return value.toString();
    }

    @Override public String getValueDescription() {
        return "Flag value must be a whole number (large numbers allowed)";
    }
}
