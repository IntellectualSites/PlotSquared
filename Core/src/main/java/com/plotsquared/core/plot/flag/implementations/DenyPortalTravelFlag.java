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

public class DenyPortalTravelFlag extends BooleanFlag<DenyPortalTravelFlag> {

    public static final DenyPortalTravelFlag DENY_PORTAL_TRAVEL_TRUE = new DenyPortalTravelFlag(true);
    public static final DenyPortalTravelFlag DENY_PORTAL_TRAVEL_FALSE = new DenyPortalTravelFlag(false);

    private DenyPortalTravelFlag(final boolean value) {
        super(value, TranslatableCaption.of("flags.flag_description_deny_portal_travel"));
    }

    @Override
    protected DenyPortalTravelFlag flagOf(final @NonNull Boolean value) {
        return value ? DENY_PORTAL_TRAVEL_TRUE : DENY_PORTAL_TRAVEL_FALSE;
    }

}
