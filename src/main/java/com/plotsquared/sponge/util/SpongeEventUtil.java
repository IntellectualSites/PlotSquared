package com.plotsquared.sponge.util;

import java.util.ArrayList;
import java.util.UUID;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventManager;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.intellectualcrafters.plot.util.EventUtil;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.events.ClusterFlagRemoveEvent;
import com.plotsquared.sponge.events.PlayerClaimPlotEvent;
import com.plotsquared.sponge.events.PlayerEnterPlotEvent;
import com.plotsquared.sponge.events.PlayerLeavePlotEvent;
import com.plotsquared.sponge.events.PlayerPlotDeniedEvent;
import com.plotsquared.sponge.events.PlayerPlotHelperEvent;
import com.plotsquared.sponge.events.PlayerPlotTrustedEvent;
import com.plotsquared.sponge.events.PlayerTeleportToPlotEvent;
import com.plotsquared.sponge.events.PlotClearEvent;
import com.plotsquared.sponge.events.PlotDeleteEvent;
import com.plotsquared.sponge.events.PlotFlagAddEvent;
import com.plotsquared.sponge.events.PlotFlagRemoveEvent;
import com.plotsquared.sponge.events.PlotMergeEvent;
import com.plotsquared.sponge.events.PlotRateEvent;
import com.plotsquared.sponge.events.PlotUnlinkEvent;

public class SpongeEventUtil extends EventUtil {
    
    public EventManager events;
    
    public SpongeEventUtil() {
        events = SpongeMain.THIS.getGame().getEventManager();
    }
    
    public boolean callEvent(final Event event) {
        return !events.post(event);
    }
    
    @Override
    public boolean callClaim(final PlotPlayer player, final Plot plot, final boolean auto) {
        return callEvent(new PlayerClaimPlotEvent(SpongeUtil.getPlayer(player), plot, auto));
    }
    
    @Override
    public boolean callTeleport(final PlotPlayer player, final Location from, final Plot plot) {
        return callEvent(new PlayerTeleportToPlotEvent(SpongeUtil.getPlayer(player), from, plot));
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
        return callEvent(new PlotMergeEvent(SpongeUtil.getWorld(world), plot, plots));
    }
    
    @Override
    public boolean callUnlink(final String world, final ArrayList<PlotId> plots) {
        return callEvent(new PlotUnlinkEvent(SpongeUtil.getWorld(world), plots));
    }
    
    @Override
    public void callEntry(final PlotPlayer player, final Plot plot) {
        callEvent(new PlayerEnterPlotEvent(SpongeUtil.getPlayer(player), plot));
    }
    
    @Override
    public void callLeave(final PlotPlayer player, final Plot plot) {
        callEvent(new PlayerLeavePlotEvent(SpongeUtil.getPlayer(player), plot));
    }
    
    @Override
    public void callDenied(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added) {
        callEvent(new PlayerPlotDeniedEvent(SpongeUtil.getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public void callTrusted(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added) {
        callEvent(new PlayerPlotHelperEvent(SpongeUtil.getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public void callMember(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added) {
        callEvent(new PlayerPlotTrustedEvent(SpongeUtil.getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public boolean callFlagRemove(final Flag flag, final PlotCluster cluster) {
        return callEvent(new ClusterFlagRemoveEvent(flag, cluster));
    }
    
    @Override
    public Rating callRating(final PlotPlayer player, final Plot plot, final Rating rating) {
        final PlotRateEvent event = new PlotRateEvent(player, rating, plot);
        events.post(event);
        return event.getRating();
    }
    
}
