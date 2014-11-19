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
import com.intellectualcrafters.plot.util.PlotHelper;
import com.sun.istack.internal.NotNull;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * plot settings
 *
 * @author Citymonstret
 * @author Empire92
 */
@SuppressWarnings("unused")
public class PlotSettings {
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
    private Set<Flag> flags;
    /**
     * Home Position
     */
    private PlotHomePosition position;
    /**
     * Plot
     */
    private Plot plot;

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
     * <b>Check if the plot is merged in a direction</b><br>
     * 0 = North<br>
     * 1 = East<br>
     * 2 = South<br>
     * 3 = West<br>
     *
     * @param direction Direction to check
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
        return PlotHelper.getPlotBottomLoc(plot.getWorld(), plot.getId()).add(1, 0, 1).getBlock().getBiome();
    }

    /**
     * @param flag to add
     */
    public void addFlag(final Flag flag) {
        final Flag hasFlag = getFlag(flag.getKey());
        if (hasFlag != null) {
            this.flags.remove(hasFlag);
        }
        this.flags.add(flag);
    }

    /**
     * Get all flags applied for the plot
     *
     * @return flags
     */
    public Set<Flag> getFlags() {
        return this.flags;
    }

    /**
     * Set multiple flags
     *
     * @param flags Flag Array
     */
    public void setFlags(@NotNull final Flag[] flags) {
        this.flags = new HashSet<>(Arrays.asList(flags));
    }

    /**
     * Get a flag
     *
     * @param flag Flag to get
     * @return flag
     */
    public Flag getFlag(final String flag) {
        for (final Flag myflag : this.flags) {
            if (myflag.getKey().equals(flag)) {
                return myflag;
            }
        }
        return null;
    }

    public PlotHomePosition getPosition() {
        return this.position;
    }

    public void setPosition(final PlotHomePosition position) {
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
        Flag greeting = getFlag("greeting");
        if (greeting != null) {
            return greeting.getValue();
        }
        return "";
    }

    /**
     * Get the "farewell" flag value
     *
     * @return Farewell flag
     */
    public String getLeaveMessage() {
        Flag farewell = getFlag("farewell");
        if (farewell != null) {
            return farewell.getValue();
        }
        return "";
    }

    public ArrayList<PlotComment> getComments(final int tier) {
        final ArrayList<PlotComment> c = new ArrayList<>();
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

    public void removeComment(PlotComment comment) {
        if (this.comments.contains(comment)) {
            this.comments.remove(comment);
        }
    }

    public void removeComments(ArrayList<PlotComment> comments) {
        for (PlotComment comment : comments) {
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
