package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
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
import com.plotsquared.sponge.events.PlotComponentSetEvent;
import com.plotsquared.sponge.events.PlotDeleteEvent;
import com.plotsquared.sponge.events.PlotFlagAddEvent;
import com.plotsquared.sponge.events.PlotFlagRemoveEvent;
import com.plotsquared.sponge.events.PlotMergeEvent;
import com.plotsquared.sponge.events.PlotRateEvent;
import com.plotsquared.sponge.events.PlotUnlinkEvent;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventManager;

import java.util.ArrayList;
import java.util.UUID;

public class SpongeEventUtil extends EventUtil {
    
    public EventManager events;
    
    public SpongeEventUtil() {
        this.events = SpongeMain.THIS.getGame().getEventManager();
    }

    public boolean callEvent(Event event) {
        return !this.events.post(event);
    }
    
    @Override
    public boolean callClaim(PlotPlayer player, Plot plot, boolean auto) {
        return callEvent(new PlayerClaimPlotEvent(SpongeUtil.getPlayer(player), plot, auto));
    }
    
    @Override
    public boolean callTeleport(PlotPlayer player, Location from, Plot plot) {
        return callEvent(new PlayerTeleportToPlotEvent(SpongeUtil.getPlayer(player), from, plot));
    }

    @Override
    public boolean callComponentSet(Plot plot, String component) {
        return callEvent(new PlotComponentSetEvent(plot, component));
    }
    
    @Override
    public boolean callClear(Plot plot) {
        return callEvent(new PlotClearEvent(plot));
    }
    
    @Override
    public void callDelete(Plot plot) {
        callEvent(new PlotDeleteEvent(plot));
    }
    
    @Override
    public boolean callFlagAdd(Flag flag, Plot plot) {
        return callEvent(new PlotFlagAddEvent(flag, plot));
    }
    
    @Override
    public boolean callFlagRemove(Flag<?> flag, Plot plot, Object value) {
        return callEvent(new PlotFlagRemoveEvent(flag, plot));
    }
    
    @Override
    public boolean callMerge(Plot plot, ArrayList<PlotId> plots) {
        return callEvent(new PlotMergeEvent(SpongeUtil.getWorld(plot.getWorldName()), plot, plots));
    }
    
    @Override
    public boolean callUnlink(PlotArea area, ArrayList<PlotId> plots) {
        return callEvent(new PlotUnlinkEvent(SpongeUtil.getWorld(area.worldname), plots));
    }
    
    @Override
    public void callEntry(PlotPlayer player, Plot plot) {
        callEvent(new PlayerEnterPlotEvent(SpongeUtil.getPlayer(player), plot));
    }
    
    @Override
    public void callLeave(PlotPlayer player, Plot plot) {
        callEvent(new PlayerLeavePlotEvent(SpongeUtil.getPlayer(player), plot));
    }
    
    @Override
    public void callDenied(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotDeniedEvent(SpongeUtil.getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public void callTrusted(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotTrustedEvent(SpongeUtil.getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public void callMember(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotHelperEvent(SpongeUtil.getPlayer(initiator), plot, player, added));
    }
    
    @Override
    public boolean callFlagRemove(Flag flag, Object object, PlotCluster cluster) {
        return callEvent(new ClusterFlagRemoveEvent(flag, cluster));
    }
    
    @Override
    public Rating callRating(PlotPlayer player, Plot plot, Rating rating) {
        PlotRateEvent event = new PlotRateEvent(player, rating, plot);
        this.events.post(event);
        return event.getRating();
    }
    
}
