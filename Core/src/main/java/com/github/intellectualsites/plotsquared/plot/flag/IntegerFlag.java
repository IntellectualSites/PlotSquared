package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

public class IntegerFlag extends Flag<Integer> {

    public IntegerFlag(String name) {
        super(Captions.FLAG_CATEGORY_INTEGERS, name);
    }

    @Override public String getValueDescription() {
        return Captions.FLAG_ERROR_INTEGER.getTranslated();
    }

    @Override public String valueToString(Object value) {
        return value.toString();
    }

    @Override public Integer parseValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public boolean isEqual(Plot plot, int value) {
        Integer existing = FlagManager.getPlotFlagRaw(plot, this);
        return existing != null && existing == value;
    }

    public boolean isMore(Plot plot, int value) {
        Integer existing = FlagManager.getPlotFlagRaw(plot, this);
        return existing != null && existing > value;
    }

    public boolean isLess(Plot plot, int value) {
        Integer existing = FlagManager.getPlotFlagRaw(plot, this);
        return existing != null && existing < value;
    }
}
