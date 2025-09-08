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
package com.plotsquared.core.services.impl;

import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.persistence.repository.api.PlotSettingsRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.services.api.PlotService;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlotDefaultService implements PlotService {

    private final PlotRepository repository;
    private final PlotSettingsRepository settingsRepository;

    @Inject
    public PlotDefaultService(final PlotRepository repository, final PlotSettingsRepository settingsRepository) {
        this.repository = repository;
        this.settingsRepository = settingsRepository;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> swapPlots(final @NotNull Plot plot1, final @NotNull Plot plot2) {
        return CompletableFuture.completedFuture(this.repository.swapPlots(plot1, plot2));
    }

    @Override
    public void movePlot(final @NotNull Plot originalPlot, final @NotNull Plot newPlot) {
        this.repository.movePlots(originalPlot, newPlot);
    }

    @Override
    public void setOwner(final @NotNull Plot plot, final @NotNull UUID uuid) {
        this.repository.setOwner(plot, uuid);
    }

    @Override
    public void createPlotsAndData(final @NotNull List<Plot> plots, final Runnable whenDone) {
        this.repository.createPlotsAndData(plots);
        whenDone.run();
    }

    @Override
    public void createPlotSafe(final @NotNull Plot plot, final @NotNull Runnable success, final Runnable failure) {
        boolean created = this.repository.createPlotSafe(plot);
        if (created) {
            success.run();
        } else {
            failure.run();
        }
    }

    @Override
    public void createPlotAndSettings(final @NotNull Plot plot, final Runnable whenDone) {
        this.repository.createPlotAndSettings(plot);
        whenDone.run();
    }

    @Override
    public void delete(final @NotNull Plot plot) {
        this.repository.delete(plot);
    }

    @Override
    public void deleteRatings(final Plot plot) {
        this.repository.deleteRatings(plot);
    }

    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        return this.repository.getPlots();
    }

    @Override
    public void setMerged(final Plot plot, final boolean[] merged) {
        String world = plot.getWorldName();
        int x = plot.getId().getX();
        int z = plot.getId().getY();
        Optional<PlotEntity> pe = this.repository.findByWorldAndId(world, x, z);
        pe.ifPresent(entity -> {
            int mask = com.plotsquared.core.util.HashUtil.hash(merged);
            this.settingsRepository.updateMerged(entity.getId(), mask);
        });
    }

    @Override
    public void setAlias(final Plot plot, final String alias) {
        Optional<PlotEntity> pe = this.repository.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> this.settingsRepository.updateAlias(entity.getId(), alias));
    }

    @Override
    public void setPosition(final Plot plot, final String position) {
        Optional<PlotEntity> pe = this.repository.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> this.settingsRepository.updatePosition(entity.getId(), position));
    }

    @Override
    public void purgeIds(final Set<Integer> uniqueIds) {
        this.repository.purgeIds(uniqueIds);
    }

    @Override
    public void purge(final PlotArea area, final Set<PlotId> plotIds) {
        this.repository.purgeByWorldAndPlotIds(area.getWorldName(), plotIds);
    }

}
