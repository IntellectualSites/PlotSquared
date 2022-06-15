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
package com.plotsquared.bukkit.inject;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.backup.BackupProfile;
import com.plotsquared.core.backup.NullBackupManager;
import com.plotsquared.core.backup.PlayerBackupProfile;
import com.plotsquared.core.backup.SimpleBackupManager;
import com.plotsquared.core.inject.factory.PlayerBackupProfileFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackupModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + BackupModule.class.getSimpleName());

    @Override
    protected void configure() {
        try {
            install(new FactoryModuleBuilder()
                    .implement(BackupProfile.class, PlayerBackupProfile.class).build(PlayerBackupProfileFactory.class));
            bind(BackupManager.class).to(SimpleBackupManager.class);
        } catch (final Exception e) {
            LOGGER.error("Failed to initialize backup manager", e);
            LOGGER.error("Backup features will be disabled");
            bind(BackupManager.class).to(NullBackupManager.class);
        }
    }

}
