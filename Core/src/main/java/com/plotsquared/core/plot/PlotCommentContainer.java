/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.plot;

import com.plotsquared.core.plot.comment.PlotComment;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Container for {@link com.plotsquared.core.plot.Plot} comments
 */
public final class PlotCommentContainer {

    private final Plot plot;

    PlotCommentContainer(@Nonnull final Plot plot) {
        this.plot = plot;
    }

    /**
     * Remove a comment from the plot
     *
     * @param comment Comment to remove
     * @return {@code true} if the comment was removed, {@code false} if not
     */
    public boolean removeComment(@Nonnull final PlotComment comment) {
        return this.getSettings().removeComment(comment);
    }

    /**
     * Remove a list of comments from the plot
     *
     * @param comments Comments to remove
     */
    public void removeComments(@Nonnull final List<PlotComment> comments) {
        this.getSettings().removeComments(comments);
    }

    /**
     * Get all comments in a specific inbox
     *
     * @param inbox Inbox
     * @return List of comments
     */
    @Nonnull public List<PlotComment> getComments(@Nonnull final String inbox) {
        return this.getSettings().getComments(inbox);
    }

    /**
     * Add a comment to the plot
     *
     * @param comment Comment to add
     */
    public void addComment(@Nonnull final PlotComment comment) {
        this.getSettings().addComment(comment);
    }

    /**
     * Set the plot comments
     *
     * @param list New comments
     */
    public void setComments(@Nonnull final List<PlotComment> list) {
        this.getSettings().setComments(list);
    }

    @Nonnull private PlotSettings getSettings() {
        if (this.plot.getSettings() == null) {
            throw new IllegalStateException("Cannot access comments for unowned plots");
        }
        return this.plot.getSettings();
    }

}
