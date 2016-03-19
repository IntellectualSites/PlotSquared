package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.HastebinUtility;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.io.File;
import java.io.IOException;

@CommandDeclaration(command = "debugpaste", aliases = "dp", usage = "/plot debugpaste", description = "Upload settings.yml & latest.log to HasteBin",
        permission = "plots.debugpaste", category = CommandCategory.DEBUG)
public class DebugPaste extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final String settingsYML = HastebinUtility.upload(PS.get().configFile);
                    String latestLOG;
                    try {
                        latestLOG = HastebinUtility.upload(new File(PS.get().IMP.getDirectory(), "../../logs/latest.log"));
                    } catch (IOException e) {
                        MainUtil.sendMessage(plr, "&clatest.log is too big to be pasted, will ignore");
                        latestLOG = "too big :(";
                    }
                    final StringBuilder b = new StringBuilder();
                    b.append("# Welcome to this paste\n# It is meant to provide us at IntellectualSites with better information about your problem\n\n# We will start with some informational files\n");
                    b.append("links.settings_yml: ").append(settingsYML).append("\n");
                    b.append("links.latest_log: ").append(latestLOG).append("\n");
                    b.append("\n# Server Information\n");
                    int[] sVersion = PS.get().IMP.getServerVersion();
                    b.append("version.server: ").append(sVersion[0]).append('.').append(sVersion[1]).append('.').append(sVersion[2]).append("\n");
                    b.append("online_mode: ").append(UUIDHandler.getUUIDWrapper()).append(";").append(!Settings.OFFLINE_MODE).append("\n");
                    b.append("plugins:");
                    for (String id : PS.get().IMP.getPluginIds()) {
                        String[] split = id.split(":");
                        String[] split2 = split[0].split(";");
                        String enabled = split.length == 2 ? split[1] : "unknown";
                        String name = split2[0];
                        String version = split2.length == 2 ? split2[1] : "unknown";
                        b.append("\n  ").append(name).append(":\n    ").append("version: '").append(version).append("'").append("\n    enabled: ").append(enabled);
                    }
                    b.append("\n\n# YAY! Now, let's see what we can find in your JVM\n");
                    final Runtime runtime = Runtime.getRuntime();
                    b.append("memory.free: ").append(runtime.freeMemory()).append("\n");
                    b.append("memory.max: ").append(runtime.maxMemory()).append("\n");
                    b.append("java.specification.version: '").append(System.getProperty("java.specification.version")).append("'\n");
                    b.append("java.vendor: '").append(System.getProperty("java.vendor")).append("'\n");
                    b.append("java.version: '").append(System.getProperty("java.version")).append("'\n");
                    b.append("os.arch: '").append(System.getProperty("os.arch")).append("'\n");
                    b.append("os.name: '").append(System.getProperty("os.name")).append("'\n");
                    b.append("os.version: '").append(System.getProperty("os.version")).append("'\n\n");
                    b.append("# Okay :D Great. You are now ready to create your bug report!");
                    b.append("\n# You can do so at https://github.com/IntellectualSites/PlotSquared/issues");
                    
                    final String link = HastebinUtility.upload(b.toString());
                    plr.sendMessage(C.DEBUG_REPORT_CREATED.s().replace("%url%", link));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }
}
