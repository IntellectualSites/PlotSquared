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

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import org.jetbrains.annotations.NotNull;

public class KeepFlag extends PlotFlag<Object, KeepFlag> {

    public static final KeepFlag KEEP_FLAG_FALSE = new KeepFlag(false);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected KeepFlag(@NotNull Object value) {
        super(value, Captions.FLAG_CATEGORY_MIXED, Captions.FLAG_DESCRIPTION_KEEP);
    }

    @Override public KeepFlag parse(@NotNull String input) throws FlagParseException {
        if (MathMan.isInteger(input)) {
            final long value = Long.parseLong(input);
            if (value < 0) {
                throw new FlagParseException(this, input, Captions.FLAG_ERROR_KEEP);
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
                return flagOf(MainUtil.timeToSec(input) * 1000 + System.currentTimeMillis());
        }
    }

    @Override public KeepFlag merge(@NotNull Object newValue) {
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

    @Override protected KeepFlag flagOf(@NotNull Object value) {
        return new KeepFlag(value);
    }

}
