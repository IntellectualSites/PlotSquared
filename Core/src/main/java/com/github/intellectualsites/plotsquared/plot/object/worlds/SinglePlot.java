package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.object.BlockLoc;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        HashSet<UUID> denied, String alias, BlockLoc position, Collection<Flag> flags,
        PlotArea area, boolean[] merged, long timestamp, int temp) {
        super(id, owner, trusted, members, denied, alias, position, flags, area, merged, timestamp,
            temp);
    }

    @Override public String getWorldName() {
        return getId().toCommaSeparatedString();
    }

    @Override public SinglePlotArea getArea() {
        return (SinglePlotArea) super.getArea();
    }

    public boolean teleportPlayer(final PlotPlayer player) {
        if (isLoaded()) {
            return super.teleportPlayer(player);
        } else {
            Captions.NOT_LOADED.send(player);
            return false;
        }
    }

    @Override public Location getSide() {
        return getCenter();
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
