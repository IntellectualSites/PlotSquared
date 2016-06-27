package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void creatureSpawnEvent(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        switch (entity.getType()) {
            case ENDER_CRYSTAL:
                Location location = BukkitUtil.getLocation(entity.getLocation());
                PlotArea area = location.getPlotArea();
                if (area == null) {
                    return;
                }
                Plot plot = area.getOwnedPlotAbs(location);
                if (plot == null) {
                    if (!area.MOB_SPAWNING) {
                        event.setCancelled(true);
                    }
                    return;
                }
                if (PlayerEvents.checkEntity(entity, plot)) {
                    event.setCancelled(true);
                }
        }
    }
}
