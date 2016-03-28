////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.comment.PlotComment;

import java.util.ArrayList;
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
     * @deprecated Raw access
     */
    @Deprecated
    public boolean[] merged = new boolean[]{false, false, false, false};
    /**
     * Plot alias.
     * @deprecated Raw access
     */
    @Deprecated
    public String alias = "";
    /**
     * Comments.
     * @deprecated Raw access
     */
    @Deprecated
    public List<PlotComment> comments = null;

    /**
     * The ratings for a plot.
     * @deprecated Raw access
     */
    @Deprecated
    public HashMap<UUID, Integer> ratings;

    /**
     * Flags.
     * @deprecated Raw access
     */
    @Deprecated
    public HashMap<String, Flag> flags;
    /**
     * Home Position.
     * @deprecated Raw access
     */
    @Deprecated
    private BlockLoc position;

    /**
     * Constructor
     *
     */
    public PlotSettings() {
        this.flags = new HashMap<>();
    }

    /**
     * <b>Check if the plot is merged in a direction</b><br> 0 = North<br> 1 = East<br> 2 = South<br> 3 = West<br>
     *
     * @param direction Direction to check
     *
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
        return this.ratings == null ? new HashMap<UUID, Integer>() : this.ratings;
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
        Flag greeting = FlagManager.getSettingFlag(area, this, "greeting");
        if (greeting != null) {
            return greeting.getValueString();
        }
        return "";
    }

    /**
     * Get the "farewell" flag value.
     *
     * @param plotArea The PlotArea
     * @return Farewell flag
     */
    public String getLeaveMessage(PlotArea plotArea) {
        Flag farewell = FlagManager.getSettingFlag(plotArea, this, "farewell");
        if (farewell != null) {
            return farewell.getValueString();
        }
        return "";
    }

    public ArrayList<PlotComment> getComments(String inbox) {
        ArrayList<PlotComment> c = new ArrayList<>();
        if (this.comments == null) {
            return null;
        }
        for (PlotComment comment : this.comments) {
            if (comment.inbox.equals(inbox)) {
                c.add(comment);
            }
        }
        return c;
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
