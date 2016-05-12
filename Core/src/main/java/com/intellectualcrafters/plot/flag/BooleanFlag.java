package com.intellectualcrafters.plot.flag;

import com.intellectualcrafters.plot.object.Plot;

public class BooleanFlag extends Flag<Boolean> {

    public BooleanFlag(String name) {
        super(name);
    }

    @Override public String valueToString(Object value) {
        if (((boolean) value)) {
            return "true";
        } else {
            return "false";
        }
    }

    @Override public Boolean parseValue(String value) {
        switch (value.toLowerCase()) {
            case "1":
            case "yes":
            case "allow":
            case "true":
                return true;
            case "0":
            case "no":
            case "deny":
            case "false":
                return false;
            default:
                return null;
        }
    }

    @Override public String getValueDescription() {
        return "Flag value must be a boolean (true|false)";
    }

    public boolean isTrue(Plot plot) {
        Boolean value = FlagManager.getPlotFlagRaw(plot, this);
        return Boolean.TRUE == value;
    }

    public boolean isFalse(Plot plot) {
        Boolean value = FlagManager.getPlotFlagRaw(plot, this);
        return Boolean.FALSE == value;
    }
}
