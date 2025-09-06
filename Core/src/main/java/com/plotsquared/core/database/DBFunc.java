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
import com.plotsquared.core.persistence.entity.PlotCommentEntity;
import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.entity.PlotFlagEntity;
import com.plotsquared.core.persistence.repository.api.PlotCommentRepository;
import com.plotsquared.core.persistence.repository.api.PlotDeniedRepository;
import com.plotsquared.core.persistence.repository.api.PlotFlagRepository;
import com.plotsquared.core.persistence.repository.api.PlotMembershipRepository;
import com.plotsquared.core.persistence.repository.api.PlotRatingRepository;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.persistence.repository.api.PlotSettingsRepository;
import com.plotsquared.core.persistence.repository.api.PlotTrustedRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.task.RunnableVal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    public static void setMerged(Plot plot, boolean[] merged) {
        if (plot == null || merged == null) {
            return;
        }
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
    }

    public static void setFlag(Plot plot, PlotFlag<?, ?> flag) {
        if (plot == null || flag == null) {
            return;
        }
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotFlagRepository flagRepo = PlotSquared.platform().injector().getInstance(PlotFlagRepository.class);
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> {
            long plotId = entity.getId();
            String name = flag.getName();
            String value = flag.toString();
            var existing = flagRepo.findByPlotAndName(plotId, name);
            if (existing.isPresent()) {
                var e = existing.get();
                e.setFlagValue(value);
                flagRepo.save(e);
            } else {
                PlotFlagEntity e = new PlotFlagEntity();
                PlotEntity pref = new PlotEntity();
                pref.setId(entity.getId());
                e.setPlot(pref);
                e.setFlag(name);
                e.setFlagValue(value);
                flagRepo.save(e);
            }
        });
    }

    public static void removeFlag(Plot plot, PlotFlag<?, ?> flag) {
        if (plot == null || flag == null) {
            return;
        }
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotFlagRepository flagRepo = PlotSquared.platform().injector().getInstance(PlotFlagRepository.class);
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> flagRepo.deleteByPlotAndName(entity.getId(), flag.getName()));
    }

    /**
     * @param plot
     * @param alias
     */
    public static void setAlias(Plot plot, String alias) {
        if (plot == null) {
            return;
        }
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotSettingsRepository settingsRepo = PlotSquared.platform().injector().getInstance(PlotSettingsRepository.class);
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> settingsRepo.updateAlias(entity.getId(), alias));
    }

    public static void purgeIds(Set<Integer> uniqueIds) {
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        plotRepo.purgeIds(uniqueIds);
    }

    public static void purge(PlotArea area, Set<PlotId> plotIds) {
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        plotRepo.purgeByWorldAndPlotIds(area.getWorldName(), plotIds);
    }

    /**
     * @param plot
     * @param position
     */
    public static void setPosition(Plot plot, String position) {
        if (plot == null || position == null) {
            return;
        }
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotSettingsRepository settingsRepo = PlotSquared.platform().injector().getInstance(PlotSettingsRepository.class);
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> settingsRepo.updatePosition(entity.getId(), position));
    }

    /**
     * @param plot
     * @param comment
     */
    public static void removeComment(Plot plot, PlotComment comment) {
        PlotCommentRepository repo = PlotSquared.platform().injector().getInstance(PlotCommentRepository.class);
        String world = plot.getWorldName();
        int hash = plot.getId().hashCode();
        repo.deleteOne(world, hash, comment.inbox(), comment.senderName(), comment.comment());
    }

    public static void clearInbox(Plot plot, String inbox) {
        PlotCommentRepository repo = PlotSquared.platform().injector().getInstance(PlotCommentRepository.class);
        if (plot != null) {
            String world = plot.getWorldName();
            int hash = plot.getId().hashCode();
            repo.clearInbox(world, hash, inbox);
        }
    }

    /**
     * @param plot
     * @param comment
     */
    public static void setComment(Plot plot, PlotComment comment) {
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
    }

    /**
     * @param plot
     */
    public static void getComments(
            Plot plot, String inbox,
            RunnableVal<List<PlotComment>> whenDone
    ) {
        PlotCommentRepository repo = PlotSquared.platform().injector().getInstance(PlotCommentRepository.class);
        List<PlotComment> out = new ArrayList<>();
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
    }

    /**
     * @param plot
     * @param uuid
     */
    public static void removeTrusted(Plot plot, UUID uuid) {
        if (plot == null) {
            return;
        }
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotTrustedRepository trustedRepo = PlotSquared.platform().injector().getInstance(PlotTrustedRepository.class);
        String world = plot.getArea().toString();
        int x = plot.getId().getX();
        int z = plot.getId().getY();
        java.util.Optional<PlotEntity> ent = plotRepo.findByWorldAndId(world, x, z);
        if (ent.isPresent() && ent.get().getId() != null) {
            trustedRepo.remove(ent.get().getId(), uuid.toString());
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
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotDeniedRepository deniedRepo = PlotSquared.platform().injector().getInstance(PlotDeniedRepository.class);
        String world = plot.getArea().toString();
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> deniedRepo.add(entity.getId(), uuid.toString()));
    }

    public static HashMap<UUID, Integer> getRatings(Plot plot) {
        if (plot == null) {
            return new HashMap<>(0);
        }
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotRatingRepository ratingRepo = PlotSquared.platform().injector().getInstance(PlotRatingRepository.class);
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
    }

    public static void setRating(Plot plot, UUID rater, int value) {
        if (plot == null || rater == null) {
            return;
        }
        PlotRepository plotRepo = PlotSquared.platform().injector().getInstance(PlotRepository.class);
        PlotRatingRepository ratingRepo = PlotSquared.platform().injector().getInstance(PlotRatingRepository.class);
        Optional<PlotEntity> pe = plotRepo.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> ratingRepo.upsert(entity.getId(), rater.toString(), value));
    }

}
