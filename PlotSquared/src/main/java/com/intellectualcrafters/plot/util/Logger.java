////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;

/**
 * Logging of errors and debug messages.
 *
 * @author Citymonstret
 */
public class Logger {

    private static ArrayList<String> entries;
    private static File log;

    public static void setup(final File file) {
        log = file;
        entries = new ArrayList<>();
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                entries.add(line);
            }
            reader.close();
        } catch (final IOException e) {
            PlotMain.sendConsoleSenderMessage(C.PREFIX.s() + "File setup error Logger#setup");
        }
    }

    public static void write() throws IOException {
        final FileWriter writer = new FileWriter(log);
        for (final String string : entries) {
            writer.write(string + System.lineSeparator());
        }
        writer.close();
    }

    public static void add(final LogLevel level, final String string) {
        append("[" + level.toString() + "] " + string);
    }

    private static void append(final String string) {
        entries.add("[" + new Date().toString() + "]" + string);
    }

    public enum LogLevel {
        GENERAL("General"),
        WARNING("Warning"),
        DANGER("Danger");
        private final String name;

        LogLevel(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
