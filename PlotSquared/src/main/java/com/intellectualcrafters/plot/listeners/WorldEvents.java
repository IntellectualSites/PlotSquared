package com.intellectualcrafters.plot.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class WorldEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onWorldInit(final WorldInitEvent event) {
        final World world = event.getWorld();
        final ChunkGenerator gen = world.getGenerator();
        if (gen instanceof PlotGenerator) {
            PlotSquared.loadWorld(world.getName(), (PlotGenerator) gen);
        } else {
            if (PlotSquared.config.contains("worlds." + world.getName())) {
                PlotSquared.loadWorld(world.getName(), null);
            }
        }
    }
    
    @EventHandler
    public void worldLoad(final WorldLoadEvent event) {
        UUIDHandler.cacheAll(event.getWorld().getName());
    }
}
