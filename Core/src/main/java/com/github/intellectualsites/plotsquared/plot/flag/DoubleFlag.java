package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;

public class DoubleFlag extends Flag<Double> {

    public DoubleFlag(String name) {
        super(Captions.FLAG_CATEGORY_DECIMAL, name);
    }

    @Override public String valueToString(Double value) {
        return value.toString();
    }

    @Override public Double parseValue(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return null;
        }

    }

    @Override public String getValueDescription() {
        return Captions.FLAG_ERROR_BOOLEAN.getTranslated();
    }
}
