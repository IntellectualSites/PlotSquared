package com.plotsquared.bukkit.util.bukkit;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.EventUtil;
import com.plotsquared.bukkit.events.*;
import com.plotsquared.bukkit.object.BukkitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.UUID;

public class BukkitEventUtil extends EventUtil {

    public Player getPlayer(PlotPlayer player) {
        return ((BukkitPlayer) player).player;
    }
    
    public boolean callEvent(Event event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event instanceof Cancellable) {
            return !((Cancellable) event).isCancelled();
        }
        return true;
    }
    
    @Override
    public boolean callClaim(PlotPlayer player, Plot plot, boolean auto) {
        return callEvent(new PlayerClaimPlotEvent(getPlayer(player), plot, auto));
    }

    @Override
    public boolean callTeleport(PlotPlayer player, Location from, Plot plot) {
        return callEvent(new PlayerTeleportToPlotEvent(getPlayer(player), from, plot));
    }

    @Override
    public boolean callClear(String world, PlotId id) {
        return callEvent(new PlotClearEvent(world, id));
    }

    @Override
    public void callDelete(String world, PlotId id) {
        callEvent(new PlotDeleteEvent(world, id));
    }

    @Override
    public boolean callFlagAdd(Flag flag, Plot plot) {
        return callEvent(new PlotFlagAddEvent(flag, plot));
    }

    @Override
    public boolean callFlagRemove(Flag flag, Plot plot) {
        return callEvent(new PlotFlagRemoveEvent(flag, plot));
    }

    @Override
    public boolean callMerge(String world, Plot plot, ArrayList<PlotId> plots) {
        return callEvent(new PlotMergeEvent(BukkitUtil.getWorld(world), plot, plots));
    }

    @Override
    public boolean callUnlink(String world, ArrayList<PlotId> plots) {
        return callEvent(new PlotUnlinkEvent(BukkitUtil.getWorld(world), plots));
    }

    @Override
    public void callEntry(PlotPlayer player, Plot plot) {
        callEvent(new PlayerEnterPlotEvent(getPlayer(player), plot));
    }

    @Override
    public void callLeave(PlotPlayer player, Plot plot) {
        callEvent(new PlayerLeavePlotEvent(getPlayer(player), plot));
    }

    @Override
    public void callDenied(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotDeniedEvent(getPlayer(initiator), plot, player, added));
    }

    @Override
    public void callTrusted(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotHelperEvent(getPlayer(initiator), plot, player, added));
    }

    @Override
    public void callMember(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotTrustedEvent(getPlayer(initiator), plot, player, added));
    }

    @Override
    public boolean callFlagRemove(Flag flag, PlotCluster cluster) {
        return callEvent(new ClusterFlagRemoveEvent(flag, cluster));
    }
    
}
