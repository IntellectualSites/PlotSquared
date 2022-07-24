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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.TimeUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class KeepFlag extends PlotFlag<Object, KeepFlag> {

    public static final KeepFlag KEEP_FLAG_FALSE = new KeepFlag(false);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected KeepFlag(@NonNull Object value) {
        super(value, TranslatableCaption.of("flags.flag_category_mixed"), TranslatableCaption.of("flags.flag_description_keep"));
    }

    @Override
    public KeepFlag parse(@NonNull String input) throws FlagParseException {
        if (MathMan.isInteger(input)) {
            final long value = Long.parseLong(input);
            if (value < 0) {
                throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_keep"));
            } else {
                return flagOf(value);
            }
        }
        return switch (input.toLowerCase()) {
            case "true" -> flagOf(true);
            case "false" -> flagOf(false);
            default -> flagOf(TimeUtil.timeToSec(input) * 1000 + System.currentTimeMillis());
        };
    }

    @Override
    public KeepFlag merge(@NonNull Object newValue) {
        if (newValue.equals(true)) {
            return flagOf(true);
        } else if (newValue.equals(false)) {
            if (getValue().equals(true) || getValue().equals(false)) {
                return this;
            } else {
                return flagOf(newValue);
            }
        } else {
            if (getValue().equals(true)) {
                return this;
            } else if (getValue().equals(false)) {
                return flagOf(newValue);
            } else {
                long currentValue = (long) getValue();
                return flagOf((long) newValue + currentValue);
            }
        }
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public String getExample() {
        return "3w 4d 2h";
    }

    @Override
    protected KeepFlag flagOf(@NonNull Object value) {
        return new KeepFlag(value);
    }

}
