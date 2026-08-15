/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.HideInfoFlag;
import com.plotsquared.core.util.TabCompletions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@CommandDeclaration(command = "info",
        aliases = "i",
        usage = "/plot info <id> [-f to force info]",
        category = CommandCategory.INFO)
public class Info extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        Plot plot;
        String arg;
        if (args.length > 0) {
            arg = args[0];
            switch (arg) {
                // TODO: (re?)implement /plot info inv. (it was never properly implemented)
                case "trusted", "alias", "biome", "denied", "flags", "id", "size", "members", "creationdate", "seen", "owner", "rating", "likes" ->
                        plot = Plot
                                .getPlotFromString(player, null, false);
                default -> {
                    plot = Plot.getPlotFromString(player, arg, false);
                    if (args.length == 2) {
                        arg = args[1];
                    } else {
                        arg = null;
                    }
                }
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
                args = new String[]{args[1]};
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
                                TagResolver.resolver(
                                        "node",
                                        Tag.inserting(Permission.PERMISSION_AREA_INFO_FORCE)
                                )
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
                    TagResolver.resolver(
                            "plot",
                            Tag.inserting(Component.text(plot.getId().getX() + ";" + plot.getId().getY()))
                    )
            );
            return true;
        }
        Caption info = TranslatableCaption.of("info.plot_info_format");
        boolean full;
        if (arg != null) {
            info = getCaption(arg);
            if (info == null) {
                if (Settings.Ratings.USE_LIKES) {
                    player.sendMessage(TranslatableCaption.of("info.plot_info_categories.use_likes"));
                } else {
                    player.sendMessage(TranslatableCaption.of("info.plot_info_categories.use_rating"));
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

    @Override
    public Collection<Command> tab(PlotPlayer<?> player, String[] args, boolean space) {
        final List<String> completions = new LinkedList<>();
        if (player.hasPermission(Permission.PERMISSION_AREA_INFO_FORCE)) {
            completions.add("-f");
        }

        final List<Command> commands = completions.stream().filter(completion -> completion
                        .toLowerCase()
                        .startsWith(args[0].toLowerCase()))
                .map(completion -> new Command(null, true, completion, "", RequiredType.PLAYER, CommandCategory.INFO) {
                }).collect(Collectors.toCollection(LinkedList::new));

        if (player.hasPermission(Permission.PERMISSION_AREA_INFO_FORCE) && args[0].length() > 0) {
            commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
        }

        return commands;
    }

    private Caption getCaption(String string) {
        return switch (string) {
            case "trusted" -> TranslatableCaption.of("info.plot_info_trusted");
            case "alias" -> TranslatableCaption.of("info.plot_info_alias");
            case "biome" -> TranslatableCaption.of("info.plot_info_biome");
            case "denied" -> TranslatableCaption.of("info.plot_info_denied");
            case "flags" -> TranslatableCaption.of("info.plot_info_flags");
            case "id" -> TranslatableCaption.of("info.plot_info_id");
            case "size" -> TranslatableCaption.of("info.plot_info_size");
            case "members" -> TranslatableCaption.of("info.plot_info_members");
            case "owner" -> TranslatableCaption.of("info.plot_info_owner");
            case "rating" -> TranslatableCaption.of("info.plot_info_rating");
            case "likes" -> TranslatableCaption.of("info.plot_info_likes");
            case "seen" -> TranslatableCaption.of("info.plot_info_seen");
            case "creationdate" -> TranslatableCaption.of("info.plot_info_creationdate");
            default -> null;
        };
    }

}
