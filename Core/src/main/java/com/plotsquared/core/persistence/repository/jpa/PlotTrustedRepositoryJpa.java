package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.PlotTrustedEntity;
import com.plotsquared.core.persistence.repository.api.PlotTrustedRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PlotTrustedRepositoryJpa implements PlotTrustedRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotTrustedRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotTrustedRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<String> findUsers(long plotId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PlotTrusted.findUsers", String.class)
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
            PlotTrustedEntity e = new PlotTrustedEntity();
            e.setPlotId(plotId);
            e.setUserUuid(userUuid);
            em.persist(e);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to add plot trusted (plotId={}, userUuid={})", plotId, userUuid, ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void remove(long plotId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("PlotTrusted.delete")
                    .setParameter("plotId", plotId)
                    .setParameter("uuid", userUuid)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to remove plot trusted (plotId={}, userUuid={})", plotId, userUuid, ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void deleteByPlotId(long plotId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM PlotTrustedEntity e WHERE e.plotId = :plotId")
                    .setParameter("plotId", plotId)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete all plot trusted users (plotId={})", plotId, ex);
            throw ex;
        } finally { em.close(); }
    }
}
