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
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("ClusterHelper.findUsers", String.class)
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
            ClusterHelperEntity e = new ClusterHelperEntity();
            e.setClusterId(clusterId);
            e.setUserUuid(userUuid);
            em.persist(e);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to add cluster helper (clusterId={}, userUuid={})", clusterId, userUuid, ex);
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
            em.createNamedQuery("ClusterHelper.delete")
                    .setParameter("clusterId", clusterId)
                    .setParameter("uuid", userUuid)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to remove cluster helper (clusterId={}, userUuid={})", clusterId, userUuid, ex);
            throw ex;
        } finally {
            em.close();
        }
    }
}
