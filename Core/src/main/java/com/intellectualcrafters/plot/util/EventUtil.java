package com.intellectualcrafters.plot.util;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.LazyBlock;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.intellectualcrafters.plot.object.worlds.SinglePlotArea;
import com.intellectualcrafters.plot.util.expiry.ExpireManager;
import com.plotsquared.listener.PlayerBlockEventType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import static com.plotsquared.listener.PlayerBlockEventType.*;

@SuppressWarnings("Guava") public abstract class EventUtil {

    public static EventUtil manager = null;

    /**
     * Submit a plot rate event and return the updated rating
     *
     * @param player Player that rated the plot
     * @param plot   Plot that was rated
     * @param rating Rating given to the plot
     * @return Updated rating or null if the event was cancelled
     */
    @Nullable public abstract Rating callRating(PlotPlayer player, Plot plot, Rating rating);

    public abstract boolean callClaim(PlotPlayer player, Plot plot, boolean auto);

    public abstract boolean callTeleport(PlotPlayer player, Location from, Plot plot);

    public abstract boolean callComponentSet(Plot plot, String component);

    public abstract boolean callClear(Plot plot);

    public abstract boolean callDelete(Plot plot);

    public abstract boolean callFlagAdd(Flag flag, Plot plot);

    public abstract boolean callFlagRemove(Flag<?> flag, Plot plot, Object value);

    public abstract boolean callFlagRemove(Flag<?> flag, Object value, PlotCluster cluster);

    public abstract boolean callMerge(Plot plot, int dir, int max);

    public abstract boolean callAutoMerge(Plot plot, ArrayList<PlotId> plots);

    public abstract boolean callUnlink(PlotArea area, ArrayList<PlotId> plots);

    public abstract void callEntry(PlotPlayer player, Plot plot);

    public abstract void callLeave(PlotPlayer player, Plot plot);

    public abstract void callDenied(PlotPlayer initiator, Plot plot, UUID player, boolean added);

    public abstract void callTrusted(PlotPlayer initiator, Plot plot, UUID player, boolean added);

    public abstract void callMember(PlotPlayer initiator, Plot plot, UUID player, boolean added);

    public abstract boolean callOwnerChange(PlotPlayer initiator, Plot plot, UUID newOwner,
        UUID oldOwner, boolean hasOldOwner);

