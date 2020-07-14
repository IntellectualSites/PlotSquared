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
package com.plotsquared.core.uuid.offline;

import com.google.common.base.Charsets;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;
import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Name provider service that creates UUIDs from usernames
 */
public class OfflineModeUUIDService implements UUIDService {

    @Nonnull protected final UUID getFromUsername(@Nonnull String username) {
        if (Settings.UUID.FORCE_LOWERCASE) {
            username = username.toLowerCase(Locale.ENGLISH);
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
    }

    @Override @Nonnull public List<UUIDMapping> getNames(@Nonnull final List<UUID> uuids) {
        return Collections.emptyList();
    }

    @Override @Nonnull public List<UUIDMapping> getUUIDs(@Nonnull List<String> usernames) {
        final List<UUIDMapping> mappings = new ArrayList<>(usernames.size());
        for (final String username : usernames) {
            mappings.add(new UUIDMapping(getFromUsername(username), username));
        }
        return mappings;
    }

}
