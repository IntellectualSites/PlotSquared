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

public abstract class TimedFlag<T, F extends PlotFlag<TimedFlag.Timed<T>, F>>
        extends PlotFlag<TimedFlag.Timed<T>, F> {

    private final T defaultValue;

    protected TimedFlag(@NonNull Timed<T> value, T defaultValue, @NonNull Caption flagDescription) {
        super(value, TranslatableCaption.of("flags.flag_category_intervals"), flagDescription);
        this.defaultValue = defaultValue;
    }

    @Override
    public F parse(@NonNull String input) throws FlagParseException {
        String[] split = input.split(" ", 2);
        int interval;
        try {
            interval = Integer.parseInt(split[0]);
        } catch (Throwable throwable) {
            throw new FlagParseException(
                    this,
                    input,
                    TranslatableCaption.of("flags.flag_error_integer")
            );
        }
        if (interval < 1) {
            throw new FlagParseException(
                    this,
                    input,
                    TranslatableCaption.of("flags.flag_error_integer")
            );
        }
        if (split.length == 1) {
            return flagOf(new Timed<>(interval, defaultValue));
        }
        final T parsedValue = parseValue(split[1]);
        return flagOf(new Timed<>(interval, parsedValue));
    }

    @Override
    public F merge(@NonNull Timed<T> newValue) {
        return flagOf(
                new Timed<>(getValue().interval + newValue.interval, mergeValue(newValue.value)));
    }

    protected abstract T parseValue(String input) throws FlagParseException;

    protected abstract T mergeValue(T other);

    @Override
    public String toString() {
        return getValue().toString();
    }

    public record Timed<T>(
            int interval,
            T value
    ) {

        @Override
        public String toString() {
            return String.format("%d %s", interval, value);
        }

    }

}
