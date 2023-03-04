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
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.InvalidConfigurationException;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.inject.annotations.WorldFile;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.setup.PlotAreaBuilder;
import com.plotsquared.core.setup.SettingsNodesWrapper;
import com.plotsquared.core.util.FileBytes;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@CommandDeclaration(command = "template",
        permission = "plots.admin",
        usage = "/plot template [import | export] <world> <template>",
        category = CommandCategory.ADMINISTRATION)
public class Template extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final YamlConfiguration worldConfiguration;
    private final File worldFile;
    private final SetupUtils setupUtils;
    private final WorldUtil worldUtil;

    @Inject
    public Template(
            final @NonNull PlotAreaManager plotAreaManager,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            @WorldFile final @NonNull File worldFile,
            final @NonNull SetupUtils setupUtils,
            final @NonNull WorldUtil worldUtil
    ) {
        this.plotAreaManager = plotAreaManager;
        this.worldConfiguration = worldConfiguration;
        this.worldFile = worldFile;
        this.setupUtils = setupUtils;
        this.worldUtil = worldUtil;
    }

    public static boolean extractAllFiles(String world, String template) {
        try {
            File folder =
                    FileUtils.getFile(PlotSquared.platform().getDirectory(), Settings.Paths.TEMPLATES);
            if (!folder.exists()) {
                return false;
            }
            File output = PlotSquared.platform().getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            File input = new File(folder + File.separator + template + ".template");
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(input))) {
                ZipEntry ze = zis.getNextEntry();
                byte[] buffer = new byte[2048];
                while (ze != null) {
                    if (!ze.isDirectory()) {
                        String name = ze.getName().replace('\\', File.separatorChar)
                                .replace('/', File.separatorChar);
                        File newFile = new File(
                                (output + File.separator + name).replaceAll("__TEMP_DIR__", world));
                        File parent = newFile.getParentFile();
                        if (parent != null) {
                            parent.mkdirs();
                        }
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    ze = zis.getNextEntry();
                }
                zis.closeEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getBytes(PlotArea plotArea) {
        ConfigurationSection section = PlotSquared
                .get()
                .getWorldConfiguration()
                .getConfigurationSection("worlds." + plotArea.getWorldName());
        YamlConfiguration config = new YamlConfiguration();
        String generator = PlotSquared.platform().setupUtils().getGenerator(plotArea);
        if (generator != null) {
            config.set("generator.plugin", generator);
        }
        for (String key : section.getKeys(true)) {
            config.set(key, section.get(key));
        }
        return config.saveToString().getBytes();
    }

    public static void zipAll(String world, Set<FileBytes> files) throws IOException {
        File output = FileUtils.getFile(PlotSquared.platform().getDirectory(), Settings.Paths.TEMPLATES);
        output.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(
                output + File.separator + world + ".template");
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (FileBytes file : files) {
                ZipEntry ze = new ZipEntry(file.path());
                zos.putNextEntry(ze);
                zos.write(file.data());
            }
            zos.closeEntry();
        }
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length != 2 && args.length != 3) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("export")) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver("value", Tag.inserting(Component.text("/plot template export <world>")))
                    );
                    return true;
                } else if (args[0].equalsIgnoreCase("import")) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver(
                                    "value",
                                    Tag.inserting(Component.text("/plot template import <world> <template>"))
                            )
                    );
                    return true;
                }
            }
            sendUsage(player);
            return true;
        }
        final String world = args[1];
        switch (args[0].toLowerCase()) {
            case "import" -> {
                if (args.length != 3) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver(
                                    "value",
                                    Tag.inserting(Component.text("/plot template import <world> <template>"))
                            )
                    );
                    return false;
                }
                if (this.plotAreaManager.hasPlotArea(world)) {
                    player.sendMessage(
                            TranslatableCaption.of("setup.setup_world_taken"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(world)))
                    );
                    return false;
                }
                boolean result = extractAllFiles(world, args[2]);
                if (!result) {
                    player.sendMessage(
                            TranslatableCaption.of("template.invalid_template"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(args[2])))
                    );
                    return false;
                }
                File worldFile = FileUtils.getFile(
                        PlotSquared.platform().getDirectory(),
                        Settings.Paths.TEMPLATES + File.separator + "tmp-data.yml"
                );
                YamlConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldFile);
                this.worldConfiguration.set("worlds." + world, worldConfig.get(""));
                try {
                    this.worldConfiguration.save(this.worldFile);
                    this.worldConfiguration.load(this.worldFile);
                } catch (InvalidConfigurationException | IOException e) {
                    e.printStackTrace();
                }
                String manager =
                        worldConfig.getString("generator.plugin", PlotSquared.platform().pluginName());
                String generator = worldConfig.getString("generator.init", manager);
                PlotAreaBuilder builder = PlotAreaBuilder.newBuilder()
                        .plotAreaType(ConfigurationUtil.getType(worldConfig))
                        .terrainType(ConfigurationUtil.getTerrain(worldConfig))
                        .plotManager(manager)
                        .generatorName(generator)
                        .settingsNodesWrapper(new SettingsNodesWrapper(new ConfigurationNode[0], null))
                        .worldName(world);

                this.setupUtils.setupWorld(builder);
                TaskManager.runTask(() -> {
                    player.teleport(this.worldUtil.getSpawn(world), TeleportCause.COMMAND_TEMPLATE);
                    player.sendMessage(TranslatableCaption.of("setup.setup_finished"));
                });
                return true;
            }
            case "export" -> {
                if (args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver("value", Tag.inserting(Component.text("/plot template export <world>")))
                    );
                    return false;
                }
                final PlotArea area = this.plotAreaManager.getPlotAreaByString(world);
                if (area == null) {
                    player.sendMessage(
                            TranslatableCaption.of("errors.not_valid_plot_world"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(args[1])))
                    );
                    return false;
                }
                final PlotManager manager = area.getPlotManager();
                TaskManager.runTaskAsync(() -> {
                    try {
                        manager.exportTemplate();
                    } catch (Exception e) { // Must recover from any exception thrown a third party template manager
                        e.printStackTrace();
                        player.sendMessage(
                                TranslatableCaption.of("template.template_failed"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(e.getMessage())))
                        );
                        return;
                    }
                    player.sendMessage(TranslatableCaption.of("setup.setup_finished"));
                });
                return true;
            }
            default -> sendUsage(player);
        }
        return false;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        if (args.length == 1) {
            final List<String> completions = new LinkedList<>();
            if (player.hasPermission(Permission.PERMISSION_TEMPLATE_EXPORT)) {
                completions.add("export");
            }
            if (player.hasPermission(Permission.PERMISSION_TEMPLATE_IMPORT)) {
                completions.add("import");
            }
            final List<Command> commands = completions.stream().filter(completion -> completion
                            .toLowerCase()
                            .startsWith(args[0].toLowerCase()))
                    .map(completion -> new Command(
                            null,
                            true,
                            completion,
                            "",
                            RequiredType.NONE,
                            CommandCategory.ADMINISTRATION
                    ) {
                    }).collect(Collectors.toCollection(LinkedList::new));
            if (player.hasPermission(Permission.PERMISSION_TEMPLATE) && args[0].length() > 0) {
                commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
            }
            return commands;
        }
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

}
