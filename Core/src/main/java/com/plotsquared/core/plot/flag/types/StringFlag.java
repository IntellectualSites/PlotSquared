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
package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Plot flag representing a string value.
 * This should be used where strings are not "keys" themselves, e.g. when setting enums
 */
public abstract class StringFlag<F extends StringFlag<F>> extends PlotFlag<String, F> {

    /**
     * Construct a new flag instance.
     *
     * @param value           Flag value
     * @param flagCategory    The flag category
     * @param flagDescription A caption describing the flag functionality
     */
    protected StringFlag(
            final @NonNull String value,
            final @NonNull Caption flagCategory,
            final @NonNull Caption flagDescription
    ) {
        super(value, flagCategory, flagDescription);
    }

    @Override
    public boolean isValuedPermission() {
        return false;
    }

}
