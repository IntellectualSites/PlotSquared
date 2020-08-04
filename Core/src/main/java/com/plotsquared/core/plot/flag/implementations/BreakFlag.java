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
import com.plotsquared.core.plot.flag.types.BlockTypeListFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class BreakFlag extends BlockTypeListFlag<BreakFlag> {

    public static final BreakFlag BREAK_NONE = new BreakFlag(Collections.emptyList());

    protected BreakFlag(List<BlockTypeWrapper> blockTypeList) {
        super(blockTypeList, TranslatableCaption.of("flags.flag_description_break"));
    }

    @Override protected BreakFlag flagOf(@Nonnull List<BlockTypeWrapper> value) {
        return new BreakFlag(value);
    }

}
