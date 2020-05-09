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
import com.plotsquared.core.plot.Plot;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class BackupManager {

    @Getter private final Path backupPath;

    public BackupManager() throws Exception {
        this.backupPath = PlotSquared.imp().getDirectory().toPath().resolve("backups");
        if (!Files.exists(backupPath)) {
            Files.createDirectory(backupPath);
        }
    }

    /**
     * Get  the backup profile for a plot based on its
     * current owner (if there is one)
     *
     * @param plot Plot to get the backup profile for
     * @return Backup profile
     */
    @NotNull public BackupProfile getProfile(@NotNull final Plot plot) {
        if (plot.hasOwner()) {
            return new PlayerBackupProfile(plot.getOwnerAbs(), plot, this);
        }
        return new NullBackupProfile();
    }

}
