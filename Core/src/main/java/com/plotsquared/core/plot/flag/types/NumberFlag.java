/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class NumberFlag<N extends Number & Comparable<N>, F extends PlotFlag<N, F>>
        extends PlotFlag<N, F> {

    protected final N minimum;
    protected final N maximum;

    protected NumberFlag(
            @NonNull N value, N minimum, N maximum, @NonNull Caption flagCategory,
            @NonNull Caption flagDescription
    ) {
        super(value, flagCategory, flagDescription);
        if (maximum.compareTo(minimum) < 0) {
            throw new IllegalArgumentException(
                    "Maximum may not be less than minimum:" + maximum + " < " + minimum);
        }
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public F parse(@NonNull String input) throws FlagParseException {
        final N parsed = parseNumber(input);
        if (parsed.compareTo(minimum) < 0 || parsed.compareTo(maximum) > 0) {
            throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_integer"));
        }
        return flagOf(parsed);

    }

    /**
     * Parse the raw string input to the number type.
     *
     * @param input the string to parse the number from.
     * @return the parsed number.
     * @throws FlagParseException if the number couldn't be parsed.
     */
    @NonNull
    protected abstract N parseNumber(String input) throws FlagParseException;

}
