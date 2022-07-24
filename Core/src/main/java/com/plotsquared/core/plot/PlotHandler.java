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
package com.plotsquared.core.plot;

import java.util.Set;
import java.util.UUID;

public class PlotHandler {

    public static boolean sameOwners(final Plot plot1, final Plot plot2) {
        if (plot1.getOwnerAbs() == null || plot2.getOwnerAbs() == null) {
            return false;
        }
        final Set<UUID> owners = plot1.getOwners();
        for (UUID owner : owners) {
            if (plot2.isOwner(owner)) {
                return true;
            }
        }
        return false;
    }

}
