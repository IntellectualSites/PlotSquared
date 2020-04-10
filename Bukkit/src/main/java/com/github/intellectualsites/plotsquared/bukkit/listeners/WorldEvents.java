package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.plot.object.worlds.PlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotAreaManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;

@SuppressWarnings("unused")
public class WorldEvents implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onWorldInit(WorldInitEvent event) {
    World world = event.getWorld();
    String name = world.getName();
    PlotAreaManager manager = PlotSquared.get().getPlotAreaManager();
    if (manager instanceof SinglePlotAreaManager) {
      SinglePlotAreaManager single = (SinglePlotAreaManager) manager;
      if (single.isWorld(name)) {
        world.setKeepSpawnInMemory(false);
        return;
      }
    }
    ChunkGenerator gen = world.getGenerator();
    if (gen instanceof GeneratorWrapper) {
      PlotSquared.get().loadWorld(name, (GeneratorWrapper<?>) gen);
    } else {
      PlotSquared.get().loadWorld(name, new BukkitPlotGenerator(name, gen));
    }
  }
}
