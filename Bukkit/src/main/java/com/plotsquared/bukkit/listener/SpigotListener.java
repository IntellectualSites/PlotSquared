package com.plotsquared.bukkit.listener;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.plot.flag.implementations.BeaconEffectsFlag;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fallback listener for paper events on spigot
 */
public class SpigotListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEffect(@NotNull EntityPotionEffectEvent event) {
        if (event.getCause() != EntityPotionEffectEvent.Cause.BEACON) return;
        Entity entity = event.getEntity();

        Location location = BukkitUtil.adapt(entity.getLocation());
        Plot plot = location.getPlot();
        if (plot == null) {
            return;
        }

        FlagContainer container = plot.getFlagContainer();
        BeaconEffectsFlag effectsEnabled = container.getFlag(BeaconEffectsFlag.class);
        if (effectsEnabled != null && !effectsEnabled.getValue()) {
            event.setCancelled(true);
        }
    }

}
