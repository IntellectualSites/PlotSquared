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
package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.events.PlotChangeOwnerEvent;
import com.github.intellectualsites.plotsquared.plot.events.PlotUnlinkEvent;
import com.github.intellectualsites.plotsquared.plot.events.Result;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.*;

import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "setowner",
    permission = "plots.set.owner",
    description = "Set the plot owner",
    usage = "/plot setowner <player>",
    aliases = {"owner", "so", "seto"},
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    confirmation = true)
public class Owner extends SetCommand {

    @Override public boolean set(final PlotPlayer player, final Plot plot, String value) {
        if (value == null || value.isEmpty()) {
            Captions.SET_OWNER_MISSING_PLAYER.send(player);
            return false;
        }
        Set<Plot> plots = plot.getConnectedPlots();
        UUID uuid = null;
        String name;
        if (value.length() == 36) {
            try {
                uuid = UUID.fromString(value);
            } catch (Exception ignored) {
            }
        } else {
            uuid = UUIDHandler.getUUID(value, null);
        }
        if (uuid == null) {
            Captions.INVALID_PLAYER.send(player, value);
            return false;
        }
        if (value.equalsIgnoreCase("none") || value.equalsIgnoreCase("null") || value
            .equalsIgnoreCase("-")) {
            uuid = null;
        }
        PlotChangeOwnerEvent event = PlotSquared.get().getEventDispatcher()
            .callOwnerChange(player, plot, plot.hasOwner() ? plot.getOwnerAbs() : null, uuid,
                plot.hasOwner());
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Owner change");
            return false;
        }
        uuid = event.getNewOwner();
        name = uuid == null ? value : UUIDHandler.getName(uuid);
        boolean force = event.getEventResult() == Result.FORCE;
        if (uuid == null) {
            if (!force && !Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SET_OWNER.getTranslated(),
                    true)) {
                return false;
            }
            PlotUnlinkEvent unlinkEvent = PlotSquared.get().getEventDispatcher()
                .callUnlink(plot.getArea(), plot, false, false, PlotUnlinkEvent.REASON.NEW_OWNER);
            if (unlinkEvent.getEventResult() == Result.DENY) {
                sendMessage(player, Captions.EVENT_DENIED, "Unlink on owner change");
                return true;
            }
            plot.unlinkPlot(unlinkEvent.isCreateRoad(), unlinkEvent.isCreateRoad());
            Set<Plot> connected = plot.getConnectedPlots();
            for (Plot current : connected) {
                current.unclaim();
                current.removeSign();
            }
            MainUtil.sendMessage(player, Captions.SET_OWNER);
            return true;
        }
        final PlotPlayer other = UUIDHandler.getPlayer(uuid);
        if (plot.isOwner(uuid)) {
            Captions.ALREADY_OWNER.send(player, MainUtil.getName(uuid));
            return false;
        }
        if (!force && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SET_OWNER)) {
            if (other == null) {
                Captions.INVALID_PLAYER_OFFLINE.send(player, value);
                return false;
            }
            int size = plots.size();
            int currentPlots = (Settings.Limit.GLOBAL ?
                other.getPlotCount() :
                other.getPlotCount(plot.getWorldName())) + size;
            if (currentPlots > other.getAllowedPlots()) {
                sendMessage(player, Captions.CANT_TRANSFER_MORE_PLOTS);
                return false;
            }
        }
        final String finalName = name;
        final UUID finalUUID = uuid;
        final boolean removeDenied = plot.isDenied(finalUUID);
        Runnable run = () -> {
            if (plot.setOwner(finalUUID, player)) {
                if (removeDenied)
                    plot.removeDenied(finalUUID);
                plot.setSign(finalName);
                MainUtil.sendMessage(player, Captions.SET_OWNER);
                if (other != null) {
                    MainUtil.sendMessage(other, Captions.NOW_OWNER,
                        plot.getArea() + ";" + plot.getId());
                }
            } else {
                MainUtil.sendMessage(player, Captions.SET_OWNER_CANCELLED);
            }
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, "/plot set owner " + value, run);
        } else {
            TaskManager.runTask(run);
        }
        return true;
    }
}
