package com.intellectualcrafters.plot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.masks.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;


/**
 * 
 * @author Citymonstret
 *
 */
public class PWE {
	
	public static void setMask(Player p, Location l) {
	    LocalSession s = PlotMain.worldEdit.getSession(p);
	    Plot plot = PlayerFunctions.getCurrentPlot(p);
		if (plot!=null) {
		    boolean r;
		    if (plot.getOwner()!=null)
		        r = plot.getOwner().equals(p.getUniqueId());
		    else
		        r = false;
		    if (!r) {
		        if (p.hasPermission("plots.we.member") && plot.hasRights(p))
		            r = true;
		        else if (p.hasPermission("plots.we.bypass")) {
		            s.setMask(null);
		            return;
		        }
		    }
		    if (r) {
		        World w = p.getWorld();
		        Location b = PlotHelper.getPlotBottomLoc(w, plot.id);
		        Location t = PlotHelper.getPlotTopLoc(w, plot.id);
		        Vector p1 = new Vector(b.getBlockX(),b.getBlockY(),b.getBlockZ());
                Vector p2 = new Vector(t.getBlockX(),t.getBlockY(),t.getBlockZ());
                LocalWorld world = PlotMain.worldEdit.wrapPlayer(p).getWorld();
                CuboidRegion cr = new CuboidRegion(world, p1, p2);
                RegionMask rm = new RegionMask(cr);
                s.setMask(rm);
                return;
		    }
		}
		if(s.getMask() == null) {
            BukkitPlayer plr = PlotMain.worldEdit.wrapPlayer(p);
            LocalWorld world = plr.getWorld();
            Vector p1 = new Vector(0,0,0), p2 = new Vector(0,0,0);
            s.setMask(new RegionMask(new CuboidRegion(world, p1, p2)));
        }
	}
	
	public static void removeMask(Player p) {
		LocalSession s = PlotMain.worldEdit.getSession(p);
		s.setMask(null);
	}
}
