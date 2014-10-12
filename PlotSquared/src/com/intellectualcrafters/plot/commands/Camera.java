package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ca.mera.CameraAPI;
import ca.mera.CameraController;
import ca.mera.events.TravelEndEvent;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;

/**
 * Created by Citymonstret on 2014-08-15.
 */
public class Camera extends SubCommand implements Listener {

	private CameraAPI api;

	public Camera() {
		super("camera", "plots.camera", "Go into camera mode", "camera", "c", CommandCategory.TELEPORT);
		this.api = CameraAPI.getInstance();
		this.travelers = new ArrayList<String>();
	}

	private ArrayList<String> travelers;

	@Override
	public boolean execute(Player player, String... args) {
		if (!PlayerFunctions.isInPlot(player)) {
			PlayerFunctions.sendMessage(player, C.NOT_IN_PLOT);
			return false;
		}
		this.api = CameraAPI.getInstance();
		Plot plot = PlotHelper.getCurrentPlot(player.getLocation());
		if (this.api.isTravelling(player)) {
			this.api.stopTravel(player);
			PlayerFunctions.sendMessage(player, C.CAMERA_STOPPED);
			return true;
		}
		this.api.travel(getController(player, plot));
		PlayerFunctions.sendMessage(player, C.CAMERA_STARTED, plot.getId().x + ";" + plot.getId().y);
		this.travelers.add(player.getName());
		return true;
	}

	@EventHandler
	public void onTravelEnded(TravelEndEvent event) {
		if (this.travelers.contains(event.getPlayer().getName())) {

			this.travelers.remove(event.getPlayer().getName());
			PlayerFunctions.sendMessage(event.getPlayer(), C.CAMERA_STOPPED);
		}
		if (this.travelers.contains(event.getPlayer().getName())) {
			event.getHandlers().bake();
		}
	}

	public CameraController getController(Player player, Plot plot) {
		World w = Bukkit.getWorld(plot.world);
		int seconds = PlotHelper.getPlotWidth(w, plot.id) * 5;
		Location loc1, loc2, loc3, loc4, loc5;
		double y = player.getLocation().getY();
		Location bottomLoc = PlotHelper.getPlotBottomLoc(w, plot.id);
		Location topLoc = PlotHelper.getPlotTopLoc(w, plot.id);
		World world = bottomLoc.getWorld();
		int maxX = Math.max(bottomLoc.getBlockX(), topLoc.getBlockX());
		int maxZ = Math.max(bottomLoc.getBlockZ(), topLoc.getBlockZ());
		int minX = Math.min(bottomLoc.getBlockX(), topLoc.getBlockX());
		int minZ = Math.min(bottomLoc.getBlockZ(), topLoc.getBlockZ());
		loc1 = new Location(world, maxX, y, maxZ);
		loc2 = new Location(world, maxX, y, minZ);
		loc3 = new Location(world, minX, y, minZ);
		loc4 = new Location(world, minX, y, maxZ);
		loc1.setYaw(((3 / 4.0F) * 360.0F) - 0.5F);
		loc3.setYaw(((1 / 4.0F) * 360.0F) - 0.5F);
		loc4.setYaw(((2 / 4.0F) * 360.0F) - 0.5F);
		loc2.setYaw(((0 / 4.0F) * 360.0F) - 0.5F);
		loc5 = loc1.clone();
		CameraController controller = this.api.createController(player, seconds, loc1, loc2, loc3, loc4, loc5);
		return controller;
	}
}
