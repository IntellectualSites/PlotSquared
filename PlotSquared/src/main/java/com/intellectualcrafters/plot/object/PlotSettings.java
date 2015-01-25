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
import java.util.Set;

import org.bukkit.block.Biome;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.util.PlotHelper;

/**
 * plot settings
 *
 * @author Citymonstret
 * @author Empire92
 */
@SuppressWarnings("unused") public class PlotSettings {
    /**
     * Plot
     */
    private final Plot plot;
    /**
     * merged plots
     */
    private boolean[] merged = new boolean[]{false, false, false, false};
    /**
     * plot alias
     */
    private String alias;
    /**
     * Comments
     */
    private ArrayList<PlotComment> comments = null;
    /**
     * Flags
     */
    public Set<Flag> flags;
    /**
     * Home Position
     */
    private BlockLoc position;

    /**
     * Constructor
     *
     * @param plot object
     */
    public PlotSettings(final Plot plot) {
        this.alias = "";
        this.plot = plot;
    }

    /**
     * <b>Check if the plot is merged in a direction</b><br> 0 = North<br> 1 = East<br> 2 = South<br> 3 = West<br>
     *
     * @param direction Direction to check
     *
     * @return boolean merged
     */
    public boolean getMerged(final int direction) {
        return this.merged[direction];
    }

    /**
     * Returns true if the plot is merged (i.e. if it's a mega plot)
     */
    public boolean isMerged() {
        return (this.merged[0] || this.merged[1] || this.merged[2] || this.merged[3]);
    }

    public boolean[] getMerged() {
        return this.merged;
    }

    public void setMerged(final boolean[] merged) {
        this.merged = merged;
    }

    public void setMerged(final int direction, final boolean merged) {
        this.merged[direction] = merged;
    }

    /**
     * @return biome at plot loc
     */
    public Biome getBiome() {
        return PlotHelper.getPlotBottomLoc(this.plot.getWorld(), this.plot.getId()).add(1, 0, 1).getBlock().getBiome();
    }

    public BlockLoc getPosition() {
        if (this.position == null) {
            return new BlockLoc(0, 0, 0);
        }
        return this.position;
    }

    public void setPosition(BlockLoc position) {
        this.position = position;
    }

    public String getAlias() {
        return this.alias;
    }

    /**
     * Set the plot alias
     *
     * @param alias alias to be used
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    public String getJoinMessage() {
        final Flag greeting = FlagManager.getPlotFlag(plot, "greeting");
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
    public String getLeaveMessage() {
        final Flag farewell = FlagManager.getPlotFlag(plot, "farewell");
        if (farewell != null) {
            return farewell.getValueString();
        }
        return "";
    }

    public ArrayList<PlotComment> getComments(final int tier) {
        final ArrayList<PlotComment> c = new ArrayList<>();
        if (this.comments == null) {
            return null;
        }
        for (final PlotComment comment : this.comments) {
            if (comment.tier == tier) {
                c.add(comment);
            }
        }
        return c;
    }

    public void setComments(final ArrayList<PlotComment> comments) {
        this.comments = comments;
    }

    public void removeComment(final PlotComment comment) {
        if (this.comments.contains(comment)) {
            this.comments.remove(comment);
        }
    }

    public void removeComments(final ArrayList<PlotComment> comments) {
        for (final PlotComment comment : comments) {
            removeComment(comment);
        }
    }

    public void addComment(final PlotComment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }
}
