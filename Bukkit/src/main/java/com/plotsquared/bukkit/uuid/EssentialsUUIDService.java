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

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * UUID service using the EssentialsX API
 */
public class EssentialsUUIDService implements UUIDService {

    private final Essentials essentials;

    public EssentialsUUIDService() {
        this.essentials = Essentials.getPlugin(Essentials.class);
    }

    @Override @Nonnull public List<UUIDMapping> getNames(@Nonnull final List<UUID> uuids) {
        return Collections.emptyList();
    }

    @Override @Nonnull public List<UUIDMapping> getUUIDs(@Nonnull final List<String> usernames) {
        final List<UUIDMapping> mappings = new ArrayList<>(usernames.size());
        for (final String username : usernames) {
            try {
                final User user = essentials.getUser(username);
                if (user != null) {
                    final UUID uuid = user.getConfigUUID();
                    if (uuid != null) {
                        mappings.add(new UUIDMapping(uuid, username));
                    }
                }
            } catch (final Exception ignored){}
        }
        return mappings;
    }

}
