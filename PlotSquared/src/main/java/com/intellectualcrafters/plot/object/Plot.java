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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.util.PlotHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * The plot class
 *
 * @author Citymonstret
 * @author Empire92
 */
@SuppressWarnings("javadoc")
public class Plot implements Cloneable {

    /**
     * plot ID
     */
    public PlotId id;
    /**
     * plot world
     */
    public String world;
    /**
     * plot owner
     */
    public UUID owner;
    /**
     * Deny Entry
     */
    public boolean deny_entry;
    /**
     * List of helpers (with plot permissions)
     */
    public ArrayList<UUID> helpers;
    /**
     * List of trusted users (with plot permissions)
     */
    public ArrayList<UUID> trusted;
    /**
     * List of denied players
     */
    public ArrayList<UUID> denied;
    /**
     * External settings class
     */
    public PlotSettings settings;
    /**
     * Delete on next save cycle?
     */
    public boolean delete;
    /**
     * Has the plot changed since the last save cycle?
     */
    public boolean hasChanged = false;
    public boolean countsTowardsMax = true;

    /**
     * Primary constructor
     *
     * @param id
     * @param owner
     * @param plotBiome
     * @param helpers
     * @param denied
     *
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("unused")
    public Plot(final PlotId id, final UUID owner, final Biome plotBiome, final ArrayList<UUID> helpers, final ArrayList<UUID> denied, final String world) {
        this.id = id;
        this.settings = new PlotSettings(this);
        this.owner = owner;
        this.deny_entry = this.owner == null;
        this.helpers = helpers;
        this.denied = denied;
        this.trusted = new ArrayList<>();
        this.settings.setAlias("");
        this.settings.setPosition(PlotHomePosition.DEFAULT);
        this.delete = false;
        this.settings.setFlags(new Flag[0]);
        this.world = world;
    }

    /**
     * Primary constructor
     *
     * @param id
     * @param owner
     * @param helpers
     * @param denied
     */
    public Plot(final PlotId id, final UUID owner, final ArrayList<UUID> helpers, final ArrayList<UUID> denied, final String world) {
        this.id = id;
        this.settings = new PlotSettings(this);
        this.owner = owner;
        this.deny_entry = this.owner == null;
        this.helpers = helpers;
        this.denied = denied;
        this.trusted = new ArrayList<>();
        this.settings.setAlias("");
        this.settings.setPosition(PlotHomePosition.DEFAULT);
        this.delete = false;
        this.settings.setFlags(new Flag[0]);
        this.world = world;
    }

    /**
     * Constructor for saved plots
     *
     * @param id
     * @param owner
     * @param plotBiome
     * @param helpers
     * @param denied
     * @param merged
     *
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("unused")
    public Plot(final PlotId id, final UUID owner, final Biome plotBiome, final ArrayList<UUID> helpers, final ArrayList<UUID> trusted, final ArrayList<UUID> denied, final String alias, final PlotHomePosition position, final Flag[] flags, final String world, final boolean[] merged) {
        this.id = id;
        this.settings = new PlotSettings(this);
        this.owner = owner;
        this.deny_entry = this.owner != null;
        this.trusted = trusted;
        this.helpers = helpers;
        this.denied = denied;
        this.settings.setAlias(alias);
        this.settings.setPosition(position);
        this.settings.setMerged(merged);
        this.delete = false;
        if (flags != null) {
            this.settings.setFlags(flags);
        } else {
            this.settings.setFlags(new Flag[0]);
        }
        this.world = world;
    }

    /**
     * Constructor for saved plots
     *
     * @param id
     * @param owner
     * @param helpers
     * @param denied
     * @param merged
     */
    public Plot(final PlotId id, final UUID owner, final ArrayList<UUID> helpers, final ArrayList<UUID> trusted, final ArrayList<UUID> denied, final String alias, final PlotHomePosition position, final Flag[] flags, final String world, final boolean[] merged) {
        this.id = id;
        this.settings = new PlotSettings(this);
        this.owner = owner;
        this.deny_entry = this.owner != null;
        this.trusted = trusted;
        this.helpers = helpers;
        this.denied = denied;
        this.settings.setAlias(alias);
        this.settings.setPosition(position);
        this.settings.setMerged(merged);
        this.delete = false;
        if (flags != null) {
            this.settings.setFlags(flags);
        } else {
            this.settings.setFlags(new Flag[0]);
        }
        this.world = world;
    }

    /**
     * Check if the plot has a set owner
     *
     * @return false if there is no owner
     */
    public boolean hasOwner() {
        return this.owner != null;
    }

