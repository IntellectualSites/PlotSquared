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
import com.plotsquared.core.persistence.entity.ClusterInvitedEntity;
import com.plotsquared.core.persistence.repository.api.ClusterInvitedRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ClusterInvitedRepositoryJpa implements ClusterInvitedRepository {

    private static final Logger LOGGER = LogManager.getLogger(ClusterInvitedRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public ClusterInvitedRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<String> findUsers(long clusterId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("ClusterInvited.findUsers", String.class)
                    .setParameter("clusterId", clusterId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void add(long clusterId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ClusterInvitedEntity e = new ClusterInvitedEntity();
            e.setClusterId(clusterId);
            e.setUserUuid(userUuid);
            em.persist(e);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to add cluster invited (clusterId={}, userUuid={})", clusterId, userUuid, ex);
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public void remove(long clusterId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("ClusterInvited.delete")
                    .setParameter("clusterId", clusterId)
                    .setParameter("uuid", userUuid)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to remove cluster invited (clusterId={}, userUuid={})", clusterId, userUuid, ex);
            throw ex;
        } finally {
            em.close();
        }
    }
}
