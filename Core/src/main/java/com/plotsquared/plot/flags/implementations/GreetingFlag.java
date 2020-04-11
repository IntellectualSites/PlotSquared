package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

public class GreetingFlag extends PlotFlag<String, GreetingFlag> {

    public static final GreetingFlag GREETING_FLAG_EMPTY = new GreetingFlag("");

    protected GreetingFlag(@NotNull String value) {
        super(value, Captions.FLAG_CATEGORY_STRING, Captions.FLAG_DESCRIPTION_GREETING);
    }

    @Override public GreetingFlag parse(@NotNull String input) {
        return flagOf(input);
    }

    @Override public GreetingFlag merge(@NotNull String newValue) {
        return flagOf(this.getValue() + " " + newValue);
    }

    @Override public String toString() {
        return this.getValue();
    }

    @Override public String getExample() {
        return "&6Welcome to my plot!";
    }

    @Override protected GreetingFlag flagOf(@NotNull String value) {
        return new GreetingFlag(value);
    }

}
