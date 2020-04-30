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

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.HideInfoFlag;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.RunnableVal;

@CommandDeclaration(command = "info",
    aliases = "i",
    description = "Display plot info",
    usage = "/plot info <id> [-f, to force info]",
    category = CommandCategory.INFO)
public class Info extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Plot plot;
        String arg;
        if (args.length > 0) {
            arg = args[0];
            switch (arg) {
                case "trusted":
                case "alias":
                case "inv":
                case "biome":
                case "denied":
                case "flags":
                case "id":
                case "size":
                case "members":
                case "seen":
                case "owner":
                case "rating":
                case "likes":
                    plot = MainUtil.getPlotFromString(player, null, false);
                    break;
                default:
                    plot = MainUtil.getPlotFromString(player, arg, false);
                    if (args.length == 2) {
                        arg = args[1];
                    } else {
                        arg = null;
                    }
                    break;
            }
            if (plot == null) {
                plot = player.getCurrentPlot();
            }
        } else {
            arg = null;
            plot = player.getCurrentPlot();
        }
        if (plot == null) {
            MainUtil.sendMessage(player, Captions.NOT_IN_PLOT.getTranslated());
            return false;
        }

        if (arg != null) {
            if (args.length == 1) {
                args = new String[0];
            } else {
                args = new String[] {args[1]};
            }
        }

        // hide-info flag
        if (plot.getFlag(HideInfoFlag.class)) {
            boolean allowed = false;
            for (final String argument : args) {
                if (argument.equalsIgnoreCase("-f")) {
                    if (!player
                        .hasPermission(Captions.PERMISSION_AREA_INFO_FORCE.getTranslated())) {
                        Captions.NO_PERMISSION.send(player, Captions.PERMISSION_AREA_INFO_FORCE);
                        return true;
                    }
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                Captions.PLOT_INFO_HIDDEN.send(player);
                return true;
            }
        }

        boolean hasOwner = plot.hasOwner();
        // Wildcard player {added}
        boolean containsEveryone = plot.getTrusted().contains(DBFunc.EVERYONE);
        boolean trustedEveryone = plot.getMembers().contains(DBFunc.EVERYONE);
        // Unclaimed?
        if (!hasOwner && !containsEveryone && !trustedEveryone) {
            MainUtil.sendMessage(player, Captions.PLOT_INFO_UNCLAIMED,
                plot.getId().x + ";" + plot.getId().y);
            return true;
        }
        String info = Captions.PLOT_INFO_FORMAT.getTranslated();
        boolean full;
        if (arg != null) {
            info = getCaption(arg);
            if (info == null) {
                if (Settings.Ratings.USE_LIKES) {
                    MainUtil.sendMessage(player,
                        "&6Categories&7: &amembers&7, &aalias&7, &abiome&7, &aseen&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, "
                            + "&aowner&7, " + " &alikes");
                } else {
                    MainUtil.sendMessage(player,
                        "&6Categories&7: &amembers&7, &aalias&7, &abiome&7, &aseen&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, "
                            + "&aowner&7, " + " &arating");
                }
                return false;
            }
            full = true;
        } else {
            full = false;
        }
        MainUtil.format(info, plot, player, full, new RunnableVal<String>() {
            @Override public void run(String value) {
                MainUtil.sendMessage(player,
                    Captions.PLOT_INFO_HEADER.getTranslated() + '\n' + value + '\n'
                        + Captions.PLOT_INFO_FOOTER.getTranslated(), false);
            }
        });
        return true;
    }

    private String getCaption(String string) {
        switch (string) {
            case "trusted":
                return Captions.PLOT_INFO_TRUSTED.getTranslated();
            case "alias":
                return Captions.PLOT_INFO_ALIAS.getTranslated();
            case "biome":
                return Captions.PLOT_INFO_BIOME.getTranslated();
            case "denied":
                return Captions.PLOT_INFO_DENIED.getTranslated();
            case "flags":
                return Captions.PLOT_INFO_FLAGS.getTranslated();
            case "id":
                return Captions.PLOT_INFO_ID.getTranslated();
            case "size":
                return Captions.PLOT_INFO_SIZE.getTranslated();
            case "members":
                return Captions.PLOT_INFO_MEMBERS.getTranslated();
            case "owner":
                return Captions.PLOT_INFO_OWNER.getTranslated();
            case "rating":
                return Captions.PLOT_INFO_RATING.getTranslated();
            case "likes":
                return Captions.PLOT_INFO_LIKES.getTranslated();
            case "seen":
                return Captions.PLOT_INFO_SEEN.getTranslated();
            default:
                return null;
        }
    }
}
