package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;

@SuppressWarnings("unused") public class PlayerEvents_1_9 implements Listener {

    private final PlayerEvents parent;

    public PlayerEvents_1_9(PlayerEvents parent) {
        this.parent = parent;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(LingeringPotionSplashEvent event) {
        LingeringPotion entity = event.getEntity();
        Location l = BukkitUtil.getLocation(entity);
        if (!PlotSquared.get().hasPlotArea(l.getWorld())) {
            return;
        }
        if (!parent.onProjectileHit(event)) {
            event.setCancelled(true);
        }
    }
}
