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

import com.google.common.primitives.Ints;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@CommandDeclaration(command = "grant",
        category = CommandCategory.CLAIMING,
        usage = "/plot grant <check | add> [player]",
        permission = "plots.grant",
        requiredType = RequiredType.NONE)
public class Grant extends Command {

    public Grant() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public CompletableFuture<Boolean> execute(
            final PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) throws CommandException {
        checkTrue(
                args.length >= 1 && args.length <= 2,
                TranslatableCaption.of("commandconfig.command_syntax"),
                TagResolver.resolver("value", Tag.inserting(Component.text("/plot grant <check | add> [player]")))
        );
        final String arg0 = args[0].toLowerCase();
        switch (arg0) {
            case "add", "check" -> {
                if (!player.hasPermission(Permission.PERMISSION_GRANT.format(arg0))) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            TagResolver.resolver("node", Tag.inserting(Component.text(Permission.PERMISSION_GRANT.format(arg0))))
                    );
                    return CompletableFuture.completedFuture(false);
                }
                if (args.length != 2) {
                    break;
                }
                PlayerManager.getUUIDsFromString(args[1], (uuids, throwable) -> {
                    if (throwable instanceof TimeoutException) {
                        player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                    } else if (throwable != null || uuids.size() != 1) {
                        player.sendMessage(
                                TranslatableCaption.of("errors.invalid_player"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(String.valueOf(uuids))))
                        );
                    } else {
                        final UUID uuid = uuids.iterator().next();
                        PlotPlayer<?> pp = PlotSquared.platform().playerManager().getPlayerIfExists(uuid);
                        if (pp != null) {
                            try (final MetaDataAccess<Integer> access = pp.accessPersistentMetaData(
                                    PlayerMetaDataKeys.PERSISTENT_GRANTED_PLOTS)) {
                                if (args[0].equalsIgnoreCase("check")) {
                                    player.sendMessage(
                                            TranslatableCaption.of("grants.granted_plots"),
                                            TagResolver.resolver("amount", Tag.inserting(Component.text(access.get().orElse(0))))
                                    );
                                } else {
                                    access.set(access.get().orElse(0) + 1);
                                    player.sendMessage(
                                            TranslatableCaption.of("grants.added"),
                                            TagResolver.resolver("grants", Tag.inserting(Component.text(access.get().orElse(0))))
                                    );
                                }
                            }
                        } else {
                            DBFunc.getPersistentMeta(uuid, new RunnableVal<>() {
                                @Override
                                public void run(Map<String, byte[]> value) {
                                    final byte[] array = value.get("grantedPlots");
                                    if (arg0.equals("check")) { // check
                                        int granted;
                                        if (array == null) {
                                            granted = 0;
                                        } else {
                                            granted = Ints.fromByteArray(array);
                                        }
                                        player.sendMessage(
                                                TranslatableCaption.of("grants.granted_plots"),
                                                TagResolver.resolver("amount", Tag.inserting(Component.text(granted)))
                                        );
                                    } else { // add
                                        int amount;
                                        if (array == null) {
                                            amount = 1;
                                        } else {
                                            amount = 1 + Ints.fromByteArray(array);
                                        }
                                        boolean replace = array != null;
                                        String key = "grantedPlots";
                                        byte[] rawData = Ints.toByteArray(amount);
                                        DBFunc.addPersistentMeta(uuid, key, rawData, replace);
                                        player.sendMessage(
                                                TranslatableCaption.of("grants.added"),
                                                TagResolver.resolver("grants", Tag.inserting(Component.text(amount)))
                                        );
                                    }
                                }
                            });
                        }
                    }
                });
                return CompletableFuture.completedFuture(true);
            }
        }
        sendUsage(player);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        if (args.length == 1) {
            final List<String> completions = new LinkedList<>();
            if (player.hasPermission(Permission.PERMISSION_GRANT_ADD)) {
                completions.add("add");
            }
            if (player.hasPermission(Permission.PERMISSION_GRANT_CHECK)) {
                completions.add("check");
            }
            final List<Command> commands = completions.stream().filter(completion -> completion
                            .toLowerCase()
                            .startsWith(args[0].toLowerCase()))
                    .map(completion -> new Command(
                            null,
                            true,
                            completion,
                            "",
                            RequiredType.NONE,
                            CommandCategory.ADMINISTRATION
                    ) {
                    }).collect(Collectors.toCollection(LinkedList::new));
            if (player.hasPermission(Permission.PERMISSION_GRANT_SINGLE) && args[0].length() > 0) {
                commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
            }
            return commands;
        } else if (args.length == 2) {
            final String subcommand = args[0].toLowerCase();
            if ((subcommand.equals("add") && player.hasPermission(Permission.PERMISSION_GRANT_ADD)) ||
                (subcommand.equals("check") && player.hasPermission(Permission.PERMISSION_GRANT_CHECK))) {
                return TabCompletions.completePlayers(player, args[1], Collections.emptyList());
            }
        }
        return Collections.emptyList();
    }

}
