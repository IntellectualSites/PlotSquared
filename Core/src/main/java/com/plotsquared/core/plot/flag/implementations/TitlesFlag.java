/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
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

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("none", "true", "false");
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
