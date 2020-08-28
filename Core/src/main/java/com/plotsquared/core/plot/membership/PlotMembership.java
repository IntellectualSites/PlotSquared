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
package com.plotsquared.core.plot.membership;

import com.google.common.base.Objects;
import com.plotsquared.core.plot.PlotPermission;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class PlotMembership {

    public static final String GLOBAL_NAMESPACE = "PlotSquared";

    private final String namespace; // Can be per-plot, or "global"
    private final String key;
    private final EnumSet<PlotPermission> permissions;

    public PlotMembership(@Nonnull final String key,
        @Nonnull final EnumSet<PlotPermission> permissions) {
        this(GLOBAL_NAMESPACE, key, permissions);
    }

    public PlotMembership(@Nonnull final String namespace, @Nonnull final String key,
        @Nonnull final EnumSet<PlotPermission> permissions) {
        this.namespace = namespace;
        this.key = key;
        this.permissions = permissions;
    }

    public boolean hasPermission(@Nonnull final PlotPermission permission) {
        return this.permissions.contains(permission);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PlotMembership that = (PlotMembership) o;
        return Objects.equal(namespace, that.namespace) && Objects.equal(key, that.key) && Objects
            .equal(permissions, that.permissions);
    }

    @Override public int hashCode() {
        return Objects.hashCode(namespace, key, permissions);
    }

}
