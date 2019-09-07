package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.listener.PlayerBlockEventType;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotArea;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class EventUtil {

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

    public abstract boolean callMerge(Plot plot, int dir, int max);

    public abstract boolean callAutoMerge(Plot plot, List<PlotId> plots);

    public abstract boolean callUnlink(PlotArea area, List<PlotId> plots);

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
        if (PlotSquared.get().worldedit != null) {
            if (player.getAttribute("worldedit")) {
                MainUtil.sendMessage(player, Captions.WORLDEDIT_BYPASSED);
            }
        }
        final Plot plot = player.getCurrentPlot();
        if (Settings.Teleport.ON_LOGIN && plot != null && !(plot
            .getArea() instanceof SinglePlotArea)) {
            TaskManager.runTask(() -> plot.teleportPlayer(player));
            MainUtil.sendMessage(player,
                Captions.TELEPORTED_TO_ROAD.f() + " (on-login) " + "(" + plot.getId().x + ";" + plot
                    .getId().y + ")");
        }
    }

    public void doRespawnTask(final PlotPlayer player) {
        final Plot plot = player.getCurrentPlot();
        if (Settings.Teleport.ON_DEATH && plot != null) {
            TaskManager.runTask(() -> plot.teleportPlayer(player));
            MainUtil.sendMessage(player, Captions.TELEPORTED_TO_ROAD);
        }
    }

    public boolean checkPlayerBlockEvent(PlotPlayer player, PlayerBlockEventType type,
        Location location, LazyBlock block, boolean notifyPerms) {
        PlotArea area = location.getPlotArea();
        assert area != null;
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if (plot.isAdded(player.getUUID())) {
                return true;
            }
        }
        switch (type) {
            case TELEPORT_OBJECT:
                return false;
            case EAT:
            case READ:
                return true;
            case BREAK_BLOCK:
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                Optional<HashSet<PlotBlock>> use = plot.getFlag(Flags.USE);
                if (use.isPresent()) {
                    HashSet<PlotBlock> value = use.get();
                    if (PlotBlock.containsEverything(value) || value
                        .contains(block.getPlotBlock())) {
                        return true;
                    }
                }
                Optional<HashSet<PlotBlock>> destroy = plot.getFlag(Flags.BREAK);
                if (destroy.isPresent()) {
                    HashSet<PlotBlock> value = destroy.get();
                    if (PlotBlock.containsEverything(value) || value
                        .contains(block.getPlotBlock())) {
                        return true;
                    }
                }
                if (Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                        false)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil.sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                    Captions.FLAG_USE.getTranslated() + '/' + Captions.FLAG_BREAK.getTranslated()));
            case BREAK_HANGING:
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.HANGING_BREAK).orElse(false)) {
                    return true;
                }
                if (plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(), false)
                        || !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_HANGING_BREAK.getTranslated()));
                }
                return Permissions.hasPermission(player,
                    Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                        notifyPerms);
            case BREAK_MISC:
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.MISC_BREAK).orElse(false)) {
                    return true;
                }
                if (plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(), false)
                        || !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_MISC_BREAK.getTranslated()));
                }
                return Permissions.hasPermission(player,
                    Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                        notifyPerms);
            case BREAK_VEHICLE:
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.VEHICLE_BREAK).orElse(false)) {
                    return true;
                }
                if (plot.hasOwner()) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                            false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_VEHICLE_BREAK.getTranslated()));
                }
                return Permissions.hasPermission(player,
                    Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                        notifyPerms);
            case INTERACT_BLOCK: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value = flagValue.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(), false)
                        || !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_USE.getTranslated()));
                }
                return true;
            }
            case PLACE_BLOCK: {
                if (plot == null) {
                    return Permissions
                        .hasPermission(player, Captions.PERMISSION_ADMIN_BUILD_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_BUILD_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.PLACE);
                HashSet<PlotBlock> value = flagValue.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_BUILD_OTHER.getTranslated(), false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_PLACE.getTranslated()));
                }
                return true;
            }
            case TRIGGER_PHYSICAL: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(), false);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            false);
                }
                if (plot.getFlag(Flags.DEVICE_INTERACT).orElse(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value = flagValue.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                            false)) {
                        return true;
                    }
                    return false;
                }
                return true;
            }
            case INTERACT_HANGING: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.HOSTILE_INTERACT).orElse(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value = flagValue.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                            false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_USE.getTranslated() + '/' + Captions.FLAG_HANGING_INTERACT
                                .getTranslated()));
                }
                return true;
            }
            case INTERACT_MISC: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.MISC_INTERACT).orElse(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value = flag.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                            false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_USE.getTranslated() + '/' + Captions.FLAG_MISC_INTERACT
                                .getTranslated()));
                }
                return true;
            }
            case INTERACT_VEHICLE: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.VEHICLE_USE).orElse(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.USE);
                HashSet<PlotBlock> value = flag.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                            false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_USE.getTranslated() + '/' + Captions.FLAG_VEHICLE_USE
                                .getTranslated()));
                }
                return true;
            }
            case SPAWN_MOB: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.MOB_PLACE).orElse(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flagValue = plot.getFlag(Flags.PLACE);
                HashSet<PlotBlock> value = flagValue.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                            false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_MOB_PLACE.getTranslated() + '/' + Captions.FLAG_PLACE
                                .getTranslated()));
                }
                return true;
            }
            case PLACE_MISC: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.MISC_PLACE).orElse(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.PLACE);
                HashSet<PlotBlock> value = flag.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                            false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_MISC_PLACE.getTranslated() + '/' + Captions.FLAG_PLACE
                                .getTranslated()));
                }

                return true;
            }
            case PLACE_VEHICLE:
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(),
                            notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(),
                            notifyPerms);
                }
                if (plot.getFlag(Flags.VEHICLE_PLACE).orElse(false)) {
                    return true;
                }
                Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.PLACE);
                HashSet<PlotBlock> value = flag.orElse(null);
                if (value == null || !PlotBlock.containsEverything(value) && !value
                    .contains(block.getPlotBlock())) {
                    if (Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                            false)) {
                        return true;
                    }
                    return !(!notifyPerms || MainUtil
                        .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                            Captions.FLAG_VEHICLE_PLACE.getTranslated() + '/' + Captions.FLAG_PLACE
                                .getTranslated()));
                }
                return true;
            default:
                break;
        }
        return true;
    }
}
