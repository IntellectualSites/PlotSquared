package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.UUID;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class EventUtil {
    
    public static EventUtil manager = null;
    
    public static void unregisterPlayer(PlotPlayer player) {
        String name = player.getName();
        if (SetupUtils.setupMap.containsKey(name)) {
            SetupUtils.setupMap.remove(name);
        }
        CmdConfirm.removePending(name);
        PlotSquared.IMP.unregister(player);
    }
    
    public abstract boolean callClaim(final PlotPlayer player, final Plot plot, final boolean auto);
    
    public abstract boolean callTeleport(final PlotPlayer player, Location from, final Plot plot);
    
    public abstract boolean callClear(final String world, final PlotId id);
    
    public abstract void callDelete(final String world, final PlotId id);
    
    public abstract boolean callFlagAdd(final Flag flag, final Plot plot);
    
    public abstract boolean callFlagRemove(final Flag flag, final Plot plot);
    
    public abstract boolean callFlagRemove(final Flag flag, final PlotCluster cluster);
    
    public abstract boolean callMerge(final String world, final Plot plot, final ArrayList<PlotId> plots);
    
    public abstract boolean callUnlink(final String world, final ArrayList<PlotId> plots);
    
    public abstract void callEntry(final PlotPlayer player, final Plot plot);
    
    public abstract void callLeave(final PlotPlayer player, final Plot plot);
    
    public abstract void callDenied(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added);
    
    public abstract void callTrusted(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added);
    
    public abstract void callMember(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added);
}
