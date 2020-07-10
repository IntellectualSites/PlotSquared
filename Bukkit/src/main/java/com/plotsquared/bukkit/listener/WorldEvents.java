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
package com.plotsquared.bukkit.listener;

import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class WorldEvents implements Listener {

    private final PlotAreaManager plotAreaManager;

    public WorldEvents(@NotNull final PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        String name = world.getName();
        if (this.plotAreaManager instanceof SinglePlotAreaManager) {
            final SinglePlotAreaManager single = (SinglePlotAreaManager) this.plotAreaManager;
            if (single.isWorld(name)) {
                world.setKeepSpawnInMemory(false);
                return;
            }
        }
        ChunkGenerator gen = world.getGenerator();
        if (gen instanceof GeneratorWrapper) {
            PlotSquared.get().loadWorld(name, (GeneratorWrapper<?>) gen);
        } else {
            PlotSquared.get().loadWorld(name, new BukkitPlotGenerator(name, gen, this.plotAreaManager));
        }
    }
}
