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
import com.plotsquared.core.plot.flag.InternalFlag;
import com.plotsquared.core.plot.flag.types.ListFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AnalysisFlag extends ListFlag<Integer, AnalysisFlag> implements InternalFlag {

    public AnalysisFlag(final List<Integer> valueList) {
        super(valueList, TranslatableCaption.of("info.none"), TranslatableCaption.of("info.none"));
    }

    @Override
    public AnalysisFlag parse(@NonNull String input) throws FlagParseException {
        final String[] split = input.split(",");
        final List<Integer> numbers = new ArrayList<>();
        for (final String element : split) {
            numbers.add(Integer.parseInt(element));
        }
        return flagOf(numbers);
    }

    @Override
    public String getExample() {
        return "";
    }

    @Override
    protected AnalysisFlag flagOf(@NonNull List<Integer> value) {
        return new AnalysisFlag(value);
    }

}
