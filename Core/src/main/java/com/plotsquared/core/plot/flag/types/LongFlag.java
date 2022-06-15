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

public abstract class LongFlag<F extends NumberFlag<Long, F>> extends NumberFlag<Long, F> {

    protected LongFlag(
            @NonNull Long value, Long minimum, Long maximum,
            @NonNull Caption flagDescription
    ) {
        super(value, minimum, maximum, TranslatableCaption.of("flags.flag_category_integers"), flagDescription);
    }

    protected LongFlag(@NonNull Long value, @NonNull Caption flagDescription) {
        this(value, Long.MIN_VALUE, Long.MAX_VALUE, flagDescription);
    }

    @Override
    public F merge(@NonNull Long newValue) {
        return flagOf(getValue() + newValue);
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public String getExample() {
        return "123456789";
    }

    @NonNull
    @Override
    protected Long parseNumber(String input) throws FlagParseException {
        try {
            return Long.parseLong(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(
                    this,
                    input,
                    TranslatableCaption.of("flags.flag_error_long")
            );
        }
    }

    @Override
    public boolean isValuedPermission() {
        return false;
    }

}
