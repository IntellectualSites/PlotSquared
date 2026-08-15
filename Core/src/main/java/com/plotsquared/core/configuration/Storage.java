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
package com.plotsquared.core.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Storage extends Config {

    public static String PREFIX = "";

    public static void save(File file) {
        save(file, Storage.class);
    }

    public static void load(File file) {
        load(file, Storage.class);
    }

    @Comment("MySQL section")
    public static final class MySQL {

        @Comment("Should MySQL be used?")
        public static boolean USE = false;
        public static String HOST = "localhost";
        public static String PORT = "3306";
        public static String USER = "root";
        public static String PASSWORD = "password";
        public static String DATABASE = "plot_db";

        @Comment("Set additional properties: https://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html")
        public static List<String>
                PROPERTIES = new ArrayList<>(Collections.singletonList("useSSL=false"));

    }


    @Comment("SQLite section")
    public static final class SQLite {

        @Comment("Should SQLite be used?")
        public static boolean USE = true;
        @Comment("The file to use")
        public static String DB = "storage";

    }

}
