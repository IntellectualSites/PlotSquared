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

import com.google.inject.Singleton;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * {@inheritDoc}
 */
@Singleton
public class NullBackupManager implements BackupManager {

    @Override
    public @NonNull BackupProfile getProfile(@NonNull Plot plot) {
        return new NullBackupProfile();
    }

    @Override
    public void automaticBackup(
            @Nullable PlotPlayer<?> plotPlayer,
            @NonNull Plot plot, @NonNull Runnable whenDone
    ) {
        whenDone.run();
    }

    @Override
    public @NonNull Path getBackupPath() {
        return Objects.requireNonNull(PlotSquared.platform()).getDirectory().toPath();
    }

    @Override
    public int getBackupLimit() {
        return 0;
    }

    @Override
    public boolean shouldAutomaticallyBackup() {
        return false;
    }

}
