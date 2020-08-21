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

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Object representing a plot backup. This does not actually contain the
 * backup itself, it is just a pointer to an available backup
 */
public class Backup {

    private final BackupProfile owner;
    private final long creationTime;
    @Nullable private final Path file;

    Backup(final BackupProfile owner, final long creationTime, final Path file) {
        this.owner = owner;
        this.creationTime = creationTime;
        this.file = file;
    }

    /**
     * Delete the backup
     */
    public void delete() {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BackupProfile getOwner() {
        return this.owner;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    @Nullable public Path getFile() {
        return this.file;
    }
}
