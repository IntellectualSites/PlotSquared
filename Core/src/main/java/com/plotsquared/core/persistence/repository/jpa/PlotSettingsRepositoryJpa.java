package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.PlotSettingsEntity;
import com.plotsquared.core.persistence.repository.api.PlotSettingsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class PlotSettingsRepositoryJpa implements PlotSettingsRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotSettingsRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotSettingsRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<PlotSettingsEntity> findByPlotId(long plotId) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(PlotSettingsEntity.class, plotId));
        } finally {
            em.close();
        }
    }

    @Override
    public void save(PlotSettingsEntity settings) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (settings.getId() == null) {
                em.persist(settings);
            } else {
                em.merge(settings);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to save plot settings (plotId={})", settings.getId(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteByPlotId(long plotId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PlotSettingsEntity e = em.find(PlotSettingsEntity.class, plotId);
            if (e != null) {
                em.remove(e);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete plot settings (plotId={})", plotId, ex);
            throw ex;
        } finally {
            em.close();
        }
    }
}
