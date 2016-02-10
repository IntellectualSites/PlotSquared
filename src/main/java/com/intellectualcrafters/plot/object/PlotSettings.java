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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.comment.PlotComment;

/**
 * Generic settings class
 * - Does not keep a reference to a parent class
 * - Direct changes here will not occur in the db (Use the parent plot object for that)
 */
public class PlotSettings {
    /**
     * merged plots
     * @deprecated Raw access
     */
    @Deprecated
    public boolean[] merged = new boolean[] { false, false, false, false };
    /**
     * plot alias
     * @deprecated Raw access
     */
    @Deprecated
    public String alias = "";
    /**
     * Comments
     * @deprecated Raw access
     */
    @Deprecated
    public List<PlotComment> comments = null;
    
    /**
     * The ratings for a plot
     * @deprecated Raw access
     */
    @Deprecated
    public HashMap<UUID, Integer> ratings;
    
    /**
     * Flags
     * @deprecated Raw access
     */
    @Deprecated
    public HashMap<String, Flag> flags;
    /**
     * Home Position
     * @deprecated Raw access
     */
    @Deprecated
    private BlockLoc position;
    
    /**
     * Constructor
     *
     */
    public PlotSettings() {
        flags = new HashMap<>();
    }
    
    /**
     * <b>Check if the plot is merged in a direction</b><br> 0 = North<br> 1 = East<br> 2 = South<br> 3 = West<br>
     *
     * @param direction Direction to check
     *
     * @return boolean merged
     */
    public boolean getMerged(final int direction) {
        return merged[direction];
    }
    
    /**
     * Returns true if the plot is merged (i.e. if it's a mega plot)
     */
    public boolean isMerged() {
        return (merged[0] || merged[1] || merged[2] || merged[3]);
    }
    
    public boolean[] getMerged() {
        return merged;
    }
    
    public void setMerged(final boolean[] merged) {
        this.merged = merged;
    }
    
    public Map<UUID, Integer> getRatings() {
        return ratings == null ? new HashMap<UUID, Integer>() : ratings;
    }

    public boolean setMerged(final int direction, final boolean merged) {
        if (this.merged[direction] != merged) {
            this.merged[direction] = merged;
            return true;
        }
        return false;
    }
    
    public BlockLoc getPosition() {
        if (position == null) {
            return new BlockLoc(0, 0, 0);
        }
        return position;
    }
    
    public void setPosition(BlockLoc position) {
        if (position != null && position.x == 0 && position.y == 0 && position.z == 0) {
            position = null;
        }
        this.position = position;
    }
    
    public String getAlias() {
        return alias;
    }
    
    /**
     * Set the plot alias
     *
     * @param alias alias to be used
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }
    
    public String getJoinMessage(PlotArea area) {
        final Flag greeting = FlagManager.getSettingFlag(area, this, "greeting");
        if (greeting != null) {
            return greeting.getValueString();
        }
        return "";
    }
    
    /**
     * Get the "farewell" flag value
     *
     * @return Farewell flag
     */
    public String getLeaveMessage(PlotArea area) {
        final Flag farewell = FlagManager.getSettingFlag(area, this, "farewell");
        if (farewell != null) {
            return farewell.getValueString();
        }
        return "";
    }
    
    public ArrayList<PlotComment> getComments(final String inbox) {
        final ArrayList<PlotComment> c = new ArrayList<>();
        if (comments == null) {
            return null;
        }
        for (final PlotComment comment : comments) {
            if (comment.inbox.equals(inbox)) {
                c.add(comment);
            }
        }
        return c;
    }
    
    public void setComments(final List<PlotComment> comments) {
        this.comments = comments;
    }
    
    public void removeComment(final PlotComment comment) {
        if (comments.contains(comment)) {
            comments.remove(comment);
        }
    }
    
    public void removeComments(final List<PlotComment> comments) {
        for (final PlotComment comment : comments) {
            removeComment(comment);
        }
    }
    
    public void addComment(final PlotComment comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
    }
}
