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
import com.plotsquared.core.persistence.entity.PlotFlagEntity;
import com.plotsquared.core.persistence.repository.api.PlotFlagRepository;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.services.api.FlagService;
import jakarta.inject.Inject;

import java.util.Optional;

public class FlagDefaultService implements FlagService {

    private final PlotRepository plotRepository;
    private final PlotFlagRepository flagRepository;

    @Inject
    public FlagDefaultService(final PlotRepository repository, final PlotFlagRepository flagRepository) {
        this.plotRepository = repository;
        this.flagRepository = flagRepository;
    }

    @Override
    public void setFlag(final Plot plot, final PlotFlag<?, ?> flag) {
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> {
            long plotId = entity.getId();
            String name = flag.getName();
            String value = flag.toString();
            var existing = this.flagRepository.findByPlotAndName(plotId, name);
            if (existing.isPresent()) {
                var e = existing.get();
                e.setFlagValue(value);
                this.flagRepository.save(e);
            } else {
                PlotFlagEntity e = new PlotFlagEntity();
                PlotEntity pref = new PlotEntity();
                pref.setId(entity.getId());
                e.setPlot(pref);
                e.setFlag(name);
                e.setFlagValue(value);
                this.flagRepository.save(e);
            }
        });
    }

    @Override
    public void removeFlag(final Plot plot, final PlotFlag<?, ?> flag) {
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(plot.getWorldName(), plot.getId().getX(),
                plot.getId().getY());
        pe.ifPresent(entity -> this.flagRepository.deleteByPlotAndName(entity.getId(), flag.getName()));
    }

}
