/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.google.common.eventbus.EventBus;
import com.plotsquared.core.configuration.caption.CaptionUtility;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
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
import com.plotsquared.core.listener.PlayerBlockEventType;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.Rating;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DeviceInteractFlag;
import com.plotsquared.core.plot.flag.implementations.MiscPlaceFlag;
import com.plotsquared.core.plot.flag.implementations.MobPlaceFlag;
import com.plotsquared.core.plot.flag.implementations.PlaceFlag;
import com.plotsquared.core.plot.flag.implementations.UseFlag;
import com.plotsquared.core.plot.flag.implementations.VehiclePlaceFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventDispatcher {

    private final EventBus eventBus = new EventBus("PlotSquaredEvents");
    private final List<Object> listeners = new ArrayList<>();
    private final WorldEdit worldEdit;

    public EventDispatcher(@Nullable final WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

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
    public void callGenericEvent(@Nonnull final Object event) {
        eventBus.post(event);
    }

    public void callEvent(@Nonnull final PlotEvent event) {
        eventBus.post(event);
    }

    public PlayerClaimPlotEvent callClaim(PlotPlayer<?> player, Plot plot, String schematic) {
        PlayerClaimPlotEvent event = new PlayerClaimPlotEvent(player, plot, schematic);
        callEvent(event);
        return event;
    }

    public PlayerAutoPlotEvent callAuto(PlotPlayer<?> player, PlotArea area, String schematic,
        int size_x, int size_z) {
        PlayerAutoPlotEvent event =
            new PlayerAutoPlotEvent(player, area, schematic, size_x, size_z);
        callEvent(event);
        return event;
    }

    public PlayerTeleportToPlotEvent callTeleport(PlotPlayer<?> player, Location from, Plot plot) {
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

    public PlotMergeEvent callMerge(Plot plot, Direction dir, int max, PlotPlayer<?> player) {
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

    public PlayerEnterPlotEvent callEntry(PlotPlayer<?> player, Plot plot) {
        PlayerEnterPlotEvent event = new PlayerEnterPlotEvent(player, plot);
        callEvent(event);
        return event;
    }

    public PlayerLeavePlotEvent callLeave(PlotPlayer<?> player, Plot plot) {
        PlayerLeavePlotEvent event = new PlayerLeavePlotEvent(player, plot);
        callEvent(event);
        return event;
    }

    public PlayerPlotDeniedEvent callDenied(PlotPlayer<?> initiator, Plot plot, UUID player,
        boolean added) {
        PlayerPlotDeniedEvent event = new PlayerPlotDeniedEvent(initiator, plot, player, added);
        callEvent(event);
        return event;
    }

    public PlayerPlotTrustedEvent callTrusted(PlotPlayer<?> initiator, Plot plot, UUID player,
        boolean added) {
        PlayerPlotTrustedEvent event = new PlayerPlotTrustedEvent(initiator, plot, player, added);
        callEvent(event);
        return event;
    }

    public PlayerPlotHelperEvent callMember(PlotPlayer<?> initiator, Plot plot, UUID player,
        boolean added) {
        PlayerPlotHelperEvent event = new PlayerPlotHelperEvent(initiator, plot, player, added);
        callEvent(event);
        return event;
    }

    public PlotChangeOwnerEvent callOwnerChange(PlotPlayer<?> initiator, Plot plot, UUID oldOwner,
        UUID newOwner, boolean hasOldOwner) {
        PlotChangeOwnerEvent event =
            new PlotChangeOwnerEvent(initiator, plot, oldOwner, newOwner, hasOldOwner);
        callEvent(event);
        return event;
    }

    public PlotRateEvent callRating(PlotPlayer<?> player, Plot plot, Rating rating) {
        PlotRateEvent event = new PlotRateEvent(player, rating, plot);
        eventBus.post(event);
        return event;
    }

    public PlotDoneEvent callDone(Plot plot) {
        PlotDoneEvent event = new PlotDoneEvent(plot);
        callEvent(event);
        return event;
    }

    public void doJoinTask(final PlotPlayer<?> player) {
        if (player == null) {
            return; //possible future warning message to figure out where we are retrieving null
        }
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.handleJoin(player);
        }
        if (this.worldEdit != null) {
            if (player.getAttribute("worldedit")) {
                player.sendMessage(TranslatableCaption.of("worldedit.worldedit_bypassed"));
            }
        }
        final Plot plot = player.getCurrentPlot();
        if (Settings.Teleport.ON_LOGIN && plot != null && !(plot
            .getArea() instanceof SinglePlotArea)) {
            TaskManager.runTask(() -> plot.teleportPlayer(player, result -> {
            }));
            player.sendMessage(TranslatableCaption.of("teleport.teleported_to_road"));
        }
    }

    public void doRespawnTask(final PlotPlayer<?> player) {
        final Plot plot = player.getCurrentPlot();
        if (Settings.Teleport.ON_DEATH && plot != null) {
            TaskManager.runTask(() -> plot.teleportPlayer(player, result -> {
            }));
            player.sendMessage(TranslatableCaption.of("teleport.teleported_to_road"));
        }
    }

    public boolean checkPlayerBlockEvent(PlotPlayer<?> player, @Nonnull PlayerBlockEventType type,
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
                    final List<BlockTypeWrapper> use = area.getRoadFlag(UseFlag.class);
                    for(final BlockTypeWrapper blockTypeWrapper : use) {
                        if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper.accepts(blockType)) {
                            return true;
                        }
                    }
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_ROAD.toString(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_UNOWNED.toString(), notifyPerms);
                }
                final List<BlockTypeWrapper> use = plot.getFlag(UseFlag.class);
                for (final BlockTypeWrapper blockTypeWrapper : use) {
                    if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper
                        .accepts(blockType)) {
                        return true;
                    }
                }
                if (Permissions.hasPermission(player, Permission.PERMISSION_ADMIN_INTERACT_OTHER.toString(), false)) {
                    return true;
                }
                if (notifyPerms) {
                    player.sendMessage(TranslatableCaption.of("commandconfig.flag_tutorial_usage"),
                            Template.of("flag", PlaceFlag.getFlagName(UseFlag.class)));
                }
                return false;
            }
            case TRIGGER_PHYSICAL: {
                if (plot == null) {
                    final List<BlockTypeWrapper> use = area.getRoadFlag(UseFlag.class);
                    for(final BlockTypeWrapper blockTypeWrapper : use) {
                        if (blockTypeWrapper.accepts(BlockTypes.AIR) || blockTypeWrapper.accepts(blockType)) {
                            return true;
                        }
                    }
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_ROAD.toString(), false);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_UNOWNED.toString(), false);
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
                    .hasPermission(player, Permission.PERMISSION_ADMIN_INTERACT_OTHER.toString(),
                        false);
            }
            case SPAWN_MOB: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_ROAD.toString(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_UNOWNED.toString(), notifyPerms);
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
                    .hasPermission(player, Permission.PERMISSION_ADMIN_INTERACT_OTHER.toString(),
                        false)) {
                    return true;
                }
                if (notifyPerms) {
                    player.sendMessage(TranslatableCaption.of("commandconfig.flag_tutorial_usage"),
                            Template.of("flag", PlotFlag.getFlagName(MobPlaceFlag.class)
                                    + '/' + PlotFlag.getFlagName(PlaceFlag.class)));
                }
                return false;
            }
            case PLACE_MISC: {
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_ROAD.toString(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_UNOWNED.toString(), notifyPerms);
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
                    .hasPermission(player, Permission.PERMISSION_ADMIN_INTERACT_OTHER.toString(),
                        false)) {
                    return true;
                }
                if (notifyPerms) {
                    player.sendMessage(TranslatableCaption.of("commandconfig.flag_tutorial_usage"),
                            Template.of("flag", PlotFlag.getFlagName(MiscPlaceFlag.class)
                                    + '/' + PlotFlag.getFlagName(PlaceFlag.class)));
                }
                return false;
            }
            case PLACE_VEHICLE:
                if (plot == null) {
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_ROAD.toString(), notifyPerms);
                }
                if (!plot.hasOwner()) {
                    return Permissions.hasPermission(player,
                        Permission.PERMISSION_ADMIN_INTERACT_UNOWNED.toString(), notifyPerms);
                }
                return plot.getFlag(VehiclePlaceFlag.class);
            default:
                break;
        }
        return true;
    }
}
