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
package com.plotsquared.core.plot;

import com.google.common.base.Objects;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.plot.membership.PlotMembership;
import com.plotsquared.core.plot.membership.PlotMemberships;

import javax.annotation.Nonnull;

/**
 * Registry of plot memberships, unique per plot.
 * Does not currently do anything, but existsfor
 * future use.
 */
public class PlotMembershipRegistry {

    private final Plot plot;

    PlotMembershipRegistry(@Nonnull final Plot plot) {
        this.plot = plot;
    }

    /**
     * Get the membership instance for a given player. Will default to {@link com.plotsquared.core.plot.membership.PlotMemberships#GUEST}
     *
     * @param player Player to check membership for
     * @return Membership that player belongs to
     */
    @Nonnull public PlotMembership getMembership(@Nonnull final OfflinePlotPlayer player) {
        if (this.plot.isOwner(player.getUUID())) {
            return PlotMemberships.OWNER;
        } else if (this.plot.getTrusted().contains(player.getUUID()) ||
                   this.plot.getTrusted().contains(DBFunc.EVERYONE)) {
            return PlotMemberships.TRUSTED;
        } else if (this.plot.getMembers().contains(player.getUUID()) ||
                   this.plot.getMembers().contains(DBFunc.EVERYONE)) {
            return PlotMemberships.ADDED;
        } else if (this.plot.isDenied(player.getUUID())) {
            return PlotMemberships.DENIED;
        } else {
            return PlotMemberships.GUEST;
        }
    }

    /**
     * Get the plot to which this registry belongs
     *
     * @return Owning plot
     */
    @Nonnull public final Plot getPlot() {
        return this.plot;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PlotMembershipRegistry that = (PlotMembershipRegistry) o;
        return Objects.equal(getPlot(), that.getPlot());
    }

    @Override public int hashCode() {
        return Objects.hashCode(getPlot());
    }

}
