package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

public class BooleanFlag extends Flag<Boolean> {

    public BooleanFlag(String name) {
        super(Captions.FLAG_CATEGORY_BOOLEAN, name);
    }

    @Override public String valueToString(Boolean value) {
        return value.toString();
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
        return Captions.FLAG_ERROR_BOOLEAN.getTranslated();
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
