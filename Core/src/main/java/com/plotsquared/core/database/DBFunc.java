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
package com.plotsquared.core.database;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.persistence.entity.ClusterEntity;
import com.plotsquared.core.persistence.entity.PlayerMetaEntity;
import com.plotsquared.core.persistence.entity.PlotCommentEntity;
import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.repository.api.ClusterHelperRepository;
import com.plotsquared.core.persistence.repository.api.ClusterInvitedRepository;
import com.plotsquared.core.persistence.repository.api.ClusterRepository;
import com.plotsquared.core.persistence.repository.api.ClusterSettingsRepository;
import com.plotsquared.core.persistence.repository.api.PlayerMetaRepository;
import com.plotsquared.core.persistence.repository.api.PlotCommentRepository;
import com.plotsquared.core.persistence.repository.api.PlotDeniedRepository;
import com.plotsquared.core.persistence.repository.api.PlotMembershipRepository;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.persistence.repository.api.PlotSettingsRepository;
import com.plotsquared.core.persistence.repository.api.PlotTrustedRepository;
import com.plotsquared.core.persistence.repository.api.ClusterSettingsRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.task.RunnableVal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Database Functions
 * - These functions do not update the local plot objects and only make changes to the DB
 */
public class DBFunc {

    /**
     * The "global" uuid.
     */
    // TODO: Use this instead. public static final UUID EVERYONE = UUID.fromString("4aa2aaa4-c06b-485c-bc58-186aa1780d9b");
    public static final UUID EVERYONE = UUID.fromString("1-1-3-3-7");
    public static final UUID SERVER = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static void addPersistentMeta(UUID uuid, String key, byte[] meta, boolean delete) {
        try {
            PlayerMetaRepository repo = PlotSquared.platform().injector().getInstance(PlayerMetaRepository.class);
            if (delete) {
                repo.delete(uuid.toString(), key);
            } else {
                repo.put(uuid.toString(), key, meta);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void getPersistentMeta(UUID uuid, RunnableVal<Map<String, byte[]>> result) {
        try {
            PlayerMetaRepository repo = PlotSquared.platform().injector().getInstance(PlayerMetaRepository.class);
            Map<String, byte[]> map = new HashMap<>();
            for (PlayerMetaEntity e : repo.findByUuid(uuid.toString())) {
                map.put(e.getKey(), e.getValue());
            }
            if (result != null) {
                result.run(map);
            }
        } catch (Throwable t) {
            if (result != null) {
                result.run(new HashMap<>());
            }
        }
    }

    public static void removePersistentMeta(UUID uuid, String key) {
        try {
            PlayerMetaRepository repo = PlotSquared.platform().injector().getInstance(PlayerMetaRepository.class);
            repo.delete(uuid.toString(), key);
        } catch (Throwable ignored) {
        }
    }

    public static CompletableFuture<Boolean> swapPlots(Plot plot1, Plot plot2) {
        if (plot1 == null || plot2 == null) {
            return CompletableFuture.completedFuture(false);
        }
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        return CompletableFuture.completedFuture(repo.swapPlots(plot1, plot2));
    }

    public static void movePlot(Plot originalPlot, Plot newPlot) {
        if (originalPlot == null || newPlot == null) {
            return;
        }
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        repo.movePlots(originalPlot, newPlot);
    }

    /**
     * Set the owner of a plot
     *
     * @param plot Plot Object
     * @param uuid New Owner
     */
    public static void setOwner(Plot plot, UUID uuid) {
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        repo.setOwner(plot, uuid);
    }

    /**
     * Create all settings + (trusted, denied, members)
     *
     * @param plots List containing all plot objects
     */
    public static void createPlotsAndData(List<Plot> plots, Runnable whenDone) {
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        repo.createPlotsAndData(plots);
        if (whenDone != null) whenDone.run();
    }

    public static void createPlotSafe(
            final Plot plot, final Runnable success,
            final Runnable failure
    ) {
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        boolean created = repo.createPlotSafe(plot);
        if (created) {
            if (success != null) success.run();
        } else {
            if (failure != null) failure.run();
        }
    }

    /**
     * Create a plot.
     *
     * @param plot Plot to create
     */
    public static void createPlotAndSettings(Plot plot, Runnable whenDone) {
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        repo.createPlotAndSettings(plot);
        if (whenDone != null) whenDone.run();
    }

    /**
     * Delete a plot.
     *
     * @param plot Plot to delete
     */
    public static void delete(Plot plot) {
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        repo.delete(plot);
    }

    /**
     * Delete the ratings for a plot.
     *
     * @param plot
     */
    public static void deleteRatings(Plot plot) {
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        repo.deleteRatings(plot);
    }

    /**
     * Delete the trusted list for a plot.
     *
     * @param plot
     */
    public static void deleteTrusted(Plot plot) {
        try {
            PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            String world = plot.getWorldName();
            var peOpt = repo.findByWorldAndId(world, plot.getId().getX(), plot.getId().getY());
            peOpt.ifPresent(pe -> {
                PlotMembershipRepository mRepo = PlotSquared.platform().injector().getInstance(PlotMembershipRepository.class);
                mRepo.deleteByPlotId(pe.getId());
            });
        } catch (Throwable ignored) {
        }
    }

    /**
     * Delete the members list for a plot.
     *
     * @param plot
     */
    public static void deleteMembers(Plot plot) {
        try {
            PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            String world = plot.getWorldName();
            var peOpt = repo.findByWorldAndId(world, plot.getId().getX(), plot.getId().getY());
            peOpt.ifPresent(pe -> {
                PlotTrustedRepository tRepo = PlotSquared.platform().injector().getInstance(PlotTrustedRepository.class);
                tRepo.deleteByPlotId(pe.getId());
            });
        } catch (Throwable ignored) {
        }
    }

    /**
     * Delete the denied list for a plot.
     *
     * @param plot
     */
    public static void deleteDenied(Plot plot) {
        try {
            PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            String world = plot.getWorldName();
            var peOpt = repo.findByWorldAndId(world, plot.getId().getX(), plot.getId().getY());
            peOpt.ifPresent(pe -> {
                PlotDeniedRepository dRepo = PlotSquared.platform().injector().getInstance(PlotDeniedRepository.class);
                dRepo.deleteByPlotId(pe.getId());
            });
        } catch (Throwable ignored) {
        }
    }

    /**
     * Delete the comments in a plot.
     *
     * @param plot
     */
    public static void deleteComments(Plot plot) {
        try {
            PlotCommentRepository repo = PlotSquared.platform().injector().getInstance(PlotCommentRepository.class);
            String world = plot.getWorldName();
            int hash = plot.getId().hashCode();
            repo.deleteByWorldAndHash(world, hash);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Deleting settings will
     * 1) Delete any settings (flags and such) associated with the plot
     * 2) Prevent any local changes to the plot from saving properly to the db
     * <p>
     * This shouldn't ever be needed
     *
     * @param plot
     */
    public static void deleteSettings(Plot plot) {

        try {
            PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            String world = plot.getWorldName();
            var peOpt = repo.findByWorldAndId(world, plot.getId().getX(), plot.getId().getY());
            peOpt.ifPresent(pe -> {
                PlotSettingsRepository sRepo = PlotSquared.platform().injector().getInstance(PlotSettingsRepository.class);
                sRepo.deleteByPlotId(pe.getId());
            });
        } catch (Throwable ignored) {
        }
    }

    public static void delete(PlotCluster toDelete) {
        if (toDelete == null) {
            return;
        }
        try {
            ClusterRepository clusterRepo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
            String world = toDelete.area != null ? toDelete.area.getWorldName() : null;
            if (world == null) {
                return;
            }
            PlotId center = toDelete.getCenterPlotId();
            java.util.Optional<com.plotsquared.core.persistence.entity.ClusterEntity> ce = clusterRepo.findByWorldAndBounds(world, center.getX(), center.getY());
            ce.ifPresent(entity -> clusterRepo.deleteById(entity.getId()));
        } catch (Throwable ignored) {
        }
    }

    /**
     * Create plot settings.
     *
     * @param id   Plot ID
     * @param plot Plot Object
     */
    public static void createPlotSettings(int id, Plot plot) {
        try {
            PlotSettingsRepository repo = PlotSquared.platform().injector().getInstance(PlotSettingsRepository.class);
            repo.createDefaultIfAbsent(id, "DEFAULT");
        } catch (Throwable ignored) {
        }
    }

    /**
     * @return Plots
     */
    public static HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        PlotRepository repo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        return repo.getPlots();
    }

    public static void setMerged(Plot plot, boolean[] merged) {
        if (plot == null || merged == null) {
            return;
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            PlotSettingsRepository settingsRepo = PlotSquared.platform().injector().getInstance(PlotSettingsRepository.class);
            String world = plot.getWorldName();
            int x = plot.getId().getX();
            int z = plot.getId().getY();
            Optional<PlotEntity> pe = plotRepo.findByWorldAndId(world, x, z);
            pe.ifPresent(entity -> {
                int mask = com.plotsquared.core.util.HashUtil.hash(merged);
                settingsRepo.updateMerged(entity.getId(), mask);
            });
        } catch (Throwable ignored) {
        }
    }

    public static void setFlag(Plot plot, PlotFlag<?, ?> flag) {
        if (plot == null || flag == null) {
            return;
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            com.plotsquared.core.persistence.repository.api.PlotFlagRepository flagRepo = PlotSquared.platform().injector().getInstance(com.plotsquared.core.persistence.repository.api.PlotFlagRepository.class);
            Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
            pe.ifPresent(entity -> {
                long plotId = entity.getId();
                String name = flag.getName();
                String value = flag.toString();
                var existing = flagRepo.findByPlotAndName(plotId, name);
                if (existing.isPresent()) {
                    var e = existing.get();
                    e.setValue(value);
                    flagRepo.save(e);
                } else {
                    com.plotsquared.core.persistence.entity.PlotFlagEntity e = new com.plotsquared.core.persistence.entity.PlotFlagEntity();
                    com.plotsquared.core.persistence.entity.PlotEntity pref = new com.plotsquared.core.persistence.entity.PlotEntity();
                    pref.setId(entity.getId());
                    e.setPlot(pref);
                    e.setFlag(name);
                    e.setValue(value);
                    flagRepo.save(e);
                }
            });
        } catch (Throwable ignored) {
        }
    }

    public static void removeFlag(Plot plot, PlotFlag<?, ?> flag) {
        if (plot == null || flag == null) {
            return;
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            com.plotsquared.core.persistence.repository.api.PlotFlagRepository flagRepo = PlotSquared.platform().injector().getInstance(com.plotsquared.core.persistence.repository.api.PlotFlagRepository.class);
            Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
            pe.ifPresent(entity -> flagRepo.deleteByPlotAndName(entity.getId(), flag.getName()));
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param plot
     * @param alias
     */
    public static void setAlias(Plot plot, String alias) {
        if (plot == null) {
            return;
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            PlotSettingsRepository settingsRepo = PlotSquared.platform().injector().getInstance(PlotSettingsRepository.class);
            Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
            pe.ifPresent(entity -> settingsRepo.updateAlias(entity.getId(), alias));
        } catch (Throwable ignored) {
        }
    }

    public static void purgeIds(Set<Integer> uniqueIds) {
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            plotRepo.purgeIds(uniqueIds);
        } catch (Throwable ignored) {
        }
    }

    public static void purge(PlotArea area, Set<PlotId> plotIds) {
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            plotRepo.purgeByWorldAndPlotIds(area.getWorldName(), plotIds);
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param plot
     * @param position
     */
    public static void setPosition(Plot plot, String position) {
        if (plot == null || position == null) {
            return;
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            PlotSettingsRepository settingsRepo = PlotSquared.platform().injector().getInstance(PlotSettingsRepository.class);
            Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
            pe.ifPresent(entity -> settingsRepo.updatePosition(entity.getId(), position));
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param plot
     * @param comment
     */
    public static void removeComment(Plot plot, PlotComment comment) {
        try {
            PlotCommentRepository repo = PlotSquared.platform().injector().getInstance(PlotCommentRepository.class);
            String world = plot.getWorldName();
            int hash = plot.getId().hashCode();
            repo.deleteOne(world, hash, comment.inbox(), comment.senderName(), comment.comment());
        } catch (Throwable ignored) {
        }
    }

    public static void clearInbox(Plot plot, String inbox) {
        try {
            PlotCommentRepository repo = PlotSquared.platform().injector().getInstance(PlotCommentRepository.class);
            if (plot != null) {
                String world = plot.getWorldName();
                int hash = plot.getId().hashCode();
                repo.clearInbox(world, hash, inbox);
            } else {
                // Fallback: no plot provided; unable to infer world. No-op to avoid unintended global deletions.
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param plot
     * @param comment
     */
    public static void setComment(Plot plot, PlotComment comment) {
        try {
            PlotCommentRepository repo = PlotSquared.platform().injector().getInstance(PlotCommentRepository.class);
            if (plot != null) {
                PlotCommentEntity entity = new PlotCommentEntity();
                entity.setWorld(plot.getWorldName());
                entity.setHashcode(plot.getId().hashCode());
                entity.setComment(comment.comment());
                entity.setInbox(comment.inbox());
                entity.setTimestamp((int) (comment.timestamp() / 1000));
                entity.setSender(comment.senderName());
                repo.save(entity);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param plot
     */
    public static void getComments(
            Plot plot, String inbox,
            RunnableVal<List<PlotComment>> whenDone
    ) {
        try {
            PlotCommentRepository repo = PlotSquared.platform().injector().getInstance(PlotCommentRepository.class);
            List<PlotComment> out = new java.util.ArrayList<>();
            if (plot != null) {
                String world = plot.getWorldName();
                int hash = plot.getId().hashCode();
                for (PlotCommentEntity e : repo.findByWorldHashAndInbox(world, hash, inbox)) {
                    PlotId id = (e.getHashcode() != null && e.getHashcode() != 0) ? PlotId.unpair(e.getHashcode()) : null;
                    long tsMillis = e.getTimestamp() != null ? e.getTimestamp().longValue() * 1000L : 0L;
                    out.add(new PlotComment(e.getWorld(), id, e.getComment(), e.getSender(), e.getInbox(), tsMillis));
                }
            }
            if (whenDone != null) {
                whenDone.run(out);
            }
        } catch (Throwable t) {
            if (whenDone != null) {
                whenDone.run(java.util.Collections.emptyList());
            }
        }
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void removeTrusted(Plot plot, UUID uuid) {
        if (plot == null) {
            return;
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            PlotTrustedRepository trustedRepo = PlotSquared.platform().injector().getInstance(PlotTrustedRepository.class);
            String world = plot.getArea().toString();
            int x = plot.getId().getX();
            int z = plot.getId().getY();
            java.util.Optional<PlotEntity> ent = plotRepo.findByWorldAndId(world, x, z);
            if (ent.isPresent() && ent.get().getId() != null) {
                trustedRepo.remove(ent.get().getId(), uuid.toString());
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param cluster
     * @param uuid
     */
    public static void removeHelper(PlotCluster cluster, UUID uuid) {
        if (cluster == null || uuid == null) {
            return;
        }
        try {
            ClusterRepository clusterRepo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
            ClusterHelperRepository helperRepo = PlotSquared.platform().injector().getInstance(ClusterHelperRepository.class);
            String world = cluster.area != null ? cluster.area.toString() : null;
            PlotId center = cluster.getCenterPlotId();
            if (world != null) {
                Optional<ClusterEntity> ent = clusterRepo.findByWorldAndBounds(world, center.getX(), center.getY());
                if (ent.isPresent() && ent.get().getId() != null) {
                    helperRepo.remove(ent.get().getId(), uuid.toString());
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param cluster
     */
    public static void createCluster(PlotCluster cluster) {
        if (cluster == null) {
            return;
        }
        try {
            ClusterRepository repo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
            ClusterEntity e = new ClusterEntity();
            e.setWorld(cluster.area != null ? cluster.area.toString() : null);
            e.setOwner(cluster.owner != null ? cluster.owner.toString() : null);
            if (cluster.getP1() != null) {
                e.setPos1X(cluster.getP1().getX());
                e.setPos1Z(cluster.getP1().getY());
            }
            if (cluster.getP2() != null) {
                e.setPos2X(cluster.getP2().getX());
                e.setPos2Z(cluster.getP2().getY());
            }
            repo.save(e);
            // Do not assign cluster.temp; avoid reassigning transient identifiers
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param current
     * @param min
     * @param max
     */
    public static void resizeCluster(PlotCluster current, PlotId min, PlotId max) {
        if (current == null || min == null || max == null) {
            return;
        }
        try {
            ClusterRepository repo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
            String world = current.area != null ? current.area.toString() : null;
            PlotId center = current.getCenterPlotId();
            if (world != null) {
                Optional<ClusterEntity> ent = repo.findByWorldAndBounds(world, center.getX(), center.getY());
                if (ent.isPresent()) {
                    ClusterEntity e = ent.get();
                    e.setPos1X(min.getX());
                    e.setPos1Z(min.getY());
                    e.setPos2X(max.getX());
                    e.setPos2Z(max.getY());
                    repo.save(e);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void removeMember(Plot plot, UUID uuid) {
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotMembershipRepository membershipRepo = PlotSquared.platform().injector().getInstance(PlotMembershipRepository.class);
        String world = plot.getArea().toString();
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> membershipRepo.remove(entity.getId(), uuid.toString()));
    }

    /**
     * @param cluster
     * @param uuid
     */
    public static void removeInvited(PlotCluster cluster, UUID uuid) {
        ClusterRepository clusterRepo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
        ClusterInvitedRepository invitedRepo = PlotSquared.platform().injector().getInstance(ClusterInvitedRepository.class);
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = clusterRepo.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> invitedRepo.remove(entity.getId(), uuid.toString()));
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void setTrusted(Plot plot, UUID uuid) {
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotTrustedRepository trustedRepo = PlotSquared.platform().injector().getInstance(PlotTrustedRepository.class);
        String world = plot.getArea().toString();
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> trustedRepo.add(entity.getId(), uuid.toString()));
    }

    public static void setHelper(PlotCluster cluster, UUID uuid) {
        ClusterRepository clusterRepo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
        ClusterHelperRepository helperRepo = PlotSquared.platform().injector().getInstance(ClusterHelperRepository.class);
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = clusterRepo.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> helperRepo.add(entity.getId(), uuid.toString()));
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void setMember(Plot plot, UUID uuid) {
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotMembershipRepository membershipRepo = PlotSquared.platform().injector().getInstance(PlotMembershipRepository.class);
        String world = plot.getArea().toString();
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> membershipRepo.add(entity.getId(), uuid.toString()));
    }

    public static void setInvited(PlotCluster cluster, UUID uuid) {
        ClusterRepository clusterRepo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
        ClusterInvitedRepository invitedRepo = PlotSquared.platform().injector().getInstance(ClusterInvitedRepository.class);
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = clusterRepo.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> invitedRepo.add(entity.getId(), uuid.toString()));
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void removeDenied(Plot plot, UUID uuid) {
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotDeniedRepository deniedRepo = PlotSquared.platform().injector().getInstance(PlotDeniedRepository.class);
        String world = plot.getArea().toString();
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> deniedRepo.remove(entity.getId(), uuid.toString()));
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void setDenied(Plot plot, UUID uuid) {
        if (plot == null || uuid == null) {
            return;
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            PlotDeniedRepository deniedRepo = PlotSquared.platform().injector().getInstance(PlotDeniedRepository.class);
            String world = plot.getArea().toString();
            PlotId pid = plot.getId();
            Optional<PlotEntity> pe = plotRepo.findByWorldAndId(world, pid.getX(), pid.getY());
            pe.ifPresent(entity -> deniedRepo.add(entity.getId(), uuid.toString()));
        } catch (Throwable ignored) {
        }
    }

    public static HashMap<UUID, Integer> getRatings(Plot plot) {
        if (plot == null) {
            return new HashMap<>(0);
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            com.plotsquared.core.persistence.repository.api.PlotRatingRepository ratingRepo = PlotSquared.platform().injector().getInstance(com.plotsquared.core.persistence.repository.api.PlotRatingRepository.class);
            Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
            HashMap<UUID, Integer> out = new HashMap<>();
            pe.ifPresent(entity -> {
                for (com.plotsquared.core.persistence.entity.PlotRatingEntity e : ratingRepo.findByPlotId(entity.getId())) {
                    try {
                        out.put(UUID.fromString(e.getPlayer()), e.getRating());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            });
            return out;
        } catch (Throwable ignored) {
            return new HashMap<>(0);
        }
    }

    public static void setRating(Plot plot, UUID rater, int value) {
        if (plot == null || rater == null) {
            return;
        }
        try {
            PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
            com.plotsquared.core.persistence.repository.api.PlotRatingRepository ratingRepo = PlotSquared.platform().injector().getInstance(com.plotsquared.core.persistence.repository.api.PlotRatingRepository.class);
            Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
            pe.ifPresent(entity -> ratingRepo.upsert(entity.getId(), rater.toString(), value));
        } catch (Throwable ignored) {
        }
    }

    public static HashMap<String, Set<PlotCluster>> getClusters() {
        HashMap<String, Set<PlotCluster>> result = new HashMap<>();
        ClusterRepository clusterRepo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
        ClusterHelperRepository helperRepo = PlotSquared.platform().injector().getInstance(ClusterHelperRepository.class);
        ClusterInvitedRepository invitedRepo = PlotSquared.platform().injector().getInstance(ClusterInvitedRepository.class);
        ClusterSettingsRepository settingsRepo = PlotSquared.platform().injector().getInstance(ClusterSettingsRepository.class);
        List<ClusterEntity> clusters = clusterRepo.findAll();
        Map<Long, PlotCluster> built = new HashMap<>();
        for (ClusterEntity ce : clusters) {
            UUID owner = Optional.ofNullable(ce.getOwner()).map(UUID::fromString).orElse(null);
            PlotCluster cluster = new PlotCluster(null, PlotId.of(ce.getPos1X(), ce.getPos1Z()), PlotId.of(ce.getPos2X(), ce.getPos2Z()), owner);
            built.put(ce.getId(), cluster);
            result.computeIfAbsent(ce.getWorld(), k -> new java.util.HashSet<>()).add(cluster);
        }
        // Populate helpers and invited
        for (Map.Entry<Long, PlotCluster> e : built.entrySet()) {
            long id = e.getKey();
            PlotCluster cluster = e.getValue();
            for (String u : helperRepo.findUsers(id)) {
                cluster.helpers.add(UUID.fromString(u));
            }
            for (String u : invitedRepo.findUsers(id)) {
                cluster.invited.add(UUID.fromString(u));
            }
            // Apply settings (alias, merged). Avoid setting temp variable.
            settingsRepo.findById(id).ifPresent(se -> {
                if (se.getAlias() != null) {
                    cluster.settings.setAlias(se.getAlias());
                }
                Integer m = se.getMerged();
                if (m != null) {
                    boolean[] merged = new boolean[4];
                    for (int i = 0; i < 4; i++) {
                        merged[3 - i] = (m & 1 << i) != 0;
                    }
                    cluster.settings.setMerged(merged);
                }
            });
        }
        return result;
    }

    public static void setPosition(PlotCluster cluster, String position) {
        if (cluster == null || position == null) {
            return;
        }
        ClusterRepository clusterRepo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
        ClusterSettingsRepository settingsRepo = PlotSquared.platform().injector().getInstance(ClusterSettingsRepository.class);
        String world = cluster.area != null ? cluster.area.getWorldName() : null;
        if (world == null) {
            return;
        }
        PlotId center = cluster.getCenterPlotId();
        Optional<ClusterEntity> ce = clusterRepo.findByWorldAndBounds(world, center.getX(), center.getY());
        ce.ifPresent(entity -> settingsRepo.updatePosition(entity.getId(), position));
    }

    public static void replaceWorld(String oldWorld, String newWorld, PlotId min, PlotId max) {
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        ClusterRepository clusterRepo = PlotSquared.platform().injector().getInstance(ClusterRepository.class);
        if (min == null) {
            plotRepo.replaceWorld(oldWorld, newWorld);
            clusterRepo.replaceWorld(oldWorld, newWorld);
        } else {
            plotRepo.replaceWorldInBounds(oldWorld, newWorld, min, max);
            clusterRepo.replaceWorldInBounds(oldWorld, newWorld, min, max);
        }
    }

}
