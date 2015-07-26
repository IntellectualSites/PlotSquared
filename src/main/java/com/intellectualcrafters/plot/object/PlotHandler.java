package com.intellectualcrafters.plot.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class PlotHandler {
    public static HashSet<UUID> getOwners(Plot plot) {
        if (plot.owner == null) {
            return new HashSet<UUID>();
        }
        if (plot.isMerged()) {
            HashSet<UUID> owners = new HashSet<UUID>();
            Plot top = MainUtil.getTopPlot(plot);
            ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(plot.id, top.id);
            for (PlotId id : ids) {
                UUID owner = MainUtil.getPlot(plot.world, id).owner;
                if (owner != null) {
                    owners.add(owner);
                }
            }
            return owners;
        }
        return new HashSet<>(Arrays.asList(plot.owner));
    }
    
    public static boolean isOwner(Plot plot, UUID uuid) {
        if (plot.owner == null) {
            return false;
        }
        if (plot.isMerged()) {
            Plot top = MainUtil.getTopPlot(plot);
            ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(plot.id, top.id);
            for (PlotId id : ids) {
                UUID owner = MainUtil.getPlot(plot.world, id).owner;
                if (owner != null && owner.equals(uuid)) {
                    return true;
                }
            }
        }
        return plot.owner.equals(uuid);
    }
    
    public static boolean isOnline(Plot plot) {
        if (plot.owner == null) {
            return false;
        }
        if (plot.isMerged()) {
            Plot top = MainUtil.getTopPlot(plot);
            ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(plot.id, top.id);
            for (PlotId id : ids) {
                UUID owner = MainUtil.getPlot(plot.world, id).owner;
                if (owner != null) {
                    if (UUIDHandler.getPlayer(owner) != null) {
                        return true;
                    }
                }
            }
            return false;
        }
        return UUIDHandler.getPlayer(plot.owner) != null;
    }
    
    public static boolean sameOwners(Plot plot1, Plot plot2) {
        if (plot1.owner == null || plot2.owner == null) {
            return false;
        }
        HashSet<UUID> owners = getOwners(plot1);
        owners.retainAll(getOwners(plot2));
        return owners.size() > 0;
    }
    
    public static boolean isAdded(Plot plot, final UUID uuid) {
        if (plot.owner == null) {
            return false;
        }
        if (isOwner(plot, uuid)) {
            return true;
        }
        if (plot.getDenied().contains(uuid)) {
            return false;
        }
        if (plot.getTrusted().contains(uuid) || plot.getTrusted().contains(DBFunc.everyone)) {
            return true;
        }
        if (plot.getMembers().contains(uuid) || plot.getMembers().contains(DBFunc.everyone)) {
            if (PlotHandler.isOnline(plot)) {
                return true;
            }
        }
        if (plot.isMerged()) {
            Plot top = MainUtil.getTopPlot(plot);
            ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(plot.id, top.id);
            for (PlotId id : ids) {
                UUID owner = MainUtil.getPlot(plot.world, id).owner;
                if (owner != null && owner.equals(uuid)) {
                    return true;
                }
            }
        }
        return false;
    }
}
