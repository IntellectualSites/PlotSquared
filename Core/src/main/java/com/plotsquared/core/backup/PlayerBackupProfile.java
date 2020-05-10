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
package com.plotsquared.core.backup;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.SchematicHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A profile associated with a player (normally a plot owner) and a
 * plot, which is used to store and retrieve plot backups
 * {@inheritDoc}
 */
@RequiredArgsConstructor
public class PlayerBackupProfile implements BackupProfile {

    private final UUID owner;
    private final Plot plot;
    private final BackupManager backupManager;

    private static boolean isValidFile(@NotNull final Path path) {
        final String name = path.getFileName().toString();
        return name.endsWith(".schem") || name.endsWith(".schematic");
    }

    @Override @NotNull public CompletableFuture<List<Backup>> listBackups() {
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
                        final BasicFileAttributes
                            basicFileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
                        backups.add(new Backup(this, basicFileAttributes.creationTime().toMillis(), file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return backups;
        });
    }

    @Override public void destroy() throws IOException {
        Files.delete(this.getBackupDirectory());
    }

    @NotNull public Path getBackupDirectory() {
        return backupManager.getBackupPath().resolve(plot.getArea().getId())
            .resolve(plot.getId().toCommaSeparatedString()).resolve(owner.toString());
    }

    @Override @NotNull public CompletableFuture<Backup> createBackup() {
        final CompletableFuture<Backup> future = new CompletableFuture<>();
        this.listBackups().thenAcceptAsync(backups -> {
            if (backups.size() == backupManager.getBackupLimit()) {
                backups.get(backups.size() - 1).delete();
            }
            final List<Plot> plots = Collections.singletonList(plot);
            final boolean result = SchematicHandler.manager.exportAll(plots, null, null, () ->
                future.complete(new Backup(this, System.currentTimeMillis(), null)));
            if (!result) {
                future.completeExceptionally(new RuntimeException("Failed to complete the backup"));
            }
        });
        return future;
    }

}
