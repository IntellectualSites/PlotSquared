package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.comment.PlotComment;

import java.util.*;

/**
 * Generic settings class.
 * - Does not keep a reference to a parent class
 * - Direct changes here will not occur in the db (Use the parent plot object for that)
 */
public class PlotSettings {

    /**
     * Merged plots.
     *
     * @deprecated Raw access
     */
    @Deprecated public boolean[] merged = new boolean[] {false, false, false, false};
    /**
     * Plot alias.
     *
     * @deprecated Raw access
     */
    @Deprecated public String alias = "";
    /**
     * Comments.
     *
     * @deprecated Raw access
     */
    @Deprecated public List<PlotComment> comments = null;

    /**
     * The ratings for a plot.
     *
     * @deprecated Raw access
     */
    @Deprecated public HashMap<UUID, Integer> ratings;

    /**
     * Flags.
     *
     * @deprecated Raw access
     */
    @Deprecated public HashMap<Flag<?>, Object> flags = new HashMap<>();
    /**
     * Home Position.
     *
     * @deprecated Raw access
     */
    @Deprecated private BlockLoc position;

    /**
     * <b>Check if the plot is merged in a direction</b><br> 0 = North<br> 1 = East<br> 2 = South<br> 3 = West<br>
     *
     * @param direction Direction to check
     * @return boolean merged
     */
    public boolean getMerged(int direction) {
        return this.merged[direction];
    }

    /**
     * Returns true if the plot is merged (i.e. if it's a mega plot)
     */
    public boolean isMerged() {
        return this.merged[0] || this.merged[1] || this.merged[2] || this.merged[3];
    }

    public boolean[] getMerged() {
        return this.merged;
    }

    public void setMerged(boolean[] merged) {
        this.merged = merged;
    }

    public Map<UUID, Integer> getRatings() {
        if (this.ratings == null) {
            this.ratings = new HashMap<>();

        }
        return this.ratings;
    }

    public boolean setMerged(int direction, boolean merged) {
        if (this.merged[direction] != merged) {
            this.merged[direction] = merged;
            return true;
        }
        return false;
    }

    public BlockLoc getPosition() {
        if (this.position == null) {
            return new BlockLoc(0, 0, 0);
        }
        return this.position;
    }

    public void setPosition(BlockLoc position) {
        if (position != null && position.x == 0 && position.y == 0 && position.z == 0) {
            position = null;
        }
        this.position = position;
    }

    public String getAlias() {
        return this.alias;
    }

    /**
     * Set the plot alias.
     *
     * @param alias alias to be used
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getJoinMessage(PlotArea area) {
        if (flags.containsKey(Flags.GREETING)) {
            return (String) flags.get(Flags.GREETING);
        } else {
            return "";
        }
    }

    /**
     * Get the "farewell" flag value.
     *
     * @param plotArea The PlotArea
     * @return Farewell flag
     */
    public String getLeaveMessage(PlotArea plotArea) {
        if (flags.containsKey(Flags.FAREWELL)) {
            return (String) flags.get(Flags.FAREWELL);
        } else {
            return "";
        }
    }

    public Optional<ArrayList<PlotComment>> getComments(String inbox) {
        ArrayList<PlotComment> c = new ArrayList<>();
        if (this.comments == null) {
            return Optional.empty();
        }
        for (PlotComment comment : this.comments) {
            if (comment.inbox.equals(inbox)) {
                c.add(comment);
            }
        }
        return Optional.of(c);
    }

    public void setComments(List<PlotComment> comments) {
        this.comments = comments;
    }

    public void removeComment(PlotComment comment) {
        if (this.comments.contains(comment)) {
            this.comments.remove(comment);
        }
    }

    public void removeComments(List<PlotComment> comments) {
        for (PlotComment comment : comments) {
            removeComment(comment);
        }
    }

    public void addComment(PlotComment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }
}
