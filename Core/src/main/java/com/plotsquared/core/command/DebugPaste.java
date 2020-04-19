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
 *                  Copyright (C) 2020 IntellectualSites
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.net.IncendoPaster;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.plotsquared.core.util.PremiumVerification;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CommandDeclaration(command = "debugpaste",
    aliases = "dp",
    usage = "/plot debugpaste",
    description = "Upload settings.yml, worlds.yml, PlotSquared.use_THIS.yml your latest.log and Multiverse's worlds.yml (if being used) to https://athion.net/ISPaster/paste",
    permission = "plots.debugpaste",
    category = CommandCategory.DEBUG,
    confirmation = true,
    requiredType = RequiredType.NONE)
public class DebugPaste extends SubCommand {

    private static String readFile(@NonNull final File file) throws IOException {
        final List<String> lines;
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            lines = reader.lines().collect(Collectors.toList());
        }
        final StringBuilder content = new StringBuilder();
        for (int i = Math.max(0, lines.size() - 1000); i < lines.size(); i++) {
            content.append(lines.get(i)).append("\n");
        }
        return content.toString();
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        TaskManager.runTaskAsync(() -> {
            try {

                StringBuilder b = new StringBuilder();
                b.append(
                    "# Welcome to this paste\n# It is meant to provide us at IntellectualSites with better information about your "
                        + "problem\n\n");
                b.append("# PlotSquared Information\n");
                b.append("This PlotSquared version is licensed to the spigot user ")
                    .append(PremiumVerification.getUserID()).append(" under ").append(
                    PremiumVerification.getDownloadID()).append("\n");
                b.append("# Server Information\n");
                b.append("Server Version: ").append(PlotSquared.get().IMP.getServerImplementation())
                    .append("\n");
                b.append("online_mode: ").append(UUIDHandler.getUUIDWrapper()).append(';')
                    .append(!Settings.UUID.OFFLINE).append('\n');
                b.append("Plugins:");
                for (Map.Entry<Map.Entry<String, String>, Boolean> pluginInfo : PlotSquared
                    .get().IMP.getPluginIds()) {
                    Map.Entry<String, String> nameVersion = pluginInfo.getKey();
                    String name = nameVersion.getKey();
                    String version = nameVersion.getValue();
                    boolean enabled = pluginInfo.getValue();
                    b.append("\n  ").append(name).append(":\n    ").append("version: '")
                        .append(version).append('\'').append("\n    enabled: ").append(enabled);
                }
                b.append("\n\n# YAY! Now, let's see what we can find in your JVM\n");
                Runtime runtime = Runtime.getRuntime();
                RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
                b.append("Uptime: ").append(
                    TimeUnit.MINUTES.convert(rb.getUptime(), TimeUnit.MILLISECONDS) + " minutes")
                    .append('\n');
                b.append("JVM Flags: ").append(rb.getInputArguments()).append('\n');
                b.append("Free Memory: ").append(runtime.freeMemory() / 1024 / 1024 + " MB")
                    .append('\n');
                b.append("Max Memory: ").append(runtime.maxMemory() / 1024 / 1024 + " MB")
                    .append('\n');
                b.append("Java Name: ").append(rb.getVmName()).append('\n');
                b.append("Java Version: '").append(System.getProperty("java.version"))
                    .append("'\n");
                b.append("Java Vendor: '").append(System.getProperty("java.vendor")).append("'\n");
                b.append("Operating System: '").append(System.getProperty("os.name")).append("'\n");
                b.append("OS Version: ").append(System.getProperty("os.version")).append('\n');
                b.append("OS Arch: ").append(System.getProperty("os.arch")).append('\n');
                b.append("# Okay :D Great. You are now ready to create your bug report!");
                b.append(
                    "\n# You can do so at https://github.com/Sauilitired/Hyperverse/issues");
                b.append("\n# or via our Discord at https://discord.gg/KxkjDVg");

                final IncendoPaster incendoPaster = new IncendoPaster("plotsquared");
                incendoPaster.addFile(new IncendoPaster.PasteFile("information", b.toString()));

                try {
                    final File logFile =
                        new File(PlotSquared.get().IMP.getDirectory(), "../../logs/latest.log");
                    if (Files.size(logFile.toPath()) > 14_000_000) {
                        throw new IOException("Too big...");
                    }
                    incendoPaster
                        .addFile(new IncendoPaster.PasteFile("latest.log", readFile(logFile)));
                } catch (IOException ignored) {
                    MainUtil
                        .sendMessage(player, "&clatest.log is too big to be pasted, will ignore");
                }

                try {
                    incendoPaster.addFile(new IncendoPaster.PasteFile("settings.yml",
                        readFile(PlotSquared.get().configFile)));
                } catch (final IllegalArgumentException ignored) {
                    MainUtil.sendMessage(player, "&cSkipping settings.yml because it's empty");
                }
                try {
                    incendoPaster.addFile(new IncendoPaster.PasteFile("worlds.yml",
                        readFile(PlotSquared.get().worldsFile)));
                } catch (final IllegalArgumentException ignored) {
                    MainUtil.sendMessage(player, "&cSkipping worlds.yml because it's empty");
                }
                try {
                    incendoPaster.addFile(new IncendoPaster.PasteFile("PlotSquared.use_THIS.yml",
                        readFile(PlotSquared.get().translationFile)));
                } catch (final IllegalArgumentException ignored) {
                    MainUtil.sendMessage(player,
                        "&cSkipping PlotSquared.use_THIS.yml because it's empty");
                }

                try {
                    final File MultiverseWorlds = new File(PlotSquared.get().IMP.getDirectory(),
                        "../Multiverse-Core/worlds.yml");
                    incendoPaster.addFile(new IncendoPaster.PasteFile("MultiverseCore/worlds.yml",
                        readFile(MultiverseWorlds)));
                } catch (final IOException ignored) {
                    MainUtil.sendMessage(player,
                        "&cSkipping Multiverse worlds.yml because the plugin is not in use");
                }

                try {
                    final String rawResponse = incendoPaster.upload();
                    final JsonObject jsonObject =
                        new JsonParser().parse(rawResponse).getAsJsonObject();

                    if (jsonObject.has("created")) {
                        final String pasteId = jsonObject.get("paste_id").getAsString();
                        final String link =
                            String.format("https://athion.net/ISPaster/paste/view/%s", pasteId);
                        player.sendMessage(
                            Captions.DEBUG_REPORT_CREATED.getTranslated().replace("%url%", link));
                    } else {
                        final String responseMessage = jsonObject.get("response").getAsString();
                        MainUtil.sendMessage(player, String
                            .format("&cFailed to create the debug paste: %s", responseMessage));
                    }
                } catch (final Throwable throwable) {
                    throwable.printStackTrace();
                    MainUtil.sendMessage(player,
                        "&cFailed to create the debug paste: " + throwable.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }
}
