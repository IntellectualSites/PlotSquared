package com.intellectualcrafters.plot.listeners.worldedit;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;

import java.util.HashSet;

public class WESubscriber {
    @Subscribe(priority=Priority.VERY_EARLY)
    public void onEditSession(EditSessionEvent event) {
        String world = event.getWorld().getName();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            String name = actor.getName();
            if (WEManager.bypass.contains(name)) {
                return;
            }

            PlotPlayer player = UUIDHandler.getPlayer(actor.getName());
            HashSet<RegionWrapper> mask = WEManager.getMask(player);
            if (mask.size() == 0) {
                if (Permissions.hasPermission(player, "plots.worldedit.bypass")) {
                    MainUtil.sendMessage(player, C.WORLDEDIT_BYPASS);
                }
                event.setExtent(new NullExtent());
                return;
            }
            if (Settings.CHUNK_PROCESSOR) {
                event.setExtent(new ProcessedWEExtent(mask, event.getExtent()));
            }
            else {
                event.setExtent(new WEExtent(mask, event.getExtent()));
            }
        }
    }
}
