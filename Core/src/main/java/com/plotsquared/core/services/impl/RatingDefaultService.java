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
import com.plotsquared.core.persistence.entity.PlotRatingEntity;
import com.plotsquared.core.persistence.repository.api.PlotRatingRepository;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.services.api.RatingService;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class RatingDefaultService implements RatingService {

    private final PlotRepository plotRepository;
    private final PlotRatingRepository ratingRepository;

    @Inject
    public RatingDefaultService(final PlotRepository repository, final PlotRatingRepository ratingRepository) {
        this.plotRepository = repository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public HashMap<UUID, Integer> getRatings(final Plot plot) {
        if (plot.getWorldName() == null) {
            return new HashMap<>(0);
        }
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        HashMap<UUID, Integer> out = new HashMap<>();
        pe.ifPresent(entity -> {
            for (PlotRatingEntity e : this.ratingRepository.findByPlotId(entity.getId())) {
                out.put(UUID.fromString(e.getPlayer()), e.getRating());
            }
        });
        return out;
    }

    @Override
    public void setRating(final Plot plot, final UUID rater, final int value) {
        if (plot.getWorldName() == null) {
            return;
        }
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> this.ratingRepository.upsert(entity.getId(), rater.toString(), value));
    }

}
