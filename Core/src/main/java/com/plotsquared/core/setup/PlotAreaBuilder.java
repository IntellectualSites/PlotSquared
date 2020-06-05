package com.plotsquared.core.setup;


import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.SetupUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class PlotAreaBuilder {
    @Getter @Setter private String generatorName;
    @Getter @Setter private String plotManager;
    @Getter @Setter private PlotAreaType plotAreaType;
    @Getter @Setter private PlotAreaTerrainType terrainType;
    @Getter @Setter private String worldName;
    @Getter @Setter private String areaName;
    @Getter private PlotId minimumId;
    @Getter private PlotId maximumId;
    @Getter @Setter private SettingsNodesWrapper settingsNodesWrapper;
    @Getter @Setter private SetupUtils setupManager;

    public static PlotAreaBuilder ofPlotArea(PlotArea area) {
        return new PlotAreaBuilder()
                .worldName(area.getWorldName())
                .areaName(area.getId())
                .plotAreaType(area.getType())
                .terrainType(area.getTerrain())
                .generatorName(area.getGenerator().getName())
                .plotManager(PlotSquared.imp().getPluginName())
                .minimumId(area.getMin())
                .maximumId(area.getMax())
                .settingsNodesWrapper(new SettingsNodesWrapper(area.getSettingNodes(), null));
    }

    public PlotAreaBuilder minimumId(PlotId minimumId) {
        if (this.maximumId != null
                && (minimumId.getX() > this.maximumId.getX() || minimumId.getY() > this.maximumId.getY())) {
            throw new IllegalStateException("minId >= maxId");
        }
        this.minimumId = minimumId;
        return this;
    }

    public PlotAreaBuilder maximumId(PlotId maximumId) {
        if (this.minimumId != null
                && (maximumId.getX() < this.minimumId.getX() || maximumId.getY() < this.minimumId.getY())) {
            throw new IllegalStateException("maxId <= minId");
        }
        this.maximumId = maximumId;
        return this;
    }

}
