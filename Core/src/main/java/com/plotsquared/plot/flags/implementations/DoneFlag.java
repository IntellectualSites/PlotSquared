package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.InternalFlag;
import com.plotsquared.plot.flags.PlotFlag;
import com.plotsquared.plot.Plot;
import org.jetbrains.annotations.NotNull;

public class DoneFlag extends PlotFlag<String, DoneFlag> implements InternalFlag {

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    public DoneFlag(@NotNull String value) {
        super(value, Captions.NONE, Captions.NONE);
    }

    public static boolean isDone(final Plot plot) {
        return !plot.getFlag(DoneFlag.class).isEmpty();
    }

    @Override public DoneFlag parse(@NotNull String input) {
        return flagOf(input);
    }

    @Override public DoneFlag merge(@NotNull String newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return this.getValue();
    }

    @Override public String getExample() {
        return "";
    }

    @Override protected DoneFlag flagOf(@NotNull String value) {
        return new DoneFlag(value);
    }

}
