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
import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.entity.PlotFlagEntity;
import com.plotsquared.core.persistence.repository.api.PlotFlagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PlotFlagRepositoryJpa implements PlotFlagRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotFlagRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotFlagRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public @NotNull List<PlotFlagEntity> findByPlotId(long plotId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("PlotFlag.findByPlot", PlotFlagEntity.class)
                    .setParameter("plotId", plotId)
                    .getResultList();
        }
    }

    @Override
    public @NotNull Optional<PlotFlagEntity> findByPlotAndName(long plotId, @NotNull String flagName) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.createNamedQuery("PlotFlag.findByPlotAndName", PlotFlagEntity.class)
                    .setParameter("plotId", plotId)
                    .setParameter("flag", flagName)
                    .getSingleResultOrNull());
        }
    }

    @Override
    public void save(@NotNull PlotFlagEntity entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            if (entity.getId() == null) {
                // ensure Plot reference is managed if set by id only
                PlotEntity plot = entity.getPlot();
                if (plot != null && plot.getId() != null && !em.contains(plot)) {
                    plot = em.getReference(PlotEntity.class, plot.getId());
                    entity.setPlot(plot);
                }
                em.persist(entity);
            } else {
                em.merge(entity);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error(
                    "Failed to save plot flag (plotId={}, flag={})",
                    entity.getPlot() != null ? entity.getPlot().getId() : null,
                    entity.getFlag(),
                    e
            );
        }
    }

    @Override
    public void deleteByPlotAndName(long plotId, @NotNull String flagName) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            var plot = em.createNamedQuery("PlotFlag.findByPlotAndName", PlotFlagEntity.class)
                    .setParameter("plotId", plotId)
                    .setParameter("flag", flagName)
                    .getSingleResultOrNull();
            if (plot != null) {
                em.remove(plot);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete plot flag (plotId={}, flag={})", plotId, flagName, e);
        }
    }
}
