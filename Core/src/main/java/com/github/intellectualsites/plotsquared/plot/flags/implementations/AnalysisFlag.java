package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.InternalFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.ListFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnalysisFlag extends ListFlag<Integer, AnalysisFlag> implements InternalFlag {

    public AnalysisFlag(final List<Integer> valueList) {
        super(valueList, Captions.NONE, Captions.NONE);
    }

    @Override public AnalysisFlag parse(@NotNull String input) throws FlagParseException {
        final String[] split = input.split(",");
        final List<Integer> numbers = new ArrayList<>();
        for (final String element : split) {
            numbers.add(Integer.parseInt(element));
        }
        return flagOf(numbers);
    }

    @Override public String getExample() {
        return "";
    }

    @Override protected AnalysisFlag flagOf(@NotNull List<Integer> value) {
        return new AnalysisFlag(value);
    }

}
