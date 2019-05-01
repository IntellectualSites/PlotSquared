package com.intellectualcrafters.plot.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.IncendoPaster;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@CommandDeclaration(command = "debugpaste",
        aliases = "dp", usage = "/plot debugpaste",
        description = "Upload settings.yml, worlds.yml, PlotSquared.use_THIS.yml and your latest.log to https://www.athion.net/ISPaster/paste",
        permission = "plots.debugpaste",
        category = CommandCategory.DEBUG)
public class DebugPaste extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final IncendoPaster incendoPaster = new IncendoPaster("plotsquared");

                    StringBuilder b = new StringBuilder();
                    b.append(
                        "# Welcome to this paste\n# It is meant to provide us at IntellectualSites with better information about your "
                            + "problem\n\n");
                    b.append("# Server Information\n");
                    b.append("server.version: ").append(PS.imp().getServerImplementation()).append("\n");
                    b.append("online_mode: ").append(UUIDHandler.getUUIDWrapper()).append(';')
                        .append(!Settings.UUID.OFFLINE).append('\n');
                    b.append("plugins:");
                    for (String id : PS.get().IMP.getPluginIds()) {
                        String[] split = id.split(":");
                        String[] split2 = split[0].split(";");
                        String enabled = split.length == 2 ? split[1] : "unknown";
                        String name = split2[0];
                        String version = split2.length == 2 ? split2[1] : "unknown";
                        b.append("\n  ").append(name).append(":\n    ").append("version: '")
                            .append(version).append('\'').append("\n    enabled: ").append(enabled);
                    }
                    b.append("\n\n# YAY! Now, let's see what we can find in your JVM\n");
                    Runtime runtime = Runtime.getRuntime();
                    b.append("memory.free: ").append(runtime.freeMemory()).append('\n');
                    b.append("memory.max: ").append(runtime.maxMemory()).append('\n');
                    b.append("java.specification.version: '")
                        .append(System.getProperty("java.specification.version")).append("'\n");
                    b.append("java.vendor: '").append(System.getProperty("java.vendor"))
                        .append("'\n");
                    b.append("java.version: '").append(System.getProperty("java.version"))
                        .append("'\n");
                    b.append("os.arch: '").append(System.getProperty("os.arch")).append("'\n");
                    b.append("os.name: '").append(System.getProperty("os.name")).append("'\n");
                    b.append("os.version: '").append(System.getProperty("os.version"))
                        .append("'\n\n");
                    b.append("# Okay :D Great. You are now ready to create your bug report!");
                    b.append(
                        "\n# You can do so at https://github.com/IntellectualSites/PlotSquared/issues");
                    b.append("\n# or via our Discord at https://discord.gg/ngZCzbU");

                    incendoPaster.addFile(new IncendoPaster.PasteFile("information", b.toString()));

                    try {
                        final File logFile = new File(PS.get().IMP.getDirectory(),
                            "../../logs/latest.log");
                        if (Files.size(logFile.toPath()) > 14_000_000) {
                            throw new IOException("Too big...");
                        }
                        incendoPaster.addFile(new IncendoPaster.PasteFile("latest.log", readFile(logFile)));
                    } catch (IOException ignored) {
                        MainUtil.sendMessage(player,
                            "&clatest.log is too big to be pasted, will ignore");
                    }

                    try {
                        incendoPaster.addFile(new IncendoPaster.PasteFile("settings.yml", readFile(PS.get().configFile)));
                    } catch (final IllegalArgumentException ignored) {
                        MainUtil.sendMessage(player, "&cSkipping settings.yml because it's empty");
                    }
                    try {
                        incendoPaster.addFile(new IncendoPaster.PasteFile("worlds.yml", readFile(PS.get().worldsFile)));
                    } catch (final IllegalArgumentException ignored) {
                        MainUtil.sendMessage(player, "&cSkipping worlds.yml because it's empty");
                    }
                    try {
                        incendoPaster.addFile(new IncendoPaster.PasteFile("PlotSquared.use_THIS.yml",
                            readFile(PS.get().translationFile)));
                    } catch (final IllegalArgumentException ignored) {
                        MainUtil.sendMessage(player, "&cSkipping PlotSquared.use_THIS.yml because it's empty");
                    }

                    try {
                        final String rawResponse = incendoPaster.upload();
                        final JsonObject jsonObject = new JsonParser().parse(rawResponse).getAsJsonObject();

                        if (jsonObject.has("created")) {
                            final String pasteId = jsonObject.get("paste_id").getAsString();
                            final String link = String.format("https://www.athion.net/ISPaster/paste/view/%s", pasteId);
                            player.sendMessage(
                                C.DEBUG_REPORT_CREATED.s().replace("%url%", link));
                        } else {
                            final String responseMessage = jsonObject.get("response").getAsString();
                            MainUtil.sendMessage(player, String.format("&cFailed to create the debug paste: %s", responseMessage));
                        }
                    } catch (final Throwable throwable) {
                        throwable.printStackTrace();
                        MainUtil.sendMessage(player, "&cFailed to create the debug paste: " + throwable.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    private static String readFile(final File file) throws IOException {
        final StringBuilder content = new StringBuilder();
        final List<String> lines = new ArrayList<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        for (int i = Math.max(0, lines.size() - 1000); i < lines.size(); i++) {
            content.append(lines.get(i)).append("\n");
        }
        return content.toString();
    }

}
