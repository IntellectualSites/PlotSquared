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
package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.InternalFlag;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.jetbrains.annotations.NotNull;

public class DoneFlag extends PlotFlag<String, DoneFlag> implements InternalFlag {

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    public DoneFlag(@NotNull String value) {
        super(value, Captions.NONE, Captions.NONE);
    }

    public static boolean isDone(final Plot plot) {
        return !plot.getFlag(DoneFlag.class).isEmpty();
    }

    @Override public DoneFlag parse(@NotNull String input) {
        return flagOf(input);
    }

    @Override public DoneFlag merge(@NotNull String newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return this.getValue();
    }

    @Override public String getExample() {
        return "";
    }

    @Override protected DoneFlag flagOf(@NotNull String value) {
        return new DoneFlag(value);
    }

}
