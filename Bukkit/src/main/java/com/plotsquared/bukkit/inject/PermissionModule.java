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
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.plotsquared.bukkit.permissions.BukkitPermissionHandler;
import com.plotsquared.bukkit.permissions.BukkitRangedPermissionResolver;
import com.plotsquared.bukkit.permissions.LuckPermsRangedPermissionResolver;
import com.plotsquared.bukkit.permissions.VaultPermissionHandler;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.permissions.RangedPermissionResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

public class PermissionModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + PermissionModule.class.getSimpleName());

    @Provides
    @Singleton
    PermissionHandler providePermissionHandler() {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                return new VaultPermissionHandler();
            }
        } catch (final Exception ignored) {
        }
        return new BukkitPermissionHandler();
    }

    @Provides
    @Singleton
    RangedPermissionResolver provideRangedPermissionResolver() {
        if (Settings.Permissions.USE_LUCKPERMS_RANGE_RESOLVER) {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                LOGGER.info("Using experimental LuckPerms ranged permission resolver");
                return new LuckPermsRangedPermissionResolver();
            }
            LOGGER.warn("Enabled LuckPerms ranged permission resolver, but LuckPerms is not installed. " +
                    "Falling back to default Bukkit ranged permission resolver");
        }
        return new BukkitRangedPermissionResolver();
    }

}
