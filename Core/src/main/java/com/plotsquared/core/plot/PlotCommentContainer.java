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
