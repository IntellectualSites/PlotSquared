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
import com.plotsquared.core.backup.BackupProfile;
import com.plotsquared.core.backup.NullBackupProfile;
import com.plotsquared.core.backup.PlayerBackupProfile;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;

import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@CommandDeclaration(command = "backup",
usage = "/plot backup <save|list|load>",
description = "Manage plot backups",
category = CommandCategory.SETTINGS,
requiredType = RequiredType.PLAYER,
permission = "plots.backup")
public final class Backup extends Command {

    public Backup() {
        super(MainCommand.getInstance(), true);
    }

    private static boolean sendMessage(PlotPlayer player, Captions message, Object... args) {
        message.send(player, args);
        return true;
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 0 || !Arrays.asList("save", "list", "load")
            .contains(args[0].toLowerCase(Locale.ENGLISH))) {
            return CompletableFuture.completedFuture(sendMessage(player, Captions.BACKUP_USAGE));
        }
        return super.execute(player, args, confirm, whenDone);
    }

    @Override public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        if (args.length == 1) {
            return Stream.of("save", "list", "load")
                          .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                          .map(value -> new Command(null, false, value, "", RequiredType.NONE, null) {})
                          .collect(Collectors.toList());
        } else if (args[0].equalsIgnoreCase("load")) {

            final Plot plot = player.getCurrentPlot();
            if (plot != null) {
                final BackupProfile backupProfile = Objects.requireNonNull(PlotSquared.imp()).getBackupManager().getProfile(plot);
                if (backupProfile instanceof PlayerBackupProfile) {
                    final CompletableFuture<List<com.plotsquared.core.backup.Backup>> backupList = backupProfile.listBackups();
                    if (backupList.isDone()) {
                        final List<com.plotsquared.core.backup.Backup> backups = backupList.getNow(new ArrayList<>());
                        if (backups.isEmpty()) {
                            return new ArrayList<>();
                        }
                        return IntStream.range(1, 1 + backups.size()).mapToObj(i -> new Command(null, false, Integer.toString(i),
                            "", RequiredType.NONE, null) {}).collect(Collectors.toList());

                    }
                }
            }
        }
        return tabOf(player, args, space);
    }

    @CommandDeclaration(command = "save",
    usage = "/plot backup save",
    description = "Create a plot backup",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER,
    permission = "plots.backup.save")
    public void save(final Command command, final PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            sendMessage(player, Captions.NOT_IN_PLOT);
        } else if (!plot.hasOwner()) {
            sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_UNOWNED.getTranslated());
        } else if (plot.isMerged()) {
            sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_MERGED.getTranslated());
        } else if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_BACKUP_OTHER)) {
            sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_ADMIN_BACKUP_OTHER);
        } else {
            final BackupProfile backupProfile = Objects.requireNonNull(PlotSquared.imp()).getBackupManager().getProfile(plot);
            if (backupProfile instanceof NullBackupProfile) {
                sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_OTHER.getTranslated());
            } else {
                backupProfile.createBackup().whenComplete((backup, throwable) -> {
                    if (throwable != null) {
                        sendMessage(player, Captions.BACKUP_SAVE_FAILED, throwable.getMessage());
                        throwable.printStackTrace();
                    } else {
                        sendMessage(player, Captions.BACKUP_SAVE_SUCCESS);
                    }
                });
            }
        }
    }

    @CommandDeclaration(command = "list",
        usage = "/plot backup list",
        description = "List available plot backups",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER,
        permission = "plots.backup.list")
    public void list(final Command command, final PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
            final Plot plot = player.getCurrentPlot();
            if (plot == null) {
                sendMessage(player, Captions.NOT_IN_PLOT);
            } else if (!plot.hasOwner()) {
                sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_UNOWNED.getTranslated());
            } else if (plot.isMerged()) {
                sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_MERGED.getTranslated());
            } else if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_BACKUP_OTHER)) {
                sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_ADMIN_BACKUP_OTHER);
            } else {
                final BackupProfile backupProfile = Objects.requireNonNull(PlotSquared.imp()).getBackupManager().getProfile(plot);
                if (backupProfile instanceof NullBackupProfile) {
                    sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_OTHER.getTranslated());
                } else {
                    backupProfile.listBackups().whenComplete((backups, throwable) -> {
                        if (throwable != null) {
                            sendMessage(player, Captions.BACKUP_LIST_FAILED, throwable.getMessage());
                            throwable.printStackTrace();
                        } else {
                            sendMessage(player, Captions.BACKUP_LIST_HEADER, plot.getId().toCommaSeparatedString());
                            try {
                                for (int i = 0; i < backups.size(); i++) {
                                    sendMessage(player, Captions.BACKUP_LIST_ENTRY, Integer.toString(i + 1),
                                        DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime
                                                .ofInstant(Instant.ofEpochMilli(backups.get(i).getCreationTime()), ZoneId
                                                .systemDefault())));
                                }
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
    }

    @CommandDeclaration(command = "load",
        usage = "/plot backup load <#>",
        description = "Restore a plot backup",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER,
        permission = "plots.backup.load")
    public void load(final Command command, final PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            sendMessage(player, Captions.NOT_IN_PLOT);
        } else if (!plot.hasOwner()) {
            sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_UNOWNED.getTranslated());
        } else if (plot.isMerged()) {
            sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_MERGED.getTranslated());
            sendMessage(player, Captions.BACKUP_IMPOSSIBLE, "merged");
        } else if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_BACKUP_OTHER)) {
            sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_ADMIN_BACKUP_OTHER);
        } else if (args.length == 0) {
            sendMessage(player, Captions.BACKUP_LOAD_USAGE);
        } else {
            final int number;
            try {
                number = Integer.parseInt(args[0]);
            } catch (final Exception e) {
                sendMessage(player, Captions.NOT_A_NUMBER, args[0]);
                return;
            }
            final BackupProfile backupProfile = Objects.requireNonNull(PlotSquared.imp()).getBackupManager().getProfile(plot);
            if (backupProfile instanceof NullBackupProfile) {
                sendMessage(player, Captions.BACKUP_IMPOSSIBLE, Captions.GENERIC_OTHER.getTranslated());
            } else {
                backupProfile.listBackups().whenComplete((backups, throwable) -> {
                    if (throwable != null) {
                        sendMessage(player, Captions.BACKUP_LOAD_FAILURE, throwable.getMessage());
                        throwable.printStackTrace();
                    } else {
                        if (number < 1 || number > backups.size()) {
                            sendMessage(player, Captions.BACKUP_LOAD_FAILURE, Captions.GENERIC_INVALID_CHOICE.getTranslated());
                        } else {
                            final com.plotsquared.core.backup.Backup backup = backups.get(number - 1);
                            if (backup == null || backup.getFile() == null || !Files.exists(backup.getFile())) {
                                sendMessage(player, Captions.BACKUP_LOAD_FAILURE, Captions.GENERIC_INVALID_CHOICE.getTranslated());
                            } else {
                                CmdConfirm.addPending(player, "/plot backup load " + number, () ->
                                    backupProfile.restoreBackup(backup).whenComplete((n, error) -> {
                                   if (error != null) {
                                       sendMessage(player, Captions.BACKUP_LOAD_FAILURE, error.getMessage());
                                   } else {
                                       sendMessage(player, Captions.BACKUP_LOAD_SUCCESS);
                                   }
                                }));
                            }
                        }
                    }
                });
            }
        }
    }

}
