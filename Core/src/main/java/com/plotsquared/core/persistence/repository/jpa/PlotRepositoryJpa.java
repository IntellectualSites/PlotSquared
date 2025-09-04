/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
            try {
                world = plot.getWorldName();
            } catch (Throwable ignored) {
            }
            if (world == null) {
                world = plot.getArea().toString();
            }
            Optional<PlotEntity> existing = findByWorldAndId(world, plot.getId().getX(), plot.getId().getY());
            if (existing.isPresent()) {
                tx.commit();
                return false;
            }
            PlotEntity pe = new PlotEntity();
            pe.setPlotIdX(plot.getId().getX());
            pe.setPlotIdZ(plot.getId().getY());
            UUID ownerUuid = null;
            try {
                ownerUuid = plot.getOwnerAbs();
            } catch (Throwable ignored) {
            }
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
            if (tx.isActive()) {
                tx.rollback();
            }
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
            try {
                world = plot.getWorldName();
            } catch (Throwable ignored) {
            }
            if (world == null) {
                world = plot.getArea().toString();
            }
            PlotEntity pe = new PlotEntity();
            pe.setPlotIdX(plot.getId().getX());
            pe.setPlotIdZ(plot.getId().getY());
            UUID ownerUuid = null;
            try {
                ownerUuid = plot.getOwnerAbs();
            } catch (Throwable ignored) {
            }
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
            if (tx.isActive()) {
                tx.rollback();
            }
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
                try {
                    ownerUuid = plot.getOwnerAbs();
                } catch (Throwable ignored) {
                }
                pe.setOwner(ownerUuid != null ? ownerUuid.toString() : com.plotsquared.core.database.DBFunc.EVERYONE.toString());
                // Prefer world name for consistency with other queries
                String world = null;
                try {
                    world = plot.getWorldName();
                } catch (Throwable ignored) {
                }
                if (world == null) {
                    world = plot.getArea().toString();
                }
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
                    LOGGER.warn(
                            "Failed to persist settings for plot (x={}, z={}, world={})",
                            plot.getId().getX(),
                            plot.getId().getY(),
                            world,
                            t
                    );
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
                    LOGGER.warn(
                            "Failed to persist flags for plot (x={}, z={}, world={})",
                            plot.getId().getX(),
                            plot.getId().getY(),
                            world,
                            t
                    );
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
                    LOGGER.warn(
                            "Failed to persist tiers for plot (x={}, z={}, world={})",
                            plot.getId().getX(),
                            plot.getId().getY(),
                            world,
                            t
                    );
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
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
            var plot1Id = findByXAndZAnyWorld(plot1.getId().getX(), plot1.getId().getY(), plot1.getWorldName())
                    .orElseThrow()
                    .getId();
            var plot2Id = findByXAndZAnyWorld(plot2.getId().getX(), plot2.getId().getY(), plot2.getWorldName())
                    .orElseThrow()
                    .getId();
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
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        return false;
    }

    @Override
    public void replaceWorldInRange(String oldWorld, String newWorld, int minX, int minZ, int maxX, int maxZ) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("UPDATE PlotEntity p SET p.world = :newWorld WHERE p.world = :oldWorld AND p.plotIdX BETWEEN :minX AND :maxX AND p.plotIdZ BETWEEN :minZ AND :maxZ")
                    .setParameter("newWorld", newWorld)
                    .setParameter("oldWorld", oldWorld)
                    .setParameter("minX", minX)
                    .setParameter("maxX", maxX)
                    .setParameter("minZ", minZ)
                    .setParameter("maxZ", maxZ)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to replace world in range (oldWorld={}, newWorld={}, range=[{}..{}]x[{}..{}])", oldWorld, newWorld, minX, maxX, minZ, maxZ, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void replaceWorldAll(String oldWorld, String newWorld) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("UPDATE PlotEntity p SET p.world = :newWorld WHERE p.world = :oldWorld")
                    .setParameter("newWorld", newWorld)
                    .setParameter("oldWorld", oldWorld)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to replace world (all plots) oldWorld={}, newWorld={}", oldWorld, newWorld, e);
            throw e;
        } finally {
            em.close();
        }
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
            if (tx.isActive()) {
                tx.rollback();
            }
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
            if (tx.isActive()) {
                tx.rollback();
            }
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
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.error(
                    "Failed to save plot (id={}, world={}, x={}, z={})",
                    plot.getId(),
                    plot.getWorld(),
                    plot.getPlotIdX(),
                    plot.getPlotIdZ(),
                    e
            );
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
            if (tx.isActive()) {
                tx.rollback();
            }
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
            try {
                world = plot.getWorldName();
            } catch (Throwable ignored) {
            }
            if (world == null) {
                world = plot.getArea().toString();
            }
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
            if (tx.isActive()) {
                tx.rollback();
            }
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
            try {
                world = plot.getWorldName();
            } catch (Throwable ignored) {
            }
            if (world == null) {
                world = plot.getArea().toString();
            }
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
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.error("Failed to delete plot (plot={})", plot, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.HashMap<String, java.util.HashMap<com.plotsquared.core.plot.PlotId, com.plotsquared.core.plot.Plot>> getPlots() {
        EntityManager em = emf.createEntityManager();
        try {
            java.util.HashMap<String, java.util.HashMap<com.plotsquared.core.plot.PlotId, com.plotsquared.core.plot.Plot>> worldMap = new java.util.HashMap<>();
            java.util.Map<Long, com.plotsquared.core.plot.Plot> byId = new java.util.HashMap<>();
            java.util.Map<String, java.util.UUID> uuidCache = new java.util.HashMap<>();

            // Load plots
            List<PlotEntity> plots = em.createQuery("SELECT p FROM PlotEntity p", PlotEntity.class).getResultList();
            for (PlotEntity p : plots) {
                String ownerStr = p.getOwner();
                java.util.UUID owner;
                if (ownerStr == null) {
                    owner = com.plotsquared.core.database.DBFunc.EVERYONE;
                } else {
                    owner = uuidCache.get(ownerStr);
                    if (owner == null) {
                        try {
                            owner = java.util.UUID.fromString(ownerStr);
                        } catch (IllegalArgumentException ex) {
                            String base = com.plotsquared.core.configuration.Settings.UUID.FORCE_LOWERCASE
                                    ? ("OfflinePlayer:" + ownerStr.toLowerCase())
                                    : ("OfflinePlayer:" + ownerStr);
                            owner = java.util.UUID.nameUUIDFromBytes(base.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        }
                        uuidCache.put(ownerStr, owner);
                    }
                }
                long time = p.getTimestamp() != null ? p.getTimestamp().getTime() : System.currentTimeMillis();
                com.plotsquared.core.plot.PlotId pid = com.plotsquared.core.plot.PlotId.of(p.getPlotIdX(), p.getPlotIdZ());
                com.plotsquared.core.plot.Plot plot = new com.plotsquared.core.plot.Plot(
                        pid,
                        owner,
                        new java.util.HashSet<>(),
                        new java.util.HashSet<>(),
                        new java.util.HashSet<>(),
                        "",
                        null,
                        null,
                        null,
                        new boolean[]{false, false, false, false},
                        time,
                        p.getId() != null ? p.getId().intValue() : -1
                );
                worldMap.computeIfAbsent(p.getWorld(), k -> new java.util.HashMap<>()).put(pid, plot);
                if (p.getId() != null) {
                    byId.put(p.getId(), plot);
                }
            }

            // Ratings (optional)
            if (com.plotsquared.core.configuration.Settings.Enabled_Components.RATING_CACHE) {
                var ratings = em.createQuery(
                        "SELECT r FROM PlotRatingEntity r",
                        com.plotsquared.core.persistence.entity.PlotRatingEntity.class
                ).getResultList();
                for (var r : ratings) {
                    var plot = byId.get(r.getPlotId());
                    if (plot != null) {
                        try {
                            plot.getSettings().getRatings().put(java.util.UUID.fromString(r.getPlayer()), r.getRating());
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }

            // Helpers -> trusted
            var helpers = em.createQuery(
                    "SELECT e FROM PlotMembershipEntity e",
                    com.plotsquared.core.persistence.entity.PlotMembershipEntity.class
            ).getResultList();
            for (var e : helpers) {
                var plot = byId.get(e.getPlotId());
                if (plot != null) {
                    try {
                        plot.getTrusted().add(java.util.UUID.fromString(e.getUserUuid()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            // Trusted -> members
            var trusted = em.createQuery(
                    "SELECT e FROM PlotTrustedEntity e",
                    com.plotsquared.core.persistence.entity.PlotTrustedEntity.class
            ).getResultList();
            for (var e : trusted) {
                var plot = byId.get(e.getPlotId());
                if (plot != null) {
                    try {
                        plot.getMembers().add(java.util.UUID.fromString(e.getUserUuid()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            // Denied
            var denied = em.createQuery(
                    "SELECT e FROM PlotDeniedEntity e",
                    com.plotsquared.core.persistence.entity.PlotDeniedEntity.class
            ).getResultList();
            for (var e : denied) {
                var plot = byId.get(e.getPlotId());
                if (plot != null) {
                    try {
                        plot.getDenied().add(java.util.UUID.fromString(e.getUserUuid()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            // Flags
            try {
                com.plotsquared.core.plot.flag.types.BlockTypeListFlag.skipCategoryVerification = true;
            } catch (Throwable ignored) {
            }
            var flags = em.createQuery(
                    "SELECT f FROM PlotFlagEntity f",
                    com.plotsquared.core.persistence.entity.PlotFlagEntity.class
            ).getResultList();
            for (var f : flags) {
                var plot = byId.get(f.getPlot().getId());
                if (plot != null) {
                    String flag = f.getFlag();
                    String value = f.getValue();
                    var registry = com.plotsquared.core.plot.flag.GlobalFlagContainer.getInstance();
                    var plotFlag = registry.getFlagFromString(flag);
                    if (plotFlag == null) {
                        plot.getFlagContainer().addUnknownFlag(flag, value);
                    } else {
                        try {
                            plot.getFlagContainer().addFlag(plotFlag.parse(value));
                        } catch (Exception ex) {
                            // ignore invalid
                        }
                    }
                }
            }
            try {
                com.plotsquared.core.plot.flag.types.BlockTypeListFlag.skipCategoryVerification = false;
            } catch (Throwable ignored) {
            }

            // Settings
            var settings = em.createQuery(
                    "SELECT s FROM PlotSettingsEntity s",
                    com.plotsquared.core.persistence.entity.PlotSettingsEntity.class
            ).getResultList();
            for (var s : settings) {
                var plot = byId.get(s.getId());
                if (plot != null) {
                    if (s.getAlias() != null) {
                        plot.getSettings().setAlias(s.getAlias());
                    }
                    String pos = s.getPosition();
                    if (pos != null) {
                        String lower = pos.toLowerCase();
                        if (!lower.isEmpty() && !"default".equals(lower) && !"0,0,0".equals(lower) && !"center".equals(lower) && !"centre".equals(
                                lower)) {
                            try {
                                plot.getSettings().setPosition(com.plotsquared.core.location.BlockLoc.fromString(pos));
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                    Integer m = s.getMerged();
                    if (m != null) {
                        boolean[] merged = new boolean[4];
                        for (int i = 0; i < 4; i++) {
                            merged[3 - i] = ((m & (1 << i)) != 0);
                        }
                        plot.getSettings().setMerged(merged);
                    }
                }
            }

            return worldMap;
        } finally {
            em.close();
        }
    }

    @Override
    public void purgeIds(java.util.Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Convert Integer to Long (plot ids are Long)
            java.util.Set<Long> longIds = new java.util.HashSet<>();
            for (Integer i : ids) {
                if (i != null) longIds.add(i.longValue());
            }
            if (longIds.isEmpty()) {
                tx.commit();
                return;
            }
            // Delete child tables first
            em.createQuery("DELETE FROM PlotFlagEntity f WHERE f.plot.id IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotMembershipEntity e WHERE e.plotId IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotTrustedEntity e WHERE e.plotId IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotDeniedEntity e WHERE e.plotId IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotRatingEntity r WHERE r.plotId IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotSettingsEntity s WHERE s.id IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            // Finally delete plots
            em.createQuery("DELETE FROM PlotEntity p WHERE p.id IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to purge plots by ids (ids={})", ids, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void purgeByWorldAndPlotIds(String world, java.util.Set<com.plotsquared.core.plot.PlotId> plotIds) {
        if (world == null || world.isEmpty() || plotIds == null || plotIds.isEmpty()) return;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Resolve plot ids by fetching candidates in the world and filtering in-memory
            List<Long> ids = new java.util.ArrayList<>();
            List<PlotEntity> candidates = em.createNamedQuery("Plot.findByWorld", PlotEntity.class)
                    .setParameter("world", world)
                    .getResultList();
            java.util.HashSet<com.plotsquared.core.plot.PlotId> lookup = new java.util.HashSet<>(plotIds);
            for (PlotEntity p : candidates) {
                if (lookup.contains(com.plotsquared.core.plot.PlotId.of(p.getPlotIdX(), p.getPlotIdZ())) && p.getId() != null) {
                    ids.add(p.getId());
                }
            }
            if (ids.isEmpty()) {
                tx.commit();
                return;
            }
            java.util.Set<Long> longIds = new java.util.HashSet<>(ids);
            // Delete child tables first
            em.createQuery("DELETE FROM PlotFlagEntity f WHERE f.plot.id IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotMembershipEntity e WHERE e.plotId IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotTrustedEntity e WHERE e.plotId IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotDeniedEntity e WHERE e.plotId IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotRatingEntity r WHERE r.plotId IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            em.createQuery("DELETE FROM PlotSettingsEntity s WHERE s.id IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            // Finally delete plots
            em.createQuery("DELETE FROM PlotEntity p WHERE p.id IN :ids")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to purge plots by world/id (world={}, plotIds={})", world, plotIds, e);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void replaceWorld(String oldWorld, String newWorld) {
        replaceWorldAll(oldWorld, newWorld);
    }

    @Override
    public void replaceWorldInBounds(String oldWorld, String newWorld, com.plotsquared.core.plot.PlotId min, com.plotsquared.core.plot.PlotId max) {
        if (min == null || max == null) return;
        replaceWorldInRange(oldWorld, newWorld, min.getX(), min.getY(), max.getX(), max.getY());
    }
}
