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
import com.plotsquared.core.persistence.entity.PlotSettingsEntity;
import com.plotsquared.core.persistence.repository.api.PlotSettingsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlotSettingsRepositoryJpa implements PlotSettingsRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotSettingsRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotSettingsRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public @NotNull Optional<PlotSettingsEntity> findByPlotId(long plotId) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(PlotSettingsEntity.class, plotId));
        }
    }

    @Override
    public void save(@NotNull PlotSettingsEntity settings) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            if (settings.getId() == null) {
                em.persist(settings);
            } else {
                em.merge(settings);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to save plot settings (plotId={})", settings.getId(), e);
        }
    }

    @Override
    public void deleteByPlotId(long plotId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            PlotSettingsEntity e = em.find(PlotSettingsEntity.class, plotId);
            if (e != null) {
                em.remove(e);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete plot settings (plotId={})", plotId, ex);
        }
    }

    @Override
    public void updateAlias(long plotId, @NotNull String alias) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("PlotSettings.updateAlias")
                    .setParameter("plotId", plotId)
                    .setParameter("alias", alias)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to update alias (plotId={})", plotId, ex);
        }
    }

    @Override
    public void updatePosition(long plotId, @NotNull String position) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("PlotSettings.updatePosition")
                    .setParameter("plotId", plotId)
                    .setParameter("pos", position)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to update position (plotId={})", plotId, ex);
        }
    }

    @Override
    public void updateMerged(long plotId, int mergedBitmask) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("PlotSettings.updateMerged")
                    .setParameter("plotId", plotId)
                    .setParameter("merged", mergedBitmask)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to update merged (plotId={})", plotId, ex);
        }
    }

    @Override
    public void createDefaultIfAbsent(long plotId, @NotNull String defaultPosition) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            PlotSettingsEntity existing = em.find(PlotSettingsEntity.class, plotId);
            if (existing == null) {
                PlotSettingsEntity se = new PlotSettingsEntity();
                se.setId(plotId);
                // attach plot reference to satisfy FK if needed
                com.plotsquared.core.persistence.entity.PlotEntity pe = em.getReference(
                        com.plotsquared.core.persistence.entity.PlotEntity.class,
                        plotId
                );
                se.setPlot(pe);
                se.setPosition(defaultPosition);
                em.persist(se);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to create default settings if absent (plotId={})", plotId, ex);
        }
    }
}
