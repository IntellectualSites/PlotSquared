package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.jetbrains.annotations.NotNull;

public class DescriptionFlag extends PlotFlag<String, DescriptionFlag> {

    public static final DescriptionFlag DESCRIPTION_FLAG_EMPTY = new DescriptionFlag("");

    protected DescriptionFlag(@NotNull String value) {
        super(value, Captions.FLAG_CATEGORY_STRING, Captions.FLAG_DESCRIPTION_DESCRIPTION);
    }

    @Override public DescriptionFlag parse(@NotNull String input) {
        return flagOf(input);
    }

    @Override public DescriptionFlag merge(@NotNull String newValue) {
        return flagOf(this.getValue() + " " + newValue);
    }

    @Override public String toString() {
        return this.getValue();
    }

    @Override public String getExample() {
        return "&6This is my plot!";
    }

    @Override protected DescriptionFlag flagOf(@NotNull String value) {
        return new DescriptionFlag(value);
    }

}
