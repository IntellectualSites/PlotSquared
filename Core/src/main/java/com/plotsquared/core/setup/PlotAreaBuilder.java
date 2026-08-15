/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.setup;


import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.SetupUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PlotAreaBuilder {

    private String generatorName;
    private String plotManager;
    @Nullable
    private PlotAreaType plotAreaType;
    private PlotAreaTerrainType terrainType;
    private String worldName;
    private String areaName;
    private PlotId minimumId;
    private PlotId maximumId;
    private SettingsNodesWrapper settingsNodesWrapper;
    private SetupUtils setupManager;

    private PlotAreaBuilder() {
    }

    public static PlotAreaBuilder newBuilder() {
        return new PlotAreaBuilder();
    }

    public static PlotAreaBuilder ofPlotArea(PlotArea area) {
        return new PlotAreaBuilder()
                .worldName(area.getWorldName())
                .areaName(area.getId())
                .plotAreaType(area.getType())
                .terrainType(area.getTerrain())
                .generatorName(area.getGenerator().getName())
                .plotManager(PlotSquared.platform().pluginName())
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

    public String generatorName() {
        return this.generatorName;
    }

    public String plotManager() {
        return this.plotManager;
    }

    @NotNull
    @Contract(" -> !null")
    public PlotAreaType plotAreaType() {
        return Objects.requireNonNullElse(this.plotAreaType, PlotAreaType.NORMAL);
    }

    public PlotAreaTerrainType terrainType() {
        return this.terrainType;
    }

    public String worldName() {
        return this.worldName;
    }

    public String areaName() {
        return this.areaName;
    }

    public PlotId minimumId() {
        return this.minimumId;
    }

    public PlotId maximumId() {
        return this.maximumId;
    }

    public SettingsNodesWrapper settingsNodesWrapper() {
        return this.settingsNodesWrapper;
    }

    public SetupUtils setupManager() {
        return this.setupManager;
    }

    public PlotAreaBuilder generatorName(String generatorName) {
        this.generatorName = generatorName;
        return this;
    }

    public PlotAreaBuilder plotManager(String plotManager) {
        this.plotManager = plotManager;
        return this;
    }

    public PlotAreaBuilder plotAreaType(@NotNull PlotAreaType plotAreaType) {
        Objects.requireNonNull(plotAreaType, "PlotAreaType must not be null");
        this.plotAreaType = plotAreaType;
        return this;
    }

    public PlotAreaBuilder terrainType(PlotAreaTerrainType terrainType) {
        this.terrainType = terrainType;
        return this;
    }

    public PlotAreaBuilder worldName(String worldName) {
        this.worldName = worldName;
        return this;
    }

    public PlotAreaBuilder areaName(String areaName) {
        this.areaName = areaName;
        return this;
    }

    public PlotAreaBuilder settingsNodesWrapper(SettingsNodesWrapper settingsNodesWrapper) {
        this.settingsNodesWrapper = settingsNodesWrapper;
        return this;
    }

    public PlotAreaBuilder setupManager(SetupUtils setupManager) {
        this.setupManager = setupManager;
        return this;
    }

}
