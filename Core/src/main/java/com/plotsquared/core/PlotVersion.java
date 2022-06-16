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
package com.plotsquared.core;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class PlotVersion {

    public final int year, month, day, hash;
    public final String versionString;
    public final int[] version;
    public final String suffix;

    public PlotVersion(
            final int year,
            final int month,
            final int day,
            final int hash,
            final String rawVersion
    ) {
        String versionString = rawVersion;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hash = hash;
        int dash = versionString.indexOf('-');
        if (dash != -1) {
            suffix = versionString.substring(dash);
            versionString = versionString.substring(0, dash);
        } else {
            suffix = "";
        }
        this.versionString = versionString.substring(versionString.indexOf('=') + 1);
        version = new int[3];
        String[] verArray = versionString.substring(versionString.indexOf('=') + 1).split("\\.");
        version[0] = verArray.length > 0 ? Integer.parseInt(verArray[0]) : 0;
        version[1] = verArray.length > 1 ? Integer.parseInt(verArray[1]) : 0;
        version[2] = verArray.length > 2 ? Integer.parseInt(verArray[2]) : 0;
    }

    public PlotVersion(
            final String rawVersion,
            final String commit,
            final String date
    ) {
        String versionString = rawVersion;
        int dash = versionString.indexOf('-');
        if (dash != -1) {
            suffix = versionString.substring(dash);
            versionString = versionString.substring(0, dash);
        } else {
            suffix = "";
        }
        this.versionString = versionString.substring(versionString.indexOf('=') + 1);
        version = new int[3];
        String[] verArray = this.versionString.split("\\.");
        version[0] = verArray.length > 0 ? Integer.parseInt(verArray[0]) : 0;
        version[1] = verArray.length > 1 ? Integer.parseInt(verArray[1]) : 0;
        version[2] = verArray.length > 2 ? Integer.parseInt(verArray[2]) : 0;

        this.hash = Integer.parseInt(commit.substring(commit.indexOf('=') + 1), 16);
        String[] split1 = date.substring(date.indexOf('=') + 1).split("\\.");
        this.year = Integer.parseInt(split1[0]);
        this.month = Integer.parseInt(split1[1]);
        this.day = Integer.parseInt(split1[2]);
    }

    public static @NonNull PlotVersion tryParse(
            final @NonNull String versionString,
            final @NonNull String commit,
            final @NonNull String date
    ) {
        try {
            return new PlotVersion(versionString, commit, date);
        } catch (Exception e) {
            e.printStackTrace();
            return new PlotVersion(0, 0, 0, 0, "0");
        }
    }

    public @NonNull String versionString() {
        if (hash == 0 && versionString == null) {
            return "NoVer-SNAPSHOT";
        } else {
            return versionString + suffix;
        }
    }

    @Override
    public String toString() {
        if (hash == 0 && versionString == null) {
            return "PlotSquared-NoVer-SNAPSHOT";
        } else {
            return "PlotSquared-" + versionString + suffix;
        }
    }

    /**
     * Compare a given version string with the one cached here.
     *
     * @param versionString the version to compare
     * @return {@code true} if the given version is a "later" version
     */
    public boolean isLaterVersion(final @NonNull String versionString) {
        int dash = versionString.indexOf('-');
        String[] verArray =
                versionString.substring(0, dash == -1 ? versionString.length() : dash).split("\\.");
        int one = Integer.parseInt(verArray[0]);
        int two = Integer.parseInt(verArray[1]);
        int three = Integer.parseInt(verArray[2]);
        if (one > version[0]) {
            return true;
        } else if (one == version[0] && two > version[1]) {
            return true;
        } else {
            return one == version[0] && two == version[1] && three > version[2];
        }
    }

    /**
     * Compare a given version with the one cached here.
     *
     * @param verArray the version to compare
     * @return {@code true} if the given version is a "later" version
     */
    public boolean isLaterVersion(int[] verArray) {
        if (verArray[0] > version[0]) {
            return true;
        } else if (verArray[0] == version[0] && verArray[1] > version[1]) {
            return true;
        } else {
            return verArray[0] == version[0] && verArray[1] == version[1]
                    && verArray[2] > version[2];
        }
    }

}
