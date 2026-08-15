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
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @since 7.2.1
 */
public class EditSignFlag extends BooleanFlag<EditSignFlag> {
    public static final EditSignFlag EDIT_SIGN_TRUE = new EditSignFlag(true);
    public static final EditSignFlag EDIT_SIGN_FALSE = new EditSignFlag(false);

    private EditSignFlag(final boolean value) {
        super(value, TranslatableCaption.of("flags.flag_description_edit_sign"));
    }

    @Override
    protected EditSignFlag flagOf(@NonNull final Boolean value) {
        return value ? EDIT_SIGN_TRUE : EDIT_SIGN_FALSE;
    }

}
