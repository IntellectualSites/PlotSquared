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
package com.plotsquared.core.backup;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A profile associated with a player (normally a plot owner) and a
 * plot, which is used to store and retrieve plot backups
 * {@inheritDoc}
 */
public class PlayerBackupProfile implements BackupProfile {

    static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    private final UUID owner;
    private final Plot plot;
    private final BackupManager backupManager;
    private final SchematicHandler schematicHandler;

    @Inject public PlayerBackupProfile(@Assisted @Nonnull final UUID owner, @Assisted @Nonnull final Plot plot,
        @Nonnull final BackupManager backupManager, @Nonnull final SchematicHandler schematicHandler) {
        this.owner = owner;
        this.plot = plot;
        this.backupManager = backupManager;
        this.schematicHandler = schematicHandler;
    }

    private volatile List<Backup> backupCache;
    private final Object backupLock = new Object();

    private static boolean isValidFile(@Nonnull final Path path) {
        final String name = path.getFileName().toString();
        return name.endsWith(".schem") || name.endsWith(".schematic");
    }

    @Override @Nonnull public CompletableFuture<List<Backup>> listBackups() {
        synchronized (this.backupLock) {
            if (this.backupCache != null) {
                return CompletableFuture.completedFuture(backupCache);
            }
            return CompletableFuture.supplyAsync(() -> {
                final Path path = this.getBackupDirectory();
                if (!Files.exists(path)) {
                    try {
                        Files.createDirectories(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Collections.emptyList();
                    }
                }
                final List<Backup> backups = new ArrayList<>();
                try {
                    Files.walk(path).filter(PlayerBackupProfile::isValidFile).forEach(file -> {
                        try {
                            final BasicFileAttributes basicFileAttributes =
                                Files.readAttributes(file, BasicFileAttributes.class);
                            backups.add(
                                new Backup(this, basicFileAttributes.creationTime().toMillis(), file));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                backups.sort(Comparator.comparingLong(Backup::getCreationTime).reversed());
                return (this.backupCache = backups);
            });
        }
    }

    @Override public void destroy() {
        this.listBackups().whenCompleteAsync((backups, error) -> {
           if (error != null) {
               error.printStackTrace();
           }
           backups.forEach(Backup::delete);
           this.backupCache = null;
        });
    }

    @Nonnull public Path getBackupDirectory() {
        return resolve(resolve(resolve(backupManager.getBackupPath(), Objects.requireNonNull(plot.getArea().toString(), "plot area id")),
            Objects.requireNonNull(plot.getId().toDashSeparatedString(), "plot id")), Objects.requireNonNull(owner.toString(), "owner"));
    }

    private static Path resolve(@Nonnull final Path parent, final String child) {
        Path path = parent;
        try {
            if (!Files.exists(parent)) {
                Files.createDirectory(parent);
            }
            path = parent.resolve(child);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    @Override @Nonnull public CompletableFuture<Backup> createBackup() {
        final CompletableFuture<Backup> future = new CompletableFuture<>();
        this.listBackups().thenAcceptAsync(backups -> {
            synchronized (this.backupLock) {
                if (backups.size() == backupManager.getBackupLimit()) {
                    backups.get(backups.size() - 1).delete();
                }
                final List<Plot> plots = Collections.singletonList(plot);
                final boolean result = this.schematicHandler.exportAll(plots, getBackupDirectory().toFile(),
                    "%world%-%id%-" + System.currentTimeMillis(), () ->
                    future.complete(new Backup(this, System.currentTimeMillis(), null)));
                if (!result) {
                    future.completeExceptionally(new RuntimeException("Failed to complete the backup"));
                }
                this.backupCache = null;
            }
        });
        return future;
    }

    @Override @Nonnull public CompletableFuture<Void> restoreBackup(@Nonnull final Backup backup) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        if (backup.getFile() == null || !Files.exists(backup.getFile())) {
            future.completeExceptionally(new IllegalArgumentException("The specific backup does not exist"));
        } else {
            TaskManager.runTaskAsync(() -> {
                Schematic schematic = null;
                try {
                    schematic = this.schematicHandler.getSchematic(backup.getFile().toFile());
                } catch (SchematicHandler.UnsupportedFormatException e) {
                    e.printStackTrace();
                }
                if (schematic == null) {
                    future.completeExceptionally(new IllegalArgumentException("The backup is non-existent or not in the correct format"));
                } else {
                    this.schematicHandler.paste(schematic, plot, 0, 1, 0, false, new RunnableVal<Boolean>() {
                        @Override public void run(Boolean value) {
                            if (value) {
                                future.complete(null);
                            } else {
                                future.completeExceptionally(new RuntimeException(MINI_MESSAGE.stripTokens(
                                    TranslatableCaption.of("schematics.schematic_paste_failed").getComponent(ConsolePlayer.getConsole()))));
                            }
                        }
                    });
                }
            });
        }
        return future;
    }

}
