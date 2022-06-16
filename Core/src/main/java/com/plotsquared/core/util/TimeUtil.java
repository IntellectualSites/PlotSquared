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

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class TimeUtil {

    private TimeUtil() {
    }

    /**
     * Format seconds into a string with the format
     * #y #w #d #h #s
     *
     * @param time Time to format
     * @return Formatted string
     */
    public static @NonNull String secToTime(@NonNegative long time) {
        StringBuilder toReturn = new StringBuilder();
        if (time >= 33868800) {
            int years = (int) (time / 33868800);
            time -= years * 33868800L;
            toReturn.append(years).append("y ");
        }
        if (time >= 604800) {
            int weeks = (int) (time / 604800);
            time -= weeks * 604800L;
            toReturn.append(weeks).append("w ");
        }
        if (time >= 86400) {
            int days = (int) (time / 86400);
            time -= days * 86400L;
            toReturn.append(days).append("d ");
        }
        if (time >= 3600) {
            int hours = (int) (time / 3600);
            time -= hours * 3600L;
            toReturn.append(hours).append("h ");
        }
        if (time >= 60) {
            int minutes = (int) (time / 60);
            time -= minutes * 60L;
            toReturn.append(minutes).append("m ");
        }
        if (toReturn.length() == 0 || time > 0) {
            toReturn.append(time).append("s ");
        }
        return toReturn.toString().trim();
    }

    /**
     * Parse a time string back into time
     *
     * @param string String to parse
     * @return Parsed time
     */
    @NonNegative
    public static long timeToSec(@NonNull String string) {
        if (MathMan.isInteger(string)) {
            return Long.parseLong(string);
        }
        string = string.toLowerCase().trim().toLowerCase();
        if (string.equalsIgnoreCase("false")) {
            return 0;
        }
        final String[] split = string.split(" ");
        long time = 0;
        for (final String value : split) {
            final int numbers = Integer.parseInt(value.replaceAll("[^\\d]", ""));
            String letters = value.replaceAll("[^a-z]", "");
            switch (letters) {
                case "week":
                case "weeks":
                case "wks":
                case "w":

                    time += 604800 * numbers;
                case "days":
                case "day":
                case "d":
                    time += 86400 * numbers;
                case "hour":
                case "hr":
                case "hrs":
                case "hours":
                case "h":
                    time += 3600 * numbers;
                case "minutes":
                case "minute":
                case "mins":
                case "min":
                case "m":
                    time += 60 * numbers;
                case "seconds":
                case "second":
                case "secs":
                case "sec":
                case "s":
                    time += numbers;
            }
        }
        return time;
    }

}
