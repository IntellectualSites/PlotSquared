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

package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author Citymonstret
 */
public class PWE {

    @SuppressWarnings("deprecation")
    public static void setMask(final Player p, final Location l) {
        try {
            LocalSession s;
            if (PlotMain.worldEdit == null) {
                s = WorldEdit.getInstance().getSession(p.getName());
            } else {
                s = PlotMain.worldEdit.getSession(p);
            }

            final PlotId id = PlayerFunctions.getPlot(l);
            if (id != null) {
                final Plot plot = PlotMain.getPlots(l.getWorld()).get(id);
                if (plot != null) {
                    boolean r;
                    r = ((plot.getOwner() != null) && plot.getOwner().equals(p.getUniqueId())) || plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUniqueId());
                    if (!r) {
                        if (p.hasPermission("plots.worldedit.bypass")) {
                            removeMask(p, s);
                            return;
                        }
                    } else {

                        final World w = p.getWorld();

                        final Location bloc = PlotHelper.getPlotBottomLoc(w, plot.id);
                        final Location tloc = PlotHelper.getPlotTopLoc(w, plot.id);

                        final Vector bvec = new Vector(bloc.getBlockX() + 1, bloc.getBlockY(), bloc.getBlockZ() + 1);
                        final Vector tvec = new Vector(tloc.getBlockX(), tloc.getBlockY(), tloc.getBlockZ());

                        final LocalWorld lw = PlotMain.worldEdit.wrapPlayer(p).getWorld();

                        final CuboidRegion region = new CuboidRegion(lw, bvec, tvec);
                        final RegionMask mask = new RegionMask(region);
                        s.setMask(mask);
                        return;
                    }
                }
            }
            if (noMask(s)) {
                final BukkitPlayer plr = PlotMain.worldEdit.wrapPlayer(p);
                final Vector p1 = new Vector(69, 69, 69), p2 = new Vector(69, 69, 69);
                s.setMask(new RegionMask(new CuboidRegion(plr.getWorld(), p1, p2)));
            }
        } catch (final Exception e) {
            // throw new
            // PlotSquaredException(PlotSquaredException.PlotError.MISSING_DEPENDENCY,
            // "WorldEdit == Null?");
        }
    }

    public static boolean noMask(final LocalSession s) {
        return s.getMask() == null;
    }

    public static void setNoMask(final Player p) {
        try {
            LocalSession s;
            if (PlotMain.worldEdit == null) {
                s = WorldEdit.getInstance().getSession(p.getName());
            } else {
                s = PlotMain.worldEdit.getSession(p);
            }
            final BukkitPlayer plr = PlotMain.worldEdit.wrapPlayer(p);
            final Vector p1 = new Vector(69, 69, 69), p2 = new Vector(69, 69, 69);
            s.setMask(new RegionMask(new CuboidRegion(plr.getWorld(), p1, p2)));
        } catch (final Exception e) {

        }
    }

    public static void removeMask(final Player p, final LocalSession s) {
        final Mask mask = null;
        s.setMask(mask);
    }

    public static void removeMask(final Player p) {
        try {
            LocalSession s;
            if (PlotMain.worldEdit == null) {
                s = WorldEdit.getInstance().getSession(p.getName());
            } else {
                s = PlotMain.worldEdit.getSession(p);
            }
            removeMask(p, s);
        } catch (final Exception e) {
            // throw new
            // PlotSquaredException(PlotSquaredException.PlotError.MISSING_DEPENDENCY,
            // "WorldEdit == Null?");
        }
    }
}
