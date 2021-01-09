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
 *                  Copyright (C) 2021 IntellectualSites
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
package com.plotsquared.bukkit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaVersionCheck {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + JavaVersionCheck.class.getSimpleName());

    private static int checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        final Matcher matcher = Pattern.compile("(?:1\\.)?(\\d+)").matcher(javaVersion);
        if (!matcher.find()) {
            logger.error("Failed to determine Java version; Could not parse: {}", javaVersion);
            return -1;
        }

        final String version = matcher.group(1);
        try {
            return Integer.parseInt(version);
        } catch (final NumberFormatException e) {
            logger.error("Failed to determine Java version; Could not parse {} from {}", version, javaVersion, e);
            return -1;
        }
    }

    public static void checkJvm() {
        if (checkJavaVersion() < 11) {
            logger.error("************************************************************");
            logger.error("* WARNING - YOU ARE RUNNING AN OUTDATED VERSION OF JAVA.");
            logger.error("* PLOTSQUARED WILL STOP BEING COMPATIBLE WITH THIS VERSION OF");
            logger.error("* JAVA WHEN MINECRAFT 1.17 IS RELEASED.");
            logger.error("*");
            logger.error("* Please update the version of Java to 11. When Minecraft 1.17");
            logger.error("* is released, support for versions of Java prior to 11 will");
            logger.error("* be dropped.");
            logger.error("*");
            logger.error("* Current Java version: {}", System.getProperty("java.version"));
            logger.error("************************************************************");
        }
        if (checkJavaVersion() >= 15) {
            logger.error("************************************************************");
            logger.error("* PlotSquared uses Nashorn for the internal scripting engine.");
            logger.error("* Within Java 15, Nashorn has been removed from Java.");
            logger.error("* Until we add a suitable workaround, you should stick to Java 11");
            logger.error("* to use all features of PlotSquared.");
            logger.error("************************************************************");
        }
    }

}
