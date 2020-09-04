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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.query.PlotQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "alias",
    permission = "plots.alias",
    description = "Set the plot name",
    usage = "/plot alias <set|remove> <alias>",
    aliases = {"setalias", "sa", "name", "rename", "setname", "seta", "nameplot"},
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class Alias extends SubCommand {
    private static final Command SET_COMMAND = new Command(null, false, "set", null, RequiredType.NONE, null) {};
    private static final Command REMOVE_COMMAND = new Command(null, false, "remove", null, RequiredType.NONE, null) {};

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {

        if (args.length == 0) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }

        if (!plot.hasOwner()) {
            sendMessage(player, Captions.PLOT_NOT_CLAIMED);
            return false;
        }

        boolean result = false;

        boolean owner = plot.isOwner(player.getUUID());
        boolean permission;
        boolean admin;
        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length != 2) {
                    Captions.COMMAND_SYNTAX.send(player, getUsage());
                    return false;
                }

                permission = isPermitted(player, Captions.PERMISSION_ALIAS_SET)
                        || isPermitted(player, Captions.PERMISSION_ALIAS_SET_OBSOLETE);
                admin = isPermitted(player, Captions.PERMISSION_ADMIN_ALIAS_SET);
                if (!admin && !owner) {
                    MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
                    return false;
                }
                if (permission) { // is either admin or owner
                    setAlias(player, plot, args[1]);
                    return true;
                } else {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                            Captions.PERMISSION_ALIAS_SET.getTranslated());
                }

                break;
            case "remove":
                permission = isPermitted(player, Captions.PERMISSION_ALIAS_REMOVE);
                admin = isPermitted(player, Captions.PERMISSION_ADMIN_ALIAS_REMOVE);
                if (!admin && !owner) {
                    MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
                    return false;
                }
                if (permission) {
                    result = removeAlias(player, plot);
                } else {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                            Captions.PERMISSION_ALIAS_REMOVE.getTranslated());
                }
                break;
            default:
                Captions.COMMAND_SYNTAX.send(player, getUsage());
                result = false;
        }

        return result;
    }

    @Override
    public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        final List<Command> commands = new ArrayList<>(2);
        if (args.length == 1) {
            if ("set".startsWith(args[0])) {
                commands.add(SET_COMMAND);
            }
            if ("remove".startsWith(args[0])) {
                commands.add(REMOVE_COMMAND);
            }
            return commands;
        }
        return Collections.emptySet();
    }

    private void setAlias(PlotPlayer player, Plot plot, String alias) {
        if (alias.isEmpty()) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
        } else if (alias.length() >= 50) {
            MainUtil.sendMessage(player, Captions.ALIAS_TOO_LONG);
        } else if (alias.contains(" ")) {
            Captions.NOT_VALID_VALUE.send(player);
        } else if (MathMan.isInteger(alias)) {
            Captions.NOT_VALID_VALUE.send(player);
        } else {
            if (PlotQuery.newQuery().inArea(plot.getArea())
                    .withAlias(alias)
                    .anyMatch()) {
                MainUtil.sendMessage(player, Captions.ALIAS_IS_TAKEN);
                return;
            }
            if (Settings.UUID.OFFLINE) {
                plot.setAlias(alias);
                MainUtil.sendMessage(player,
                        Captions.ALIAS_SET_TO.getTranslated().replaceAll("%alias%", alias));
                return;
            }
            PlotSquared.get().getImpromptuUUIDPipeline().getSingle(alias, ((uuid, throwable) -> {
                if (throwable instanceof TimeoutException) {
                    MainUtil.sendMessage(player, Captions.FETCHING_PLAYERS_TIMEOUT);
                } else if (uuid != null) {
                    MainUtil.sendMessage(player, Captions.ALIAS_IS_TAKEN);
                } else {
                    plot.setAlias(alias);
                    MainUtil.sendMessage(player,
                        Captions.ALIAS_SET_TO.getTranslated().replaceAll("%alias%", alias));
                }
            }));
        }
    }

    private boolean removeAlias(PlotPlayer<?> player, Plot plot) {
        plot.setAlias(null);
        MainUtil.sendMessage(player, Captions.ALIAS_REMOVED.getTranslated());
        return true;
    }

    private boolean isPermitted(PlotPlayer<?> player, Captions caption) {
        return Permissions.hasPermission(player, caption);
    }
}
