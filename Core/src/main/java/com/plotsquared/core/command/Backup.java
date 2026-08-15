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

import com.google.inject.Inject;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.backup.BackupProfile;
import com.plotsquared.core.backup.NullBackupProfile;
import com.plotsquared.core.backup.PlayerBackupProfile;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.exception.PlotSquaredException;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

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
        usage = "/plot backup <save | list | load>",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER,
        permission = "plots.backup")
public final class Backup extends Command {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + Backup.class.getSimpleName());

    private final BackupManager backupManager;

    @Inject
    public Backup(final @NonNull BackupManager backupManager) {
        super(MainCommand.getInstance(), true);
        this.backupManager = backupManager;
    }

    private static boolean sendMessage(PlotPlayer<?> player) {
        player.sendMessage(
                TranslatableCaption.of("commandconfig.command_syntax"),
                TagResolver.resolver("value", Tag.inserting(Component.text("/plot backup <save | list | load>")))
        );
        return true;
    }

    @Override
    public CompletableFuture<Boolean> execute(
            PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) throws CommandException {
        if (args.length == 0 || !Arrays.asList("save", "list", "load")
                .contains(args[0].toLowerCase(Locale.ENGLISH))) {
            return CompletableFuture.completedFuture(sendMessage(player));
        }
        return super.execute(player, args, confirm, whenDone);
    }

    @Override
    public Collection<Command> tab(PlotPlayer<?> player, String[] args, boolean space) {
        if (args.length == 1) {
            return Stream.of("save", "list", "load")
                    .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                    .map(value -> new Command(null, false, value, "", RequiredType.NONE, null) {
                    }).collect(Collectors.toList());
        } else if (args[0].equalsIgnoreCase("load")) {

            final Plot plot = player.getCurrentPlot();
            if (plot != null) {
                final BackupProfile backupProfile = Objects.requireNonNull(this.backupManager.getProfile(plot));
                if (backupProfile instanceof PlayerBackupProfile) {
                    final CompletableFuture<List<com.plotsquared.core.backup.Backup>> backupList =
                            backupProfile.listBackups();
                    if (backupList.isDone()) {
                        final List<com.plotsquared.core.backup.Backup> backups =
                                backupList.getNow(new ArrayList<>());
                        if (backups.isEmpty()) {
                            return new ArrayList<>();
                        }
                        return IntStream.range(1, 1 + backups.size()).mapToObj(
                                i -> new Command(null, false, Integer.toString(i), "",
                                        RequiredType.NONE, null
                                ) {
                                }).collect(Collectors.toList());

                    }
                }
            }
        }
        return tabOf(player, args, space);
    }

    @CommandDeclaration(command = "save",
            usage = "/plot backup save",
            category = CommandCategory.SETTINGS,
            requiredType = RequiredType.PLAYER,
            permission = "plots.backup.save")
    public void save(
            final Command command, final PlotPlayer<?> player, final String[] args,
            final RunnableVal3<Command, Runnable, Runnable> confirm,
            final RunnableVal2<Command, CommandResult> whenDone
    ) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
        } else if (!plot.hasOwner()) {
            player.sendMessage(
                    TranslatableCaption.of("backups.backup_impossible"),
                    TagResolver.resolver("plot", Tag.inserting(
                            TranslatableCaption.of("generic.generic_unowned").toComponent(player)
                    ))
            );
        } else if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
        } else if (plot.isMerged()) {
            player.sendMessage(
                    TranslatableCaption.of("backups.backup_impossible"),
                    TagResolver.resolver("plot", Tag.inserting(
                            TranslatableCaption.of("generic.generic_merged").toComponent(player)
                    ))
            );
        } else if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_BACKUP_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_BACKUP_OTHER)
                    )
            );
        } else {
            final BackupProfile backupProfile = Objects.requireNonNull(this.backupManager.getProfile(plot));
            if (backupProfile instanceof NullBackupProfile) {
                player.sendMessage(
                        TranslatableCaption.of("backups.backup_impossible"),
                        TagResolver.resolver(
                                "plot", Tag.inserting(TranslatableCaption
                                        .of("generic.generic_other")
                                        .toComponent(player))
                        )
                );
            } else {
                backupProfile.createBackup().whenComplete((backup, throwable) -> {
                    if (throwable != null) {
                        player.sendMessage(
                                TranslatableCaption.of("backups.backup_save_failed"),
                                TagResolver.resolver("reason", Tag.inserting(Component.text(throwable.getMessage())))
                        );
                        throwable.printStackTrace();
                    } else {
                        player.sendMessage(TranslatableCaption.of("backups.backup_save_success"));
                    }
                });
            }
        }
    }

    @CommandDeclaration(command = "list",
            usage = "/plot backup list",
            category = CommandCategory.SETTINGS,
            requiredType = RequiredType.PLAYER,
            permission = "plots.backup.list")
    public void list(
            final Command command, final PlotPlayer<?> player, final String[] args,
            final RunnableVal3<Command, Runnable, Runnable> confirm,
            final RunnableVal2<Command, CommandResult> whenDone
    ) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
        } else if (!plot.hasOwner()) {
            player.sendMessage(
                    TranslatableCaption.of("backups.backup_impossible"),
                    TagResolver.resolver("plot", Tag.inserting(
                            TranslatableCaption.of("generic.generic_unowned").toComponent(player)
                    ))
            );
        } else if (plot.isMerged()) {
            player.sendMessage(
                    TranslatableCaption.of("backups.backup_impossible"),
                    TagResolver.resolver("plot", Tag.inserting(
                            TranslatableCaption.of("generic.generic_merged").toComponent(player)
                    ))
            );
        } else if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
        } else if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_BACKUP_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_BACKUP_OTHER)
                    )
            );
        } else {
            final BackupProfile backupProfile = Objects.requireNonNull(this.backupManager.getProfile(plot));
            if (backupProfile instanceof NullBackupProfile) {
                player.sendMessage(
                        TranslatableCaption.of("backups.backup_impossible"),
                        TagResolver.resolver("plot", Tag.inserting(
                                TranslatableCaption.of("generic.generic_other").toComponent(player)
                        ))
                );
            } else {
                backupProfile.listBackups().whenComplete((backups, throwable) -> {
                    if (throwable != null) {
                        player.sendMessage(
                                TranslatableCaption.of("backups.backup_list_failed"),
                                TagResolver.resolver("reason", Tag.inserting(Component.text(throwable.getMessage())))
                        );
                        throwable.printStackTrace();
                    } else {
                        player.sendMessage(
                                TranslatableCaption.of("backups.backup_list_header"),
                                TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toCommaSeparatedString())))
                        );
                        try {
                            for (int i = 0; i < backups.size(); i++) {
                                player.sendMessage(
                                        TranslatableCaption.of("backups.backup_list_entry"),
                                        TagResolver.builder()
                                                .tag("number", Tag.inserting(Component.text(i + 1)))
                                                .tag(
                                                        "value",
                                                        Tag.inserting(Component.text(DateTimeFormatter.RFC_1123_DATE_TIME.format(
                                                                ZonedDateTime.ofInstant(
                                                                        Instant.ofEpochMilli(backups.get(i).getCreationTime()),
                                                                        ZoneId.systemDefault()
                                                                ))))
                                                )
                                                .build()
                                );
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
            category = CommandCategory.SETTINGS,
            requiredType = RequiredType.PLAYER,
            permission = "plots.backup.load")
    public void load(
            final Command command, final PlotPlayer<?> player, final String[] args,
            final RunnableVal3<Command, Runnable, Runnable> confirm,
            final RunnableVal2<Command, CommandResult> whenDone
    ) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
        } else if (!plot.hasOwner()) {
            player.sendMessage(
                    TranslatableCaption.of("backups.backup_impossible"),
                    TagResolver.resolver("plot", Tag.inserting(
                            TranslatableCaption.of("generic.generic_unowned").toComponent(player)
                    ))
            );
        } else if (plot.isMerged()) {
            player.sendMessage(
                    TranslatableCaption.of("backups.backup_impossible"),
                    TagResolver.resolver("plot", Tag.inserting(
                            TranslatableCaption.of("generic.generic_merged").toComponent(player)
                    ))
            );
        } else if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
        } else if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_BACKUP_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_BACKUP_OTHER)
                    )
            );
        } else if (args.length == 0) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Usage: /plot backup save/list/load")))
            );
        } else {
            final int number;
            try {
                number = Integer.parseInt(args[0]);
            } catch (final Exception e) {
                player.sendMessage(
                        TranslatableCaption.of("invalid.not_a_number"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(args[0])))
                );
                return;
            }
            final BackupProfile backupProfile = Objects.requireNonNull(this.backupManager.getProfile(plot));
            if (backupProfile instanceof NullBackupProfile) {
                player.sendMessage(
                        TranslatableCaption.of("backups.backup_impossible"),
                        TagResolver.resolver(
                                "plot", Tag.inserting(
                                        TranslatableCaption.of("generic.generic_other").toComponent(player)
                                )
                        )
                );
            } else {
                backupProfile.listBackups().whenComplete((backups, throwable) -> {
                    if (throwable != null) {
                        Component reason;
                        if (throwable instanceof PlotSquaredException pe) {
                            reason = pe.getCaption().toComponent(player);
                        } else {
                            reason = Component.text(throwable.getMessage());
                        }
                        player.sendMessage(
                                TranslatableCaption.of("backups.backup_load_failure"),
                                TagResolver.resolver("reason", Tag.inserting(reason))
                        );
                        LOGGER.error("Error loading player ({}) backup", player.getName(), throwable);
                        return;
                    }
                    if (number < 1 || number > backups.size()) {
                        player.sendMessage(
                                TranslatableCaption.of("backups.backup_impossible"),
                                TagResolver.resolver(
                                        "plot",
                                        Tag.inserting(TranslatableCaption
                                                .of("generic.generic_invalid_choice")
                                                .toComponent(player))
                                )
                        );
                    } else {
                        final com.plotsquared.core.backup.Backup backup =
                                backups.get(number - 1);
                        if (backup == null || backup.getFile() == null || !Files
                                .exists(backup.getFile())) {
                            player.sendMessage(
                                    TranslatableCaption.of("backups.backup_impossible"),
                                    TagResolver.resolver(
                                            "plot",
                                            Tag.inserting(TranslatableCaption
                                                    .of("generic.generic_invalid_choice")
                                                    .toComponent(player))
                                    )
                            );
                        } else {
                            CmdConfirm.addPending(
                                    player, "/plot backup load " + number,
                                    () -> backupProfile.restoreBackup(backup, player)
                                            .whenComplete((n, error) -> {
                                                if (error != null) {
                                                    player.sendMessage(
                                                            TranslatableCaption.of("backups.backup_load_failure"),
                                                            TagResolver.resolver(
                                                                    "reason",
                                                                    Tag.inserting(Component.text(error.getMessage()))
                                                            )
                                                    );
                                                } else {
                                                    player.sendMessage(TranslatableCaption.of("backups.backup_load_success"));
                                                }
                                            })
                            );
                        }
                    }
                });
            }
        }
    }

}
