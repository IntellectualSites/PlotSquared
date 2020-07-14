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
package com.plotsquared.bukkit.managers;

import com.google.inject.Singleton;
import org.bukkit.World;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.world.WorldConfiguration;
import se.hyperver.hyperverse.world.WorldConfigurationBuilder;
import se.hyperver.hyperverse.world.WorldFeatures;
import se.hyperver.hyperverse.world.WorldType;

/**
 * Hyperverse specific manager that creates worlds
 * using Hyperverse's API
 */
@Singleton public class HyperverseWorldManager extends BukkitWorldManager {

    @Override @Nullable
    public World handleWorldCreation(@Nonnull String worldName, @Nullable String generator) {
        // First let Bukkit register the world
        this.setGenerator(worldName, generator);
        // Create the world
        final WorldConfigurationBuilder worldConfigurationBuilder = WorldConfiguration.builder()
            .setName(worldName).setType(WorldType.OVER_WORLD);
        if (generator != null) {
            worldConfigurationBuilder.setGenerator(generator).setWorldFeatures(WorldFeatures.FLATLAND);
        }
        try {
            return Hyperverse.getApi().createWorld(worldConfigurationBuilder.createWorldConfiguration())
                .getBukkitWorld();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public String getName() {
        return "bukkit-hyperverse";
    }

}
