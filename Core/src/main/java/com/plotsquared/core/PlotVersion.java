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
package com.plotsquared.core;

public class PlotVersion {
    public final int year, month, day, hash;
    public final String versionString;
    public final int[] version;

    public PlotVersion(int year, int month, int day, int hash, String versionString) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hash = hash;
        this.versionString = versionString.substring(versionString.indexOf('=') + 1);
        version = new int[3];
        String[] verArray = versionString.substring(versionString.indexOf('=') + 1).split("\\.");
        version[0] = verArray.length > 0 ? Integer.parseInt(verArray[0]) : 0;
        version[1] = verArray.length > 1 ? Integer.parseInt(verArray[1]) : 0;
        version[2] = verArray.length > 2 ? Integer.parseInt(verArray[2]) : 0;
    }

    public PlotVersion(String versionString, String commit, String date) {
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

    public static PlotVersion tryParse(String versionString, String commit, String date) {
        try {
            return new PlotVersion(versionString, commit, date);
        } catch (Exception e) {
            e.printStackTrace();
            return new PlotVersion(0, 0, 0, 0, "0");
        }
    }

    public String versionString() {
        if (hash == 0 && versionString == null) {
            return "NoVer-SNAPSHOT";
        } else {
            return versionString;
        }
    }

    @Override public String toString() {
        if (hash == 0 && versionString == null) {
            return "PlotSquared-NoVer-SNAPSHOT";
        } else {
            return "PlotSquared-" + versionString;
        }
    }

    /**
     * Compare a given version string with the one cached here.
     *
     * @param versionString the version to compare
     * @return true if the given version is a "later" version
     */
    public boolean isLaterVersion(String versionString) {
        String[] verArray = versionString.split("\\.");
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
     * @return true if the given version is a "later" version
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
