package com.intellectualcrafters.plot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.database.DBFunc;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;

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
            }
            else {
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
                    }
                    else {

                        final World w = p.getWorld();

                        final Location bloc = PlotHelper.getPlotBottomLoc(w, plot.id);
                        final Location tloc = PlotHelper.getPlotTopLoc(w, plot.id);

                        final Vector bvec = new Vector(bloc.getBlockX() + 1, bloc.getBlockY() + 1, bloc.getBlockZ() + 1);
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
        }
        catch (final Exception e) {
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
            }
            else {
                s = PlotMain.worldEdit.getSession(p);
            }
            final BukkitPlayer plr = PlotMain.worldEdit.wrapPlayer(p);
            final Vector p1 = new Vector(69, 69, 69), p2 = new Vector(69, 69, 69);
            s.setMask(new RegionMask(new CuboidRegion(plr.getWorld(), p1, p2)));
        }
        catch (final Exception e) {

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
            }
            else {
                s = PlotMain.worldEdit.getSession(p);
            }
            removeMask(p, s);
        }
        catch (final Exception e) {
            // throw new
            // PlotSquaredException(PlotSquaredException.PlotError.MISSING_DEPENDENCY,
            // "WorldEdit == Null?");
        }
    }
}
