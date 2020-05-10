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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.plot.Plot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@inheritDoc}
 */
@RequiredArgsConstructor public class SimpleBackupManager implements BackupManager {

    @Getter private final Path backupPath;
    private final boolean automaticBackup;
    @Getter private final int backupLimit;

    public SimpleBackupManager() throws Exception {
        this.backupPath = Objects.requireNonNull(PlotSquared.imp()).getDirectory().toPath().resolve("backups");
        if (!Files.exists(backupPath)) {
            Files.createDirectory(backupPath);
        }
        this.automaticBackup = Settings.Backup.AUTOMATIC_BACKUPS;
        this.backupLimit = Settings.Backup.BACKUP_LIMIT;
    }

    @Override @NotNull public BackupProfile getProfile(@NotNull final Plot plot) {
        if (plot.hasOwner() && !plot.isMerged()) {
            return new PlayerBackupProfile(plot.getOwnerAbs(), plot, this);
        }
        return new NullBackupProfile();
    }

    @Override @NotNull public CompletableFuture<?> automaticBackup(@NotNull final Plot plot) {
        final BackupProfile profile;
        if (!this.shouldAutomaticallyBackup() || (profile = getProfile(plot)) instanceof NullBackupProfile) {
            return CompletableFuture.completedFuture(null);
        }
        return profile.createBackup();
    }

    @Override public boolean shouldAutomaticallyBackup() {
        return this.automaticBackup;
    }

}
