package com.plotsquared.core.persistence.repository.jpa;

import com.google.inject.Inject;
import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.plot.Plot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlotRepositoryJpa implements PlotRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotRepositoryJpa.class);

    private final EntityManagerFactory emf;

    @Inject
    public PlotRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public boolean swapPlots(final Plot plot1, final Plot plot2) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            var plot1Id = findByXAndZAnyWorld(plot1.getId().getX(), plot1.getId().getY(), plot1.getWorldName()).orElseThrow().getId();
            var plot2Id = findByXAndZAnyWorld(plot2.getId().getX(), plot2.getId().getY(), plot2.getWorldName()).orElseThrow().getId();
            em.createNamedQuery("Plot.updateXANDZ")
                            .setParameter("id", plot1Id)
                            .setParameter("x", plot1.getId().getX())
                            .setParameter("z", plot1.getId().getY());
            em.createNamedQuery("Plot.updateXANDZ")
                    .setParameter("id", plot2Id)
                    .setParameter("x", plot2.getId().getX())
                    .setParameter("z", plot2.getId().getY());
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
        }
        return false;
    }

    @Override
    public void movePlots(final Plot originPlot, final Plot newPlot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            var originPlotId = findByXAndZAnyWorld(
                    originPlot.getId().getX(),
                    originPlot.getId().getY(),
                    originPlot.getWorldName()
            ).orElseThrow().getId();
            em.createNamedQuery("Plot.movePlot")
                    .setParameter("id", originPlotId)
                    .setParameter("plotIdX", newPlot.getId().getX())
                    .setParameter("plotIdZ", newPlot.getId().getY())
                    .setParameter("world", newPlot.getWorldName());
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to move plots (plot1={}, plot2={})", originPlot, newPlot, e);
            throw e;
        }
    }

    @Override
    public void setOwner(final Plot plot, final UUID newOwner) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("Plot.setOwner")
                    .setParameter("world", plot.getWorldName())
                    .setParameter("x", plot.getId().getX())
                    .setParameter("z", plot.getId().getY())
                    .setParameter("owner", newOwner);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to set plot owner (plot={}, newOwner={})", plot, newOwner, e);
            throw e;
        }
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
    public Optional<PlotEntity> findByXAndZAnyWorld(final int x, final int z, final String world) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Plot.findByXAndZAndWorld", PlotEntity.class)
                    .setParameter("x", x)
                    .setParameter("z", z)
                    .setParameter("world", world)
                    .getResultStream().findFirst();
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
