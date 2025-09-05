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
import com.plotsquared.core.plot.PlotId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull Optional<ClusterEntity> findById(long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(ClusterEntity.class, id));
        }
    }

    @Override
    public @NotNull Optional<ClusterEntity> findByWorldAndBounds(@NotNull String world, int x, int z) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("Cluster.findByWorldAndBounds", ClusterEntity.class)
                    .setParameter("world", world)
                    .setParameter("x", x)
                    .setParameter("z", z)
                    .getResultStream().findFirst();
        }
    }

    @Override
    public @NotNull List<ClusterEntity> findByWorld(@NotNull String world) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("Cluster.findByWorld", ClusterEntity.class)
                    .setParameter("world", world)
                    .getResultList();
        }
    }

    @Override
    public @NotNull List<ClusterEntity> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("Cluster.finaAll", ClusterEntity.class)
                    .getResultList();
        }
    }

    @Override
    public void save(@NotNull ClusterEntity cluster) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
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
        }
    }

    @Override
    public void deleteById(long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            ClusterEntity e = em.find(ClusterEntity.class, id);
            if (e != null) {
                em.remove(e);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete cluster by id (id={})", id, ex);
        }
    }

    @Override
    public void updateWorldAll(@NotNull String oldWorld, @NotNull String newWorld) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("Cluster.updateWorld")
                    .setParameter("newWorld", newWorld)
                    .setParameter("oldWorld", oldWorld)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to update cluster world (all) oldWorld={}, newWorld={}", oldWorld, newWorld, ex);
        }
    }

    @Override
    public void updateWorldInBounds(@NotNull String oldWorld, @NotNull String newWorld, int minX, int minZ, int maxX, int maxZ) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("Cluster.updateWorldInBounds")
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
            LOGGER.error(
                    "Failed to update cluster world in bounds oldWorld={}, newWorld={}, bounds=[{}..{}]x[{}..{}]",
                    oldWorld,
                    newWorld,
                    minX,
                    maxX,
                    minZ,
                    maxZ,
                    ex
            );
        }
    }

    @Override
    public void replaceWorld(String oldWorld, String newWorld) {
        updateWorldAll(oldWorld, newWorld);
    }

    @Override
    public void replaceWorldInBounds(String oldWorld, String newWorld, PlotId min, PlotId max) {
        updateWorldInBounds(oldWorld, newWorld, min.getX(), min.getY(), max.getX(), max.getY());
    }
}
