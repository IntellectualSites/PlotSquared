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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.query.PlotQuery;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "alias",
        permission = "plots.alias",
        usage = "/plot alias <set | remove> <alias>",
        aliases = {"setalias", "sa", "name", "rename", "setname", "seta", "nameplot"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER)
public class Alias extends SubCommand {

    private static final Command SET_COMMAND = new Command(null, false, "set", null, RequiredType.NONE, null) {
    };
    private static final Command REMOVE_COMMAND = new Command(null, false, "remove", null, RequiredType.NONE, null) {
    };

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {

        if (args.length == 0) {
            sendUsage(player);
            return false;
        }

        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }

        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("working.plot_not_claimed"));
            return false;
        }

        boolean result = false;

        boolean owner = plot.isOwner(player.getUUID());
        boolean permission;
        boolean admin;
        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (args.length != 2) {
                    sendUsage(player);
                    return false;
                }
                permission = isPermitted(player, Permission.PERMISSION_ALIAS_SET);
                admin = isPermitted(player, Permission.PERMISSION_ADMIN_ALIAS_SET);
                if (!admin && !owner) {
                    player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
                    return false;
                }
                if (permission) { // is either admin or owner
                    setAlias(player, plot, args[1]);
                    return true;
                } else {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ALIAS_SET)
                            )
                    );
                }
            }
            case "remove" -> {
                permission = isPermitted(player, Permission.PERMISSION_ALIAS_REMOVE);
                admin = isPermitted(player, Permission.PERMISSION_ADMIN_ALIAS_REMOVE);
                if (!admin && !owner) {
                    player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
                    return false;
                }
                if (permission) {
                    result = removeAlias(player, plot);
                } else {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_ALIAS_REMOVE)
                            )
                    );
                }
            }
            default -> {
                sendUsage(player);
                result = false;
            }
        }

        return result;
    }

    @Override
    public Collection<Command> tab(PlotPlayer<?> player, String[] args, boolean space) {
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

    private void setAlias(PlotPlayer<?> player, Plot plot, String alias) {
        if (alias.isEmpty()) {
            sendUsage(player);
        } else if (alias.length() >= 50) {
            player.sendMessage(TranslatableCaption.of("alias.alias_too_long"));
        } else if (MathMan.isInteger(alias)) {
            player.sendMessage(TranslatableCaption.of("flag.not_valid_value")); // TODO this is obviously wrong
        } else {
            if (PlotQuery.newQuery().inArea(plot.getArea())
                    .withAlias(alias)
                    .anyMatch()) {
                player.sendMessage(
                        TranslatableCaption.of("alias.alias_is_taken"),
                        TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
                );
                return;
            }
            if (Settings.UUID.OFFLINE) {
                plot.setAlias(alias);
                player.sendMessage(
                        TranslatableCaption.of("alias.alias_set_to"),
                        TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
                );
                return;
            }
            PlotSquared.get().getImpromptuUUIDPipeline().getSingle(alias, ((uuid, throwable) -> {
                if (throwable instanceof TimeoutException) {
                    player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                } else if (uuid != null) {
                    player.sendMessage(
                            TranslatableCaption.of("alias.alias_is_taken"),
                            TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
                    );
                } else {
                    plot.setAlias(alias);
                    player.sendMessage(
                            TranslatableCaption.of("alias.alias_set_to"),
                            TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
                    );
                }
            }));
        }
    }

    private boolean removeAlias(PlotPlayer<?> player, Plot plot) {
        String alias = plot.getAlias();
        if (!plot.getAlias().isEmpty()) {
            player.sendMessage(
                    TranslatableCaption.of("alias.alias_removed"),
                    TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
            );
        } else {
            player.sendMessage(
                    TranslatableCaption.of("alias.no_alias_set")
            );
        }
        plot.setAlias(null);
        return true;
    }

    private boolean isPermitted(PlotPlayer<?> player, Permission permission) {
        return player.hasPermission(permission);
    }

}
