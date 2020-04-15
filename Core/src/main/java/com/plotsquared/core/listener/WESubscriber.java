package com.plotsquared.core.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.config.Settings;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.WEManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;

import java.util.Set;

public class WESubscriber {

    @Subscribe(priority = Priority.VERY_EARLY) public void onEditSession(EditSessionEvent event) {
        if (!Settings.Enabled_Components.WORLDEDIT_RESTRICTIONS) {
            WorldEdit.getInstance().getEventBus().unregister(this);
            return;
        }
        World worldObj = event.getWorld();
        if (worldObj == null) {
            return;
        }
        String world = worldObj.getName();
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            String name = actor.getName();
            PlotPlayer plotPlayer = PlotPlayer.wrap(name);
            Set<CuboidRegion> mask;
            if (plotPlayer == null) {
                Player player = (Player) actor;
                Location location = player.getLocation();
                com.plotsquared.core.location.Location pLoc =
                    new com.plotsquared.core.location.Location(
                        player.getWorld().getName(), location.getBlockX(), location.getBlockX(),
                        location.getBlockZ());
                Plot plot = pLoc.getPlot();
                if (plot == null) {
                    event.setExtent(new NullExtent());
                    return;
                }
                mask = plot.getRegions();
            } else if (plotPlayer.getAttribute("worldedit")) {
                return;
            } else {
                mask = WEManager.getMask(plotPlayer);
                if (mask.isEmpty()) {
                    if (Permissions.hasPermission(plotPlayer, "plots.worldedit.bypass")) {
                        MainUtil.sendMessage(plotPlayer, Captions.WORLDEDIT_BYPASS);
                    }
                    if (PlotSquared.get().hasPlotArea(world)) {
                        event.setExtent(new NullExtent());
                    }
                    return;
                }
            }
            if (Settings.Enabled_Components.CHUNK_PROCESSOR) {
                if (PlotSquared.get().hasPlotArea(world)) {
                    event.setExtent(
                        new ProcessedWEExtent(world, mask, event.getMaxBlocks(), event.getExtent(),
                            event.getExtent()));
                }
            } else if (PlotSquared.get().hasPlotArea(world)) {
                event.setExtent(new WEExtent(mask, event.getExtent()));
            }
        }
    }
}
