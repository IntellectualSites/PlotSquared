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
import com.plotsquared.core.persistence.entity.ClusterSettingsEntity;
import com.plotsquared.core.persistence.repository.api.ClusterSettingsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ClusterSettingsRepositoryJpa implements ClusterSettingsRepository {

    private static final Logger LOGGER = LogManager.getLogger(ClusterSettingsRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public ClusterSettingsRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<ClusterSettingsEntity> findById(long clusterId) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(ClusterSettingsEntity.class, clusterId));
        }
    }

    @Override
    public void updatePosition(long clusterId, @NotNull String position) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("ClusterSettings.updatePosition")
                    .setParameter("clusterId", clusterId)
                    .setParameter("pos", position)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to update cluster position (clusterId={}, position={})", clusterId, position, ex);
        }
    }
}
