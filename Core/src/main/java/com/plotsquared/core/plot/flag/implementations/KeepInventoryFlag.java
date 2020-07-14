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
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import javax.annotation.Nonnull;

public class KeepInventoryFlag extends BooleanFlag<KeepInventoryFlag> {

    public static final KeepInventoryFlag KEEP_INVENTORY_TRUE = new KeepInventoryFlag(true);
    public static final KeepInventoryFlag KEEP_INVENTORY_FALSE = new KeepInventoryFlag(false);

    private KeepInventoryFlag(final boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_KEEP_INVENTORY);
    }

    @Override protected KeepInventoryFlag flagOf(@Nonnull final Boolean value) {
        return value ? KEEP_INVENTORY_TRUE : KEEP_INVENTORY_FALSE;
    }

}
