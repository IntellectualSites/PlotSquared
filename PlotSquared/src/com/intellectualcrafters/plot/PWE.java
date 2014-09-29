package com.intellectualcrafters.plot;

import com.sk89q.worldedit.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.masks.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * 
 * @author Citymonstret
 * 
 */
public class PWE {

    @SuppressWarnings("unused")
    public static void setMask(Player p, Location l) {
        try {
            LocalSession s;
            if (PlotMain.worldEdit == null) {
                s = WorldEdit.getInstance().getSession(p.getName());
            } else {
                s = PlotMain.worldEdit.getSession(p);
            }
            Plot plot = PlayerFunctions.getCurrentPlot(p);
            if (plot != null) {
                boolean r;
                r = plot.getOwner() != null && plot.getOwner().equals(p.getUniqueId());
                if (!r) {
                    if (p.hasPermission("plots.we.member") && plot.hasRights(p)) {
                        r = true;
                    } else if (p.hasPermission("plots.we.bypass")) {
                        s.setMask(null);
                        return;
                    }
                }
                if (r) {
                    World w = p.getWorld();
                    Location b = PlotHelper.getPlotBottomLoc(w, plot.id);
                    Location t = PlotHelper.getPlotTopLoc(w, plot.id);
                    Vector p1 = new Vector(b.getBlockX(), b.getBlockY(), b.getBlockZ());
                    Vector p2 = new Vector(t.getBlockX(), t.getBlockY(), t.getBlockZ());
                    LocalWorld world = PlotMain.worldEdit.wrapPlayer(p).getWorld();
                    CuboidRegion cr = new CuboidRegion(world, p1, p2);
                    RegionMask rm = new RegionMask(cr);
                    s.setMask(rm);
                    return;
                }
            }
            if (s.getMask() == null) {
                BukkitPlayer plr = PlotMain.worldEdit.wrapPlayer(p);
                LocalWorld world = plr.getWorld();
                Vector p1 = new Vector(0, 0, 0), p2 = new Vector(0, 0, 0);
                s.setMask(new RegionMask(new CuboidRegion(world, p1, p2)));
            }
        } catch(Exception e) {
            throw new PlotSquaredException(PlotSquaredException.PlotError.MISSING_DEPENDENCY, "WorldEdit == Null?");
        }
    }

    public static void removeMask(Player p) {
        try {
            LocalSession s;
            if(PlotMain.worldEdit == null) {
                s = WorldEdit.getInstance().getSession(p.getName());
            } else {
                s = PlotMain.worldEdit.getSession(p);
            }
            s.setMask(null);
        } catch(Exception e) {
            throw new PlotSquaredException(PlotSquaredException.PlotError.MISSING_DEPENDENCY, "WorldEdit == Null?");
        }
    }
}
