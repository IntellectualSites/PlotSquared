package com.github.intellectualsites.plotsquared.nukkit.util;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import com.github.intellectualsites.plotsquared.nukkit.NukkitMain;
import com.github.intellectualsites.plotsquared.nukkit.events.*;
import com.github.intellectualsites.plotsquared.nukkit.object.NukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;

import java.util.ArrayList;
import java.util.UUID;

public class NukkitEventUtil extends EventUtil {

    private final NukkitMain plugin;

    public NukkitEventUtil(NukkitMain plugin) {
        this.plugin = plugin;
    }

    public Player getPlayer(PlotPlayer player) {
        if (player instanceof NukkitPlayer) {
            return ((NukkitPlayer) player).player;
        }
        return null;
    }

    public boolean callEvent(Event event) {
        plugin.getServer().getPluginManager().callEvent(event);
        return !(event instanceof Cancellable) || !event.isCancelled();
    }

    @Override public boolean callClaim(PlotPlayer player, Plot plot, boolean auto) {
        return callEvent(new PlayerClaimPlotEvent(getPlayer(player), plot, auto));
    }

    @Override public boolean callTeleport(PlotPlayer player, Location from, Plot plot) {
        return callEvent(new PlayerTeleportToPlotEvent(getPlayer(player), from, plot));
    }

    @Override public boolean callComponentSet(Plot plot, String component) {
        return callEvent(new PlotComponentSetEvent(plot, component));
    }

    @Override public boolean callClear(Plot plot) {
        return callEvent(new PlotClearEvent(plot));
    }

    @Override public void callDelete(Plot plot) {
        callEvent(new PlotDeleteEvent(plot));
    }

    @Override public boolean callFlagAdd(Flag flag, Plot plot) {
        return callEvent(new PlotFlagAddEvent(flag, plot));
    }

    @Override public boolean callFlagRemove(Flag<?> flag, Plot plot, Object value) {
        return callEvent(new PlotFlagRemoveEvent(flag, plot));
    }

    @Override public boolean callMerge(Plot plot, ArrayList<PlotId> plots) {
        return callEvent(new PlotMergeEvent(NukkitUtil.getWorld(plot.getWorldName()), plot, plots));
    }

    @Override public boolean callUnlink(PlotArea area, ArrayList<PlotId> plots) {
        return callEvent(new PlotUnlinkEvent(NukkitUtil.getWorld(area.worldname), area, plots));
    }

    @Override public void callEntry(PlotPlayer player, Plot plot) {
        callEvent(new PlayerEnterPlotEvent(getPlayer(player), plot));
    }

    @Override public void callLeave(PlotPlayer player, Plot plot) {
        callEvent(new PlayerLeavePlotEvent(getPlayer(player), plot));
    }

    @Override public void callDenied(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotDeniedEvent(getPlayer(initiator), plot, player, added));
    }

    @Override public void callTrusted(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotTrustedEvent(getPlayer(initiator), plot, player, added));
    }

    @Override public void callMember(PlotPlayer initiator, Plot plot, UUID player, boolean added) {
        callEvent(new PlayerPlotHelperEvent(getPlayer(initiator), plot, player, added));
    }

    @Override public boolean callFlagRemove(Flag flag, Object object, PlotCluster cluster) {
        return callEvent(new ClusterFlagRemoveEvent(flag, cluster));
    }

    @Override public Rating callRating(PlotPlayer player, Plot plot, Rating rating) {
        PlotRateEvent event = new PlotRateEvent(player, rating, plot);
        plugin.getServer().getPluginManager().callEvent(event);
        return event.getRating();
    }

}
