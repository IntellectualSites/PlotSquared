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

import com.google.common.primitives.Ints;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.CaptionUtility;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@CommandDeclaration(command = "grant",
    category = CommandCategory.CLAIMING,
    usage = "/plot grant <check|add> [player]",
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
        checkTrue(args.length >= 1 && args.length <= 2, Captions.COMMAND_SYNTAX, getUsage());
        final String arg0 = args[0].toLowerCase();
        switch (arg0) {
            case "add":
            case "check":
                if (!Permissions.hasPermission(player, CaptionUtility
                    .format(player, Captions.PERMISSION_GRANT.getTranslated(), arg0))) {
                    Captions.NO_PERMISSION.send(player, CaptionUtility
                        .format(player, Captions.PERMISSION_GRANT.getTranslated(), arg0));
                    return CompletableFuture.completedFuture(false);
                }
                if (args.length > 2) {
                    break;
                }
                PlayerManager.getUUIDsFromString(args[1], (uuids, throwable) -> {
                    if (throwable instanceof TimeoutException) {
                        MainUtil.sendMessage(player, Captions.FETCHING_PLAYERS_TIMEOUT);
                    } else if (throwable != null || uuids.size() != 1) {
                        MainUtil.sendMessage(player, Captions.INVALID_PLAYER);
                    } else {
                        final UUID uuid = uuids.toArray(new UUID[0])[0];
                        final Consumer<byte[]> result = array -> {
                            if (arg0.equals("check")) { // check
                                int granted;
                                if (array == null) {
                                    granted = 0;
                                } else {
                                    granted = Ints.fromByteArray(array);
                                }
                                Captions.GRANTED_PLOTS.send(player, granted);
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

                                PlotPlayer online = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
                                if (online != null) {
                                    online.setPersistentMeta(key, rawData);
                                } else {
                                    DBFunc.addPersistentMeta(uuid, key, rawData, replace);
                                }
                            }
                        };
                        PlotPlayer<?> pp = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
                        if (pp != null) {
                            result.accept(player.getPersistentMeta("grantedPlots"));
                        } else {
                            DBFunc.getPersistentMeta(uuid, new RunnableVal<Map<String, byte[]>>() {
                                @Override public void run(Map<String, byte[]> value) {
                                    result.accept(value.get("grantedPlots"));
                                }
                            });
                        }
                    }
                });
                return CompletableFuture.completedFuture(true);
        }
        Captions.COMMAND_SYNTAX.send(player, getUsage());
        return CompletableFuture.completedFuture(true);
    }
}
