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

package com.plotsquared.core.uuid;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

/**
 * A pair consisting of a UUID and a username
 */
public class UUIDMapping {

    private final UUID uuid;
    private final String username;

    public UUIDMapping(@Nonnull final UUID uuid, final String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Nonnull public String getUsername() {
        return this.username;
    }

    @Nonnull public UUID getUuid() {
        return this.uuid;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UUIDMapping)) {
            return false;
        }
        final UUIDMapping other = (UUIDMapping) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$uuid = this.getUuid();
        final Object other$uuid = other.getUuid();
        if (!Objects.equals(this$uuid, other$uuid)) {
            return false;
        }
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        return Objects.equals(this$username, other$username);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UUIDMapping;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $uuid = this.getUuid();
        result = result * PRIME + $uuid.hashCode();
        final Object $username = this.getUsername();
        result = result * PRIME + $username.hashCode();
        return result;
    }
}
