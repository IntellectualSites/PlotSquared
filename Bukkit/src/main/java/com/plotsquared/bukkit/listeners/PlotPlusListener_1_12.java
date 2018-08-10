package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.UUID;

public class PlotPlusListener_1_12 implements Listener {
    @EventHandler public void onItemPickup(EntityPickupItemEvent event) {
        LivingEntity ent = event.getEntity();
        if (ent instanceof Player) {
            Player player = (Player) ent;
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
            if (plot == null) {
                return;
            }
            UUID uuid = pp.getUUID();
            if (!plot.isAdded(uuid) && Flags.DROP_PROTECTION.isTrue(plot)) {
                event.setCancelled(true);
            }
        }
    }
}
