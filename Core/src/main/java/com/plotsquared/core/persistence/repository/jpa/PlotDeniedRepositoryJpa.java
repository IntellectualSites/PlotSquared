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
import com.plotsquared.core.persistence.entity.PlotDeniedEntity;
import com.plotsquared.core.persistence.repository.api.PlotDeniedRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PlotDeniedRepositoryJpa implements PlotDeniedRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotDeniedRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotDeniedRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<String> findUsers(long plotId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PlotDenied.findUsers", String.class)
                    .setParameter("plotId", plotId)
                    .getResultList();
        } finally { em.close(); }
    }

    @Override
    public void add(long plotId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PlotDeniedEntity e = new PlotDeniedEntity();
            e.setPlotId(plotId);
            e.setUserUuid(userUuid);
            em.persist(e);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to add plot denied (plotId={}, userUuid={})", plotId, userUuid, ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void remove(long plotId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("PlotDenied.delete")
                    .setParameter("plotId", plotId)
                    .setParameter("uuid", userUuid)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to remove plot denied (plotId={}, userUuid={})", plotId, userUuid, ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void deleteByPlotId(long plotId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM PlotDeniedEntity e WHERE e.plotId = :plotId")
                    .setParameter("plotId", plotId)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete all plot denied users (plotId={})", plotId, ex);
            throw ex;
        } finally { em.close(); }
    }
}
