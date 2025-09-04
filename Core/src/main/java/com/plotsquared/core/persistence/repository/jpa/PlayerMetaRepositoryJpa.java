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
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PlayerMeta.findByUuid", PlayerMetaEntity.class)
                    .setParameter("uuid", uuid)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void put(String uuid, String key, byte[] value) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
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
            LOGGER.error("Failed to put player meta (uuid={}, key={}, value.length={})", uuid, key, value != null ? value.length : null, ex);
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(String uuid, String key) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("PlayerMeta.deleteByUuidAndKey")
                    .setParameter("uuid", uuid)
                    .setParameter("key", key)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete player meta (uuid={}, key={})", uuid, key, ex);
            throw ex;
        } finally {
            em.close();
        }
    }
}
