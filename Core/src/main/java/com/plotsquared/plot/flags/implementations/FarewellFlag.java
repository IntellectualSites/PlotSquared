package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

public class FarewellFlag extends PlotFlag<String, FarewellFlag> {

    public static final FarewellFlag FAREWELL_FLAG_EMPTY = new FarewellFlag("");

    protected FarewellFlag(@NotNull String value) {
        super(value, Captions.FLAG_CATEGORY_STRING, Captions.FLAG_DESCRIPTION_FAREWELL);
    }

    @Override public FarewellFlag parse(@NotNull String input) {
        return flagOf(input);
    }

    @Override public FarewellFlag merge(@NotNull String newValue) {
        return flagOf(this.getValue() + " " + newValue);
    }

    @Override public String toString() {
        return this.getValue();
    }

    @Override public String getExample() {
        return "&cBye :(";
    }

    @Override protected FarewellFlag flagOf(@NotNull String value) {
        return new FarewellFlag(value);
    }

}
