package com.intellectualcrafters.plot.flag;

public class IntegerFlag extends Flag<Integer> {

    public IntegerFlag(String name) {
        super(name);
    }

    @Override public String getValueDescription() {
        return "Flag value must be a whole number";
    }

    @Override public String valueToString(Object value) {
        return value.toString();
    }

    @Override public Integer parseValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
