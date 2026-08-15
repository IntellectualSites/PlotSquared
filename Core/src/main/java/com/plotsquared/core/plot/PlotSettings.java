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

import com.google.common.collect.ImmutableList;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.plot.comment.PlotComment;

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
    private boolean[] merged = new boolean[]{false, false, false, false};
    /**
     * Plot alias.
     */
    private String alias = "";
    /**
     * The ratings for a plot.
     */
    private HashMap<UUID, Integer> ratings;
    /**
     * Plot comments.
     */
    private List<PlotComment> comments = null;
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

    public Map<UUID, Integer> getRatings() {
        if (this.ratings == null) {
            this.ratings = new HashMap<>();
        }
        return this.ratings;
    }

    public void setRatings(HashMap<UUID, Integer> ratings) {
        this.ratings = ratings;
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
            return BlockLoc.MINY;
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

    public List<PlotComment> getComments(String inbox) {
        if (this.comments == null) {
            return Collections.emptyList();
        }

        return this.comments.stream().filter(comment -> comment.inbox().equals(inbox))
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

    public boolean[] getMerged() {
        return this.merged;
    }

    public void setMerged(boolean[] merged) {
        this.merged = merged;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setComments(List<PlotComment> comments) {
        this.comments = comments;
    }

}
