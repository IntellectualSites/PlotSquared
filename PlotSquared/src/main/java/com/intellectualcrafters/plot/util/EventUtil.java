package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.FlagValue.PlotBlockListValue;
import com.intellectualcrafters.plot.listeners.PlayerBlockEventType;
import com.intellectualcrafters.plot.object.LazyBlock;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
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
        PS.get().IMP.unregister(player);
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
    
    public boolean checkPlayerBlockEvent(PlotPlayer pp, PlayerBlockEventType type, Location loc, LazyBlock block, boolean notifyPerms) {
        Plot plot = MainUtil.getPlot(loc);
        UUID uuid = pp.getUUID();
        if (plot == null) {
            if (!MainUtil.isPlotAreaAbs(loc)) {
                return true;
            }
        }
        else if (plot.isAdded(uuid)) {
            return true;
        }
        switch (type) {
            case TELEPORT_OBJECT: {
                return false;
            }
            case EAT:
            case READ: {
                return true;
            }
            case BREAK_BLOCK: {
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.BREAK_ROAD.s, notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.BREAK_UNOWNED.s, notifyPerms);
                }
                Flag flag = FlagManager.getPlotFlag(plot, "break");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    return Perm.hasPermission(pp, Perm.BREAK_OTHER.s, notifyPerms);
                }
            }
            case BREAK_HANGING:
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.BREAK_ROAD.s, notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "hanging-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.BREAK_OTHER.s, notifyPerms);
                }
                return Perm.hasPermission(pp, Perm.BREAK_UNOWNED.s, notifyPerms);
            case BREAK_MISC:
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.BREAK_ROAD.s, notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "misc-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.BREAK_OTHER.s, notifyPerms);
                }
                return Perm.hasPermission(pp, Perm.BREAK_UNOWNED.s, notifyPerms);
            case BREAK_VEHICLE:
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.BREAK_ROAD.s, notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "vehicle-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.BREAK_OTHER.s, notifyPerms);
                }
                return Perm.hasPermission(pp, Perm.BREAK_UNOWNED.s, notifyPerms);
            case INTERACT_BLOCK: {
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.INTERACT_ROAD.s, notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.INTERACT_UNOWNED.s, notifyPerms);
                }
                Flag flag = FlagManager.getPlotFlag(plot, "use");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    return Perm.hasPermission(pp, Perm.INTERACT_OTHER.s, notifyPerms);
                }
            }
            case PLACE_BLOCK: {
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.BUILD_ROAD.s, notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.BUILD_UNOWNED.s, notifyPerms);
                }
                Flag flag = FlagManager.getPlotFlag(plot, "place");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    return Perm.hasPermission(pp, Perm.BUILD_OTHER.s, notifyPerms);
                }
            }
            case TRIGGER_PHYSICAL: {
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.INTERACT_ROAD.s, false);
                }
                if (!plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.INTERACT_UNOWNED.s, false);
                }
                if (FlagManager.isPlotFlagTrue(plot, "device-interact")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlag(plot, "use");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    return Perm.hasPermission(pp, Perm.INTERACT_OTHER.s, notifyPerms);
                }
            }
            case INTERACT_HANGING: {
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.INTERACT_ROAD.s, notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.INTERACT_UNOWNED.s, notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "hanging-interact")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlag(plot, "use");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    return Perm.hasPermission(pp, Perm.INTERACT_OTHER.s, notifyPerms);
                }
            }
            case INTERACT_MISC: {
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.INTERACT_ROAD.s, notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.INTERACT_UNOWNED.s, notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "misc-interact")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlag(plot, "use");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    return Perm.hasPermission(pp, Perm.INTERACT_OTHER.s, notifyPerms);
                }
            }
            case INTERACT_VEHICLE: {
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.INTERACT_ROAD.s, notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.INTERACT_UNOWNED.s, notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "vehicle-use")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlag(plot, "use");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    return Perm.hasPermission(pp, Perm.INTERACT_OTHER.s, notifyPerms);
                }
            }
            case SPAWN_MOB: {
                if (plot == null) {
                    return Perm.hasPermission(pp, Perm.INTERACT_OTHER.s, notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Perm.hasPermission(pp, Perm.INTERACT_UNOWNED.s, notifyPerms);
                }
                
                if (FlagManager.isPlotFlagTrue(plot, "mob-place")) {
                    return true;
                }
                Flag flag = FlagManager.getPlotFlag(plot, "place");
                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    return Perm.hasPermission(pp, Perm.INTERACT_OTHER.s, notifyPerms);
                }
            }
            case PLACE_HANGING:
                break;
            case PLACE_MISC:
                break;
            case PLACE_VEHICLE:
                break;
            default:
                break;
        }
        return true;
    }
}
