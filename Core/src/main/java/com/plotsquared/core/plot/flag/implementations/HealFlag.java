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

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.types.TimedFlag;
import org.jetbrains.annotations.NotNull;

public class HealFlag extends TimedFlag<Integer, HealFlag> {
    public static final HealFlag HEAL_NOTHING = new HealFlag(new Timed<>(0, 0));

    protected HealFlag(@NotNull Timed<Integer> value) {
        super(value, 1, Captions.FLAG_DESCRIPTION_HEAL);
    }

    @Override protected Integer parseValue(String input) throws FlagParseException {
        int parsed;
        try {
            parsed = Integer.parseInt(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, Captions.NOT_A_NUMBER, input);
        }
        if (parsed < 1) {
            throw new FlagParseException(this, input, Captions.NUMBER_NOT_POSITIVE, parsed);
        }
        return parsed;
    }

    @Override protected Integer mergeValue(Integer other) {
        return this.getValue().getValue() + other;
    }

    @Override public String getExample() {
        return "20 2";
    }

    @Override protected HealFlag flagOf(@NotNull Timed<Integer> value) {
        return new HealFlag(value);
    }
}
