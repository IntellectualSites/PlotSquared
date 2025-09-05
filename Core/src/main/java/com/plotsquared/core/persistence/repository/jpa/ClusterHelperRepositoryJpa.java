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
import com.plotsquared.core.persistence.entity.ClusterHelperEntity;
import com.plotsquared.core.persistence.repository.api.ClusterHelperRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ClusterHelperRepositoryJpa implements ClusterHelperRepository {

    private static final Logger LOGGER = LogManager.getLogger(ClusterHelperRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public ClusterHelperRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<String> findUsers(long clusterId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("ClusterHelper.findUsers", String.class)
                    .setParameter("clusterId", clusterId)
                    .getResultList();
        }
    }

    @Override
    public void add(long clusterId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            ClusterHelperEntity e = new ClusterHelperEntity();
            e.setClusterId(clusterId);
            e.setUserUuid(userUuid);
            em.persist(e);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to add cluster helper (clusterId={}, userUuid={})", clusterId, userUuid, ex);
        }
    }

    @Override
    public void remove(long clusterId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("ClusterHelper.delete")
                    .setParameter("clusterId", clusterId)
                    .setParameter("uuid", userUuid)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to remove cluster helper (clusterId={}, userUuid={})", clusterId, userUuid, ex);
        }
    }
}
