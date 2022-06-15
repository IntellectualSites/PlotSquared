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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;
import java.util.Objects;

public interface BackupManager {

    /**
     * This will perform an automatic backup of the plot iff the plot has an owner,
     * automatic backups are enabled.
     * Otherwise it will complete immediately.
     *
     * @param player   Player that triggered the backup
     * @param plot     Plot to perform the automatic backup on
     * @param whenDone Action that runs when the automatic backup has been completed
     */
    static void backup(@Nullable PlotPlayer<?> player, final @NonNull Plot plot, @NonNull Runnable whenDone) {
        Objects.requireNonNull(PlotSquared.platform()).backupManager().automaticBackup(player, plot, whenDone);
    }

    /**
     * Get the backup profile for a plot based on its
     * current owner (if there is one)
     *
     * @param plot Plot to get the backup profile for
     * @return Backup profile
     */
    @NonNull BackupProfile getProfile(final @NonNull Plot plot);

    /**
     * This will perform an automatic backup of the plot iff the plot has an owner,
     * automatic backups are enabled.
     * Otherwise it will complete immediately.
     *
     * @param player   Player that triggered the backup
     * @param plot     Plot to perform the automatic backup on
     * @param whenDone Action that runs when the automatic backup has been completed
     */
    void automaticBackup(@Nullable PlotPlayer<?> player, final @NonNull Plot plot, @NonNull Runnable whenDone);

    /**
     * Get the directory in which backups are stored
     *
     * @return Backup directory path
     */
    @NonNull Path getBackupPath();

    /**
     * Get the maximum amount of backups that may be stored for
     * a plot-owner combo
     *
     * @return Backup limit
     */
    int getBackupLimit();

    /**
     * Returns true if (potentially) destructive actions should cause
     * PlotSquared to create automatic plot backups
     *
     * @return {@code true} if automatic backups are enabled
     */
    boolean shouldAutomaticallyBackup();

}
