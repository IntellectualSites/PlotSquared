package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.generator.GridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.SetupUtils;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SinglePlotArea extends GridPlotWorld {

    public boolean VOID = false;

    public SinglePlotArea() {
        super("*", null, new SingleWorldGenerator(), null, null);
        this.ALLOW_SIGNS = false;
        this.DEFAULT_HOME = new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override public void loadConfiguration(ConfigurationSection config) {
        VOID = config.getBoolean("void", false);
    }

    public void loadWorld(final PlotId id) {
        TaskManager.IMP.sync(new RunnableVal<Object>() {
            @Override public void run(Object value) {
                String worldName = id.toCommaSeparatedString();
                if (WorldUtil.IMP.isWorld(worldName)) {
                    return;
                }
                SetupObject setup = new SetupObject();
                setup.plotManager = "PlotSquared:single";
                setup.setupGenerator = "PlotSquared:single";
                setup.type = TYPE;
                setup.terrain = TERRAIN;
                setup.step = new ConfigurationNode[0];
                setup.world = worldName;
                SetupUtils.manager.setupWorld(setup);
            }
        });
        //        String worldName = plot.getWorldName();
        //        World world = Bukkit.getWorld(worldName);
        //        if (world != null) {
        //            return world;
        //        }
        //        WorldCreator wc = new WorldCreator(worldName);
        //        wc.generator("PlotSquared:single");
        //        wc.environment(World.Environment.NORMAL);
        //        wc.type(WorldType.FLAT);
        //        return AsyncWorld.create(wc);
    }



    @Override public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] {
            new ConfigurationNode("void", this.VOID, "Void world", Configuration.BOOLEAN)};
    }

    @Nullable @Override public Plot getOwnedPlot(@Nonnull final Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorld());
        Plot plot = pid == null ? null : this.plots.get(pid);
        return plot == null ? null : plot.getBasePlot(false);
    }

    @Nullable @Override public Plot getOwnedPlotAbs(@Nonnull Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorld());
        return pid == null ? null : plots.get(pid);
    }

    @Nullable @Override public Plot getPlot(@Nonnull final Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorld());
        return pid == null ? null : getPlot(pid);
    }

    @Nullable @Override public Plot getPlotAbs(@Nonnull final Location location) {
        final PlotId pid = PlotId.fromStringOrNull(location.getWorld());
        return pid == null ? null : getPlotAbs(pid);
    }

    public boolean addPlot(@Nonnull Plot plot) {
        plot = adapt(plot);
        return super.addPlot(plot);
    }

    @Override public boolean addPlotAbs(@Nonnull Plot plot) {
        plot = adapt(plot);
        return super.addPlotAbs(plot);
    }

    @Override public boolean addPlotIfAbsent(@Nonnull Plot plot) {
        plot = adapt(plot);
        return super.addPlotIfAbsent(plot);
    }

    protected Plot adapt(Plot p) {
        if (p instanceof SinglePlot) {
            return p;
        }
        PlotSettings s = p.getSettings();
        p = new SinglePlot(p.getId(), p.owner, p.getTrusted(), p.getMembers(), p.getDenied(),
            s.alias, s.getPosition(), null, this, s.merged, p.getTimestamp(), p.temp);
        p.getSettings().flags = s.flags;
        return p;
    }

    @Nullable public Plot getPlotAbs(@Nonnull final PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            return new SinglePlot(this, id);
        }
        return plot;
    }

    @Nullable public Plot getPlot(@Nonnull PlotId id) {
        // TODO
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            return new SinglePlot(this, id);
        }
        return plot.getBasePlot(false);
    }
}
