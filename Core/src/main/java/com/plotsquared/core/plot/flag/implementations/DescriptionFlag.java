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
import com.plotsquared.core.plot.flag.types.StringFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DescriptionFlag extends StringFlag<DescriptionFlag> {

    public static final DescriptionFlag DESCRIPTION_FLAG_EMPTY = new DescriptionFlag("");

    protected DescriptionFlag(@NonNull String value) {
        super(
                value,
                TranslatableCaption.of("flags.flag_category_string"),
                TranslatableCaption.of("flags.flag_description_description")
        );
    }

    @Override
    public DescriptionFlag parse(@NonNull String input) {
        return flagOf(input);
    }

    @Override
    public DescriptionFlag merge(@NonNull String newValue) {
        return flagOf(this.getValue() + " " + newValue);
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    @Override
    public String getExample() {
        return "<gold>This is my plot!";
    }

    @Override
    protected DescriptionFlag flagOf(@NonNull String value) {
        return new DescriptionFlag(value);
    }

}
