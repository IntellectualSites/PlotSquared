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

import com.google.common.util.concurrent.RateLimiter;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.enginehub.squirrelid.Profile;
import org.enginehub.squirrelid.resolver.HttpRepositoryService;
import org.enginehub.squirrelid.resolver.ProfileService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * UUID service using SquirrelID
 */
@SuppressWarnings("UnstableApiUsage")
public class SquirrelIdUUIDService implements UUIDService {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + SquirrelIdUUIDService.class.getSimpleName());

    private final ProfileService profileService;
    private final RateLimiter rateLimiter;

    /**
     * Create a new SquirrelID UUID service
     *
     * @param rateLimit Mojangs rate limit is 600 requests per 10 minutes.
     *                  This parameter specifies how many of those requests
     *                  we can use before our internal rate limit kicks in.
     */
    public SquirrelIdUUIDService(final int rateLimit) {
        this.profileService = HttpRepositoryService.forMinecraft();
        // RateLimiter uses request per seconds. The constructor
        // parameter rateLimit is requests per 600 seconds
        this.rateLimiter = RateLimiter.create(rateLimit / 600.0D);
    }

    @Override
    public @NonNull List<UUIDMapping> getNames(final @NonNull List<UUID> uuids) {
        final List<UUIDMapping> results = new ArrayList<>(uuids.size());
        this.rateLimiter.acquire(uuids.size());
        try {
            try {
                for (final Profile profile : this.profileService.findAllByUuid(uuids)) {
                    results.add(new UUIDMapping(profile.getUniqueId(), profile.getName()));
                }
            } catch (final IllegalArgumentException illegalArgumentException) {
                //
                // This means that the UUID was invalid for whatever reason, we'll try to
                // go through them one by one
                //
                if (uuids.size() >= 2) {
                    if (Settings.DEBUG) {
                        LOGGER.info("(UUID) Found invalid UUID in batch. Will try each UUID individually.");
                    }
                    for (final UUID uuid : uuids) {
                        final List<UUIDMapping> result = this.getNames(Collections.singletonList(uuid));
                        if (result.isEmpty()) {
                            continue;
                        }
                        results.add(result.get(0));
                    }
                } else if (uuids.size() == 1 && Settings.DEBUG) {
                    LOGGER.info("(UUID) Found invalid UUID: {}", uuids.get(0));
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public @NonNull List<UUIDMapping> getUUIDs(final @NonNull List<String> usernames) {
        final List<UUIDMapping> results = new ArrayList<>(usernames.size());
        this.rateLimiter.acquire(usernames.size());
        try {
            for (final Profile profile : this.profileService.findAllByName(usernames)) {
                results.add(new UUIDMapping(profile.getUniqueId(), profile.getName()));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return results;
    }

}
