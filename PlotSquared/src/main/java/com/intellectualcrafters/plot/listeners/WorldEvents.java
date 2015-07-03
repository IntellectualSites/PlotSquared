package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;

public class WorldEvents implements Listener {
    
    public static String lastWorld = null;
    
    public static String getName(World world) {
        if (lastWorld != null && !lastWorld.equals("CheckingPlotSquaredGenerator")) {
            return lastWorld;
        }
        else {
            return world.getName();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onWorldInit(final WorldInitEvent event) {
        final World world = event.getWorld();
        String name = getName(world);
        final ChunkGenerator gen = world.getGenerator();
        if (gen instanceof PlotGenerator) {
            //
            PlotSquared.getInstance().loadWorld(name, (PlotGenerator) gen);
        } else {
            if (PlotSquared.config.contains("worlds." + name)) {
                PlotSquared.getInstance().loadWorld(name, null);
            }
        }
        lastWorld = null;
    }
    
    @EventHandler
    public void worldLoad(final WorldLoadEvent event) {
        UUIDHandler.cacheAll(event.getWorld().getName());
    }
}
