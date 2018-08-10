package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.*;

import java.util.ArrayList;
import java.util.UUID;

public class EventUtilTest extends EventUtil {

    @Override public Rating callRating(PlotPlayer player, Plot plot, Rating rating) {
        return null;
    }

    @Override public boolean callClaim(PlotPlayer player, Plot plot, boolean auto) {
        return false;
    }

    @Override public boolean callTeleport(PlotPlayer player, Location from, Plot plot) {
        return false;
    }

    @Override public boolean callComponentSet(Plot plot, String component) {
        return false;
    }

    @Override public boolean callClear(Plot plot) {
        return false;
    }

    @Override public void callDelete(Plot plot) {
    }

    @Override public boolean callFlagAdd(Flag flag, Plot plot) {
        return true;
    }

    @Override public boolean callFlagRemove(Flag<?> flag, Plot plot, Object value) {
        return true;
    }

    @Override public boolean callFlagRemove(Flag<?> flag, Object value, PlotCluster cluster) {
        return true;
    }

    @Override public boolean callMerge(Plot plot, ArrayList<PlotId> plots) {
        return false;
    }

    @Override public boolean callUnlink(PlotArea area, ArrayList<PlotId> plots) {
        return false;
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
}
