package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.PlotRatingEntity;
import com.plotsquared.core.persistence.repository.api.PlotRatingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PlotRatingRepositoryJpa implements PlotRatingRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotRatingRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotRatingRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<PlotRatingEntity> findByPlotId(long plotId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PlotRating.findByPlot", PlotRatingEntity.class)
                    .setParameter("plotId", plotId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void upsert(long plotId, String playerUuid, int rating) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int updated = em.createNamedQuery("PlotRating.updateValue")
                    .setParameter("rating", rating)
                    .setParameter("plotId", plotId)
                    .setParameter("player", playerUuid)
                    .executeUpdate();
            if (updated == 0) {
                PlotRatingEntity entity = new PlotRatingEntity();
                entity.setPlotId(plotId);
                entity.setPlayer(playerUuid);
                entity.setRating(rating);
                em.persist(entity);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to upsert plot rating (plotId={}, playerUuid={}, rating={})", plotId, playerUuid, rating, e);
            throw e;
        } finally {
            em.close();
        }
    }
}
