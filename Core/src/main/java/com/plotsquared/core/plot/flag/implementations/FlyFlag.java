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

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.PlotFlag;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

public class FlyFlag extends PlotFlag<FlyFlag.FlyStatus, FlyFlag> {

    public static final FlyFlag FLIGHT_FLAG_DISABLED = new FlyFlag(FlyStatus.DISABLED);
    public static final FlyFlag FLIGHT_FLAG_ENABLED = new FlyFlag(FlyStatus.ENABLED);
    public static final FlyFlag FLIGHT_FLAG_DEFAULT = new FlyFlag(FlyStatus.DEFAULT);

    protected FlyFlag(final FlyStatus value) {
        super(value, TranslatableCaption.of("flags.flag_category_boolean"), TranslatableCaption.of("flags.flag_description_flight"));
    }

    @Override public FlyFlag parse(@Nonnull final String input) {
        switch (input.toLowerCase()) {
            case "true":
            case "enabled":
            case "allow":
                return FLIGHT_FLAG_ENABLED;
            case "false":
            case "disabled":
            case "disallow":
                return FLIGHT_FLAG_DISABLED;
            default:
                return FLIGHT_FLAG_DEFAULT;
        }
    }

    @Override public FlyFlag merge(@Nonnull final FlyStatus newValue) {
        if (newValue == FlyStatus.ENABLED || this.getValue() == FlyStatus.ENABLED) {
            return FLIGHT_FLAG_ENABLED;
        }
        return flagOf(newValue);
    }

    @Override public String toString() {
        return this.getValue().name().toLowerCase();
    }

    @Override public String getExample() {
        return "true";
    }

    @Override protected FlyFlag flagOf(@Nonnull final FlyStatus value) {
        switch (value) {
            case ENABLED:
                return FLIGHT_FLAG_ENABLED;
            case DISABLED:
                return FLIGHT_FLAG_DISABLED;
            default:
                return FLIGHT_FLAG_DEFAULT;
        }
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("true", "false", "default");
    }

    public enum FlyStatus {
        ENABLED, DISABLED, DEFAULT
    }

}
