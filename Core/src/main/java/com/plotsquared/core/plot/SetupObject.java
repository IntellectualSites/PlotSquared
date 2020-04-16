/*
 *
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
package com.plotsquared.core.plot;

import com.plotsquared.core.config.ConfigurationNode;
import com.plotsquared.core.util.SetupUtils;

public class SetupObject {

    /**
     * Specify a SetupUtils object here to override the existing
     */
    public SetupUtils setupManager;

    /**
     * The current state
     */
    public int current = 0;

    /**
     * The index in generator specific settings
     */
    public int setup_index = 0;

    /**
     * The name of the world
     */
    public String world = null;

    /**
     * The name of the plot manager
     */
    public String plotManager = null;

    /**
     * The name of the generator to use for world creation
     */
    public String setupGenerator = null;

    /**
     * The management type (normal, augmented, partial)
     */
    public PlotAreaType type;

    /**
     * The terrain type
     */
    public PlotAreaTerrainType terrain;

    /**
     * Area ID (may be null)
     */
    public String id;

    /**
     * Minimum plot id (may be null)
     */
    public PlotId min;

    /**
     * Max plot id (may be null)
     */
    public PlotId max;

    /**
     * Generator specific configuration steps
     */
    public ConfigurationNode[] step = null;
}
