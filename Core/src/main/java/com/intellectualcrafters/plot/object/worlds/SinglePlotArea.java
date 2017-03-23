package com.intellectualcrafters.plot.object.worlds;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.GridPlotWorld;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotSettings;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.WorldUtil;

public class SinglePlotArea extends GridPlotWorld {

    private final SinglePlotArea instance;
    public boolean VOID = false;

    public SinglePlotArea() {
        super("*", null, new SingleWorldGenerator(), null, null);
        instance = this;
        this.ALLOW_SIGNS = false;
        this.DEFAULT_HOME = new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void loadConfiguration(ConfigurationSection config) {
        VOID = config.getBoolean("void", false);
    }

    public void loadWorld(PlotId id) {
        String worldName = id.toCommaSeparatedString();
        if (WorldUtil.IMP.isWorld(worldName)) return;
        SetupObject setup = new SetupObject();
        setup.plotManager = "PlotSquared:single";
        setup.setupGenerator = "PlotSquared:single";
        setup.type = 0;
        setup.terrain = 0;
        setup.step = new ConfigurationNode[0];
        setup.world = worldName;
        SetupUtils.manager.setupWorld(setup);
    }

    @Override
    public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] {
                new ConfigurationNode("void", this.VOID, "Void world", Configuration.BOOLEAN) };
    }

    @Override
    public Plot getOwnedPlot(Location location) {
        PlotId pid = PlotId.fromString(location.getWorld());
        if (pid == null) {
            return null;
        }
        Plot plot = this.plots.get(pid);
        return plot == null ? null : plot.getBasePlot(false);
    }

    @Override
    public Plot getOwnedPlotAbs(Location location) {
        PlotId pid = PlotId.fromString(location.getWorld());
        if (pid == null) {
            return null;
        }
        return plots.get(pid);
    }

    @Override
    public Plot getPlot(Location location) {
        PlotId pid = PlotId.fromString(location.getWorld());
        if (pid == null) {
            return null;
        }
        return getPlot(pid);
    }

    @Override
    public Plot getPlotAbs(Location location) {
        PlotId pid = PlotId.fromString(location.getWorld());
        if (pid == null) {
            return null;
        }
        return getPlotAbs(pid);
    }

    public boolean addPlot(Plot p) {
        PlotSettings s = p.getSettings();
        p = new SinglePlot(p.getId(), p.owner, p.getTrusted(), p.getMembers(), p.getDenied(), s.alias,  s.getPosition(), null, this, s.merged, p.getTimestamp(), p.temp);
        p.getSettings().flags = s.flags;
        for (PlotPlayer pp : p.getPlayersInPlot()) {
            pp.setMeta("lastplot", p);
        }
        return this.plots.put(p.getId(), p) == null;
    }

    public Plot getPlotAbs(PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            return new SinglePlot(this, id);
        }
        return plot;
    }

    public Plot getPlot(PlotId id) {
        // TODO
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            return new SinglePlot(this, id);
        }
        return plot.getBasePlot(false);
    }
}
