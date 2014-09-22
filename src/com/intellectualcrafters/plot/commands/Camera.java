package com.intellectualcrafters.plot.commands;

import ca.mera.CameraAPI;
import ca.mera.CameraController;
import ca.mera.events.TravelEndEvent;
import com.intellectualcrafters.plot.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

/**
 * Created by Citymonstret on 2014-08-15.
 */
public class Camera extends SubCommand implements Listener {

    private CameraAPI api;
    public Camera() {
        super("camera", "plots.camera", "Go into camera mode", "camera", "c", CommandCategory.TELEPORT);
        api = CameraAPI.getInstance();
        travelers = new ArrayList<String>();
    }

    private ArrayList<String> travelers;

    @Override
    public boolean execute(Player player, String ... args) {
        if(!PlayerFunctions.isInPlot(player)) {
            PlayerFunctions.sendMessage(player, C.NOT_IN_PLOT);
            return false;
        }
        api = CameraAPI.getInstance();
        Plot plot = PlotHelper.getCurrentPlot(player.getLocation());
        if(api.isTravelling(player)) {
            api.stopTravel(player);
            PlayerFunctions.sendMessage(player, C.CAMERA_STOPPED);
            return true;
        }
        api.travel(getController(player, plot));
        PlayerFunctions.sendMessage(player, C.CAMERA_STARTED, plot.getId().x+";"+plot.getId().y);
        travelers.add(player.getName());
        return true;
    }

    @EventHandler
    public void onTravelEnded(TravelEndEvent event) {
        if(travelers.contains(event.getPlayer().getName())) {

            travelers.remove(event.getPlayer().getName());
            PlayerFunctions.sendMessage(event.getPlayer(), C.CAMERA_STOPPED);
        }
        if(travelers.contains(event.getPlayer().getName())) {
            event.getHandlers().bake();
        }
    }

    public CameraController getController(Player player, Plot plot) {
        World w = Bukkit.getWorld(plot.world);
        PlotWorld plotworld = PlotMain.getWorldSettings(w);
        int seconds = plotworld.PLOT_WIDTH * 5;
        Location loc1, loc2, loc3, loc4, loc5;
        double y = player.getLocation().getY();
        Location bottomLoc = PlotHelper.getPlotBottomLoc(w, plot.id);
        Location topLoc =    PlotHelper.getPlotTopLoc(w, plot.id   );
        World world = bottomLoc.getWorld();
        int maxX = Math.max(bottomLoc.getBlockX(), topLoc.getBlockX());
        int maxZ = Math.max(bottomLoc.getBlockZ(), topLoc.getBlockZ());
        int minX = Math.min(bottomLoc.getBlockX(), topLoc.getBlockX());
        int minZ = Math.min(bottomLoc.getBlockZ(), topLoc.getBlockZ());
        loc1 = new Location(world, maxX, y, maxZ);
        loc2 = new Location(world, maxX, y, minZ);
        loc3 = new Location(world, minX, y, minZ);
        loc4 = new Location(world, minX, y, maxZ);
        loc1.setYaw((3 / 4.0F * 360.0F) - 0.5F);
        loc3.setYaw((1 / 4.0F * 360.0F) - 0.5F);
        loc4.setYaw((2 / 4.0F * 360.0F) - 0.5F);
        loc2.setYaw((0 / 4.0F * 360.0F) - 0.5F);
        loc5 = loc1.clone();
        CameraController controller = api.createController(player, seconds, loc1, loc2, loc3, loc4, loc5);
        return controller;
    }
}
