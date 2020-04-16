/*
 *
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
package com.plotsquared.core.plot.world;

import com.plotsquared.core.generator.SingleWorldGenerator;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.collection.ArrayUtil;
import com.plotsquared.core.util.SetupUtils;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;

public class SinglePlotAreaManager extends DefaultPlotAreaManager {
    private final SinglePlotArea[] array;
    private SinglePlotArea area;
    private PlotArea[] all;

    public SinglePlotAreaManager() {
        this.area = new SinglePlotArea();
        this.array = new SinglePlotArea[] {area};
        this.all = new PlotArea[] {area};
        SetupUtils.generators.put("PlotSquared:single",
            new SingleWorldGenerator().specify("CheckingPlotSquaredGenerator"));
    }

    public SinglePlotArea getArea() {
        return area;
    }

    public void setArea(SinglePlotArea area) {
        this.area = area;
        array[0] = area;
        all = ArrayUtil.concatAll(super.getAllPlotAreas(), array);
    }

    public boolean isWorld(String id) {
        char[] chars = id.toCharArray();
        if (chars.length == 1 && chars[0] == '*') {
            return true;
        }
        int mode = 0;
        for (char c : chars) {
            switch (mode) {
                case 0:
                    mode = 1;
                    if (c == '-') {
                        continue;
                    }
                case 1:
                    if ((c <= '/') || (c >= ':')) {
                        if (c == '.') {
                            mode = 2;
                            continue;
                        }
                        return false;
                    } else {
                        continue;
                    }
                case 2:
                    mode = 3;
                    if (c == '-') {
                        continue;
                    }
                case 3:
                    if ((c <= '/') || (c >= ':')) {
                        return false;
                    }
                    continue;
            }
        }
        return mode == 3;
    }

    @Override public PlotArea getApplicablePlotArea(Location location) {
        String world = location.getWorld();
        return isWorld(world) || world.equals("*") || super.getAllPlotAreas().length == 0 ?
            area :
            super.getApplicablePlotArea(location);
    }

    @Override public PlotArea getPlotArea(String world, String id) {
        PlotArea found = super.getPlotArea(world, id);
        if (found != null) {
            return found;
        }
        return isWorld(world) || world.equals("*") ? area : super.getPlotArea(world, id);
    }

    @Override public PlotArea getPlotArea(@NotNull Location location) {
        PlotArea found = super.getPlotArea(location);
        if (found != null) {
            return found;
        }
        return isWorld(location.getWorld()) || location.getWorld().equals("*") ? area : null;
    }

    @Override public PlotArea[] getPlotAreas(String world, CuboidRegion region) {
        PlotArea[] found = super.getPlotAreas(world, region);
        if (found != null && found.length != 0) {
            return found;
        }
        return isWorld(world) || world.equals("*") ?
            array :
            all.length == 0 ? noPlotAreas : super.getPlotAreas(world, region);
    }

    @Override public PlotArea[] getAllPlotAreas() {
        return all;
    }

    @Override public String[] getAllWorlds() {
        return super.getAllWorlds();
    }

    @Override public void addPlotArea(PlotArea area) {
        if (area == this.area) {
            return;
        }
        super.addPlotArea(area);
        all = ArrayUtil.concatAll(super.getAllPlotAreas(), array);
    }

    @Override public void removePlotArea(PlotArea area) {
        if (area == this.area) {
            throw new UnsupportedOperationException("Cannot remove base area!");
        }
        super.removePlotArea(area);
    }

    @Override public void addWorld(String worldName) {
        super.addWorld(worldName);
    }

    @Override public void removeWorld(String worldName) {
        super.removeWorld(worldName);
    }
}
