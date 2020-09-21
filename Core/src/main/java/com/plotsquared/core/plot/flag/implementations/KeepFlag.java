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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.TimeUtil;

import javax.annotation.Nonnull;

public class KeepFlag extends PlotFlag<Object, KeepFlag> {

    public static final KeepFlag KEEP_FLAG_FALSE = new KeepFlag(false);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected KeepFlag(@Nonnull Object value) {
        super(value, TranslatableCaption.of("flags.flag_category_mixed"), TranslatableCaption.of("flags.flag_description_keep"));
    }

    @Override public KeepFlag parse(@Nonnull String input) throws FlagParseException {
        if (MathMan.isInteger(input)) {
            final long value = Long.parseLong(input);
            if (value < 0) {
                throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_keep"));
            } else {
                return flagOf(value);
            }
        }
        switch (input.toLowerCase()) {
            case "true":
                return flagOf(true);
            case "false":
                return flagOf(false);
            default:
                return flagOf(TimeUtil.timeToSec(input) * 1000 + System.currentTimeMillis());
        }
    }

    @Override public KeepFlag merge(@Nonnull Object newValue) {
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

    @Override public String toString() {
        return getValue().toString();
    }

    @Override public String getExample() {
        return "3w 4d 2h";
    }

    @Override protected KeepFlag flagOf(@Nonnull Object value) {
        return new KeepFlag(value);
    }

}
