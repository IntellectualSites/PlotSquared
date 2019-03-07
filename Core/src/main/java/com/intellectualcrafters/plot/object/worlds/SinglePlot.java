package com.intellectualcrafters.plot.object.worlds;

import com.google.common.collect.Sets;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class SinglePlot extends Plot {
    private HashSet<RegionWrapper> regions = Sets.newHashSet(
        new RegionWrapper(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
            Integer.MAX_VALUE));

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

    @Override protected boolean isLoaded() {
        getArea().loadWorld(getId());
        return super.isLoaded();
    }

    @Override public Location getSide() {
        return getCenter();
    }

    @Nonnull @Override public HashSet<RegionWrapper> getRegions() {
        return regions;
    }

    public boolean teleportPlayer(final PlotPlayer player) {
        if (isLoaded()) {
            return super.teleportPlayer(player);
        } else {
            C.NOT_LOADED.send(player);
            return false;
        }
    }

    // getCenter getSide getHome getDefaultHome getBiome
}
