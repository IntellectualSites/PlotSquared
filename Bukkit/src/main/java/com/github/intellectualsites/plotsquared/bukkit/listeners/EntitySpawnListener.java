package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
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
                EntityType type = entity.getType();
                switch (type) {
                    case DROPPED_ITEM:
                        if (Settings.Enabled_Components.KILL_ROAD_ITEMS) {
                            break;
                        }
                    case PLAYER:
                        return;
                }
                if (type.isAlive() || !area.MISC_SPAWN_UNOWNED) {
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
                if (!entity.hasMetadata("plot")) {
                    entity.setMetadata("plot",
                        new FixedMetadataValue((Plugin) PlotSquared.get().IMP, plot.getId()));
                }
        }
    }
}
