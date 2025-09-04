package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.ClusterSettingsEntity;
import com.plotsquared.core.persistence.repository.api.ClusterSettingsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(ClusterSettingsEntity.class, clusterId));
        } finally {
            em.close();
        }
    }

    @Override
    public void updatePosition(long clusterId, String position) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("ClusterSettings.updatePosition")
                    .setParameter("clusterId", clusterId)
                    .setParameter("pos", position)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to update cluster position (clusterId={}, position={})", clusterId, position, ex);
            throw ex;
        } finally {
            em.close();
        }
    }
}
