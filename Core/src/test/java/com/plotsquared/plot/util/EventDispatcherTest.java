package com.plotsquared.plot.util;

import com.plotsquared.events.PlayerAutoPlotEvent;
import com.plotsquared.events.PlayerClaimPlotEvent;
import com.plotsquared.events.PlayerEnterPlotEvent;
import com.plotsquared.events.PlayerLeavePlotEvent;
import com.plotsquared.events.PlayerPlotDeniedEvent;
import com.plotsquared.events.PlayerPlotHelperEvent;
import com.plotsquared.events.PlayerPlotTrustedEvent;
import com.plotsquared.events.PlayerTeleportToPlotEvent;
import com.plotsquared.events.PlotAutoMergeEvent;
import com.plotsquared.events.PlotChangeOwnerEvent;
import com.plotsquared.events.PlotClearEvent;
import com.plotsquared.events.PlotComponentSetEvent;
import com.plotsquared.events.PlotDeleteEvent;
import com.plotsquared.events.PlotDoneEvent;
import com.plotsquared.events.PlotFlagAddEvent;
import com.plotsquared.events.PlotFlagRemoveEvent;
import com.plotsquared.events.PlotMergeEvent;
import com.plotsquared.events.PlotRateEvent;
import com.plotsquared.events.PlotUnlinkEvent;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.plot.flags.PlotFlag;
import com.plotsquared.location.Direction;
import com.plotsquared.location.Location;
import com.plotsquared.plot.Plot;
import com.plotsquared.plot.PlotArea;
import com.plotsquared.plot.PlotId;
import com.plotsquared.plot.Rating;
import com.plotsquared.util.EventDispatcher;
import com.sk89q.worldedit.function.pattern.Pattern;

import java.util.List;
import java.util.UUID;

public class EventDispatcherTest extends EventDispatcher {

    @Override public PlotRateEvent callRating(PlotPlayer player, Plot plot, Rating rating) {
        return null;
    }

    @Override
    public PlayerClaimPlotEvent callClaim(PlotPlayer player, Plot plot, String schematic) {
        return null;
    }

    @Override
    public PlayerAutoPlotEvent callAuto(PlotPlayer player, PlotArea area, String schematic,
        int size_x, int size_z) {
        return null;
    }

    @Override
    public PlayerTeleportToPlotEvent callTeleport(PlotPlayer player, Location from, Plot plot) {
        return null;
    }

    @Override
    public PlotComponentSetEvent callComponentSet(Plot plot, String component, Pattern pattern) {
        return null;
    }

    @Override public PlotClearEvent callClear(Plot plot) {
        return null;
    }

    @Override public PlotDeleteEvent callDelete(Plot plot) {
        return null;
    }

    @Override public PlotFlagAddEvent callFlagAdd(PlotFlag<?, ?> flag, Plot plot) {
        return null;
    }

    @Override public PlotFlagRemoveEvent callFlagRemove(PlotFlag<?, ?> flag, Plot plot) {
        return null;
    }

    @Override
    public PlotMergeEvent callMerge(Plot plot, Direction dir, int max, PlotPlayer player) {
        return null;
    }

    @Override public PlotAutoMergeEvent callAutoMerge(Plot plot, List<PlotId> plots) {
        return null;
    }

    @Override public PlotUnlinkEvent callUnlink(PlotArea area, Plot plot, boolean createRoad,
        boolean createSign, PlotUnlinkEvent.REASON reason) {
        return null;
    }

    @Override public PlayerEnterPlotEvent callEntry(PlotPlayer player, Plot plot) {
        return null;
    }

    @Override public PlayerLeavePlotEvent callLeave(PlotPlayer player, Plot plot) {
        return null;
    }

    @Override public PlayerPlotDeniedEvent callDenied(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        return null;
    }

    @Override
    public PlayerPlotTrustedEvent callTrusted(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        return null;
    }

    @Override public PlayerPlotHelperEvent callMember(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        return null;
    }

    @Override
    public PlotChangeOwnerEvent callOwnerChange(PlotPlayer initiator, Plot plot, UUID newOwner,
        UUID oldOwner, boolean hasOldOwner) {
        return null;
    }

    @Override public PlotDoneEvent callDone(Plot plot) {
        return null;
    }
}
