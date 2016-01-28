package com.plotsquared.bukkit.util;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.intellectualcrafters.plot.util.EventUtil;
import com.plotsquared.bukkit.events.ClusterFlagRemoveEvent;
import com.plotsquared.bukkit.events.PlayerClaimPlotEvent;
import com.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.plotsquared.bukkit.events.PlayerLeavePlotEvent;
import com.plotsquared.bukkit.events.PlayerPlotDeniedEvent;
import com.plotsquared.bukkit.events.PlayerPlotHelperEvent;
import com.plotsquared.bukkit.events.PlayerPlotTrustedEvent;
import com.plotsquared.bukkit.events.PlayerTeleportToPlotEvent;
import com.plotsquared.bukkit.events.PlotClearEvent;
import com.plotsquared.bukkit.events.PlotDeleteEvent;
import com.plotsquared.bukkit.events.PlotFlagAddEvent;
import com.plotsquared.bukkit.events.PlotFlagRemoveEvent;
import com.plotsquared.bukkit.events.PlotMergeEvent;
import com.plotsquared.bukkit.events.PlotRateEvent;
import com.plotsquared.bukkit.events.PlotUnlinkEvent;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class BukkitEventUtil extends EventUtil {
    
    public Player getPlayer(final PlotPlayer player) {
        if (player instanceof BukkitPlayer) {
            return ((BukkitPlayer) player).player;
        }
        return null;
    }
    
    public boolean callEvent(final Event event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event instanceof Cancellable) {
            return !((Cancellable) event).isCancelled();
        }
        return true;
    }
    
    @Override
    public boolean callClaim(final PlotPlayer player, final Plot plot, final boolean auto) {
        return callEvent(new PlayerClaimPlotEvent(getPlayer(player), plot, auto));
    }
    
    @Override
    public boolean callTeleport(final PlotPlayer player, final Location from, final Plot plot) {
        return callEvent(new PlayerTeleportToPlotEvent(getPlayer(player), from, plot));
    }
    
    @Override
    public boolean callClear(final String world, final PlotId id) {
        return callEvent(new PlotClearEvent(world, id));
    }
    
    @Override
    public void callDelete(final String world, final PlotId id) {
        callEvent(new PlotDeleteEvent(world, id));
    }
    
    @Override
    public boolean callFlagAdd(final Flag flag, final Plot plot) {
        return callEvent(new PlotFlagAddEvent(flag, plot));
    }
    
    @Override
    public boolean callFlagRemove(final Flag flag, final Plot plot) {
        return callEvent(new PlotFlagRemoveEvent(flag, plot));
    }
    
    @Override
    public boolean callMerge(final String world, final Plot plot, final ArrayList<PlotId> plots) {
        return callEvent(new PlotMergeEvent(BukkitUtil.getWorld(world), plot, plots));
    }
    
    @Override
    public boolean callUnlink(final String world, final ArrayList<PlotId> plots) {
        return callEvent(new PlotUnlinkEvent(BukkitUtil.getWorld(world), plots));
    }
    
    @Override
    public void callEntry(final PlotPlayer player, final Plot plot) {
        callEvent(new PlayerEnterPlotEvent(getPlayer(player), plot));
    }
    
    @Override
    public void callLeave(final PlotPlayer player, final Plot plot) {
        callEvent(new PlayerLeavePlotEvent(getPlayer(player), plot));
    }
    
    @Override
    public void callDenied(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added) {
        callEvent(new PlayerPlotDeniedEvent(getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public void callTrusted(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added) {
        callEvent(new PlayerPlotHelperEvent(getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public void callMember(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added) {
        callEvent(new PlayerPlotTrustedEvent(getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public boolean callFlagRemove(final Flag flag, final PlotCluster cluster) {
        return callEvent(new ClusterFlagRemoveEvent(flag, cluster));
    }
    
    @Override
    public Rating callRating(final PlotPlayer player, final Plot plot, final Rating rating) {
        final PlotRateEvent event = new PlotRateEvent(player, rating, plot);
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event.getRating();
    }
    
}
