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
package com.plotsquared.core.uuid.offline;

import com.google.common.base.Charsets;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Name provider service that creates UUIDs from usernames
 */
public class OfflineModeUUIDService implements UUIDService {

    @NonNull
    protected final UUID getFromUsername(@NonNull String username) {
        if (Settings.UUID.FORCE_LOWERCASE) {
            username = username.toLowerCase(Locale.ENGLISH);
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
    }

    @Override
    public @NonNull List<@NonNull UUIDMapping> getNames(final @NonNull List<@NonNull UUID> uuids) {
        return Collections.emptyList();
    }

    @Override
    public @NonNull List<@NonNull UUIDMapping> getUUIDs(@NonNull List<@NonNull String> usernames) {
        final List<UUIDMapping> mappings = new ArrayList<>(usernames.size());
        for (final String username : usernames) {
            mappings.add(new UUIDMapping(getFromUsername(username), username));
        }
        return mappings;
    }

}
