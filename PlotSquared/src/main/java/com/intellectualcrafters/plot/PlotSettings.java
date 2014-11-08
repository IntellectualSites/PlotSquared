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

package com.intellectualcrafters.plot;

import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * plot settings
 *
 * @author Citymonstret
 */
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
     * plot biome
     */
    private Biome biome;

    private ArrayList<PlotComment> comments = null;
    /**
     *
     */
    private Set<Flag> flags;

    private PlotHomePosition position;

    /**
     * Constructor
     *
     * @param plot
     */
    public PlotSettings(final Plot plot) {
        this.alias = "";
    }

    /**
     * <b>Check if the plot is merged in a direction</b><br>
     * 0 = North<br>
     * 1 = East<br>
     * 2 = South<br>
     * 3 = West<br>
     *
     * @param direction
     * @return boolean
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
     * @param b
     */
    public void setBiome(final Biome b) {
        this.biome = b;
    }

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    public Biome getBiome() {
        return this.biome;
    }

    /**
     * @param alias
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * @param flag
     */
    public void addFlag(final Flag flag) {
        final Flag hasFlag = getFlag(flag.getKey());
        if (hasFlag != null) {
            this.flags.remove(hasFlag);
        }
        this.flags.add(flag);
    }

    /**
     * @param flags
     */
    public void setFlags(final Flag[] flags) {
        this.flags = new HashSet<Flag>(Arrays.asList(flags));
    }

    /**
     * @return
     */
    public Set<Flag> getFlags() {
        return this.flags;
    }

    /**
     * @param flag
     * @return
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

    public String getJoinMessage() {
        return "";
    }

    public String getLeaveMessage() {
        return "";
    }

    public ArrayList<PlotComment> getComments(final int tier) {
        final ArrayList<PlotComment> c = new ArrayList<PlotComment>();
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
            this.comments = new ArrayList<PlotComment>();
        }
        this.comments.add(comment);
    }
}
