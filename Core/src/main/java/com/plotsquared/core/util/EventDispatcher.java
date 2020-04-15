package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.events.PlayerAutoPlotEvent;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlayerEnterPlotEvent;
import com.plotsquared.core.events.PlayerLeavePlotEvent;
import com.plotsquared.core.events.PlayerPlotDeniedEvent;
import com.plotsquared.core.events.PlayerPlotHelperEvent;
import com.plotsquared.core.events.PlayerPlotTrustedEvent;
import com.plotsquared.core.events.PlayerTeleportToPlotEvent;
import com.plotsquared.core.events.PlotAutoMergeEvent;
import com.plotsquared.core.events.PlotChangeOwnerEvent;
import com.plotsquared.core.events.PlotClearEvent;
import com.plotsquared.core.events.PlotComponentSetEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.events.PlotDoneEvent;
import com.plotsquared.core.events.PlotEvent;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.PlotRateEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.config.CaptionUtility;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.config.Settings;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DeviceInteractFlag;
import com.plotsquared.core.plot.flag.implementations.MiscPlaceFlag;
import com.plotsquared.core.plot.flag.implementations.MobPlaceFlag;
import com.plotsquared.core.plot.flag.implementations.PlaceFlag;
import com.plotsquared.core.plot.flag.implementations.UseFlag;
import com.plotsquared.core.plot.flag.implementations.VehiclePlaceFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import com.plotsquared.core.listener.PlayerBlockEventType;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.Rating;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.util.task.TaskManager;
import com.google.common.eventbus.EventBus;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventDispatcher {

    private EventBus eventBus = new EventBus("PlotSquaredEvents");

    private List<Object> listeners = new ArrayList<>();

    public void registerListener(Object listener) {
        eventBus.register(listener);
        listeners.add(listener);
    }

    public void unregisterListener(Object listener) {
        eventBus.unregister(listener);
        listeners.remove(listener);
    }

    public void unregisterAll() {
        for (Object listener : listeners) {
            eventBus.unregister(listener);
        }
    }

    public void callEvent(@NotNull final PlotEvent event) {
        eventBus.post(event);
    }

    public PlayerClaimPlotEvent callClaim(PlotPlayer player, Plot plot, String schematic) {
        PlayerClaimPlotEvent event = new PlayerClaimPlotEvent(player, plot, schematic);
        callEvent(event);
        return event;
    }

    public PlayerAutoPlotEvent callAuto(PlotPlayer player, PlotArea area, String schematic,
        int size_x, int size_z) {
        PlayerAutoPlotEvent event =
            new PlayerAutoPlotEvent(player, area, schematic, size_x, size_z);
        callEvent(event);
        return event;
    }

    public PlayerTeleportToPlotEvent callTeleport(PlotPlayer player, Location from, Plot plot) {
        PlayerTeleportToPlotEvent event = new PlayerTeleportToPlotEvent(player, from, plot);
        callEvent(event);
        return event;
    }

    public PlotComponentSetEvent callComponentSet(Plot plot, String component, Pattern pattern) {
        PlotComponentSetEvent event = new PlotComponentSetEvent(plot, component, pattern);
        callEvent(event);
        return event;
    }

    public PlotClearEvent callClear(Plot plot) {
        PlotClearEvent event = new PlotClearEvent(plot);
        callEvent(event);
        return event;
    }

    public PlotDeleteEvent callDelete(Plot plot) {
        PlotDeleteEvent event = new PlotDeleteEvent(plot);
        callEvent(event);
        return event;
    }

    public PlotFlagAddEvent callFlagAdd(PlotFlag<?, ?> flag, Plot plot) {
        PlotFlagAddEvent event = new PlotFlagAddEvent(flag, plot);
        callEvent(event);
        return event;
    }

    public PlotFlagRemoveEvent callFlagRemove(PlotFlag<?, ?> flag, Plot plot) {
        PlotFlagRemoveEvent event = new PlotFlagRemoveEvent(flag, plot);
        callEvent(event);
        return event;
    }

    public PlotMergeEvent callMerge(Plot plot, Direction dir, int max, PlotPlayer player) {
        PlotMergeEvent event = new PlotMergeEvent(plot.getWorldName(), plot, dir, max, player);
        callEvent(event);
        return event;
    }

    public PlotAutoMergeEvent callAutoMerge(Plot plot, List<PlotId> plots) {
        PlotAutoMergeEvent event = new PlotAutoMergeEvent(plot.getWorldName(), plot, plots);
        callEvent(event);
        return event;
    }

    public PlotUnlinkEvent callUnlink(PlotArea area, Plot plot, boolean createRoad,
        boolean createSign, PlotUnlinkEvent.REASON reason) {
        PlotUnlinkEvent event = new PlotUnlinkEvent(area, plot, createRoad, createSign, reason);
        callEvent(event);
        return event;
    }

    public PlayerEnterPlotEvent callEntry(PlotPlayer player, Plot plot) {
        PlayerEnterPlotEvent event = new PlayerEnterPlotEvent(player, plot);
        callEvent(event);
        return event;
    }

    public PlayerLeavePlotEvent callLeave(PlotPlayer player, Plot plot) {
        PlayerLeavePlotEvent event = new PlayerLeavePlotEvent(player, plot);
        callEvent(event);
        return event;
    }

    public PlayerPlotDeniedEvent callDenied(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        PlayerPlotDeniedEvent event = new PlayerPlotDeniedEvent(initiator, plot, player, added);
        callEvent(event);
        return event;
    }

    public PlayerPlotTrustedEvent callTrusted(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        PlayerPlotTrustedEvent event = new PlayerPlotTrustedEvent(initiator, plot, player, added);
        callEvent(event);
        return event;
    }

    public PlayerPlotHelperEvent callMember(PlotPlayer initiator, Plot plot, UUID player,
        boolean added) {
        PlayerPlotHelperEvent event = new PlayerPlotHelperEvent(initiator, plot, player, added);
        callEvent(event);
        return event;
    }

    public PlotChangeOwnerEvent callOwnerChange(PlotPlayer initiator, Plot plot, UUID oldOwner,
        UUID newOwner, boolean hasOldOwner) {
        PlotChangeOwnerEvent event =
            new PlotChangeOwnerEvent(initiator, plot, oldOwner, newOwner, hasOldOwner);
        callEvent(event);
        return event;
    }

    public PlotRateEvent callRating(PlotPlayer player, Plot plot, Rating rating) {
        PlotRateEvent event = new PlotRateEvent(player, rating, plot);
        eventBus.post(event);
        return event;
    }

    public PlotDoneEvent callDone(Plot plot) {
        PlotDoneEvent event = new PlotDoneEvent(plot);
        callEvent(event);
        return event;
    }

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
            TaskManager.runTask(() -> plot.teleportPlayer(player, result -> {}));
            MainUtil.sendMessage(player,
                CaptionUtility.format(player, Captions.TELEPORTED_TO_ROAD.getTranslated())
                    + " (on-login) " + "(" + plot.getId().x + ";" + plot.getId().y + ")");
        }
    }

    public void doRespawnTask(final PlotPlayer player) {
        final Plot plot = player.getCurrentPlot();
        if (Settings.Teleport.ON_DEATH && plot != null) {
            TaskManager.runTask(() -> plot.teleportPlayer(player, result -> {}));
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
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper
                        .accepts(blockType)) {
                        return true;
                    }
                }
                return Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                        false) || !(!notifyPerms || MainUtil
                    .sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
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
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper
                        .accepts(blockType)) {
                        return true;
                    }
                }
                return Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                        false);
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
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper
                        .accepts(blockType)) {
                        return true;
                    }
                }
                if (Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                        false)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil.sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
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
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper
                        .accepts(blockType)) {
                        return true;
                    }
                }
                if (Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_INTERACT_OTHER.getTranslated(),
                        false)) {
                    return true;
                }
                return !(!notifyPerms || MainUtil.sendMessage(player, Captions.FLAG_TUTORIAL_USAGE,
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
