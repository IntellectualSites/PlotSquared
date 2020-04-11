package com.plotsquared.plot.worlds;

import com.plotsquared.plot.flags.PlotFlag;
import com.plotsquared.location.BlockLoc;
import com.plotsquared.location.Location;
import com.plotsquared.plot.Plot;
import com.plotsquared.plot.PlotArea;
import com.plotsquared.plot.PlotId;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class SinglePlot extends Plot {
    private Set<CuboidRegion> regions = Collections.singleton(
        new CuboidRegion(BlockVector3.at(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
            BlockVector3.at(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)));

    public SinglePlot(PlotArea area, PlotId id, UUID owner) {
        super(area, id, owner);
    }

    public SinglePlot(PlotArea area, PlotId id) {
        super(area, id);
    }

    public SinglePlot(PlotArea area, PlotId id, UUID owner, int temp) {
        super(area, id, owner, temp);
    }

    public SinglePlot(PlotId id, UUID owner, HashSet<UUID> trusted, HashSet<UUID> members,
        HashSet<UUID> denied, String alias, BlockLoc position, Collection<PlotFlag<?, ?>> flags,
        PlotArea area, boolean[] merged, long timestamp, int temp) {
        super(id, owner, trusted, members, denied, alias, position, flags, area, merged, timestamp,
            temp);
    }

    @Override public String getWorldName() {
        return getId().getX() + "." + getId().getY();
    }

    @Override public SinglePlotArea getArea() {
        return (SinglePlotArea) super.getArea();
    }

    @Override public void getSide(Consumer<Location> result) {
        getCenter(result);
    }

    @Override protected boolean isLoaded() {
        getArea().loadWorld(getId());
        return super.isLoaded();
    }

    @NotNull @Override public Set<CuboidRegion> getRegions() {
        return regions;
    }

    // getCenter getSide getHome getDefaultHome getBiome
}
