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
package com.plotsquared.core.services.impl;

import com.plotsquared.core.persistence.entity.PlotCommentEntity;
import com.plotsquared.core.persistence.repository.api.PlotCommentRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.services.api.CommentService;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CommentDefaultService implements CommentService {

    private final PlotCommentRepository repository;

    @Inject
    public CommentDefaultService(final PlotCommentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void removeComment(final Plot plot, final PlotComment comment) {
        String world = plot.getWorldName();
        if (world == null) {
            return;
        }
        int hash = plot.getId().hashCode();
        this.repository.deleteOne(world, hash, comment.inbox(), comment.senderName(), comment.comment());
    }

    @Override
    public void setComment(final Plot plot, final PlotComment comment) {
        PlotCommentEntity entity = new PlotCommentEntity();
        entity.setWorld(plot.getWorldName());
        entity.setHashcode(plot.getId().hashCode());
        entity.setComment(comment.comment());
        entity.setInbox(comment.inbox());
        entity.setTimestamp((int) (comment.timestamp() / 1000));
        entity.setSender(comment.senderName());
        this.repository.save(entity);
    }

    @Override
    public void clearInbox(final Plot plot, final String inbox) {
        String world = plot.getWorldName();
        if (world == null) {
            return;
        }
        int hash = plot.getId().hashCode();
        this.repository.clearInbox(world, hash, inbox);
    }

    @Override
    public void getComments(final Plot plot, final @NotNull String inbox, final @NotNull Consumer<List<PlotComment>> whenDone) {
        List<PlotComment> out = new ArrayList<>();
        String world = plot.getWorldName();
        int hash = plot.getId().hashCode();
        for (PlotCommentEntity e : this.repository.findByWorldHashAndInbox(world, hash, inbox)) {
            PlotId id = (e.getHashcode() != null && e.getHashcode() != 0) ? PlotId.unpair(e.getHashcode()) : null;
            long tsMillis = e.getTimestamp() != null ? e.getTimestamp().longValue() * 1000L : 0L;
            out.add(new PlotComment(e.getWorld(), id, e.getComment(), e.getSender(), e.getInbox(), tsMillis));
        }
        whenDone.accept(out);
    }

}
