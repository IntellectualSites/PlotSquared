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
package com.plotsquared.core.backup;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.exception.PlotSquaredException;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + PlayerBackupProfile.class.getSimpleName());

    private final UUID owner;
    private final Plot plot;
    private final BackupManager backupManager;
    private final SchematicHandler schematicHandler;
    private final Object backupLock = new Object();
    private volatile List<Backup> backupCache;

    @Inject
    public PlayerBackupProfile(
            @Assisted final @NonNull UUID owner, @Assisted final @NonNull Plot plot,
            final @NonNull BackupManager backupManager, final @NonNull SchematicHandler schematicHandler
    ) {
        this.owner = owner;
        this.plot = plot;
        this.backupManager = backupManager;
        this.schematicHandler = schematicHandler;
    }

    private static boolean isValidFile(final @NonNull Path path) {
        final String name = path.getFileName().toString();
        return name.endsWith(".schem") || name.endsWith(".schematic");
    }

    private static Path resolve(final @NonNull Path parent, final String child) {
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
            LOGGER.error("Error resolving {} from {}", child, parent, e);
        }
        return path;
    }

    @Override
    public @NonNull CompletableFuture<List<Backup>> listBackups() {
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
                        LOGGER.error("Error creating directory {}", path, e);
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
                            LOGGER.error("Error getting attributes for file {} to create backup", file, e);
                        }
                    });
                } catch (IOException e) {
                    LOGGER.error("Error walking files from {}", path, e);
                }
                backups.sort(Comparator.comparingLong(Backup::getCreationTime).reversed());
                return (this.backupCache = backups);
            });
        }
    }

    @Override
    public void destroy() {
        this.listBackups().whenCompleteAsync((backups, error) -> {
            if (error != null) {
                LOGGER.error("Error while listing backups", error);
            }
            backups.forEach(Backup::delete);
            this.backupCache = null;
        });
    }

    public @NonNull Path getBackupDirectory() {
        return resolve(
                resolve(
                        resolve(backupManager.getBackupPath(), Objects.requireNonNull(plot.getArea().toString(), "plot area id")),
                        Objects.requireNonNull(plot.getId().toDashSeparatedString(), "plot id")
                ), Objects.requireNonNull(owner.toString(), "owner")
        );
    }

    @Override
    public @NonNull CompletableFuture<Backup> createBackup() {
        final CompletableFuture<Backup> future = new CompletableFuture<>();
        this.listBackups().thenAcceptAsync(backups -> {
            synchronized (this.backupLock) {
                if (backups.size() == backupManager.getBackupLimit()) {
                    backups.get(backups.size() - 1).delete();
                }
                final List<Plot> plots = Collections.singletonList(plot);
                final boolean result = this.schematicHandler.exportAll(
                        plots, getBackupDirectory().toFile(),
                        "%world%-%id%-" + System.currentTimeMillis(), () ->
                                future.complete(new Backup(this, System.currentTimeMillis(), null))
                );
                if (!result) {
                    future.completeExceptionally(new RuntimeException("Failed to complete the backup"));
                }
                this.backupCache = null;
            }
        });
        return future;
    }

    @Override
    public @NonNull CompletableFuture<Void> restoreBackup(final @NonNull Backup backup, @Nullable PlotPlayer<?> player) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        if (backup.getFile() == null || !Files.exists(backup.getFile())) {
            future.completeExceptionally(new IllegalArgumentException("The specific backup does not exist"));
        } else {
            TaskManager.runTaskAsync(() -> {
                Schematic schematic = null;
                try {
                    schematic = this.schematicHandler.getSchematic(backup.getFile().toFile());
                } catch (SchematicHandler.UnsupportedFormatException e) {
                    LOGGER.error("Unsupported format for backup {}", backup.getFile(), e);
                }
                if (schematic == null) {
                    future.completeExceptionally(new IllegalArgumentException(
                            "The backup is non-existent or not in the correct format"));
                } else {
                    this.schematicHandler.paste(
                            schematic,
                            plot,
                            0,
                            plot.getArea().getMinBuildHeight(),
                            0,
                            false,
                            player,
                            new RunnableVal<>() {
                                @Override
                                public void run(Boolean value) {
                                    if (value) {
                                        future.complete(null);
                                    } else {
                                        future.completeExceptionally(new PlotSquaredException(
                                                TranslatableCaption
                                                        .of("schematics.schematic_paste_failed")));
                                    }
                                }
                            }
                    );
                }
            });
        }
        return future;
    }

}
