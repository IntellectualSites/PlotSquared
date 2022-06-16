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
import com.plotsquared.bukkit.permissions.VaultPermissionHandler;
import com.plotsquared.core.permissions.PermissionHandler;
import org.bukkit.Bukkit;

public class PermissionModule extends AbstractModule {

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

}
