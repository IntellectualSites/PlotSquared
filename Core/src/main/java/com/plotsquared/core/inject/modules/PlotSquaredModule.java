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
package com.plotsquared.core.inject.modules;

import com.google.inject.AbstractModule;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.inject.annotations.BackgroundPipeline;
import com.plotsquared.core.inject.annotations.ConfigFile;
import com.plotsquared.core.inject.annotations.ImpromptuPipeline;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.inject.annotations.WorldFile;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.uuid.UUIDPipeline;
import com.sk89q.worldedit.WorldEdit;

import java.io.File;

public class PlotSquaredModule extends AbstractModule {

    @Override protected void configure() {
        final PlotSquared plotSquared = PlotSquared.get();
        bind(YamlConfiguration.class).annotatedWith(WorldConfig.class).toInstance(plotSquared.getWorldConfiguration());
        bind(File.class).annotatedWith(WorldFile.class).toInstance(plotSquared.getWorldsFile());
        bind(File.class).annotatedWith(ConfigFile.class).toInstance(plotSquared.getConfigFile());
        bind(PlotAreaManager.class).toInstance(plotSquared.getPlotAreaManager());
        bind(PlotListener.class).toInstance(plotSquared.getPlotListener());
        bind(UUIDPipeline.class).annotatedWith(ImpromptuPipeline.class).toInstance(plotSquared.getImpromptuUUIDPipeline());
        bind(UUIDPipeline.class).annotatedWith(BackgroundPipeline.class).toInstance(plotSquared.getBackgroundUUIDPipeline());
        bind(WorldEdit.class).toInstance(WorldEdit.getInstance());
        bind(EventDispatcher.class).toInstance(plotSquared.getEventDispatcher());
    }

}
