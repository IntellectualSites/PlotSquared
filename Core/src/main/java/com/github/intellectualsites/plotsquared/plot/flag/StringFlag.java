package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;

public class StringFlag extends Flag<String> {

    public StringFlag(String name) {
        super(Captions.FLAG_CATEGORY_STRING, name);
    }

    @Override public String valueToString(Object value) {
        return value.toString();
    }

    @Override public String parseValue(String value) {
        return value;
    }

    @Override public String getValueDescription() {
        return "Flag value must be alphanumeric. Some special characters are allowed.";
    }
}
