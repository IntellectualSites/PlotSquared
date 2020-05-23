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
package com.plotsquared.bukkit.uuid;

import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UUID service that uses the LuckPerms API
 */
public class LuckPermsUUIDService implements UUIDService {

    private final LuckPerms luckPerms;

    public LuckPermsUUIDService() {
        final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
        } else {
            throw new IllegalStateException("LuckPerms not available");
        }
    }

    @Override @NotNull public List<UUIDMapping> getNames(@NotNull final List<UUID> uuids) {
       final List<UUIDMapping> mappings = new ArrayList<>(uuids.size());
       final UserManager userManager = this.luckPerms.getUserManager();
       for (final UUID uuid : uuids) {
           try {
               final String username = userManager.lookupUsername(uuid).get();
               if (username != null) {
                   mappings.add(new UUIDMapping(uuid, username));
               }
           } catch (final Exception ignored) {}
       }
       return mappings;
    }

    @Override @NotNull public List<UUIDMapping> getUUIDs(@NotNull final List<String> usernames) {
        final List<UUIDMapping> mappings = new ArrayList<>(usernames.size());
        final UserManager userManager = this.luckPerms.getUserManager();
        for (final String username : usernames) {
            try {
                final UUID uuid = userManager.lookupUniqueId(username).get();
                if (username != null) {
                    mappings.add(new UUIDMapping(uuid, username));
                }
            } catch (final Exception ignored) {}
        }
        return mappings;
    }

}
