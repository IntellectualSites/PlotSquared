/*
 *
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
package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.config.Caption;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.jetbrains.annotations.NotNull;

public abstract class NumberFlag<N extends Number & Comparable<N>, F extends PlotFlag<N, F>>
    extends PlotFlag<N, F> {
    protected final N minimum;
    protected final N maximum;

    protected NumberFlag(@NotNull N value, N minimum, N maximum, @NotNull Caption flagCategory,
        @NotNull Caption flagDescription) {
        super(value, flagCategory, flagDescription);
        if (maximum.compareTo(minimum) < 0) {
            throw new IllegalArgumentException(
                "Maximum may not be less than minimum:" + maximum + " < " + minimum);
        }
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        final N parsed = parseNumber(input);
        if (parsed.compareTo(minimum) < 0 || parsed.compareTo(maximum) > 0) {
            throw new FlagParseException(this, input, Captions.NUMBER_NOT_IN_RANGE, minimum,
                maximum);
        }
        return flagOf(parsed);

    }

    /**
     * Parse the raw string input to the number type.
     * Throw a {@link FlagParseException} if the number couldn't be parsed.
     *
     * @param input the string to parse the number from.
     * @return the parsed number.
     */
    @NotNull protected abstract N parseNumber(String input) throws FlagParseException;
}
