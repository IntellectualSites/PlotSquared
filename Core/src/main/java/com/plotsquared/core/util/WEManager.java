/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
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

    private static final BlockVector3 MIN = BlockVector3.at(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    private static final BlockVector3 MAX = BlockVector3.at(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

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

    public static HashSet<CuboidRegion> getMask(PlotPlayer<?> player) {
        HashSet<CuboidRegion> regions = new HashSet<>();
        UUID uuid = player.getUUID();
        Location location = player.getLocation();
        String world = location.getWorldName();
        if (!PlotSquared.get().getPlotAreaManager().hasPlotArea(world)) {
            regions.add(new CuboidRegion(MIN, MAX));
            return regions;
        }
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            return regions;
        }
        boolean allowMember = player.hasPermission("plots.worldedit.member");
        Plot plot = player.getCurrentPlot();
        try (final MetaDataAccess<Plot> metaDataAccess =
                     player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_WORLD_EDIT_REGION_PLOT)) {
            if (plot == null) {
                plot = metaDataAccess.get().orElse(null);
            }
            if (plot != null && (!Settings.Done.RESTRICT_BUILDING || !DoneFlag.isDone(plot)) && (
                    (allowMember && plot.isAdded(uuid)) || (!allowMember && plot.isOwner(uuid) || plot
                            .getTrusted().contains(uuid))) && !plot.getFlag(NoWorldeditFlag.class)) {
                for (CuboidRegion region : plot.getRegions()) {
                    BlockVector3 pos1 = region.getMinimumPoint().withY(area.getMinBuildHeight());
                    BlockVector3 pos2 = region.getMaximumPoint().withY(area.getMaxBuildHeight() - 1);
                    CuboidRegion copy = new CuboidRegion(pos1, pos2);
                    regions.add(copy);
                }
                metaDataAccess.set(plot);
            }
        }
        return regions;
    }

}
