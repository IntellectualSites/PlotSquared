package com.github.intellectualsites.plotsquared.plot.listener;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;

import java.util.HashSet;
import java.util.UUID;

public class WEManager {

    public static boolean maskContains(HashSet<RegionWrapper> mask, int x, int y, int z) {
        for (RegionWrapper region : mask) {
            if (region.isIn(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean maskContains(HashSet<RegionWrapper> mask, int x, int z) {
        for (RegionWrapper region : mask) {
            if (region.isIn(x, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean maskContains(HashSet<RegionWrapper> mask, double dx, double dy,
        double dz) {
        int x = Math.toIntExact(Math.round(dx >= 0 ? dx - 0.5 : dx + 0.5));
        int y = Math.toIntExact(Math.round(dy - 0.5));
        int z = Math.toIntExact(Math.round(dz >= 0 ? dz - 0.5 : dz + 0.5));
        for (RegionWrapper region : mask) {
            if (region.isIn(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean maskContains(HashSet<RegionWrapper> mask, double dx, double dz) {
        int x = Math.toIntExact(Math.round(dx >= 0 ? dx - 0.5 : dx + 0.5));
        int z = Math.toIntExact(Math.round(dz >= 0 ? dz - 0.5 : dz + 0.5));
        for (RegionWrapper region : mask) {
            if (region.isIn(x, z)) {
                return true;
            }
        }
        return false;
    }

    public static HashSet<RegionWrapper> getMask(PlotPlayer player) {
        HashSet<RegionWrapper> regions = new HashSet<>();
        UUID uuid = player.getUUID();
        Location location = player.getLocation();
        String world = location.getWorld();
        if (!PlotSquared.get().hasPlotArea(world)) {
            regions.add(new RegionWrapper(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
                Integer.MAX_VALUE));
            return regions;
        }
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            return regions;
        }
        boolean allowMember = player.hasPermission("plots.worldedit.member");
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            plot = player.getMeta("WorldEditRegionPlot");
        }
        if (plot != null && (!Settings.Done.RESTRICT_BUILDING || !Flags.DONE.isSet(plot)) && (
            (allowMember && plot.isAdded(uuid)) || (!allowMember && (plot.isOwner(uuid)) || plot
                .getTrusted().contains(uuid))) && !(Flags.NO_WORLDEDIT.isTrue(plot))) {
            for (RegionWrapper region : plot.getRegions()) {
                RegionWrapper copy =
                    new RegionWrapper(region.minX, region.maxX, area.MIN_BUILD_HEIGHT,
                        area.MAX_BUILD_HEIGHT, region.minZ, region.maxZ);
                regions.add(copy);
            }
            player.setMeta("WorldEditRegionPlot", plot);
        }
        return regions;
    }

    public static boolean intersects(RegionWrapper region1, RegionWrapper region2) {
        return region1.intersects(region2);
    }

    public static boolean regionContains(RegionWrapper selection, HashSet<RegionWrapper> mask) {
        for (RegionWrapper region : mask) {
            if (intersects(region, selection)) {
                return true;
            }
        }
        return false;
    }
}
