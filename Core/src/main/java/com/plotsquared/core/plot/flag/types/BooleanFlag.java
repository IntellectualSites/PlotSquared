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
import com.plotsquared.core.plot.flag.PlotFlag;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public abstract class BooleanFlag<F extends PlotFlag<Boolean, F>> extends PlotFlag<Boolean, F> {

    private static final Collection<String> positiveValues =
        Arrays.asList("1", "yes", "allow", "true");
    private static final Collection<String> negativeValues =
        Arrays.asList("0", "no", "deny", "disallow", "false");

    /**
     * Construct a new flag instance.
     *
     * @param value       Flag value
     * @param description Flag description
     */
    protected BooleanFlag(final boolean value, final Caption description) {
        super(value, TranslatableCaption.of("flags.flag_category_boolean"), description);
    }

    /**
     * Construct a new boolean flag, with
     * {@code false} as the default value.
     *
     * @param description Flag description
     */
    protected BooleanFlag(final Caption description) {
        this(false, description);
    }

    @Override public F parse(@Nonnull String input) throws FlagParseException {
        if (positiveValues.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.flagOf(true);
        } else if (negativeValues.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.flagOf(false);
        } else {
            throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_boolean"));
        }
    }

    @Override public F merge(@Nonnull Boolean newValue) {
        return this.flagOf(getValue() || newValue);
    }

    @Override public String getExample() {
        return "true";
    }

    @Override public String toString() {
        return this.getValue().toString();
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("true", "false");
    }
}
