package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;

public class WorldEvents implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        String name = world.getName();
        ChunkGenerator gen = world.getGenerator();
        if (gen instanceof GeneratorWrapper) {
            PS.get().loadWorld(name, (GeneratorWrapper<?>) gen);
        } else {
            PS.get().loadWorld(name, new BukkitPlotGenerator(name, gen));
        }
    }
}
