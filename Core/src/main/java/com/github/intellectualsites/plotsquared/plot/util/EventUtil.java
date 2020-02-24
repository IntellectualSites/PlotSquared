package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DeviceInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MiscPlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MobPlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.UseFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VehiclePlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.BlockTypeWrapper;
import com.github.intellectualsites.plotsquared.plot.listener.PlayerBlockEventType;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.Rating;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotArea;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

    public abstract boolean callFlagAdd(PlotFlag<?, ?> flag, Plot plot);

    public abstract boolean callFlagRemove(PlotFlag<?, ?> flag, Plot plot, Object value);

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
                CaptionUtility.format(player, Captions.TELEPORTED_TO_ROAD.getTranslated()) + " (on-login) " + "(" + plot.getId().x + ";" + plot
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

    public boolean checkPlayerBlockEvent(PlotPlayer player, @NotNull PlayerBlockEventType type,
        Location location, BlockType blockType, boolean notifyPerms) {
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
            case READ:
                return true;
            case INTERACT_BLOCK: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(), notifyPerms);
                }
                final List<BlockTypeWrapper> use = plot.getFlag(UseFlag.class);
                for (final BlockTypeWrapper blockTypeWrapper : use) {
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper.accepts(blockType)) {
                        return true;
                    }
                }
                return Permissions.hasPermission(player,
                    Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(), false) || !(
                    !notifyPerms || MainUtil.sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                        Captions.FLAG_USE.getTranslated()));
            }
            case TRIGGER_PHYSICAL: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(), false);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(), false);
                }
                if (plot.getFlag(DeviceInteractFlag.class)) {
                    return true;
                }
                List<BlockTypeWrapper> use = plot.getFlag(UseFlag.class);
                for (final BlockTypeWrapper blockTypeWrapper : use) {
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper.accepts(blockType)) {
                        return true;
                    }
                }
                return Permissions.hasPermission(player,
                    Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(), false);
            }
            case SPAWN_MOB: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(), notifyPerms);
                }
                if (plot.getFlag(MobPlaceFlag.class)) {
                    return true;
                }
                List<BlockTypeWrapper> place = plot.getFlag(PlaceFlag.class);
                for (final BlockTypeWrapper blockTypeWrapper : place) {
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper.accepts(blockType)) {
                        return true;
                    }
                }
                if (Permissions.hasPermission(player,
                    Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(), false)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil
                    .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                        Captions.FLAG_MOB_PLACE.getTranslated() + '/' + Captions.FLAG_PLACE
                            .getTranslated()));
            }
            case PLACE_MISC: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(), notifyPerms);
                }
                if (plot.getFlag(MiscPlaceFlag.class)) {
                    return true;
                }
                List<BlockTypeWrapper> place = plot.getFlag(PlaceFlag.class);
                for (final BlockTypeWrapper blockTypeWrapper : place) {
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper.accepts(blockType)) {
                        return true;
                    }
                }
                if (Permissions.hasPermission(player,
                    Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(), false)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil
                    .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
                        Captions.FLAG_MISC_PLACE.getTranslated() + '/' + Captions.FLAG_PLACE
                            .getTranslated()));
            }
            case PLACE_VEHICLE:
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_ROAD.getTranslated(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Captions.PERMISSION_ADMIN_INTERACT_UNOWNED.getTranslated(), notifyPerms);
                }
                return plot.getFlag(VehiclePlaceFlag.class);
            default:
                break;
        }
        return true;
    }
}
