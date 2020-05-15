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

import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.util.PlatformWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Default Bukkit world manager. It will handle world creation by
 * registering the generator in bukkit.yml
 */
public class BukkitWorldManager implements PlatformWorldManager<World> {

    @Override public void initialize() {
    }

    @Override @Nullable
    public World handleWorldCreation(@NotNull String worldName, @Nullable String generator) {
        this.setGenerator(worldName, generator);
        final WorldCreator wc = new WorldCreator(worldName);
        wc.environment(World.Environment.NORMAL);
        if (generator != null) {
            wc.generator(generator);
            wc.type(WorldType.FLAT);
        }
        return Bukkit.createWorld(wc);
    }

    protected void setGenerator(@Nullable final String worldName, @Nullable final String generator) {
        if (generator == null) {
            return;
        }
        File file = new File("bukkit.yml").getAbsoluteFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set(String.format("worlds.%s.generator", worldName), generator);
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public String getName() {
        return "bukkit";
    }

}
