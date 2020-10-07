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
package com.plotsquared.bukkit.uuid;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UUID service that uses the Paper profile API
 */
public class PaperUUIDService implements UUIDService {

    @Override @Nonnull public List<UUIDMapping> getNames(@Nonnull final List<UUID> uuids) {
        final List<UUIDMapping> mappings = new ArrayList<>(uuids.size());
        for (final UUID uuid : uuids) {
            final PlayerProfile playerProfile = Bukkit.createProfile(uuid);
            if ((playerProfile.isComplete() || playerProfile.completeFromCache()) && playerProfile.getId() != null) {
                mappings.add(new UUIDMapping(playerProfile.getId(), playerProfile.getName()));
            }
        }
        return mappings;
    }

    @Override @Nonnull public List<UUIDMapping> getUUIDs(@Nonnull final List<String> usernames) {
        final List<UUIDMapping> mappings = new ArrayList<>(usernames.size());
        for (final String username : usernames) {
            final PlayerProfile playerProfile = Bukkit.createProfile(username);
            if ((playerProfile.isComplete() || playerProfile.completeFromCache()) && playerProfile.getId() != null) {
                mappings.add(new UUIDMapping(playerProfile.getId(), playerProfile.getName()));
            }
        }
        return mappings;
    }

}
