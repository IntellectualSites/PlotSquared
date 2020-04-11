package com.plotsquared.plot;

import com.plotsquared.location.BlockLoc;
import com.plotsquared.location.Direction;
import com.plotsquared.plot.comment.PlotComment;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generic settings class.
 * - Does not keep a reference to a parent class
 * - Direct changes here will not occur in the db (Use the parent plot object for that)
 */
public class PlotSettings {

    /**
     * Merged plots.
     */
    @Getter private boolean[] merged = new boolean[] {false, false, false, false};
    /**
     * Plot alias.
     */
    @Getter @Setter private String alias = "";
    /**
     * The ratings for a plot.
     */
    @Setter private HashMap<UUID, Integer> ratings;
    /**
     * Plot comments.
     */
    @Setter private List<PlotComment> comments = null;
    /**
     * Home Position.
     */
    private BlockLoc position;

    /**
     * <b>Check if the plot is merged in a direction</b><br> 0 = North<br> 1 = East<br> 2 = South<br> 3 = West<br>
     *
     * @param direction Direction to check
     * @return boolean merged
     */
    public boolean getMerged(int direction) {
        return this.merged[direction];
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

    public boolean setMerged(Direction direction, boolean merged) {
        if (Direction.ALL == direction) {
            throw new IllegalArgumentException("You cannot use Direction.ALL in this method!");
        }
        if (this.merged[direction.getIndex()] != merged) {
            this.merged[direction.getIndex()] = merged;
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
        if (position != null && position.getX() == 0 && position.getY() == 0
            && position.getZ() == 0) {
            position = null;
        }
        this.position = position;
    }

    @SuppressWarnings({"UnstableApiUsage"}) public List<PlotComment> getComments(String inbox) {
        if (this.comments == null) {
            return Collections.emptyList();
        }

        return this.comments.stream().filter(comment -> comment.inbox.equals(inbox))
            .collect(ImmutableList.toImmutableList());
    }

    boolean removeComment(PlotComment comment) {
        if (this.comments == null) {
            return false;
        }
        return this.comments.remove(comment);
    }

    void removeComments(List<PlotComment> comments) {
        comments.forEach(this::removeComment);
    }

    void addComment(PlotComment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }
}
