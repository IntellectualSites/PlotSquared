package com.github.intellectualsites.plotsquared.plot.listener;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.NoWorldeditFlag;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.world.RegionUtil;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WEManager {

    public static boolean maskContains(Set<CuboidRegion> mask, int x, int y, int z) {
        for (CuboidRegion region : mask) {
            if (RegionUtil.contains(region, x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean maskContains(Set<CuboidRegion> mask, int x, int z) {
        for (CuboidRegion region : mask) {
            if (RegionUtil.contains(region, x, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean maskContains(Set<CuboidRegion> mask, double dx, double dy, double dz) {
        int x = Math.toIntExact(Math.round(dx >= 0 ? dx - 0.5 : dx + 0.5));
        int y = Math.toIntExact(Math.round(dy - 0.5));
        int z = Math.toIntExact(Math.round(dz >= 0 ? dz - 0.5 : dz + 0.5));
        for (CuboidRegion region : mask) {
            if (RegionUtil.contains(region, x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public static boolean maskContains(Set<CuboidRegion> mask, double dx, double dz) {
        int x = Math.toIntExact(Math.round(dx >= 0 ? dx - 0.5 : dx + 0.5));
        int z = Math.toIntExact(Math.round(dz >= 0 ? dz - 0.5 : dz + 0.5));
        for (CuboidRegion region : mask) {
            if (RegionUtil.contains(region, x, z)) {
                return true;
            }
        }
        return false;
    }

    public static HashSet<CuboidRegion> getMask(PlotPlayer player) {
        HashSet<CuboidRegion> regions = new HashSet<>();
        UUID uuid = player.getUUID();
        Location location = player.getLocation();
        String world = location.getWorld();
        if (!PlotSquared.get().hasPlotArea(world)) {
            regions.add(RegionUtil
                .createRegion(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
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
        if (plot != null && (!Settings.Done.RESTRICT_BUILDING || !DoneFlag.isDone(plot)) && (
            (allowMember && plot.isAdded(uuid)) || (!allowMember && (plot.isOwner(uuid)) || plot
                .getTrusted().contains(uuid))) && !plot.getFlag(NoWorldeditFlag.class)) {
            for (CuboidRegion region : plot.getRegions()) {
                BlockVector3 pos1 = region.getMinimumPoint().withY(area.MIN_BUILD_HEIGHT);
                BlockVector3 pos2 = region.getMaximumPoint().withY(area.MAX_BUILD_HEIGHT);
                CuboidRegion copy = new CuboidRegion(pos1, pos2);
                regions.add(copy);
            }
            player.setMeta("WorldEditRegionPlot", plot);
        }
        return regions;
    }

    public static boolean intersects(CuboidRegion region1, CuboidRegion region2) {
        return RegionUtil.intersects(region1, region2);
    }

    public static boolean regionContains(CuboidRegion selection, HashSet<CuboidRegion> mask) {
        for (CuboidRegion region : mask) {
            if (intersects(region, selection)) {
                return true;
            }
        }
        return false;
    }
}
