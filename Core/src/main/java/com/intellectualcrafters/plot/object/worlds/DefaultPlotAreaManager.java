package com.intellectualcrafters.plot.object.worlds;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.area.QuadMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultPlotAreaManager implements PlotAreaManager {

    // All plot areas
    private PlotArea[] plotAreas = new PlotArea[0];
    // All plot areas mapped by world
    private final HashMap<String, PlotArea[]> plotAreaMap = new HashMap<>();
    // All plot areas mapped by position
    private final HashMap<String, QuadMap<PlotArea>> plotAreaGrid = new HashMap<>();
    // Optimization if there are no hash collisions
    private boolean plotAreaHasCollision = false;
    private final HashSet<Integer> plotAreaHashCheck = new HashSet<>();
    protected final PlotArea[] noPlotAreas = new PlotArea[0];
    private String[] worlds = new String[0];

    @Override
    public PlotArea[] getAllPlotAreas() {
        return plotAreas;
    }

    @Override
    public PlotArea getApplicablePlotArea(Location location) {
        switch (this.plotAreas.length) {
            case 0:
                return null;
            case 1:
                return this.plotAreas[0];
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                String world = location.getWorld();
                int hash = world.hashCode();
                for (PlotArea area : this.plotAreas) {
                    if (hash == area.worldhash) {
                        if (area.contains(location.getX(), location.getZ()) && (!this.plotAreaHasCollision || world.equals(area.worldname))) {
                            return area;
                        }
                    }
                }
                return null;
            default:
                PlotArea[] areas = this.plotAreaMap.get(location.getWorld());
                if (areas == null) {
                    return null;
                }
                int y;
                int x;
                switch (areas.length) {
                    case 1:
                        return areas[0];
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        x = location.getX();
                        y = location.getY();
                        for (PlotArea area : areas) {
                            if (area.contains(x, y)) {
                                return area;
                            }
                        }
                        return null;
                    default:
                        QuadMap<PlotArea> search = this.plotAreaGrid.get(location.getWorld());
                        return search.get(location.getX(), location.getZ());
                }
        }
    }

    @Override
    public void addPlotArea(PlotArea plotArea) {
        HashSet<PlotArea> localAreas = new HashSet<>(Arrays.asList(getPlotAreas(plotArea.worldname, null)));
        HashSet<PlotArea> globalAreas = new HashSet<>(Arrays.asList(plotAreas));
        localAreas.add(plotArea);
        globalAreas.add(plotArea);
        this.plotAreas = globalAreas.toArray(new PlotArea[globalAreas.size()]);
        this.plotAreaMap.put(plotArea.worldname, localAreas.toArray(new PlotArea[localAreas.size()]));
        QuadMap<PlotArea> map = this.plotAreaGrid.get(plotArea.worldname);
        if (map == null) {
            map = new QuadMap<PlotArea>(Integer.MAX_VALUE, 0, 0) {
                @Override
                public RegionWrapper getRegion(PlotArea value) {
                    return value.getRegion();
                }
            };
            this.plotAreaGrid.put(plotArea.worldname, map);
        }
        map.add(plotArea);
    }

    @Override
    public void removePlotArea(PlotArea area) {
        ArrayList<PlotArea> globalAreas = new ArrayList<PlotArea>(Arrays.asList(plotAreas));
        globalAreas.remove(area);
        this.plotAreas = globalAreas.toArray(new PlotArea[globalAreas.size()]);
        if (globalAreas.isEmpty()) {
            this.plotAreaMap.remove(area.worldname);
            this.plotAreaGrid.remove(area.worldname);
        } else {
            this.plotAreaMap.put(area.worldname, globalAreas.toArray(new PlotArea[globalAreas.size()]));
            this.plotAreaGrid.get(area.worldname).remove(area);
        }
    }

    @Override
    public PlotArea getPlotArea(String world, String id) {
        PlotArea[] areas = this.plotAreaMap.get(world);
        if (areas == null) {
            return null;
        }
        if (areas.length == 1) {
            return areas[0];
        } else if (id == null) {
            return null;
        }
        for (PlotArea area : areas) {
            if (StringMan.isEqual(id, area.id)) {
                return area;
            }
        }
        return null;
    }

    @Override
    public PlotArea getPlotArea(Location location) {
        switch (this.plotAreas.length) {
            case 0:
                return null;
            case 1:
                PlotArea pa = this.plotAreas[0];
                return pa.contains(location) ? pa : null;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                String world = location.getWorld();
                int hash = world.hashCode();
                for (PlotArea area : this.plotAreas) {
                    if (hash == area.worldhash) {
                        if (area.contains(location.getX(), location.getZ()) && (!this.plotAreaHasCollision || world.equals(area.worldname))) {
                            return area;
                        }
                    }
                }
                return null;
            default:
                PlotArea[] areas = this.plotAreaMap.get(location.getWorld());
                if (areas == null) {
                    return null;
                }
                int x;
                int y;
                switch (areas.length) {
                    case 0:
                        PlotArea a = areas[0];
                        return a.contains(location.getX(), location.getZ()) ? a : null;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        x = location.getX();
                        y = location.getY();
                        for (PlotArea area : areas) {
                            if (area.contains(x, y)) {
                                return area;
                            }
                        }
                        return null;
                    default:
                        QuadMap<PlotArea> search = this.plotAreaGrid.get(location.getWorld());
                        return search.get(location.getX(), location.getZ());
                }
        }
    }

    @Override
    public PlotArea[] getPlotAreas(String world, RegionWrapper region) {
        if (region == null) {
            PlotArea[] areas = this.plotAreaMap.get(world);
            if (areas == null) {
                return noPlotAreas;
            }
            return areas;
        }
        QuadMap<PlotArea> areas = this.plotAreaGrid.get(world);
        if (areas == null) {
            return noPlotAreas;
        } else {
            Set<PlotArea> found = areas.get(region);
            return found.toArray(new PlotArea[found.size()]);
        }
    }

    @Override
    public void addWorld(String worldName) {
        if (!this.plotAreaHasCollision && !this.plotAreaHashCheck.add(worldName.hashCode())) {
            this.plotAreaHasCollision = true;
        }
        Set<String> tmp = new LinkedHashSet<>();
        Collections.addAll(tmp, worlds);
        tmp.add(worldName);
        worlds = tmp.toArray(new String[tmp.size()]);
    }

    @Override
    public void removeWorld(String worldName) {
        Set<String> tmp = new LinkedHashSet<>();
        Collections.addAll(tmp, worlds);
        tmp.remove(worldName);
        worlds = tmp.toArray(new String[tmp.size()]);
    }

    @Override
    public String[] getAllWorlds() {
        return worlds;
    }
}
