package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.IncendoPaster;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@CommandDeclaration(command = "debugpaste", aliases = "dp", usage = "/plot debugpaste",
    description = "Upload settings.yml, worlds.yml, commands.yml and latest.log to www.hastebin.com",
    permission = "plots.debugpaste", category = CommandCategory.DEBUG) public class DebugPaste
    extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        TaskManager.runTaskAsync(() -> {
            try {
                final IncendoPaster incendoPaster = new IncendoPaster("plotsquared");

                StringBuilder b = new StringBuilder();
                b.append(
                    "# Welcome to this paste\n# It is meant to provide us at IntellectualSites with better information about your "
                        + "problem\n\n");
                b.append("# Server Information\n");
                b.append("server.version: ").append(PlotSquared.get().IMP.getServerImplementation()).append("\n");
                b.append("online_mode: ").append(UUIDHandler.getUUIDWrapper()).append(';')
                    .append(!Settings.UUID.OFFLINE).append('\n');
                b.append("plugins:");
                for (String id : PlotSquared.get().IMP.getPluginIds()) {
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
                    final File logFile = new File(PlotSquared.get().IMP.getDirectory(),
                        "../../logs/latest.log");
                    if (Files.size(logFile.toPath()) > 14_000_000) {
                        throw new IOException("Too big...");
                    }
                    incendoPaster.addFile(new IncendoPaster.PasteFile("latest.log", readFile(logFile)));
                } catch (IOException ignored) {
                    MainUtil.sendMessage(player,
                        "&clatest.log is too big to be pasted, will ignore");
                }

                incendoPaster.addFile(new IncendoPaster.PasteFile("settings.yml", readFile(PlotSquared.get().configFile)));
                incendoPaster.addFile(new IncendoPaster.PasteFile("worlds.yml", readFile(PlotSquared.get().worldsFile)));
                incendoPaster.addFile(new IncendoPaster.PasteFile("PlotSquared.use_THIS.yml", readFile(PlotSquared.get().translationFile)));
                try {
                    final String rawResponse = incendoPaster.upload();
                    final JsonObject jsonObject = new JsonParser().parse(rawResponse).getAsJsonObject();

                    if (jsonObject.has("created")) {
                        final String pasteId = jsonObject.get("paste_id").getAsString();
                        final String link = String.format("https://incendo.org/paste/view/%s", pasteId);
                        player.sendMessage(
                            C.DEBUG_REPORT_CREATED.s().replace("%url%", link));
                    } else {
                        final String responseMessage = jsonObject.get("response").getAsString();
                        MainUtil.sendMessage(player, String.format("&cFailed  to create the debug paste: %s", responseMessage));
                    }
                } catch (final Throwable throwable) {
                    throwable.printStackTrace();
                    MainUtil.sendMessage(player, "&cFailed to create the debug paste: " + throwable.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    private static String readFile(@NonNull final File file) throws IOException {
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
