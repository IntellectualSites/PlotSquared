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
package com.intellectualcrafters.plot.util.bukkit;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * @author Citymonstret
 * @author Empire92
 */
public class PWE {
    public static void setMask(final PlotPlayer p, final Location l, final boolean force) {
        try {
            LocalSession s = WorldEdit.getInstance().getSession(p.getName());
            if (!PlotSquared.isPlotWorld(l.getWorld())) {
                removeMask(p);
            }
            final PlotId id = MainUtil.getPlotId(l);
            if (id != null) {
                final Plot plot = PlotSquared.getPlots(l.getWorld()).get(id);
                if (plot != null) {
                    if (FlagManager.isPlotFlagTrue(plot, "no-worldedit")) {
                        return;
                    }
                    final boolean r = ((plot.getOwner() != null) && plot.getOwner().equals(p.getUUID())) || plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUUID());
                    if (r) {
                        final String w = l.getWorld();
                        final Location bloc = MainUtil.getPlotBottomLoc(w, plot.id);
                        final Location tloc = MainUtil.getPlotTopLoc(w, plot.id);
                        final Vector bvec = new Vector(bloc.getX() + 1, bloc.getY(), bloc.getZ() + 1);
                        final Vector tvec = new Vector(tloc.getX(), tloc.getY(), tloc.getZ());
                        
                        
                        // FIXME unchecked casting 
                        final LocalWorld lw = PlotSquared.worldEdit.wrapPlayer(((BukkitPlayer) p).player).getWorld();
                        final CuboidRegion region = new CuboidRegion(lw, bvec, tvec);
                        final RegionMask mask = new RegionMask(region);
                        s.setMask(mask);
                        return;
                    }
                }
            }
            if (force ^ (noMask(s) && !p.hasPermission("plots.worldedit.bypass"))) {
                // FIXME unchecked casting 
                final com.sk89q.worldedit.bukkit.BukkitPlayer plr = PlotSquared.worldEdit.wrapPlayer(((BukkitPlayer) p).player);
                final Vector p1 = new Vector(69, 69, 69), p2 = new Vector(69, 69, 69);
                s.setMask(new RegionMask(new CuboidRegion(plr.getWorld(), p1, p2)));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean hasMask(final PlotPlayer p) {
        LocalSession s = WorldEdit.getInstance().getSession(p.getName());
        return !noMask(s);
    }
    
    public static boolean noMask(final LocalSession s) {
        return s.getMask() == null;
    }
    
    @SuppressWarnings("deprecation")
    public static void setNoMask(final PlotPlayer p) {
        try {
            LocalSession s = WorldEdit.getInstance().getSession(p.getName());
            final com.sk89q.worldedit.bukkit.BukkitPlayer plr = PlotSquared.worldEdit.wrapPlayer(((BukkitPlayer) p).player);
            final Vector p1 = new Vector(69, 69, 69), p2 = new Vector(69, 69, 69);
            s.setMask(new RegionMask(new CuboidRegion(plr.getWorld(), p1, p2)));
        } catch (final Exception e) {
            //
        }
    }
    
    public static void removeMask(final PlotPlayer p, final LocalSession s) {
        final Mask mask = null;
        s.setMask(mask);
    }
    
    public static void removeMask(final PlotPlayer p) {
        try {
            LocalSession s = WorldEdit.getInstance().getSession(p.getName());
            removeMask(p, s);
        } catch (final Exception e) {
            // throw new
            // PlotSquaredException(PlotSquaredException.PlotError.MISSING_DEPENDENCY,
            // "WorldEdit == Null?");
        }
    }
}
