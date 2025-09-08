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
import com.plotsquared.core.persistence.entity.PlotTrustedEntity;
import com.plotsquared.core.persistence.repository.api.PlotTrustedRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlotTrustedRepositoryJpa implements PlotTrustedRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotTrustedRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotTrustedRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<String> findUsers(long plotId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("PlotTrusted.findUsers", String.class)
                    .setParameter("plotId", plotId)
                    .getResultList();
        }
    }

    @Override
    public void add(long plotId, @NotNull String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            PlotTrustedEntity e = new PlotTrustedEntity();
            e.setPlotId(plotId);
            e.setUserUuid(userUuid);
            em.persist(e);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to add plot trusted (plotId={}, userUuid={})", plotId, userUuid, ex);
        }
    }

    @Override
    public void remove(long plotId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("PlotTrusted.deleteByPlotIdAndUserUUID")
                    .setParameter("plotId", plotId)
                    .setParameter("uuid", userUuid)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to remove plot trusted (plotId={}, userUuid={})", plotId, userUuid, ex);
        }
    }

    @Override
    public void deleteByPlotId(long plotId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("PlotTrusted.deleteByPlotId")
                    .setParameter("plotId", plotId)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete all plot trusted users (plotId={})", plotId, ex);
        }
    }
}
