package com.intellectualcrafters.plot.flag;

public class DoubleFlag extends Flag<Double> {

    public DoubleFlag(String name) {
        super(name);
    }

    @Override public String valueToString(Object value) {
        return value.toString();
    }

    @Override public Double parseValue(String value) {
        try {
            return Double.parseDouble(value);
        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    @Override public String getValueDescription() {
        return "Flag value must be a number.";
    }
}