    /**
     * Check if the player is either the owner or on the helpers list
     *
     * @param player
     * @return true if the player is added as a helper or is the owner
     */
    public boolean hasRights(final Player player) {
        return PlotMain.hasPermission(player, "plots.admin") || ((this.helpers != null) && this.helpers.contains(DBFunc.everyone)) || ((this.helpers != null) && this.helpers.contains(player.getUniqueId())) || ((this.owner != null) && this.owner.equals(player.getUniqueId())) || ((this.owner != null) && (this.trusted != null) && (Bukkit.getPlayer(this.owner) != null) && (this.trusted.contains(player.getUniqueId()) || this.trusted.contains(DBFunc.everyone)));
    }

    /**
     * Should the player be allowed to enter?
     *
     * @param player
     * @return false if the player is allowed to enter
     */
    public boolean deny_entry(final Player player) {
        return (this.denied != null) && ((this.denied.contains(DBFunc.everyone) && !this.hasRights(player)) || (!this.hasRights(player) && this.denied.contains(player.getUniqueId())));
    }

    /**
     * Get the UUID of the owner
     */
    public UUID getOwner() {
        return this.owner;
    }

    /**
     * Set the owner
     *
     * @param player
     */
    public void setOwner(final Player player) {
        this.owner = player.getUniqueId();
    }

    /**
     * Get the plot ID
     */
    public PlotId getId() {
        return this.id;
    }

    /**
     * Get the plot World
     */
    public World getWorld() {
        return Bukkit.getWorld(this.world);
    }

    /**
     * Get a clone of the plot
     *
     * @return
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Plot p = (Plot) super.clone();
        if (!p.equals(this) || p != this) {
            return new Plot(id, owner, helpers, trusted, denied, settings.getAlias(), settings.getPosition(), settings.getFlags().toArray(new Flag[settings.getFlags().size()]), getWorld().getName(), settings.getMerged());
        }
        return p;
    }

    /**
     * Deny someone (use DBFunc.addDenied() as well)
     *
     * @param uuid
     */
    public void addDenied(final UUID uuid) {
        this.denied.add(uuid);
    }

    /**
     * Add someone as a helper (use DBFunc as well)
     *
     * @param uuid
     */
    public void addHelper(final UUID uuid) {
        this.helpers.add(uuid);
    }

    /**
     * Add someone as a trusted user (use DBFunc as well)
     *
     * @param uuid
     */
    public void addTrusted(final UUID uuid) {
        this.trusted.add(uuid);
    }

    /**
     * Get plot display name
     *
     * @return alias if set, else id
     */
    public String getDisplayName() {
        if (this.settings.getAlias().length() > 1) {
            return this.settings.getAlias();
        }
        return this.getId().x + ";" + this.getId().y;
    }

    /**
     * Remove a denied player (use DBFunc as well)
     *
     * @param uuid
     */
    public void removeDenied(final UUID uuid) {
        this.denied.remove(uuid);
    }

    /**
     * Remove a helper (use DBFunc as well)
     *
     * @param uuid
     */
    public void removeHelper(final UUID uuid) {
        this.helpers.remove(uuid);
    }

    /**
     * Remove a trusted user (use DBFunc as well)
     *
     * @param uuid
     */
    public void removeTrusted(final UUID uuid) {
        this.trusted.remove(uuid);
    }

    /**
     * Clear a plot
     *
     * @param plr initiator
     */
    public void clear(final Player plr) {
        PlotHelper.clear(plr, this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Plot other = (Plot) obj;
        return ((this.id.x.equals(other.id.x)) && (this.id.y.equals(other.id.y)) && (this.world.equals(other.world)));
    }

    /**
     * Get the plot hashcode
     *
     * @return integer. You can easily make this a character array <br>
     * xI = c[0]
     * x = c[1 -> xI...]
     * yI = c[xI ... + 1]
     * y = c[xI ... + 2 -> yI ...]
     */
    @Override
    public int hashCode() {
        int x = id.x;
        int y = id.y;
        if (x >= 0) {
            if (y >= 0) {
                return x * x + 3 * x + 2 * x * y + y + y * y;
            } else {
                int y1 = -y;
                return x * x + 3 * x + 2 * x * y1 + y1 + y1 * y1 + 1;
            }
        } else {
            int x1 = -x;
            if (y >= 0) {
                return -(x1 * x1 + 3 * x1 + 2 * x1 * y + y + y * y);
            } else {
                int y1 = -y;
                return -(x1 * x1 + 3 * x1 + 2 * x1 * y1 + y1 + y1 * y1 + 1);
            }
        }
    }
}
