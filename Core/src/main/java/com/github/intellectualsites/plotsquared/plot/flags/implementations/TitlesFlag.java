package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

public class TitlesFlag extends PlotFlag<TitlesFlag.TitlesFlagValue, TitlesFlag> {

    public static final TitlesFlag TITLES_NONE = new TitlesFlag(TitlesFlagValue.NONE);
    public static final TitlesFlag TITLES_TRUE = new TitlesFlag(TitlesFlagValue.TRUE);
    public static final TitlesFlag TITLES_FALSE = new TitlesFlag(TitlesFlagValue.FALSE);

    private TitlesFlag(final TitlesFlagValue value) {
        super(value, Captions.FLAG_CATEGORY_ENUM, Captions.FLAG_DESCRIPTION_TITLES);
    }

    @Override public TitlesFlag parse(@NotNull final String input) throws FlagParseException {
        final TitlesFlagValue titlesFlagValue = TitlesFlagValue.fromString(input);
        if (titlesFlagValue == null) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_ENUM,
                Arrays.asList("none", "true", "false"));
        }
        return flagOf(titlesFlagValue);
    }

    @Override public TitlesFlag merge(@NotNull TitlesFlagValue newValue) {
        if (newValue == TitlesFlagValue.TRUE || newValue == TitlesFlagValue.FALSE) {
            return flagOf(newValue);
        }
        return this;
    }

    @Override public String toString() {
        return getValue().name().toLowerCase(Locale.ENGLISH);
    }

    @Override public String getExample() {
        return "true";
    }

    @Override protected TitlesFlag flagOf(@NotNull TitlesFlagValue value) {
        if (value == TitlesFlagValue.TRUE) {
            return TITLES_TRUE;
        } else if (value == TitlesFlagValue.FALSE) {
            return TITLES_FALSE;
        }
        return TITLES_NONE;
    }

    public enum TitlesFlagValue {
        NONE,
        TRUE,
        FALSE;

        @Nullable public static TitlesFlagValue fromString(final String value) {
            if (value.equalsIgnoreCase("true")) {
                return TRUE;
            } else if (value.equalsIgnoreCase("false")) {
                return FALSE;
            } else if (value.equalsIgnoreCase("none")) {
                return NONE;
            }
            return null;
        }
    }

}
