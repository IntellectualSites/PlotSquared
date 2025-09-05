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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.persistence.entity.PlotDeniedEntity;
import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.entity.PlotFlagEntity;
import com.plotsquared.core.persistence.entity.PlotMembershipEntity;
import com.plotsquared.core.persistence.entity.PlotRatingEntity;
import com.plotsquared.core.persistence.entity.PlotSettingsEntity;
import com.plotsquared.core.persistence.entity.PlotTrustedEntity;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.types.BlockTypeListFlag;
import com.plotsquared.core.util.HashUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlotRepositoryJpa implements PlotRepository {

    private static final Logger LOGGER = LogManager.getLogger(PlotRepositoryJpa.class);
    private static final UUID EVERYONE = UUID.fromString("1-1-3-3-7");

    private final EntityManagerFactory emf;

    @Inject
    public PlotRepositoryJpa(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public boolean createPlotSafe(final Plot plot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            String world = Optional.of(plot).map(Plot::getWorldName).orElse(Optional.of(plot).map(Plot::getArea).map(Object::toString).orElse(null));
            Optional<PlotEntity> existing = findByWorldAndId(world, plot.getId().getX(), plot.getId().getY());
            if (existing.isPresent()) {
                tx.commit();
                return false;
            }
            PlotEntity pe = new PlotEntity();
            pe.setPlotIdX(plot.getId().getX());
            pe.setPlotIdZ(plot.getId().getY());
            UUID ownerUuid = plot.getOwnerAbs();
            pe.setOwner(Optional.ofNullable(ownerUuid).map(UUID::toString).orElse(EVERYONE.toString()));
            pe.setWorld(world);
            em.persist(pe);
            em.flush();
            PlotSettingsEntity se = new PlotSettingsEntity();
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
        }
        return false;
    }

    @Override
    public void createPlotAndSettings(final Plot plot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            String world = Optional.of(plot).map(Plot::getWorldName).orElse(Optional.of(plot).map(Plot::getArea).map(Object::toString).orElse(null));
            PlotEntity pe = new PlotEntity();
            pe.setPlotIdX(plot.getId().getX());
            pe.setPlotIdZ(plot.getId().getY());
            UUID ownerUuid = plot.getOwnerAbs();
            pe.setOwner(Optional.ofNullable(ownerUuid).map(UUID::toString).orElse(EVERYONE.toString()));
            pe.setWorld(world);
            em.persist(pe);
            em.flush();
            PlotSettingsEntity se = new PlotSettingsEntity();
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
        }
    }

    @Override
    public void createPlotsAndData(final List<Plot> plots) {
        if (plots.isEmpty()) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            for (final Plot plot : plots) {
                // Persist plot row
                PlotEntity pe = new PlotEntity();
                pe.setPlotIdX(plot.getId().getX());
                pe.setPlotIdZ(plot.getId().getY());
                UUID ownerUuid = plot.getOwnerAbs();
                pe.setOwner(Optional.ofNullable(ownerUuid).map(UUID::toString).orElse(EVERYONE.toString()));
                // Prefer world name for consistency with other queries
                String world = Optional.of(plot).map(Plot::getWorldName).orElse(Optional.of(plot).map(Plot::getArea).map(Object::toString).orElse(null));
                pe.setWorld(world);
                em.persist(pe);
                em.flush(); // ensure ID is generated

                long plotId = pe.getId();

                // Persist settings (alias, merged, position) similar to legacy behavior
                try {
                    var ps = plot.getSettings();
                    PlotSettingsEntity se = new PlotSettingsEntity();
                    se.setPlot(pe);
                    String alias = ps.getAlias();
                    if (alias != null && !alias.isEmpty()) {
                        se.setAlias(alias);
                    }
                    boolean[] merged = ps.getMerged();
                    if (merged != null) {
                        int hash = HashUtil.hash(merged);
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
                    if (flagContainer.getFlagMap() != null) {
                        for (var flagEntry : flagContainer.getFlagMap().values()) {
                            PlotFlagEntity fe = new PlotFlagEntity();
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
                    for (UUID uuid : plot.getTrusted()) {
                        PlotMembershipEntity e = new PlotMembershipEntity();
                        e.setPlotId(plotId);
                        e.setUserUuid(uuid.toString());
                        em.persist(e);
                    }
                    // trusted table from plot.getMembers()
                    for (UUID uuid : plot.getMembers()) {
                        PlotTrustedEntity e = new PlotTrustedEntity();
                        e.setPlotId(plotId);
                        e.setUserUuid(uuid.toString());
                        em.persist(e);
                    }
                    // denied table from plot.getDenied()
                    for (UUID uuid : plot.getDenied()) {
                        PlotDeniedEntity e = new PlotDeniedEntity();
                        e.setPlotId(plotId);
                        e.setUserUuid(uuid.toString());
                        em.persist(e);
                    }
                } catch (Exception exception) {
                    LOGGER.warn(
                            "Failed to persist tiers for plot (x={}, z={}, world={})",
                            plot.getId().getX(),
                            plot.getId().getY(),
                            world,
                            exception
                    );
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.error("Failed bulk create plots and data", e);
        }
    }

    @Override
    public boolean swapPlots(final @NotNull Plot plot1, final @NotNull Plot plot2) {
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
    public void replaceWorldInRange(@NotNull String oldWorld, @NotNull String newWorld, int minX, int minZ, int maxX, int maxZ) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("Plot.replaceWorldInBounds")
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
            LOGGER.error(
                    "Failed to replace world in range (oldWorld={}, newWorld={}, range=[{}..{}]x[{}..{}])",
                    oldWorld,
                    newWorld,
                    minX,
                    maxX,
                    minZ,
                    maxZ,
                    e
            );
        }
    }

    @Override
    public void replaceWorldAll(@NotNull String oldWorld, @NotNull String newWorld) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.createNamedQuery("Plot.replaceWorldAll")
                    .setParameter("newWorld", newWorld)
                    .setParameter("oldWorld", oldWorld)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to replace world (all plots) oldWorld={}, newWorld={}", oldWorld, newWorld, e);
        }
    }

    @Override
    public void movePlots(final @NotNull Plot originPlot, final @NotNull Plot newPlot) {
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
        }
    }

    @Override
    public void setOwner(final @NotNull Plot plot, final @NotNull UUID newOwner) {
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
        }
    }

    @Override
    public @NotNull Optional<PlotEntity> findById(long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return Optional.ofNullable(em.find(PlotEntity.class, id));
        }
    }

    @Override
    public @NotNull Optional<PlotEntity> findByXAndZAnyWorld(final int x, final int z, final @NotNull String world) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("Plot.findByXAndZAndWorld", PlotEntity.class)
                    .setParameter("x", x)
                    .setParameter("z", z)
                    .setParameter("world", world)
                    .getResultStream().findFirst();
        }
    }

    @Override
    public @NotNull Optional<PlotEntity> findByWorldAndId(@NotNull String world, int x, int z) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("Plot.findByWorldAndId", PlotEntity.class)
                    .setParameter("world", world)
                    .setParameter("x", x)
                    .setParameter("z", z)
                    .getResultStream().findFirst();
        }
    }

    @Override
    public @NotNull List<PlotEntity> findByOwner(@NotNull String ownerUuid) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("Plot.findByOwner", PlotEntity.class)
                    .setParameter("owner", ownerUuid)
                    .getResultList();
        }
    }

    @Override
    public @NotNull List<PlotEntity> findByWorld(@NotNull String world) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createNamedQuery("Plot.findByWorld", PlotEntity.class)
                    .setParameter("world", world)
                    .getResultList();
        }
    }

    @Override
    public void save(@NotNull PlotEntity plot) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
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
        }
    }

    @Override
    public void deleteById(long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
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
        try (em) {
            tx.begin();
            String world = Optional.of(plot).map(Plot::getWorldName).orElse(Optional
                    .of(plot)
                    .map(Plot::getArea)
                    .map(Object::toString)
                    .orElse(null));
            PlotEntity pe = em.createNamedQuery("Plot.findByWorldAndId", PlotEntity.class)
                    .setParameter("world", world)
                    .setParameter("x", plot.getId().getX())
                    .setParameter("z", plot.getId().getY())
                    .getResultStream().findFirst().orElse(null);
            if (pe != null && pe.getId() != null) {
                Long plotId = pe.getId();
                // Delete children first to satisfy FK constraints
                em.createNamedQuery("PlotFlag.deleteByPlot")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                em.createNamedQuery("PlotHelper.deleteByPlotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                em.createNamedQuery("PlotTrusted.deleteByPlotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                em.createNamedQuery("PlotDenied.deleteByPlotId")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                em.createNamedQuery("PlotRating.deleteByPlot")
                        .setParameter("plotId", plotId)
                        .executeUpdate();
                // Remove settings explicitly to mirror legacy behavior and avoid orphan rows
                em.createNamedQuery("PlotSettings.deleteByPlot")
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
        }
    }

    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        HashMap<String, HashMap<PlotId, Plot>> worldMap = new HashMap<>();
        Map<Long, Plot> byId = new HashMap<>();
        Map<String, UUID> uuidCache = new HashMap<>();
        try (EntityManager em = emf.createEntityManager()) {
            // Load plots
            List<PlotEntity> plots = em.createNamedQuery("Plot.findAll", PlotEntity.class).getResultList();
            for (PlotEntity p : plots) {
                String ownerStr = p.getOwner();
                UUID owner;
                if (ownerStr == null) {
                    owner = EVERYONE;
                } else {
                    owner = uuidCache.get(ownerStr);
                    if (owner == null) {
                        try {
                            owner = UUID.fromString(ownerStr);
                        } catch (IllegalArgumentException ex) {
                            String base = Settings.UUID.FORCE_LOWERCASE
                                    ? ("OfflinePlayer:" + ownerStr.toLowerCase())
                                    : ("OfflinePlayer:" + ownerStr);
                            owner = UUID.nameUUIDFromBytes(base.getBytes(StandardCharsets.UTF_8));
                        }
                        uuidCache.put(ownerStr, owner);
                    }
                }
                long time = p.getTimestamp() != null ? p.getTimestamp().getTime() : System.currentTimeMillis();
                PlotId pid = PlotId.of(p.getPlotIdX(), p.getPlotIdZ());
                Plot plot = new Plot(
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
                worldMap.computeIfAbsent(p.getWorld(), k -> new HashMap<>()).put(pid, plot);
                if (p.getId() != null) {
                    byId.put(p.getId(), plot);
                }
            }

            // Ratings (optional)
            if (Settings.Enabled_Components.RATING_CACHE) {
                var ratings = em.createNamedQuery("PlotRating.findAll", PlotRatingEntity.class).getResultList();
                for (var r : ratings) {
                    var plot = byId.get(r.getPlotId());
                    if (plot != null) {
                        plot.getSettings().getRatings().put(UUID.fromString(r.getPlayer()), r.getRating());
                    }
                }
            }

            // Helpers -> trusted
            var helpers = em.createNamedQuery("PlotHelper.findAll", PlotMembershipEntity.class).getResultList();
            for (var e : helpers) {
                var plot = byId.get(e.getPlotId());
                if (plot != null) {
                    plot.getTrusted().add(UUID.fromString(e.getUserUuid()));
                }
            }

            // Trusted -> members
            var trusted = em.createNamedQuery("PlotTrusted.findAll", PlotTrustedEntity.class).getResultList();
            for (var e : trusted) {
                var plot = byId.get(e.getPlotId());
                if (plot != null) {
                    plot.getMembers().add(UUID.fromString(e.getUserUuid()));
                }
            }

            // Denied
            var denied = em.createNamedQuery("PlotDenied.findAll", PlotDeniedEntity.class).getResultList();
            for (var e : denied) {
                var plot = byId.get(e.getPlotId());
                if (plot != null) {
                    plot.getDenied().add(UUID.fromString(e.getUserUuid()));
                }
            }

            // Flags
            BlockTypeListFlag.skipCategoryVerification = true;
            var flags = em.createNamedQuery("PlotFlag.findAll", PlotFlagEntity.class).getResultList();
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
                        plot.getFlagContainer().addFlag(plotFlag.parse(value));
                    }
                }
            }
            BlockTypeListFlag.skipCategoryVerification = false;

            // Settings
            var settings = em.createNamedQuery("PlotSettings.findAll", PlotSettingsEntity.class).getResultList();
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
                            plot.getSettings().setPosition(BlockLoc.fromString(pos));
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
        } catch (FlagParseException e) {
            LOGGER.error("Failed to load plots due to flag parse error", e);
            return worldMap;
        }
    }

    @Override
    public void purgeIds(@NotNull Set<Integer> ids) {
        if (ids.isEmpty()) return;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            // Convert Integer to Long (plot ids are Long)
            Set<Long> longIds = new HashSet<>();
            for (Integer i : ids) {
                if (i != null) longIds.add(i.longValue());
            }
            if (longIds.isEmpty()) {
                tx.commit();
                return;
            }
            // Delete child tables first
            em.createNamedQuery("PlotFlag.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotTrusted.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotHelper.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotDenied.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotRating.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotSettings.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            // Finally delete plots
            em.createNamedQuery("Plot.deleteAllInIds")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to purge plots by ids (ids={})", ids, e);
        }
    }

    @Override
    public void purgeByWorldAndPlotIds(String world, Set<PlotId> plotIds) {
        if (world.isEmpty() || plotIds.isEmpty()) return;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            // Resolve plot ids by fetching candidates in the world and filtering in-memory
            List<Long> ids = new ArrayList<>();
            List<PlotEntity> candidates = em.createNamedQuery("Plot.findByWorld", PlotEntity.class)
                    .setParameter("world", world)
                    .getResultList();
            HashSet<PlotId> lookup = new HashSet<>(plotIds);
            for (PlotEntity p : candidates) {
                if (lookup.contains(PlotId.of(p.getPlotIdX(), p.getPlotIdZ())) && p.getId() != null) {
                    ids.add(p.getId());
                }
            }
            if (ids.isEmpty()) {
                tx.commit();
                return;
            }
            Set<Long> longIds = new HashSet<>(ids);
            // Delete child tables first
            em.createNamedQuery("PlotFlag.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotTrusted.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotHelper.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotDenied.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotRating.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("PlotSettings.deleteAllInPlotIds")
                    .setParameter("plotIds", longIds)
                    .executeUpdate();
            em.createNamedQuery("Plot.deleteAllInIds")
                    .setParameter("ids", longIds)
                    .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            LOGGER.error("Failed to purge plots by world/id (world={}, plotIds={})", world, plotIds, e);
        }
    }

    @Override
    public void replaceWorld(@NotNull String oldWorld, @NotNull String newWorld) {
        replaceWorldAll(oldWorld, newWorld);
    }

    @Override
    public void replaceWorldInBounds(@NotNull String oldWorld, @NotNull String newWorld, PlotId min, PlotId max) {
        replaceWorldInRange(oldWorld, newWorld, min.getX(), min.getY(), max.getX(), max.getY());
    }
}
