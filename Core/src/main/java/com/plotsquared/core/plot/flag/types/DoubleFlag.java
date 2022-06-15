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
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class DoubleFlag<F extends NumberFlag<Double, F>> extends NumberFlag<Double, F> {

    protected DoubleFlag(
            @NonNull Double value, Double minimum, Double maximum,
            @NonNull Caption flagDescription
    ) {
        super(value, minimum, maximum, TranslatableCaption.of("flags.flag_category_doubles"), flagDescription);
    }

    protected DoubleFlag(@NonNull Double value, @NonNull Caption flagDescription) {
        this(value, Double.MIN_VALUE, Double.MAX_VALUE, flagDescription);
    }

    @Override
    public F merge(@NonNull Double newValue) {
        return flagOf(getValue() + newValue);
    }

    @Override
    public String getExample() {
        return "12.175";
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @NonNull
    @Override
    protected Double parseNumber(String input) throws FlagParseException {
        try {
            return Double.parseDouble(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_double"));
        }
    }

    @Override
    public boolean isValuedPermission() {
        return false;
    }

}
