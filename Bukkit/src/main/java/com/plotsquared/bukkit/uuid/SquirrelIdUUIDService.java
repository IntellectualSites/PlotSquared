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

import com.google.common.util.concurrent.RateLimiter;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDService;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;
import com.sk89q.squirrelid.resolver.ProfileService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UUID service using SquirrelID
 */
@SuppressWarnings("UnstableApiUsage")
public class SquirrelIdUUIDService implements UUIDService {

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

    @Override @NotNull public List<UUIDMapping> getNames(@NotNull final List<UUID> uuids) {
        final List<UUIDMapping> results = new ArrayList<>(uuids.size());
        this.rateLimiter.acquire(uuids.size());
        try {
            for (final Profile profile : this.profileService.findAllById(uuids)) {
                results.add(new UUIDMapping(profile.getUniqueId(), profile.getName()));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override @NotNull public List<UUIDMapping> getUUIDs(@NotNull final List<String> usernames) {
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
