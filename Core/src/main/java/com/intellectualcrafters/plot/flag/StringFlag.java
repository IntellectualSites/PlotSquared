package com.intellectualcrafters.plot.flag;


public class StringFlag extends Flag<String> {

    public StringFlag(String name) {
        super(name);
    }

    @Override public String valueToString(Object value) {
        return ((String) value);
    }

    @Override public String parseValue(String value) {
        return null;
    }

    @Override public String getValueDescription() {
        return null;
    }
}
