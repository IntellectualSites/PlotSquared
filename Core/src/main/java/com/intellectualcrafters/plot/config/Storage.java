package com.intellectualcrafters.plot.config;

import java.io.File;

public class Storage extends Config {

    @Final
    public static final String VERSION = null; // This value is set from PS before loading

    @Comment("MySQL section")
    public static final class MYSQL {
        @Comment("Should MySQL be used?")
        public static final boolean USE = false;
        public static final String HOST = "localhost";
        public static final String PORT = "3306";
        public static final String USER = "root";
        public static final String PASSWORD = "password";
        public static final String DATABASE = "plot_db";
    }

    @Comment("SQLite section")
    public static final class SQLITE {
        @Comment("Should SQLite be used?")
        public static boolean USE = true;
        @Comment("The file to use")
        public static String DB = "storage";
    }

    public static final String PREFIX = "";

    public static void save(File file) {
        save(file, Storage.class);
    }

    public static void load(File file) {
        load(file, Storage.class);
    }

}
