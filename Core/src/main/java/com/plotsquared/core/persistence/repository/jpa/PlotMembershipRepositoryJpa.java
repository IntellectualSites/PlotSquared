package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.PlotMembershipEntity;
import com.plotsquared.core.persistence.repository.api.PlotMembershipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PlotMembershipRepositoryJpa implements PlotMembershipRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotMembershipRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotMembershipRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<String> findUsers(long plotId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PlotHelper.findUsers", String.class)
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
            PlotMembershipEntity e = new PlotMembershipEntity();
            e.setPlotId(plotId);
            e.setUserUuid(userUuid);
            em.persist(e);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to add plot member (plotId={}, userUuid={})", plotId, userUuid, ex);
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public void remove(long plotId, String userUuid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNamedQuery("PlotHelper.delete")
                    .setParameter("plotId", plotId)
                    .setParameter("uuid", userUuid)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to remove plot member (plotId={}, userUuid={})", plotId, userUuid, ex);
            throw ex;
        } finally { em.close(); }
    }
}
