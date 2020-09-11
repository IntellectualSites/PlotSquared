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

import com.plotsquared.core.player.PlotPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BackupProfile {

    /**
     * Asynchronously populate a list of available backups under this profile
     *
     * @return Future that will be completed with available backups
     */
    @Nonnull CompletableFuture<List<Backup>> listBackups();

    /**
     * Remove all backups stored for this profile
     */
    void destroy();

    /**
     * Get the directory containing the backups for this profile.
     * This directory may not actually exist.
     *
     * @return Folder that contains the backups for this profile
     */
    @Nonnull Path getBackupDirectory();

    /**
     * Create a backup of the plot. If the profile is at the
     * maximum backup capacity, the oldest backup will be deleted.
     *
     * @return Future that completes with the created backup.
     */
    @Nonnull CompletableFuture<Backup> createBackup();

    /**
     * Restore a backup
     *
     * @param backup Backup to restore
     * @param player The player restoring the backup
     * @return Future that completes when the backup has finished
     */
    @Nonnull CompletableFuture<Void> restoreBackup(@Nonnull final Backup backup, @Nullable PlotPlayer<?> player);

}
