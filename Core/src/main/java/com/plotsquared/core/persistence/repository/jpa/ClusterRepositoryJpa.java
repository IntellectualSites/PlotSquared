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
import com.plotsquared.core.persistence.entity.ClusterEntity;
import com.plotsquared.core.persistence.repository.api.ClusterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class ClusterRepositoryJpa implements ClusterRepository {

    private static final Logger LOGGER = LogManager.getLogger(ClusterRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public ClusterRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<ClusterEntity> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(ClusterEntity.class, id));
        } finally { em.close(); }
    }

    @Override
    public Optional<ClusterEntity> findByWorldAndBounds(String world, int x, int z) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Cluster.findByWorldAndBounds", ClusterEntity.class)
                    .setParameter("world", world)
                    .setParameter("x", x)
                    .setParameter("z", z)
                    .getResultStream().findFirst();
        } finally { em.close(); }
    }

    @Override
    public List<ClusterEntity> findByWorld(String world) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM ClusterEntity c WHERE c.world = :world", ClusterEntity.class)
                    .setParameter("world", world)
                    .getResultList();
        } finally { em.close(); }
    }

    @Override
    public List<ClusterEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM ClusterEntity c", ClusterEntity.class)
                    .getResultList();
        } finally { em.close(); }
    }

    @Override
    public void save(ClusterEntity cluster) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (cluster.getId() == null) {
                em.persist(cluster);
            } else {
                em.merge(cluster);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to save cluster (id={}, world={})", cluster.getId(), cluster.getWorld(), ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void deleteById(long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ClusterEntity e = em.find(ClusterEntity.class, id);
            if (e != null) {
                em.remove(e);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete cluster by id (id={})", id, ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void updateWorldAll(String oldWorld, String newWorld) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("UPDATE ClusterEntity c SET c.world = :newWorld WHERE c.world = :oldWorld")
                    .setParameter("newWorld", newWorld)
                    .setParameter("oldWorld", oldWorld)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to update cluster world (all) oldWorld={}, newWorld={}", oldWorld, newWorld, ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void updateWorldInBounds(String oldWorld, String newWorld, int minX, int minZ, int maxX, int maxZ) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("UPDATE ClusterEntity c SET c.world = :newWorld WHERE c.world = :oldWorld AND c.pos1X <= :maxX AND c.pos1Z <= :maxZ AND c.pos2X >= :minX AND c.pos2Z >= :minZ")
                    .setParameter("newWorld", newWorld)
                    .setParameter("oldWorld", oldWorld)
                    .setParameter("minX", minX)
                    .setParameter("minZ", minZ)
                    .setParameter("maxX", maxX)
                    .setParameter("maxZ", maxZ)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to update cluster world in bounds oldWorld={}, newWorld={}, bounds=[{}..{}]x[{}..{}]", oldWorld, newWorld, minX, maxX, minZ, maxZ, ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void replaceWorld(String oldWorld, String newWorld) {
        updateWorldAll(oldWorld, newWorld);
    }

    @Override
    public void replaceWorldInBounds(String oldWorld, String newWorld, com.plotsquared.core.plot.PlotId min, com.plotsquared.core.plot.PlotId max) {
        if (min == null || max == null) return;
        updateWorldInBounds(oldWorld, newWorld, min.getX(), min.getY(), max.getX(), max.getY());
    }
}