    public void doJoinTask(final PlotPlayer player) {
        if (player == null) {
            return; //possible future warning message to figure out where we are retrieving null
        }
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.handleJoin(player);
        }
        if (PS.get().worldedit != null) {
            if (player.getAttribute("worldedit")) {
                MainUtil.sendMessage(player, C.WORLDEDIT_BYPASSED);
            }
        }
        final Plot plot = player.getCurrentPlot();
        if (Settings.Teleport.ON_LOGIN && plot != null && !(plot.getArea() instanceof SinglePlotArea)) {
            TaskManager.runTask(() -> plot.teleportPlayer(player));
            MainUtil.sendMessage(player,
                C.TELEPORTED_TO_ROAD.f() + " (on-login) " + "(" + plot.getId().x + ";" + plot
                    .getId().y + ")");
        }
    }

    public void doRespawnTask(final PlotPlayer player) {
        final Plot plot = player.getCurrentPlot();
        if (Settings.Teleport.ON_DEATH && plot != null) {
            TaskManager.runTask(() -> plot.teleportPlayer(player));
            MainUtil.sendMessage(player, C.TELEPORTED_TO_ROAD);
        }
    }

    public boolean checkPlayerBlockEvent(PlotPlayer player, PlayerBlockEventType type,
        Location location, LazyBlock block, boolean notifyPerms) {
        if (type == EAT || type == READ) {
            return true;
        }
        if (type == TELEPORT_OBJECT) {
            return false;
        }
        PlotArea area = player.getPlotAreaAbs();
        assert area != null;
        Plot plot = location.getPlot();
        if (plot != null) {
            if (plot.isAdded(player.getUUID())) {
                return true;
            }
            if (!plot.hasOwner()) {
                return Permissions
                    .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), notifyPerms);
            }
        } else {
            return Permissions
                .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), notifyPerms);

        }
        switch (type) {
            case BREAK_BLOCK:
                Optional<HashSet<PlotBlock>> use = plot.getFlag(Flags.USE);
                if (use.isPresent()) {
                    HashSet<PlotBlock> value = use.get();
                    if (value.contains(PlotBlock.EVERYTHING) || value
                        .contains(block.getPlotBlock())) {
                        return true;
                    }
                }
                Optional<HashSet<PlotBlock>> destroy = plot.getFlag(Flags.BREAK);
                if (destroy.isPresent()) {
                    HashSet<PlotBlock> value = destroy.get();
                    if (value.contains(PlotBlock.EVERYTHING) || value
                        .contains(block.getPlotBlock())) {
                        return true;
                    }
                }
                if (Permissions
                    .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil.sendMessage(player, C.FLAG_TUTORIAL_USAGE,
                    C.FLAG_USE.s() + '/' + C.FLAG_BREAK.s()));
            case BREAK_HANGING:
                if (plot.getFlag(Flags.HANGING_BREAK).or(false)) {
                    return true;
                }
                return
                    Permissions.hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)
                        || !(!notifyPerms || MainUtil
                        .sendMessage(player, C.FLAG_TUTORIAL_USAGE, C.FLAG_HANGING_BREAK.s()));
            case BREAK_MISC:
                if (plot.getFlag(Flags.MISC_BREAK).or(false)) {
                    return true;
                }
                return
                    Permissions.hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)
                        || !(!notifyPerms || MainUtil
                        .sendMessage(player, C.FLAG_TUTORIAL_USAGE, C.FLAG_MISC_BREAK.s()));
            case BREAK_VEHICLE:
                if (plot.getFlag(Flags.VEHICLE_BREAK).or(false)) {
                    return true;
                }
                if (Permissions
                    .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil
                    .sendMessage(player, C.FLAG_TUTORIAL_USAGE, C.FLAG_VEHICLE_BREAK.s()));
            case INTERACT_BLOCK: {
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value;
                if (flagValue.isPresent()) {
                    value = flagValue.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    return Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false) || !(
                        !notifyPerms || MainUtil
                            .sendMessage(player, C.FLAG_TUTORIAL_USAGE, C.FLAG_USE.s()));
                }
                return true;
            }
            case PLACE_BLOCK: {
                if (!plot.hasOwner()) {
                    return Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_BUILD_UNOWNED.s(), notifyPerms);
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.PLACE);
                HashSet<PlotBlock> value;
                if (flagValue.isPresent()) {
                    value = flagValue.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_BUILD_OTHER.s(), false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, C.FLAG_TUTORIAL_USAGE, C.FLAG_PLACE.s()));
                }
                return true;
            }
            case TRIGGER_PHYSICAL: {
                if (plot.getFlag(Flags.DEVICE_INTERACT).or(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value;
                if (flagValue.isPresent()) {
                    value = flagValue.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                        return true;
                    }
                    return false;
                }
                return true;
            }
            case INTERACT_HANGING: {
                if (plot.getFlag(Flags.HOSTILE_INTERACT).or(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value;
                if (flagValue.isPresent()) {
                    value = flagValue.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(player, C.FLAG_TUTORIAL_USAGE,
                        C.FLAG_USE.s() + '/' + C.FLAG_HANGING_INTERACT.s()));
                }
                return true;
            }
            case INTERACT_MISC: {
                if (plot.getFlag(Flags.MISC_INTERACT).or(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value;
                if (flag.isPresent()) {
                    value = flag.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(player, C.FLAG_TUTORIAL_USAGE,
                        C.FLAG_USE.s() + '/' + C.FLAG_MISC_INTERACT.s()));
                }
                return true;
            }
            case INTERACT_VEHICLE: {
                if (plot.getFlag(Flags.VEHICLE_USE).or(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value;
                if (flag.isPresent()) {
                    value = flag.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(player, C.FLAG_TUTORIAL_USAGE,
                        C.FLAG_USE.s() + '/' + C.FLAG_VEHICLE_USE.s()));
                }
                return true;
            }
            case SPAWN_MOB: {
                if (plot.getFlag(Flags.MOB_PLACE).or(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.PLACE);
                HashSet<PlotBlock> value;
                if (flagValue.isPresent()) {
                    value = flagValue.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(player, C.FLAG_TUTORIAL_USAGE,
                        C.FLAG_MOB_PLACE.s() + '/' + C.FLAG_PLACE.s()));
                }
                return true;
            }
            case PLACE_MISC: {
                if (plot.getFlag(Flags.MISC_PLACE).or(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.PLACE);
                HashSet<PlotBlock> value;
                if (flag.isPresent()) {
                    value = flag.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(player, C.FLAG_TUTORIAL_USAGE,
                        C.FLAG_MISC_PLACE.s() + '/' + C.FLAG_PLACE.s()));
                }

                return true;
            }
            case PLACE_VEHICLE:
                if (plot.getFlag(Flags.VEHICLE_PLACE).or(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.PLACE);
                HashSet<PlotBlock> value;
                if (flag.isPresent()) {
                    value = flag.get();
                } else {
                    value = null;
                }
                if (value == null || !value.contains(PlotBlock.EVERYTHING) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions
                        .hasPermission(player, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil.sendMessage(player, C.FLAG_TUTORIAL_USAGE,
                        C.FLAG_VEHICLE_PLACE.s() + '/' + C.FLAG_PLACE.s()));
                }
                return true;
            default:
                break;
        }
        return true;
    }
}
