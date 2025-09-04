package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class PlotRepositoryJpa implements PlotRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<PlotEntity> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(PlotEntity.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<PlotEntity> findByWorldAndId(String world, int x, int z) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Plot.findByWorldAndId", PlotEntity.class)
                    .setParameter("world", world)
                    .setParameter("x", x)
                    .setParameter("z", z)
                    .getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    @Override
    public List<PlotEntity> findByOwner(String ownerUuid) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Plot.findByOwner", PlotEntity.class)
                    .setParameter("owner", ownerUuid)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<PlotEntity> findByWorld(String world) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Plot.findByWorld", PlotEntity.class)
                    .setParameter("world", world)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void save(PlotEntity plot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (plot.getId() == null) {
                em.persist(plot);
            } else {
                em.merge(plot);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to save plot (id={}, world={}, x={}, z={})", plot.getId(), plot.getWorld(), plot.getPlotIdX(), plot.getPlotIdZ(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PlotEntity ref = em.find(PlotEntity.class, id);
            if (ref != null) {
                em.remove(ref);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete plot by id (id={})", id, e);
            throw e;
        } finally {
            em.close();
        }
    }
}
