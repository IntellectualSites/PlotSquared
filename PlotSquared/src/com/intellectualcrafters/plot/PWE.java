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
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * @author Citymonstret
 */
public class PWE {

	public static void setMask(Player p, Location l) {
		try {
			LocalSession s;
			if (PlotMain.worldEdit == null) {
				s = WorldEdit.getInstance().getSession(p.getName());
			}
			else {
				s = PlotMain.worldEdit.getSession(p);
			}

			PlotId id = PlayerFunctions.getPlot(l);
			if (id != null) {
				Plot plot = PlotMain.getPlots(l.getWorld()).get(id);
				if (plot != null) {
					boolean r;
					r =
							(plot.getOwner() != null) && plot.getOwner().equals(p.getUniqueId())
									|| plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUniqueId());
					if (!r) {
						if (p.hasPermission("plots.worldedit.bypass")) {
							removeMask(p, s);
							return;
						}
					}
					else {

						World w = p.getWorld();

						Location bloc = PlotHelper.getPlotBottomLoc(w, plot.id);
						Location tloc = PlotHelper.getPlotTopLoc(w, plot.id);

						Vector bvec = new Vector(bloc.getBlockX() + 1, bloc.getBlockY() + 1, bloc.getBlockZ() + 1);
						Vector tvec = new Vector(tloc.getBlockX(), tloc.getBlockY(), tloc.getBlockZ());

						LocalWorld lw = PlotMain.worldEdit.wrapPlayer(p).getWorld();

						CuboidRegion region = new CuboidRegion(lw, bvec, tvec);
						com.sk89q.worldedit.masks.RegionMask mask = new com.sk89q.worldedit.masks.RegionMask(region);

						s.setMask(mask);
						return;
					}
				}
			}
			if (noMask(s)) {
				BukkitPlayer plr = PlotMain.worldEdit.wrapPlayer(p);
				Vector p1 = new Vector(69, 69, 69), p2 = new Vector(69, 69, 69);

				s.setMask(new com.sk89q.worldedit.masks.RegionMask(new CuboidRegion(plr.getWorld(), p1, p2)));
			}
		}
		catch (Exception e) {
			// throw new
			// PlotSquaredException(PlotSquaredException.PlotError.MISSING_DEPENDENCY,
			// "WorldEdit == Null?");
		}
	}

	public static boolean noMask(LocalSession s) {
		try {
			com.sk89q.worldedit.masks.Mask mask = s.getMask();
			return mask == null;
		}
		catch (Throwable e) {
			return true;
		}
	}

	public static void removeMask(Player p, LocalSession s) {
		try {
			s.setMask(null);
		}
		catch (Throwable e) {
			com.sk89q.worldedit.masks.Mask mask = null;
			s.setMask(mask);
		}
	}

	public static void removeMask(Player p) {
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
		catch (Exception e) {
			// throw new
			// PlotSquaredException(PlotSquaredException.PlotError.MISSING_DEPENDENCY,
			// "WorldEdit == Null?");
		}
	}
}
