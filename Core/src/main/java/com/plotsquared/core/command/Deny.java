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
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.sk89q.worldedit.world.gamemode.GameModes;

import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "deny",
    aliases = {"d", "ban"},
    description = "Deny a user from entering a plot",
    usage = "/plot deny <player|*>",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class Deny extends SubCommand {

    public Deny() {
        super(Argument.PlayerName);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {

        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DENY)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return true;
        }
        Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        if (uuids.isEmpty()) {
            MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            return false;
        }
        for (UUID uuid : uuids) {
            if (uuid == DBFunc.EVERYONE && !(
                Permissions.hasPermission(player, Captions.PERMISSION_DENY_EVERYONE) || Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DENY))) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, MainUtil.getName(uuid));
                continue;
            }
            if (plot.isOwner(uuid)) {
                MainUtil.sendMessage(player, Captions.CANT_REMOVE_OWNER, MainUtil.getName(uuid));
                return false;
            }

            if (plot.getDenied().contains(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_ADDED, MainUtil.getName(uuid));
                return false;
            }
            if (uuid != DBFunc.EVERYONE) {
                plot.removeMember(uuid);
                plot.removeTrusted(uuid);
            }
            plot.addDenied(uuid);
            PlotSquared.get().getEventDispatcher().callDenied(player, plot, uuid, true);
            if (!uuid.equals(DBFunc.EVERYONE)) {
                handleKick(UUIDHandler.getPlayer(uuid), plot);
            } else {
                for (PlotPlayer plotPlayer : plot.getPlayersInPlot()) {
                    // Ignore plot-owners
                    if (plot.isAdded(plotPlayer.getUUID())) {
                        continue;
                    }
                    handleKick(plotPlayer, plot);
                }
            }
        }
        if (!uuids.isEmpty()) {
            MainUtil.sendMessage(player, Captions.DENIED_ADDED);
        }
        return true;
    }

    private void handleKick(PlotPlayer player, Plot plot) {
        if (player == null) {
            return;
        }
        if (!plot.equals(player.getCurrentPlot())) {
            return;
        }
        if (player.hasPermission("plots.admin.entry.denied")) {
            return;
        }
        if (player.getGameMode() == GameModes.SPECTATOR) {
            player.stopSpectating();
        }
        Location location = player.getLocation();
        Location spawn = WorldUtil.IMP.getSpawn(location.getWorld());
        MainUtil.sendMessage(player, Captions.YOU_GOT_DENIED);
        if (plot.equals(spawn.getPlot())) {
            Location newSpawn =
                WorldUtil.IMP.getSpawn(PlotSquared.get().getPlotAreaManager().getAllWorlds()[0]);
            if (plot.equals(newSpawn.getPlot())) {
                // Kick from server if you can't be teleported to spawn
                player.kick(Captions.YOU_GOT_DENIED.getTranslated());
            } else {
                player.teleport(newSpawn);
            }
        } else {
            player.teleport(spawn);
        }
    }
}
