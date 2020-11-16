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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.HideInfoFlag;
import net.kyori.adventure.text.minimessage.Template;

@CommandDeclaration(command = "info",
    aliases = "i",
    usage = "/plot info <id> [-f to force info]",
    category = CommandCategory.INFO)
public class Info extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
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
                    plot = Plot.getPlotFromString(player, null, false);
                    break;
                default:
                    plot = Plot.getPlotFromString(player, arg, false);
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
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
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
                        .hasPermission(Permission.PERMISSION_AREA_INFO_FORCE.toString())) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", Permission.PERMISSION_AREA_INFO_FORCE.toString())
                        );
                        return true;
                    }
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                player.sendMessage(TranslatableCaption.of("info.plot_info_hidden"));
                return true;
            }
        }

        boolean hasOwner = plot.hasOwner();
        // Wildcard player {added}
        boolean containsEveryone = plot.getTrusted().contains(DBFunc.EVERYONE);
        boolean trustedEveryone = plot.getMembers().contains(DBFunc.EVERYONE);
        // Unclaimed?
        if (!hasOwner && !containsEveryone && !trustedEveryone) {
            player.sendMessage(
                    TranslatableCaption.of("info.plot_info_unclaimed"),
                    Template.of("plot", plot.getId().getX() + ";" + plot.getId().getY())
            );
            return true;
        }
        Caption info = TranslatableCaption.of("info.plot_info_format");
        boolean full;
        if (arg != null) {
            info = getCaption(arg);
            if (info == null) {
                if (Settings.Ratings.USE_LIKES) {
                    player.sendMessage(StaticCaption.of("&6Categories&7: &amembers&7, &aalias&7, &abiome&7, &aseen&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, "
                            + "&aowner&7, " + " &alikes"));
                } else {
                    player.sendMessage(StaticCaption.of("&6Categories&7: &amembers&7, &aalias&7, &abiome&7, &aseen&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, "
                            + "&aowner&7, " + " &arating"));
                }
                return false;
            }
            full = true;
        } else {
            full = false;
        }
        plot.format(info, player, full).thenAcceptAsync(player::sendMessage);
        return true;
    }

    private Caption getCaption(String string) {
        switch (string) {
            case "trusted":
                return TranslatableCaption.of("info.plot_info_trusted");
            case "alias":
                return TranslatableCaption.of("info.plot_info_alias");
            case "biome":
                return TranslatableCaption.of("info.plot_info_biome");
            case "denied":
                return TranslatableCaption.of("info.plot_info_denied");
            case "flags":
                return TranslatableCaption.of("info.plot_info_flags");
            case "id":
                return TranslatableCaption.of("info.plot_info_id");
            case "size":
                return TranslatableCaption.of("info.plot_info_size");
            case "members":
                return TranslatableCaption.of("info.plot_info_members");
            case "owner":
                return TranslatableCaption.of("info.plot_info_owner");
            case "rating":
                return TranslatableCaption.of("info.plot_info_rating");
            case "likes":
                return TranslatableCaption.of("info.plot_info_likes");
            case "seen":
                return TranslatableCaption.of("info.plot_info_seen");
            default:
                return null;
        }
    }
}
