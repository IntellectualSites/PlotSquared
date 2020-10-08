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

import com.google.common.primitives.Ints;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.minimessage.Template;
import com.plotsquared.core.uuid.UUIDMapping;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        checkTrue(args.length >= 1 && args.length <= 2, TranslatableCaption.of("commandconfig.command_syntax"), Template.of("value", "/plot grant <check | add> [player]"));
        final String arg0 = args[0].toLowerCase();
        switch (arg0) {
            case "add":
            case "check":
                if (!Permissions.hasPermission(player, Permission.PERMISSION_GRANT.format(arg0))) {
                    player.sendMessage(TranslatableCaption.of("permission.no_permission"),
                        Template.of("node", Permission.PERMISSION_GRANT.format(arg0)));
                    return CompletableFuture.completedFuture(false);
                }
                if (args.length > 2) {
                    break;
                }
                PlayerManager.getUUIDsFromString(args[1], (uuids, throwable) -> {
                    if (throwable instanceof TimeoutException) {
                        player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                    } else if (throwable != null || uuids.size() != 1) {
                        player.sendMessage(
                                TranslatableCaption.of("errors.invalid_player"),
                                Template.of("value", String.valueOf(uuids))
                        );
                    } else {
                        final UUIDMapping uuid = uuids.toArray(new UUIDMapping[0])[0];
                        PlotPlayer<?> pp = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid.getUuid());
                        if (pp != null) {
                            try (final MetaDataAccess<Integer> access = pp.accessPersistentMetaData(
                                PlayerMetaDataKeys.PERSISTENT_GRANTED_PLOTS)) {
                                if (args[0].equalsIgnoreCase("check")) {
                                    player.sendMessage(TranslatableCaption.of("grants.granted_plots"),
                                    Template.of("amount", String.valueOf(access.get().orElse(0))));
                                } else {
                                    access.set(access.get().orElse(0) + 1);
                                }
                            }
                        } else {
                            DBFunc.getPersistentMeta(uuid.getUuid(), new RunnableVal<Map<String, byte[]>>() {
                                @Override public void run(Map<String, byte[]> value) {
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
                                        Template.of("amount", String.valueOf(granted))
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
                                        DBFunc.addPersistentMeta(uuid.getUuid(), key, rawData, replace);
                                        player.sendMessage(
                                            TranslatableCaption.of("grants.added"),
                                            Template.of("grants", String.valueOf(amount))
                                        );
                                    }
                                }
                            });
                        }
                    }
                });
                return CompletableFuture.completedFuture(true);
        }
        sendUsage(player);
        return CompletableFuture.completedFuture(true);
    }
    @Override public Collection<Command> tab(final PlotPlayer player, String[] args, boolean space) {
        return Stream.of("check", "add")
                .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                .map(value -> new Command(null, false, value, "plots.grant", RequiredType.NONE, null) {
                }).collect(Collectors.toList());
    }
}
