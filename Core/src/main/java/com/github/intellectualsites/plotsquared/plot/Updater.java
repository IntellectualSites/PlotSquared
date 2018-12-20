package com.github.intellectualsites.plotsquared.plot;

import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

public class Updater {

    PlotVersion newVersion;
    private String changes;

    public String getChanges() {
        if (changes == null) {
            try (Scanner scanner = new Scanner(new URL("http://empcraft.com/plots/cl?" + Integer
                .toHexString(PlotSquared.get().getVersion().hash)).openStream(), "UTF-8")) {
                changes = scanner.useDelimiter("\\A").next();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        return changes;
    }

    public boolean isOutdated() {
        return newVersion != null;
    }

    public void update(String platform, PlotVersion currentVersion) {
        if (currentVersion == null || platform == null) {
            return;
        }
        try {
            String downloadUrl =
                "https://ci.athion.net/job/PlotSquared/lastSuccessfulBuild/artifact/target/PlotSquared-%platform%-%version%.jar";
            String versionUrl = "http://empcraft.com/plots/version.php?%platform%";
            URL url = new URL(versionUrl.replace("%platform%", platform));
            try (Scanner reader = new Scanner(url.openStream())) {
                String versionString = reader.next();
                PlotVersion version = new PlotVersion(versionString);
                if (version.isNewer(newVersion != null ? newVersion : currentVersion)) {
                    newVersion = version;
                    URL download = new URL(downloadUrl.replaceAll("%platform%", platform)
                        .replaceAll("%version%", versionString));
                    try (ReadableByteChannel rbc = Channels.newChannel(download.openStream())) {
                        File jarFile = PlotSquared.get().getJarFile();

                        File finalFile = new File(jarFile.getParent(),
                            "update" + File.separator + jarFile.getName());
                        File outFile = new File(jarFile.getParent(),
                            "update" + File.separator + jarFile.getName().replace(".jar", ".part"));
                        boolean exists = outFile.exists();
                        if (exists) {
                            outFile.delete();
                        } else {
                            File outFileParent = outFile.getParentFile();
                            if (!outFileParent.exists()) {
                                outFileParent.mkdirs();
                            }
                        }
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        }
                        outFile.renameTo(finalFile);
                        PlotSquared.debug("Updated PlotSquared to " + versionString);
                        MainUtil.sendAdmin(
                            "&7Restart to update PlotSquared with these changes: &c/plot changelog &7or&c "
                                + "http://empcraft.com/plots/cl?" + Integer
                                .toHexString(currentVersion.hash));
                    }
                }
            }
        } catch (Throwable ignore) {
        }
    }
}
