package com.intellectualcrafters.plot.commands;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.HastebinUtility;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "debugpaste",
aliases = { "dp" },
usage = "/plot debugpaste",
description = "Upload settings.yml & latest.log to hastebin",
permission = "plots.debugpaste",
category = CommandCategory.DEBUG)
public class DebugPaste extends SubCommand
{

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args)
    {
        TaskManager.runTaskAsync(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final String settingsYML = HastebinUtility.upload(PS.get().configFile);
                    String latestLOG;
                    try
                    {
                        latestLOG = HastebinUtility.upload(new File(BukkitMain.THIS.getDirectory(), "../../logs/latest.log"));
                    }
                    catch (final Exception e)
                    {
                        plr.sendMessage("&clatest.log is too big to be pasted, will ignore");
                        latestLOG = "too big :(";
                    }
                    final StringBuilder b = new StringBuilder();
                    b.append("# Welcome to this paste\n# It is meant to provide us at IntellectualSites with better information about your problem\n\n# We will start with some informational files\n");
                    b.append("links.settings_yml: '").append(settingsYML).append("'\n");
                    b.append("links.latest_log: '").append(latestLOG).append("'\n");
                    b.append("\n# YAAAS! Now let us move on to the server info\n");
                    b.append("version.server: '").append(Bukkit.getServer().getVersion()).append("'\n");
                    b.append("version.bukkit: '").append(Bukkit.getBukkitVersion()).append("'\n");
                    b.append("online_mode: ").append(Bukkit.getServer().getOnlineMode()).append("\n");
                    b.append("plugins:");
                    for (final Plugin p : Bukkit.getPluginManager().getPlugins())
                    {
                        b.append("\n  ").append(p.getName()).append(":\n    ").append("version: '").append(p.getDescription().getVersion()).append("'").append("\n    enabled: ").append(p.isEnabled());
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
                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }
}
