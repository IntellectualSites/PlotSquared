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
    public boolean createPlotSafe(final Plot plot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            String world = null;
            try { world = plot.getWorldName(); } catch (Throwable ignored) {}
            if (world == null) { world = plot.getArea().toString(); }
            Optional<PlotEntity> existing = findByWorldAndId(world, plot.getId().getX(), plot.getId().getY());
            if (existing.isPresent()) {
                tx.commit();
                return false;
            }
            PlotEntity pe = new PlotEntity();
            pe.setPlotIdX(plot.getId().getX());
            pe.setPlotIdZ(plot.getId().getY());
            UUID ownerUuid = null;
            try { ownerUuid = plot.getOwnerAbs(); } catch (Throwable ignored) {}
            pe.setOwner(ownerUuid != null ? ownerUuid.toString() : com.plotsquared.core.database.DBFunc.EVERYONE.toString());
            pe.setWorld(world);
            em.persist(pe);
            em.flush();
            if (pe.getId() != null) {
                plot.temp = pe.getId().intValue();
            }
            com.plotsquared.core.persistence.entity.PlotSettingsEntity se = new com.plotsquared.core.persistence.entity.PlotSettingsEntity();
            se.setPlot(pe);
            se.setPosition("DEFAULT");
            em.persist(se);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to create plot safely (plot={})", plot, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void createPlotAndSettings(final Plot plot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            String world = null;
            try { world = plot.getWorldName(); } catch (Throwable ignored) {}
            if (world == null) { world = plot.getArea().toString(); }
            PlotEntity pe = new PlotEntity();
            pe.setPlotIdX(plot.getId().getX());
            pe.setPlotIdZ(plot.getId().getY());
            UUID ownerUuid = null;
            try { ownerUuid = plot.getOwnerAbs(); } catch (Throwable ignored) {}
            pe.setOwner(ownerUuid != null ? ownerUuid.toString() : com.plotsquared.core.database.DBFunc.EVERYONE.toString());
            pe.setWorld(world);
            em.persist(pe);
            em.flush();
            if (pe.getId() != null) {
                plot.temp = pe.getId().intValue();
            }
            com.plotsquared.core.persistence.entity.PlotSettingsEntity se = new com.plotsquared.core.persistence.entity.PlotSettingsEntity();
            se.setPlot(pe);
            se.setPosition("DEFAULT");
            em.persist(se);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to create plot and settings (plot={})", plot, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void createPlotsAndData(final List<Plot> plots) {
        if (plots == null || plots.isEmpty()) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            for (final Plot plot : plots) {
                // Persist plot row
                PlotEntity pe = new PlotEntity();
                pe.setPlotIdX(plot.getId().getX());
                pe.setPlotIdZ(plot.getId().getY());
                java.util.UUID ownerUuid = null;
                try { ownerUuid = plot.getOwnerAbs(); } catch (Throwable ignored) {}
                pe.setOwner(ownerUuid != null ? ownerUuid.toString() : com.plotsquared.core.database.DBFunc.EVERYONE.toString());
                // Prefer world name for consistency with other queries
                String world = null;
                try { world = plot.getWorldName(); } catch (Throwable ignored) {}
                if (world == null) { world = plot.getArea().toString(); }
                pe.setWorld(world);
                em.persist(pe);
                em.flush(); // ensure ID is generated

                long plotId = pe.getId();

                // Persist settings (alias, merged, position) similar to legacy behavior
                try {
                    var ps = plot.getSettings();
                    if (ps != null) {
                        com.plotsquared.core.persistence.entity.PlotSettingsEntity se = new com.plotsquared.core.persistence.entity.PlotSettingsEntity();
                        se.setPlot(pe);
                        String alias = ps.getAlias();
                        if (alias != null && !alias.isEmpty()) {
                            se.setAlias(alias);
                        }
                        boolean[] merged = ps.getMerged();
                        if (merged != null) {
                            int hash = com.plotsquared.core.util.HashUtil.hash(merged);
                            se.setMerged(hash);
                        }
                        var loc = ps.getPosition();
                        String position = "DEFAULT";
                        if (loc != null) {
                            if (loc.getY() == 0) {
                                position = "DEFAULT";
                            } else {
                                position = loc.getX() + "," + loc.getY() + "," + loc.getZ();
                            }
                        }
                        se.setPosition(position);
                        em.persist(se);
                    }
                } catch (Throwable t) {
                    // log and continue
                    LOGGER.warn("Failed to persist settings for plot (x={}, z={}, world={})", plot.getId().getX(), plot.getId().getY(), world, t);
                }

                // Persist flags
                try {
                    var flagContainer = plot.getFlagContainer();
                    if (flagContainer != null && flagContainer.getFlagMap() != null) {
                        for (var flagEntry : flagContainer.getFlagMap().values()) {
                            com.plotsquared.core.persistence.entity.PlotFlagEntity fe = new com.plotsquared.core.persistence.entity.PlotFlagEntity();
                            fe.setPlot(pe);
                            fe.setFlag(flagEntry.getName());
                            fe.setValue(flagEntry.toString());
                            em.persist(fe);
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Failed to persist flags for plot (x={}, z={}, world={})", plot.getId().getX(), plot.getId().getY(), world, t);
                }

                // Persist tiers: NOTE legacy mapping members->trusted, trusted->helpers
                try {
                    // helpers table from plot.getTrusted()
                    for (java.util.UUID uuid : plot.getTrusted()) {
                        com.plotsquared.core.persistence.entity.PlotMembershipEntity e = new com.plotsquared.core.persistence.entity.PlotMembershipEntity();
                        e.setPlotId(plotId);
                        e.setUserUuid(uuid.toString());
                        em.persist(e);
                    }
                    // trusted table from plot.getMembers()
                    for (java.util.UUID uuid : plot.getMembers()) {
                        com.plotsquared.core.persistence.entity.PlotTrustedEntity e = new com.plotsquared.core.persistence.entity.PlotTrustedEntity();
                        e.setPlotId(plotId);
                        e.setUserUuid(uuid.toString());
                        em.persist(e);
                    }
                    // denied table from plot.getDenied()
                    for (java.util.UUID uuid : plot.getDenied()) {
                        com.plotsquared.core.persistence.entity.PlotDeniedEntity e = new com.plotsquared.core.persistence.entity.PlotDeniedEntity();
                        e.setPlotId(plotId);
                        e.setUserUuid(uuid.toString());
                        em.persist(e);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Failed to persist tiers for plot (x={}, z={}, world={})", plot.getId().getX(), plot.getId().getY(), world, t);
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed bulk create plots and data", e);
            throw e;
        } finally {
            em.close();
        }
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

    @Override
    public void deleteRatings(final Plot plot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            String world = null;
            try { world = plot.getWorldName(); } catch (Throwable ignored) {}
            if (world == null) { world = plot.getArea().toString(); }
            PlotEntity pe = em.createNamedQuery("Plot.findByWorldAndId", PlotEntity.class)
                    .setParameter("world", world)
                    .setParameter("x", plot.getId().getX())
                    .setParameter("z", plot.getId().getY())
                    .getResultStream().findFirst().orElse(null);
            if (pe != null && pe.getId() != null) {
                em.createQuery("DELETE FROM PlotRatingEntity r WHERE r.plotId = :plotId")
                        .setParameter("plotId", pe.getId())
                        .executeUpdate();
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete ratings for plot (plot={})", plot, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(final Plot plot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            String world = null;
            try { world = plot.getWorldName(); } catch (Throwable ignored) {}
            if (world == null) { world = plot.getArea().toString(); }
            PlotEntity pe = em.createNamedQuery("Plot.findByWorldAndId", PlotEntity.class)
                    .setParameter("world", world)
                    .setParameter("x", plot.getId().getX())
                    .setParameter("z", plot.getId().getY())
                    .getResultStream().findFirst().orElse(null);
            if (pe != null && pe.getId() != null) {
                Long plotId = pe.getId();
                // Delete children first to satisfy FK constraints
                em.createQuery("DELETE FROM PlotFlagEntity f WHERE f.plot.id = :plotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                em.createQuery("DELETE FROM PlotMembershipEntity e WHERE e.plotId = :plotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                em.createQuery("DELETE FROM PlotTrustedEntity e WHERE e.plotId = :plotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                em.createQuery("DELETE FROM PlotDeniedEntity e WHERE e.plotId = :plotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                em.createQuery("DELETE FROM PlotRatingEntity r WHERE r.plotId = :plotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                // Remove settings explicitly to mirror legacy behavior and avoid orphan rows
                em.createQuery("DELETE FROM PlotSettingsEntity s WHERE s.id = :plotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                // Remove plot
                PlotEntity managed = em.contains(pe) ? pe : em.merge(pe);
                em.remove(managed);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to delete plot (plot={})", plot, e);
            throw e;
        } finally {
            em.close();
        }
    }
}
