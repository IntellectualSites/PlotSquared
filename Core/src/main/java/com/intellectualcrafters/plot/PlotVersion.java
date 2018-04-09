package com.intellectualcrafters.plot;

public class PlotVersion {
    public final int year, month, day, hash, build;

    public PlotVersion(String version) {
        String[] split = version.substring(version.indexOf('=') + 1).split("-");
        if (split[0].equals("unknown")) {
            this.year = month = day = hash = build = 0;
            return;
        }
        String[] date = split[0].split("\\.");
        this.year = Integer.parseInt(date[0]);
        this.month = Integer.parseInt(date[1]);
        this.day = Integer.parseInt(date[2]);
        if(split[1].equals("SNAPSHOT")) { // fallback when compiling with Maven
            this.hash = 0;
            this.build = 0;
        } else {
            this.hash = Integer.parseInt(split[1], 16);
            this.build = Integer.parseInt(split[2]);
        }
    }

    @Override
    public String toString() {
        if(hash == 0 && build == 0) {
            return "PlotSquared-" + year + "." + month + "." + day + "-SNAPSHOT";
        } else {
            return "PlotSquared-" + year + "." + month + "." + day + "-" + Integer.toHexString(hash) + "-" + build;
        }
    }

    public boolean isNewer(PlotVersion other) {
        return other.build < this.build;
    }
}
