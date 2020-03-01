package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.events.Result;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.Rating;

import java.util.List;
import java.util.UUID;

public class EventDispatcherTest extends EventDispatcher {

    @Override public Rating callRating(PlotPlayer player, Plot plot, Rating rating) {
        return null;
    }

    @Override public Result callClaim(PlotPlayer player, Plot plot, boolean auto) {
        return Result.DENY;
    }

    @Override public Result callTeleport(PlotPlayer player, Location from, Plot plot) {
        return Result.DENY;
    }

    @Override public Result callComponentSet(Plot plot, String component) {
        return Result.DENY;
    }

    @Override public Result callClear(Plot plot) {
        return Result.DENY;
    }

    @Override public Result callDelete(Plot plot) {
        return Result.DENY;
    }

    @Override public Result callFlagAdd(PlotFlag<?, ?> flag, Plot plot) {
        return Result.ACCEPT;
    }

    @Override public Result callFlagRemove(PlotFlag<?, ?> flag, Plot plot, Object value) {
        return Result.ACCEPT;
    }

    @Override public Result callMerge(Plot plot, int dir, int max) {
        return Result.DENY;
    }

    @Override public Result callAutoMerge(Plot plot, List<PlotId> plots) {
        return Result.DENY;
    }

    @Override public Result callUnlink(PlotArea area, List<PlotId> plots, Plot plot) {
        return Result.DENY;
    }

    @Override public void callEntry(PlotPlayer player, Plot plot) {
    }

    @Override public void callLeave(PlotPlayer player, Plot plot) {
    }

    @Override public void callDenied(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
    }

    @Override public void callTrusted(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
    }

    @Override public void callMember(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
    }

    @Override
    public Result callOwnerChange(PlotPlayer initiator, Plot plot, UUID newOwner, UUID oldOwner,
        boolean hasOldOwner) {
        return Result.DENY;
    }
}
