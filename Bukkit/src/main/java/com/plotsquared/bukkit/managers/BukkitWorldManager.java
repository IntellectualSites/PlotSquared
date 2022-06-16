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
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.util.PlatformWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default Bukkit world manager. It will handle world creation by
 * registering the generator in bukkit.yml
 */
@Singleton
public class BukkitWorldManager implements PlatformWorldManager<World> {

    @Override
    public void initialize() {
    }

    @Override
    public @Nullable World handleWorldCreation(@NonNull String worldName, @Nullable String generator) {
        this.setGenerator(worldName, generator);
        final WorldCreator wc = new WorldCreator(worldName);
        wc.environment(World.Environment.NORMAL);
        if (generator != null) {
            wc.generator(generator);
            wc.type(WorldType.FLAT);
        }
        return Bukkit.createWorld(wc);
    }

    protected void setGenerator(final @Nullable String worldName, final @Nullable String generator) {
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

    @Override
    public String getName() {
        return "bukkit";
    }

    @Override
    public Collection<String> getWorlds() {
        final List<World> worlds = Bukkit.getWorlds();
        final List<String> worldNames = new ArrayList<>();
        for (final World world : worlds) {
            worldNames.add(world.getName());
        }
        return worldNames;
    }

}
