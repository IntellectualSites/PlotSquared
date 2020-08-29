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
package com.plotsquared.core.plot.membership;

import com.plotsquared.core.plot.PlotPermission;

import java.util.EnumSet;

/**
 * Default plot membership tiers
 */
public final class PlotMemberships {

    public static final PlotMembership OWNER = new PlotMembership("owner", EnumSet.allOf(PlotPermission.class));
    public static final PlotMembership TRUSTED = new PlotMembership("trusted", EnumSet.of(PlotPermission.ENTER_PLOT, PlotPermission.BUILD));
    public static final PlotMembership ADDED = new PlotMembership("added", EnumSet.of(PlotPermission.ENTER_PLOT));
    public static final PlotMembership GUEST = new PlotMembership("guest", EnumSet.of(PlotPermission.ENTER_PLOT));
    public static final PlotMembership DENIED = new PlotMembership("denied", EnumSet.noneOf(PlotPermission.class));

    private PlotMemberships() {
    }

}
