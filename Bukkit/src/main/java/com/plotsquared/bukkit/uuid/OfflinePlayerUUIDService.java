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
package com.plotsquared.bukkit.uuid;

import com.google.common.base.Charsets;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * UUID service that use {@link org.bukkit.OfflinePlayer offline players}
 */
public class OfflinePlayerUUIDService implements UUIDService {

    @Override
    public @NonNull List<UUIDMapping> getNames(final @NonNull List<UUID> uuids) {
        if (Settings.UUID.FORCE_LOWERCASE || Bukkit.getWorlds().isEmpty()) {
            return Collections.emptyList(); // This is useless now
        }
        final List<UUIDMapping> wrappers = new ArrayList<>(uuids.size());
        for (final UUID uuid : uuids) {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            try {
                if (offlinePlayer.hasPlayedBefore()) {
                    wrappers.add(new UUIDMapping(uuid, offlinePlayer.getName()));
                }
            } catch (final Exception ignored) {
            } /* This can be safely ignored. If this happens, it is
                                                    probably because it's called before the worlds have
                                                    been loaded. This is bad, but does not break anything */
        }
        return wrappers;
    }

    @Override
    public @NonNull List<UUIDMapping> getUUIDs(final @NonNull List<String> usernames) {
        final List<UUIDMapping> wrappers = new ArrayList<>(usernames.size());
        for (final String username : usernames) {
            if (Settings.UUID.OFFLINE) {
                if (Settings.UUID.FORCE_LOWERCASE) {
                    wrappers.add(new UUIDMapping(UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                            username.toLowerCase()).getBytes(Charsets.UTF_8)), username));
                } else {
                    wrappers.add(new UUIDMapping(UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                            username).getBytes(Charsets.UTF_8)), username));
                }
            } else {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
                if (offlinePlayer.hasPlayedBefore()) {
                    wrappers.add(new UUIDMapping(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
                }
            }
        }
        return wrappers;
    }

}
