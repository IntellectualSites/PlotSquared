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

package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.FlagParseException;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;

public abstract class LongFlag<F extends NumberFlag<Long, F>> extends NumberFlag<Long, F> {

    protected LongFlag(@Nonnull Long value, Long minimum, Long maximum,
        @Nonnull Caption flagDescription) {
        super(value, minimum, maximum, TranslatableCaption.of("flags.flag_category_integers"), flagDescription);
    }

    protected LongFlag(@Nonnull Long value, @Nonnull Caption flagDescription) {
        this(value, Long.MIN_VALUE, Long.MAX_VALUE, flagDescription);
    }

    @Override public F merge(@Nonnull Long newValue) {
        return flagOf(getValue() + newValue);
    }

    @Override public String toString() {
        return getValue().toString();
    }

    @Override public String getExample() {
        return "123456789";
    }

    @Nonnull @Override protected Long parseNumber(String input) throws FlagParseException {
        try {
            return Long.parseLong(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, TranslatableCaption.of("invalid.not_a_number"), Template.of("value", input));
        }
    }
}
