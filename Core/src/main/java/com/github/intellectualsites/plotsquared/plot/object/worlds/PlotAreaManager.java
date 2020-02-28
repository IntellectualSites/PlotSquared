package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;

public interface PlotAreaManager {

    /**
     * Get the plot area for a particular location. This
     * method assumes that the caller already knows that
     * the location belongs to a plot area, in which
     * case it will return the appropriate plot area.
     *
     * If the location does not belong to a plot area,
     * it may still return an area.
     *
     * @param location The location
     * @return An applicable area, or null
     */
    PlotArea getApplicablePlotArea(Location location);

    /**
     * Get the plot area, if there is any, for the given
     * location. This may return null, if given location
     * does not belong to a plot area.
     *
     * @param location The location
     * @return The area, if found
     */
    PlotArea getPlotArea(@NotNull Location location);

    PlotArea getPlotArea(String world, String id);

    PlotArea[] getPlotAreas(String world, CuboidRegion region);

    PlotArea[] getAllPlotAreas();

    String[] getAllWorlds();

    void addPlotArea(PlotArea area);

    void removePlotArea(PlotArea area);

    void addWorld(String worldName);

    void removeWorld(String worldName);

}
