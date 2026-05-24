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
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Represents a minecraft version. For simplicity, and compatibility with modern versions, the `1.` prefix of older versions is
 * ignored and the minor version part becomes the major version part.
 * <p>
 * <ul>
 *     <li>`1.17.0` -> `(17, 0)`</li>
 *     <li>`26.2` -> `(26, 2)`</li>
 * </ul>
 *
 * @param major the major part of the version string. For versions pre 26.1 this is the minor version part
 * @param minor the minor part of the version string. For versions pre 26.1 this is the patch version part
 */
public record MinecraftVersion(int major, int minor, int patch) implements Comparable<MinecraftVersion> {

    /**
     * Minecraft 1.17 release version
     */
    public static final MinecraftVersion CAVES_AND_CLIFFS = new MinecraftVersion(17, 0, 0);
    /**
     * Minecraft 1.18 release version
     */
    public static final MinecraftVersion CAVES_AND_CLIFFS_2 = new MinecraftVersion(18, 0, 0);
    /**
     * Minecraft 1.19 release version
     */
    public static final MinecraftVersion THE_WILD_UPDATE = new MinecraftVersion(19, 0, 0);
    /**
     * Minecraft 1.20 release version
     */
    public static final MinecraftVersion TRAILS_AND_TALES = new MinecraftVersion(20, 0, 0);
    /**
     * Minecraft 1.21 release version
     */
    public static final MinecraftVersion TRICKY_TRIALS = new MinecraftVersion(21, 0, 0);
    /**
     * Minecraft 26.1 release version
     */
    public static final MinecraftVersion TINY_TAKEOVER = new MinecraftVersion(26, 1, 0);

    private static final Comparator<MinecraftVersion> COMPARATOR = Comparator
            .comparingInt(MinecraftVersion::major)
            .thenComparingInt(MinecraftVersion::minor);
    private static MinecraftVersion current;

    public static MinecraftVersion current() {
        if (current == null) {
            //noinspection deprecation
            int[] parts = PlotSquared.platform().serverVersion();
            if (parts.length < 2) {
                throw new IllegalStateException("Version string provided by platform is malformed: " + Arrays.toString(parts));
            }
            // if real major version part is `1`, we are running on legacy versions
            if (parts[0] == 1) {
                // legacy versions don't have real patch versions (as their major part is basically a dummy)
                current = new MinecraftVersion(parts[1], parts.length > 2 ? parts[2] : 0, 0);
            } else {
                // modern versions do have a optional patch version (e.g. 26.1.2)
                current = new MinecraftVersion(parts[0], parts[1], parts.length > 2 ? parts[2] : 0);
            }
        }
        return current;
    }

    @Override
    public int compareTo(@NotNull final MinecraftVersion o) {
        return COMPARATOR.compare(this, o);
    }

    public boolean isNewerOrEqualThan(MinecraftVersion other) {
        if (this.major() > other.major()) {
            return true;
        }
        return this.major() == other.major() && this.minor() >= other.minor();
    }

    public boolean isOlderOrEqualThan(MinecraftVersion other) {
        if (this.major() < other.major()) {
            return true;
        }
        return this.major() == other.major() && this.minor() <= other.minor();
    }

    public boolean isOlderThan(MinecraftVersion other) {
        if (this.major() < other.major()) {
            return true;
        }
        return this.major() == other.major() && this.minor() < other.minor();
    }

    public boolean isNewerOrEqualThan(int otherMajor) {
        return this.major() >= otherMajor;
    }

    public boolean isNewerOrEqualThan(int otherMajor, int otherMinor) {
        return this.major() > otherMajor || (this.major() == otherMajor && this.minor() >= otherMinor);
    }

    public boolean isOlderOrEqualThan(int otherMajor) {
        return this.major() <= otherMajor;
    }

    public boolean isOlderOrEqualThan(int otherMajor, int otherMinor) {
        return this.major() < otherMajor || (this.major() == otherMajor && this.minor() <= otherMinor);
    }

}
