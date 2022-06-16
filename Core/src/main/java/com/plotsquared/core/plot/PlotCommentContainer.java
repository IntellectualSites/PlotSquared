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
package com.plotsquared.core.plot;

import com.plotsquared.core.plot.comment.PlotComment;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * Container for {@link com.plotsquared.core.plot.Plot} comments
 */
public final class PlotCommentContainer {

    private final Plot plot;

    PlotCommentContainer(final @NonNull Plot plot) {
        this.plot = plot;
    }

    /**
     * Remove a comment from the plot
     *
     * @param comment Comment to remove
     * @return {@code true} if the comment was removed, {@code false} if not
     */
    public boolean removeComment(final @NonNull PlotComment comment) {
        return this.getSettings().removeComment(comment);
    }

    /**
     * Remove a list of comments from the plot
     *
     * @param comments Comments to remove
     */
    public void removeComments(final @NonNull List<PlotComment> comments) {
        this.getSettings().removeComments(comments);
    }

    /**
     * Get all comments in a specific inbox
     *
     * @param inbox Inbox
     * @return List of comments
     */
    public @NonNull List<PlotComment> getComments(final @NonNull String inbox) {
        return this.getSettings().getComments(inbox);
    }

    /**
     * Add a comment to the plot
     *
     * @param comment Comment to add
     */
    public void addComment(final @NonNull PlotComment comment) {
        this.getSettings().addComment(comment);
    }

    /**
     * Set the plot comments
     *
     * @param list New comments
     */
    public void setComments(final @NonNull List<PlotComment> list) {
        this.getSettings().setComments(list);
    }

    @NonNull
    private PlotSettings getSettings() {
        if (this.plot.getSettings() == null) {
            throw new IllegalStateException("Cannot access comments for unowned plots");
        }
        return this.plot.getSettings();
    }

}
