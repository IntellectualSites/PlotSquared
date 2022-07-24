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
package com.plotsquared.core.uuid;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * A pair consisting of a UUID and a username
 */
public class UUIDMapping {

    private final UUID uuid;
    private final String username;

    public UUIDMapping(final @NonNull UUID uuid, final @NonNull String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public @NonNull String getUsername() {
        return this.username;
    }

    public @NonNull UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UUIDMapping that = (UUIDMapping) o;
        return uuid.equals(that.uuid) && username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, username);
    }

    /**
     * @deprecated This method is not meant to be invoked or overridden, with no replacement.
     */
    @Deprecated(forRemoval = true, since = "6.6.0")
    protected boolean canEqual(final Object other) {
        return other instanceof UUIDMapping;
    }

}
