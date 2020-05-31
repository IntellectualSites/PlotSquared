package com.plotsquared.core.setup;


import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
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
    @Getter @Setter private SettingsNodesWrapper settingsNodesWrapper;

}
