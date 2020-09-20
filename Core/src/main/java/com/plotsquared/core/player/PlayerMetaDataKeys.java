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

package com.plotsquared.core.player;

import com.google.inject.TypeLiteral;
import com.plotsquared.core.command.Auto;
import com.plotsquared.core.command.CmdInstance;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.setup.SetupProcess;

import java.util.List;

public final class PlayerMetaDataKeys {

    //@formatter:off
    public static final MetaDataKey<Boolean> PERSISTENT_FLIGHT = MetaDataKey.of("flight", new TypeLiteral<Boolean>() {});
    public static final MetaDataKey<Integer> PERSISTENT_GRANTED_PLOTS = MetaDataKey.of("grantedPlots", new TypeLiteral<Integer>() {});

    public static final MetaDataKey<Plot> TEMPORARY_LAST_PLOT = MetaDataKey.of("lastplot", new TypeLiteral<Plot>() {});
    public static final MetaDataKey<Location> TEMPORARY_MUSIC = MetaDataKey.of("music", new TypeLiteral<Location>() {});
    public static final MetaDataKey<Boolean> TEMPORARY_KICK = MetaDataKey.of("kick", new TypeLiteral<Boolean>() {});
    public static final MetaDataKey<SetupProcess> TEMPORARY_SETUP = MetaDataKey.of("setup", new TypeLiteral<SetupProcess>() {});
    public static final MetaDataKey<PlotInventory> TEMPORARY_INVENTORY = MetaDataKey.of("inventory", new TypeLiteral<PlotInventory>() {});
    public static final MetaDataKey<Boolean> TEMPORARY_IGNORE_EXPIRE_TASK = MetaDataKey.of("ignoreExpireTask", new TypeLiteral<Boolean>() {});
    public static final MetaDataKey<Plot> TEMPORARY_WORLD_EDIT_REGION_PLOT = MetaDataKey.of("WorldEditRegionPlot", new TypeLiteral<Plot>() {});
    public static final MetaDataKey<Boolean> TEMPORARY_AUTO = MetaDataKey.of(Auto.class.getName(), new TypeLiteral<Boolean>() {});
    public static final MetaDataKey<List<String>> TEMPORARY_SCHEMATICS = MetaDataKey.of("plot_schematics", new TypeLiteral<List<String>>() {});
    public static final MetaDataKey<Location> TEMPORARY_LOCATION = MetaDataKey.of("location", new TypeLiteral<Location>() {});
    public static final MetaDataKey<CmdInstance> TEMPORARY_CONFIRM = MetaDataKey.of("cmdConfirm", new TypeLiteral<CmdInstance>() {});
    //@formatter:on

    public static void load() {
        // Do nothing :D
    }

    private PlayerMetaDataKeys() {
    }

}
