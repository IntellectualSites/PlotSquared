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

import com.github.intellectualsites.plotsquared.commands.Argument;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "kick",
    aliases = "k",
    description = "Kick a player from your plot",
    permission = "plots.kick",
    usage = "/plot kick <player|*>",
    category = CommandCategory.TELEPORT,
    requiredType = RequiredType.PLAYER)
public class Kick extends SubCommand {

    public Kick() {
        super(Argument.PlayerName);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlot();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if ((!plot.hasOwner() || !plot.isOwner(player.getUUID())) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_KICK)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        if (uuids.isEmpty()) {
            MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            return false;
        }
        Set<PlotPlayer> players = new HashSet<>();
        for (UUID uuid : uuids) {
            if (uuid == DBFunc.EVERYONE) {
                for (PlotPlayer pp : plot.getPlayersInPlot()) {
                    if (pp == player || Permissions
                        .hasPermission(pp, Captions.PERMISSION_ADMIN_ENTRY_DENIED)) {
                        continue;
                    }
                    players.add(pp);
                }
                continue;
            }
            PlotPlayer pp = UUIDHandler.getPlayer(uuid);
            if (pp != null) {
                players.add(pp);
            }
        }
        players.remove(player); // Don't ever kick the calling player
        if (players.isEmpty()) {
            MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            return false;
        }
        for (PlotPlayer player2 : players) {
            if (!plot.equals(player2.getCurrentPlot())) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
                return false;
            }
            if (Permissions.hasPermission(player2, Captions.PERMISSION_ADMIN_ENTRY_DENIED)) {
                Captions.CANNOT_KICK_PLAYER.send(player, player2.getName());
                return false;
            }
            Location spawn = WorldUtil.IMP.getSpawn(location.getWorld());
            Captions.YOU_GOT_KICKED.send(player2);
            if (plot.equals(spawn.getPlot())) {
                Location newSpawn = WorldUtil.IMP
                    .getSpawn(PlotSquared.get().getPlotAreaManager().getAllWorlds()[0]);
                if (plot.equals(newSpawn.getPlot())) {
                    // Kick from server if you can't be teleported to spawn
                    player2.kick(Captions.YOU_GOT_KICKED.getTranslated());
                } else {
                    player2.plotkick(newSpawn);
                }
            } else {
                player2.plotkick(spawn);
            }
        }
        return true;
    }
}
