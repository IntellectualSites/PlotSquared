package com.github.intellectualsites.plotsquared.plot.listener;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;

import java.util.HashSet;

public class WESubscriber {

    @Subscribe(priority = Priority.VERY_EARLY) public void onEditSession(EditSessionEvent event) {
        WorldEdit worldedit = PlotSquared.get().worldedit;
        if (worldedit == null) {
            WorldEdit.getInstance().getEventBus().unregister(this);
            return;
        }
        World worldObj = event.getWorld();
        String world = worldObj.getName();
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            String name = actor.getName();
            PlotPlayer pp = PlotPlayer.wrap(name);
            HashSet<RegionWrapper> mask;
            if (pp == null) {
                Player player = (Player) actor;
                Location loc = player.getLocation();
                com.github.intellectualsites.plotsquared.plot.object.Location pLoc =
                    new com.github.intellectualsites.plotsquared.plot.object.Location(player.getWorld().getName(),
                        loc.getBlockX(), loc.getBlockX(), loc.getBlockZ());
                Plot plot = pLoc.getPlot();
                if (plot == null) {
                    event.setExtent(new NullExtent());
                    return;
                }
                mask = plot.getRegions();
            } else if (pp.getAttribute("worldedit")) {
                return;
            } else {
                mask = WEManager.getMask(pp);
                if (mask.isEmpty()) {
                    if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                        MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASS);
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
