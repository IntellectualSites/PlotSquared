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

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.google.common.primitives.Ints;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "grant",
    category = CommandCategory.CLAIMING,
    usage = "/plot grant <check|add> [player]",
    permission = "plots.grant",
    requiredType = RequiredType.NONE)
public class Grant extends Command {

    public Grant() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
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
                final UUID uuid;
                if (args.length == 2) {
                    uuid = UUIDHandler.getUUIDFromString(args[1]);
                } else {
                    uuid = player.getUUID();
                }
                if (uuid == null) {
                    Captions.INVALID_PLAYER.send(player, args[1]);
                    return CompletableFuture.completedFuture(false);
                }
                MainUtil.getPersistentMeta(uuid, "grantedPlots", new RunnableVal<byte[]>() {
                    @Override public void run(byte[] array) {
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
                            PlotPlayer online = UUIDHandler.getPlayer(uuid);
                            if (online != null) {
                                online.setPersistentMeta(key, rawData);
                            } else {
                                DBFunc.addPersistentMeta(uuid, key, rawData, replace);
                            }
                        }
                    }
                });
                return CompletableFuture.completedFuture(true);
        }
        Captions.COMMAND_SYNTAX.send(player, getUsage());
        return CompletableFuture.completedFuture(true);
    }
}
