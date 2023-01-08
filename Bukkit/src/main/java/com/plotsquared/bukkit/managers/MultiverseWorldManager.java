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
package com.plotsquared.bukkit.managers;

import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Multiverse specific manager that informs Multiverse of
 * world creation by executing a console command
 *
 * @deprecated Deprecated and scheduled for removal without replacement
 *         in favor of the build in setup wizard.
 *         However, this class will be kept around for a while, given it's not a maintenance burden.
 */
@Deprecated
@Singleton
public class MultiverseWorldManager extends BukkitWorldManager {

    @Override
    public @Nullable World handleWorldCreation(final @NonNull String worldName, final @Nullable String generator) {
        // First let Bukkit register the world
        this.setGenerator(worldName, generator);
        // Then we send the console command
        final StringBuilder commandBuilder = new StringBuilder("mv create ")
                .append(worldName).append(" normal");
        if (generator != null) {
            commandBuilder.append(" -g ").append(generator);
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), commandBuilder.toString());
        return Bukkit.getWorld(worldName);
    }

    @Override
    public String getName() {
        return "bukkit-multiverse";
    }

}
