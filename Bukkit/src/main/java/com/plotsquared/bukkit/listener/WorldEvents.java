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
package com.plotsquared.bukkit.listener;

import com.google.inject.Inject;
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
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("unused")
public class WorldEvents implements Listener {

    private final PlotAreaManager plotAreaManager;

    @Inject
    public WorldEvents(final @NonNull PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        String name = world.getName();
        if (this.plotAreaManager instanceof final SinglePlotAreaManager single) {
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
