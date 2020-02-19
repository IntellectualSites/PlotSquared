package com.github.intellectualsites.plotsquared.plot;

public class PlotVersion {
    public final int year, month, day, hash, build;

    public PlotVersion(String version, String commit, String date) {
        String[] split = version.substring(version.indexOf('=') + 1).split("\\.");
        this.build = Integer.parseInt(split[1]);
        this.hash = Integer.parseInt(commit.substring(commit.indexOf('=') + 1), 16);
        String[] split1 = date.substring(date.indexOf('=') + 1).split("\\.");
        this.year = Integer.parseInt(split1[0]);
        this.month = Integer.parseInt(split1[1]);
        this.day = Integer.parseInt(split1[2]);
    }

    @Override
    public String toString() {
        if (hash == 0 && build == 0) {
            return "PlotSquared-NoVer-SNAPSHOT";
        } else {
            return "PlotSquared-5." + build;
        }
    }
}
