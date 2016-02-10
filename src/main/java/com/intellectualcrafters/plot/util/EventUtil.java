package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.LazyBlock;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.plotsquared.bukkit.listeners.PlayerBlockEventType;

public abstract class EventUtil {
    
    public static EventUtil manager = null;
    
    public abstract Rating callRating(final PlotPlayer player, final Plot plot, final Rating rating);
    
    public abstract boolean callClaim(final PlotPlayer player, final Plot plot, final boolean auto);
    
    public abstract boolean callTeleport(final PlotPlayer player, final Location from, final Plot plot);
    
    public abstract boolean callClear(Plot plot);
    
    public abstract void callDelete(Plot plot);
    
    public abstract boolean callFlagAdd(final Flag flag, final Plot plot);
    
    public abstract boolean callFlagRemove(final Flag flag, final Plot plot);
    
    public abstract boolean callFlagRemove(final Flag flag, final PlotCluster cluster);
    
    public abstract boolean callMerge(final Plot plot, final ArrayList<PlotId> plots);
    
    public abstract boolean callUnlink(final PlotArea area, final ArrayList<PlotId> plots);
    
    public abstract void callEntry(final PlotPlayer player, final Plot plot);
    
    public abstract void callLeave(final PlotPlayer player, final Plot plot);
    
    public abstract void callDenied(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added);
    
    public abstract void callTrusted(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added);
    
    public abstract void callMember(final PlotPlayer initiator, final Plot plot, final UUID player, final boolean added);
    
    public boolean checkPlayerBlockEvent(final PlotPlayer pp, final PlayerBlockEventType type, final Location loc, final LazyBlock block, boolean notifyPerms) {
        PlotArea area = PS.get().getPlotAreaAbs(loc);
        Plot plot = area != null ? area.getPlot(loc) : null;
        if (plot == null) {
            if (area == null) {
                return true;
            }
        } else if (plot.isAdded(pp.getUUID())) {
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
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                final Flag use = FlagManager.getPlotFlagRaw(plot, "use");
                if (use != null) {
                    final HashSet<PlotBlock> value = (HashSet<PlotBlock>) use.getValue();
                    if (value.contains(PlotBlock.EVERYTHING) || value.contains(block.getPlotBlock())) {
                        return true;
                    }
                }
                final Flag destroy = FlagManager.getPlotFlagRaw(plot, "break");
                if (destroy != null) {
                    final HashSet<PlotBlock> value = (HashSet<PlotBlock>) destroy.getValue();
                    if (value.contains(PlotBlock.EVERYTHING) || value.contains(block.getPlotBlock())) {
                        return true;
                    }
                }
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_BREAK.s()));
            }
            case BREAK_HANGING:
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "hanging-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_HANGING_BREAK.s()));
                }
                return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
            case BREAK_MISC:
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "misc-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_MISC_BREAK.s()));
                }
                return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
            case BREAK_VEHICLE:
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "vehicle-break")) {
                    return true;
                }
                if (plot.hasOwner()) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_VEHICLE_BREAK.s()));
                }
                return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
            case INTERACT_BLOCK: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s()));
                }
                return true;
            }
            case PLACE_BLOCK: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED.s(), notifyPerms);
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "place");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_PLACE.s()));
                }
                return true;
            }
            case TRIGGER_PHYSICAL: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), false);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), false);
                }
                if (FlagManager.isPlotFlagTrue(plot, "device-interact")) {
                    return true;
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                        return true;
                    }
                    // TODO: fix the commented dead code
                    return true; //!(!false || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_DEVICE_INTERACT.s()));
                }
                return true;
            }
            case INTERACT_HANGING: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "hanging-interact")) {
                    return true;
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_HANGING_INTERACT.s()));
                }
                return true;
            }
            case INTERACT_MISC: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "misc-interact")) {
                    return true;
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_MISC_INTERACT.s()));
                }
                return true;
            }
            case INTERACT_VEHICLE: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                if (FlagManager.isPlotFlagTrue(plot, "vehicle-use")) {
                    return true;
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s() + "/" + C.FLAG_VEHICLE_USE.s()));
                }
                return true;
            }
            case SPAWN_MOB: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                
                if (FlagManager.isPlotFlagTrue(plot, "mob-place")) {
                    return true;
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "place");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_MOB_PLACE.s() + "/" + C.FLAG_PLACE.s()));
                }
                return true;
            }
            case PLACE_HANGING: {
                //                if (plot == null) {
                //                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms);
                //                }
                //                if (plot.owner == null) {
                //                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                //                }
                //
                //                if (FlagManager.isPlotFlagTrue(plot, "hanging-place")) {
                //                    return true;
                //                }
                //                Flag flag = FlagManager.getPlotFlag(plot, "place");
                //                HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                //                if (value == null || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                //                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms);
                //                }
                return true;
            }
            case PLACE_MISC: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                
                if (FlagManager.isPlotFlagTrue(plot, "misc-place")) {
                    return true;
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "place");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_MISC_PLACE.s() + "/" + C.FLAG_PLACE.s()));
                }
                return true;
            }
            case PLACE_VEHICLE: {
                if (plot == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms);
                }
                if (plot.owner == null) {
                    return Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
                }
                
                if (FlagManager.isPlotFlagTrue(plot, "vehicle-place")) {
                    return true;
                }
                final Flag flag = FlagManager.getPlotFlagRaw(plot, "place");
                final HashSet<PlotBlock> value = flag == null ? null : (HashSet<PlotBlock>) flag.getValue();
                if ((value == null) || (!value.contains(PlotBlock.EVERYTHING) && !value.contains(block.getPlotBlock()))) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), notifyPerms)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(pp, C.FLAG_TUTORIAL_USAGE, C.FLAG_VEHICLE_PLACE.s() + "/" + C.FLAG_PLACE.s()));
                }
                return true;
            }
            default:
                break;
        }
        return true;
    }
}
