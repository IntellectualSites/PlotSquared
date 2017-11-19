package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class EntitySpawnListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void creatureSpawnEvent(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Location location = BukkitUtil.getLocation(entity.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null) {
            if (!area.MOB_SPAWNING) {
                if (event.getEntityType().isAlive() || !area.MISC_SPAWN_UNOWNED) {
                    event.setCancelled(true);
                }
            }
            return;
        }
        if (Settings.Done.RESTRICT_BUILDING && plot.hasFlag(Flags.DONE)) {
            event.setCancelled(true);
        }
        switch (entity.getType()) {
            case ENDER_CRYSTAL:
                if (PlayerEvents.checkEntity(entity, plot)) {
                    event.setCancelled(true);
                }
            case SHULKER:
            	entity.setMetadata("ownerplot", new FixedMetadataValue((Plugin) PS.get().IMP, plot.getId()));
        }
    }
}
