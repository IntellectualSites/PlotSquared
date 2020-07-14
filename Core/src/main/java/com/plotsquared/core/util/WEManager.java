/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.flag.implementations.NoWorldeditFlag;
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

    public static HashSet<CuboidRegion> getMask(PlotPlayer<?> player) {
        HashSet<CuboidRegion> regions = new HashSet<>();
        UUID uuid = player.getUUID();
        Location location = player.getLocation();
        String world = location.getWorldName();
        if (!PlotSquared.get().getPlotAreaManager().hasPlotArea(world)) {
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
                BlockVector3 pos1 = region.getMinimumPoint().withY(area.getMinBuildHeight());
                BlockVector3 pos2 = region.getMaximumPoint().withY(area.getMaxBuildHeight());
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
