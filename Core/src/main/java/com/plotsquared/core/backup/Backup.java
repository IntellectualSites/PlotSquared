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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Object representing a plot backup. This does not actually contain the
 * backup itself, it is just a pointer to an available backup
 */
public class Backup {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + Backup.class.getSimpleName());

    private final BackupProfile owner;
    private final long creationTime;
    @Nullable
    private final Path file;

    Backup(final BackupProfile owner, final long creationTime, @Nullable final Path file) {
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
                LOGGER.error("Error deleting backup at {}", file, e);
            }
        }
    }

    public BackupProfile getOwner() {
        return this.owner;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public @Nullable Path getFile() {
        return this.file;
    }

}
