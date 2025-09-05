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
import com.plotsquared.core.persistence.entity.PlayerMetaEntity;
import com.plotsquared.core.persistence.repository.api.PlayerMetaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PlayerMetaRepositoryJpa implements PlayerMetaRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlayerMetaRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlayerMetaRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<PlayerMetaEntity> findByUuid(String uuid) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("PlayerMeta.findByUuid", PlayerMetaEntity.class)
                    .setParameter("uuid", uuid)
                    .getResultList();
        }
    }

    @Override
    public void put(String uuid, String key, byte[] value) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            // Delete existing row for same (uuid,key) then insert new
            em.createNamedQuery("PlayerMeta.deleteByUuidAndKey")
                    .setParameter("uuid", uuid)
                    .setParameter("key", key)
                    .executeUpdate();
            PlayerMetaEntity e = new PlayerMetaEntity();
            e.setUuid(uuid);
            e.setKey(key);
            e.setValue(value);
            em.persist(e);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error(
                    "Failed to put player meta (uuid={}, key={}, value.length={})",
                    uuid,
                    key,
                    value != null ? value.length : null,
                    ex
            );
        }
    }

    @Override
    public void delete(String uuid, String key) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("PlayerMeta.deleteByUuidAndKey")
                    .setParameter("uuid", uuid)
                    .setParameter("key", key)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete player meta (uuid={}, key={})", uuid, key, ex);
        }
    }
}
