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
import com.plotsquared.core.plot.flag.InternalFlag;
import com.plotsquared.core.plot.flag.types.ListFlag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AnalysisFlag extends ListFlag<Integer, AnalysisFlag> implements InternalFlag {

    public AnalysisFlag(final List<Integer> valueList) {
        super(valueList, TranslatableCaption.of("info.none"), TranslatableCaption.of("info.none"));
    }

    @Override public AnalysisFlag parse(@Nonnull String input) throws FlagParseException {
        final String[] split = input.split(",");
        final List<Integer> numbers = new ArrayList<>();
        for (final String element : split) {
            numbers.add(Integer.parseInt(element));
        }
        return flagOf(numbers);
    }

    @Override public String getExample() {
        return "";
    }

    @Override protected AnalysisFlag flagOf(@Nonnull List<Integer> value) {
        return new AnalysisFlag(value);
    }

}
