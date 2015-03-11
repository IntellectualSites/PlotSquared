////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.object;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PlotSquared;

public abstract class PlotGenerator extends ChunkGenerator {
    
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        PlotSquared.loadWorld(world.getName(), this);
//        world = Bukkit.getWorld(PlotSquared.GEN_WORLD);
        PlotWorld plotworld = PlotSquared.getPlotWorld(world.getName());
        if (!plotworld.MOB_SPAWNING) {
            if (!plotworld.SPAWN_EGGS) {
                world.setSpawnFlags(false, false);
            }
            world.setAmbientSpawnLimit(0);
            world.setAnimalSpawnLimit(0);
            world.setMonsterSpawnLimit(0);
            world.setWaterAnimalSpawnLimit(0);
        }
        else {
            world.setSpawnFlags(true, true);
            world.setAmbientSpawnLimit(-1);
            world.setAnimalSpawnLimit(-1);
            world.setMonsterSpawnLimit(-1);
            world.setWaterAnimalSpawnLimit(-1);
        }
        return getPopulators(world.getName());
    }
    
    public abstract List<BlockPopulator> getPopulators(String world);
    
    public abstract void init(PlotWorld plotworld);

    public abstract PlotWorld getNewPlotWorld(final String world);

    public abstract PlotManager getPlotManager();
    
    
}
