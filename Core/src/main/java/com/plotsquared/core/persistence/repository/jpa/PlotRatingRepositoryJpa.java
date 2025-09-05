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
package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.PlotRatingEntity;
import com.plotsquared.core.persistence.repository.api.PlotRatingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlotRatingRepositoryJpa implements PlotRatingRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotRatingRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotRatingRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public @NotNull List<PlotRatingEntity> findByPlotId(long plotId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("PlotRating.findByPlot", PlotRatingEntity.class)
                    .setParameter("plotId", plotId)
                    .getResultList();
        }
    }

    @Override
    public void upsert(long plotId, @NotNull String playerUuid, int rating) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            int updated = em.createNamedQuery("PlotRating.updateValue")
                    .setParameter("rating", rating)
                    .setParameter("plotId", plotId)
                    .setParameter("player", playerUuid)
                    .executeUpdate();
            if (updated == 0) {
                PlotRatingEntity entity = new PlotRatingEntity();
                entity.setPlotId(plotId);
                entity.setPlayer(playerUuid);
                entity.setRating(rating);
                em.persist(entity);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to upsert plot rating (plotId={}, playerUuid={}, rating={})", plotId, playerUuid, rating, e);
        }
    }
}
