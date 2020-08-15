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
package com.plotsquared.bukkit.inject;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.backup.BackupProfile;
import com.plotsquared.core.backup.NullBackupManager;
import com.plotsquared.core.backup.PlayerBackupProfile;
import com.plotsquared.core.backup.SimpleBackupManager;
import com.plotsquared.core.inject.factory.PlayerBackupProfileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + BackupModule.class.getSimpleName());

    @Override protected void configure() {
        try {
            install(new FactoryModuleBuilder()
                .implement(BackupProfile.class, PlayerBackupProfile.class).build(PlayerBackupProfileFactory.class));
            bind(BackupManager.class).to(SimpleBackupManager.class);
        } catch (final Exception e) {
            logger.error("[P2] Failed to initialize backup manager", e);
            logger.error("[P2] Backup features will be disabled");
            bind(BackupManager.class).to(NullBackupManager.class);
        }
    }

}
