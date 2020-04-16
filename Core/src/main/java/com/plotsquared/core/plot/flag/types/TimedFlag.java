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
package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.jetbrains.annotations.NotNull;

public abstract class TimedFlag<T, F extends PlotFlag<TimedFlag.Timed<T>, F>>
    extends PlotFlag<TimedFlag.Timed<T>, F> {
    private final T defaultValue;

    protected TimedFlag(@NotNull Timed<T> value, T defaultValue, @NotNull Caption flagDescription) {
        super(value, Captions.FLAG_CATEGORY_INTERVALS, flagDescription);
        this.defaultValue = defaultValue;
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        String[] split = input.split(" ", 2);
        int interval;
        try {
            interval = Integer.parseInt(split[0]);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, Captions.NOT_A_NUMBER, split[0]);
        }
        if (interval < 1) {
            throw new FlagParseException(this, input, Captions.NUMBER_NOT_POSITIVE, split[0]);
        }
        if (split.length == 1) {
            return flagOf(new Timed<>(interval, defaultValue));
        }
        final T parsedValue = parseValue(split[1]);
        return flagOf(new Timed<>(interval, parsedValue));
    }

    @Override public F merge(@NotNull Timed<T> newValue) {
        return flagOf(
            new Timed<>(getValue().interval + newValue.interval, mergeValue(newValue.value)));
    }

    protected abstract T parseValue(String input) throws FlagParseException;

    protected abstract T mergeValue(T other);

    @Override public String toString() {
        return getValue().toString();
    }

    public static final class Timed<T> {
        private final int interval;
        private final T value;

        public Timed(int interval, T value) {
            this.interval = interval;
            this.value = value;
        }

        public int getInterval() {
            return interval;
        }

        public T getValue() {
            return value;
        }

        @Override public String toString() {
            return String.format("%d %s", interval, value);
        }
    }
}
