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
 *                  Copyright (C) 2021 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.CaptionHolder;
import com.plotsquared.core.configuration.caption.Templates;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.generator.AugmentedUtils;
import com.plotsquared.core.generator.HybridPlotWorld;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.inject.annotations.WorldFile;
import com.plotsquared.core.inject.factory.HybridPlotWorldFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.setup.PlotAreaBuilder;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal3;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandDeclaration(command = "area",
        permission = "plots.area",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        aliases = "world",
        usage = "/plot area <create | info | list | tp | regen>",
        confirmation = true)
public class Area extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final YamlConfiguration worldConfiguration;
    private final File worldFile;
    private final HybridPlotWorldFactory hybridPlotWorldFactory;
    private final SetupUtils setupUtils;
    private final WorldUtil worldUtil;
    private final GlobalBlockQueue blockQueue;

    private final Map<UUID, Map<String, Object>> metaData = new HashMap<>();

    @Inject
    public Area(
            final @NonNull PlotAreaManager plotAreaManager,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            @WorldFile final @NonNull File worldFile,
            final @NonNull HybridPlotWorldFactory hybridPlotWorldFactory,
            final @NonNull SetupUtils setupUtils,
            final @NonNull WorldUtil worldUtil,
            final @NonNull GlobalBlockQueue blockQueue
    ) {
        this.plotAreaManager = plotAreaManager;
        this.worldConfiguration = worldConfiguration;
        this.worldFile = worldFile;
        this.hybridPlotWorldFactory = hybridPlotWorldFactory;
        this.setupUtils = setupUtils;
        this.worldUtil = worldUtil;
        this.blockQueue = blockQueue;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length == 0) {
            sendUsage(player);
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "single" -> {
                if (player instanceof ConsolePlayer) {
                    player.sendMessage(RequiredType.CONSOLE.getErrorMessage());
                    return false;
                }
                if (!Permissions.hasPermission(player, Permission.PERMISSION_AREA_CREATE)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", String.valueOf(Permission.PERMISSION_AREA_CREATE))
                    );
                    return false;
                }
                if (args.length < 2) {
                    player.sendMessage(
                            TranslatableCaption.of("single.single_area_needs_name"),
                            Template.of("command", "/plot area single <name>")
                    );
                    return false;
                }
                final PlotArea existingArea = this.plotAreaManager.getPlotArea(player.getLocation().getWorldName(), args[1]);
                if (existingArea != null && existingArea.getId().equalsIgnoreCase(args[1])) {
                    player.sendMessage(TranslatableCaption.of("single.single_area_name_taken"));
                    return false;
                }
                final LocalSession localSession = WorldEdit.getInstance().getSessionManager().getIfPresent(player.toActor());
                if (localSession == null) {
                    player.sendMessage(TranslatableCaption.of("single.single_area_missing_selection"));
                    return false;
                }
                Region playerSelectedRegion = null;
                try {
                    playerSelectedRegion = localSession.getSelection(((Player) player.toActor()).getWorld());
                } catch (final Exception ignored) {
                }
                if (playerSelectedRegion == null) {
                    player.sendMessage(TranslatableCaption.of("single.single_area_missing_selection"));
                    return false;
                }
                if (playerSelectedRegion.getWidth() != playerSelectedRegion.getLength()) {
                    player.sendMessage(TranslatableCaption.of("single.single_area_not_square"));
                    return false;
                }
                if (this.plotAreaManager.getPlotAreas(
                        Objects.requireNonNull(playerSelectedRegion.getWorld()).getName(),
                        CuboidRegion.makeCuboid(playerSelectedRegion)
                ).length != 0) {
                    player.sendMessage(TranslatableCaption.of("single.single_area_overlapping"));
                }
                // Alter the region
                final BlockVector3 playerSelectionMin = playerSelectedRegion.getMinimumPoint();
                final BlockVector3 playerSelectionMax = playerSelectedRegion.getMaximumPoint();
                // Create a new selection that spans the entire vertical range of the world
                final CuboidRegion selectedRegion =
                        new CuboidRegion(
                                playerSelectedRegion.getWorld(),
                                BlockVector3.at(playerSelectionMin.getX(), 0, playerSelectionMin.getZ()),
                                BlockVector3.at(playerSelectionMax.getX(), 255, playerSelectionMax.getZ())
                        );
                // There's only one plot in the area...
                final PlotId plotId = PlotId.of(1, 1);
                final HybridPlotWorld hybridPlotWorld = this.hybridPlotWorldFactory
                        .create(
                                player.getLocation().getWorldName(),
                                args[1],
                                Objects.requireNonNull(PlotSquared.platform()).defaultGenerator(),
                                plotId,
                                plotId
                        );
                // Plot size is the same as the region width
                hybridPlotWorld.PLOT_WIDTH = hybridPlotWorld.SIZE = (short) selectedRegion.getWidth();
                // We use a schematic generator
                hybridPlotWorld.setTerrain(PlotAreaTerrainType.NONE);
                // It is always a partial plot world
                hybridPlotWorld.setType(PlotAreaType.PARTIAL);
                // We save the schematic :D
                hybridPlotWorld.PLOT_SCHEMATIC = true;
                // Set the road width to 0
                hybridPlotWorld.ROAD_WIDTH = hybridPlotWorld.ROAD_OFFSET_X = hybridPlotWorld.ROAD_OFFSET_Z = 0;
                // Set the plot height to the selection height
                hybridPlotWorld.PLOT_HEIGHT = hybridPlotWorld.ROAD_HEIGHT = hybridPlotWorld.WALL_HEIGHT = playerSelectionMin.getBlockY();
                // No sign plz
                hybridPlotWorld.setAllowSigns(false);
                final File parentFile = FileUtils.getFile(
                        PlotSquared.platform().getDirectory(),
                        Settings.Paths.SCHEMATICS + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + hybridPlotWorld.getWorldName() + File.separator
                                + hybridPlotWorld.getId()
                );
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    player.sendMessage(TranslatableCaption.of("single.single_area_could_not_make_directories"));
                    return false;
                }
                final File file = new File(parentFile, "plot.schem");
                try (final ClipboardWriter clipboardWriter = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(
                        file))) {
                    final BlockArrayClipboard clipboard = new BlockArrayClipboard(selectedRegion);
                    final EditSession editSession = WorldEdit
                            .getInstance()
                            .getEditSessionFactory()
                            .getEditSession(selectedRegion.getWorld(), -1);
                    final ForwardExtentCopy forwardExtentCopy =
                            new ForwardExtentCopy(editSession, selectedRegion, clipboard, selectedRegion.getMinimumPoint());
                    forwardExtentCopy.setCopyingBiomes(true);
                    forwardExtentCopy.setCopyingEntities(true);
                    Operations.complete(forwardExtentCopy);
                    clipboardWriter.write(clipboard);
                } catch (final Exception e) {
                    player.sendMessage(TranslatableCaption.of("single.single_area_failed_to_save"));
                    e.printStackTrace();
                    return false;
                }

                // Setup schematic
                try {
                    hybridPlotWorld.setupSchematics();
                } catch (final SchematicHandler.UnsupportedFormatException e) {
                    e.printStackTrace();
                }

                // Calculate the offset
                final BlockVector3 singlePos1 = selectedRegion.getMinimumPoint();

                // Now the schematic is saved, which is wonderful!
                PlotAreaBuilder singleBuilder = PlotAreaBuilder.ofPlotArea(hybridPlotWorld).plotManager(PlotSquared
                                .platform()
                                .pluginName())
                        .generatorName(PlotSquared.platform().pluginName()).maximumId(plotId).minimumId(plotId);
                Runnable singleRun = () -> {
                    final String path =
                            "worlds." + hybridPlotWorld.getWorldName() + ".areas." + hybridPlotWorld.getId() + '-' + singleBuilder
                                    .minimumId() + '-'
                                    + singleBuilder.maximumId();
                    final int offsetX = singlePos1.getX();
                    final int offsetZ = singlePos1.getZ();
                    if (offsetX != 0) {
                        this.worldConfiguration.set(path + ".road.offset.x", offsetX);
                    }
                    if (offsetZ != 0) {
                        this.worldConfiguration.set(path + ".road.offset.z", offsetZ);
                    }
                    final String world = this.setupUtils.setupWorld(singleBuilder);
                    if (this.worldUtil.isWorld(world)) {
                        PlotSquared.get().loadWorld(world, null);
                        player.sendMessage(TranslatableCaption.of("single.single_area_created"));
                    } else {
                        player.sendMessage(
                                TranslatableCaption.of("errors.error_create"),
                                Template.of("world", hybridPlotWorld.getWorldName())
                        );
                    }
                };
                singleRun.run();
                return true;
            }
            case "c", "setup", "create" -> {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_AREA_CREATE)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", String.valueOf(Permission.PERMISSION_AREA_CREATE))
                    );
                    return false;
                }
                switch (args.length) {
                    case 1:
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Templates.of("value", "/plot area create [world[:id]] [<modifier>=<value>]...")
                        );
                        return false;
                    case 2:
                        switch (args[1].toLowerCase()) {
                            case "pos1" -> { // Set position 1
                                HybridPlotWorld area = (HybridPlotWorld) metaData.computeIfAbsent(
                                                player.getUUID(),
                                                missingUUID -> new HashMap<>()
                                        )
                                        .get("area_create_area");
                                if (area == null) {
                                    player.sendMessage(
                                            TranslatableCaption.of("commandconfig.command_syntax"),
                                            Templates.of("value", "/plot area create [world[:id]] [<modifier>=<value>]...")
                                    );
                                    return false;
                                }
                                Location location = player.getLocation();
                                metaData.computeIfAbsent(player.getUUID(), missingUUID -> new HashMap<>()).put(
                                        "area_pos1",
                                        location
                                );
                                player.sendMessage(
                                        TranslatableCaption.of("set.set_attribute"),
                                        Template.of("attribute", "area_pos1"),
                                        Template.of("value", location.getX() + "," + location.getZ())
                                );
                                player.sendMessage(
                                        TranslatableCaption.of("area.set_pos2"),
                                        Template.of("command", "/plot area create pos2")
                                );
                                return true;
                            }
                            case "pos2" -> {  // Set position 2 and finish creation for type=2 (partial)
                                final HybridPlotWorld area =
                                        (HybridPlotWorld) metaData.computeIfAbsent(
                                                        player.getUUID(),
                                                        missingUUID -> new HashMap<>()
                                                )
                                                .get("area_create_area");
                                if (area == null) {
                                    player.sendMessage(
                                            TranslatableCaption.of("commandconfig.command_syntax"),
                                            Templates.of("value", "/plot area create [world[:id]] [<modifier>=<value>]...")
                                    );
                                    return false;
                                }
                                Location pos1 = player.getLocation();
                                Location pos2 =
                                        (Location) metaData.computeIfAbsent(player.getUUID(), missingUUID -> new HashMap<>()).get(
                                                "area_pos1");
                                int dx = Math.abs(pos1.getX() - pos2.getX());
                                int dz = Math.abs(pos1.getZ() - pos2.getZ());
                                int numX = Math.max(1, (dx + 1 + area.ROAD_WIDTH + area.SIZE / 2) / area.SIZE);
                                int numZ = Math.max(1, (dz + 1 + area.ROAD_WIDTH + area.SIZE / 2) / area.SIZE);
                                int ddx = dx - (numX * area.SIZE - area.ROAD_WIDTH);
                                int ddz = dz - (numZ * area.SIZE - area.ROAD_WIDTH);
                                int bx = Math.min(pos1.getX(), pos2.getX()) + ddx;
                                int bz = Math.min(pos1.getZ(), pos2.getZ()) + ddz;
                                int tx = Math.max(pos1.getX(), pos2.getX()) - ddx;
                                int tz = Math.max(pos1.getZ(), pos2.getZ()) - ddz;
                                int lower = (area.ROAD_WIDTH & 1) == 0 ? area.ROAD_WIDTH / 2 - 1 : area.ROAD_WIDTH / 2;
                                final int offsetX = bx - (area.ROAD_WIDTH == 0 ? 0 : lower);
                                final int offsetZ = bz - (area.ROAD_WIDTH == 0 ? 0 : lower);
                                final CuboidRegion region = RegionUtil.createRegion(bx, tx, bz, tz);
                                final Set<PlotArea> areas = this.plotAreaManager.getPlotAreasSet(area.getWorldName(), region);
                                if (!areas.isEmpty()) {
                                    player.sendMessage(
                                            TranslatableCaption.of("cluster.cluster_intersection"),
                                            Template.of("cluster", areas.iterator().next().toString())
                                    );
                                    return false;
                                }
                                PlotAreaBuilder builder = PlotAreaBuilder.ofPlotArea(area).plotManager(PlotSquared
                                                .platform()
                                                .pluginName())
                                        .generatorName(PlotSquared.platform().pluginName()).minimumId(PlotId.of(1, 1))
                                        .maximumId(PlotId.of(numX, numZ));
                                final String path =
                                        "worlds." + area.getWorldName() + ".areas." + area.getId() + '-' + builder.minimumId() + '-' + builder
                                                .maximumId();
                                Runnable run = () -> {
                                    if (offsetX != 0) {
                                        this.worldConfiguration.set(path + ".road.offset.x", offsetX);
                                    }
                                    if (offsetZ != 0) {
                                        this.worldConfiguration.set(path + ".road.offset.z", offsetZ);
                                    }
                                    final String world = this.setupUtils.setupWorld(builder);
                                    if (this.worldUtil.isWorld(world)) {
                                        PlotSquared.get().loadWorld(world, null);
                                        player.teleport(this.worldUtil.getSpawn(world), TeleportCause.COMMAND_AREA_CREATE);
                                        player.sendMessage(TranslatableCaption.of("setup.setup_finished"));
                                        if (area.getTerrain() != PlotAreaTerrainType.ALL) {
                                            QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(world));
                                            queue.setChunkConsumer(chunk -> AugmentedUtils.generate(
                                                    null,
                                                    world,
                                                    chunk.getX(),
                                                    chunk.getZ(),
                                                    null
                                            ));
                                            queue.addReadChunks(region.getChunks());
                                            queue.enqueue();
                                        }
                                    } else {
                                        player.sendMessage(
                                                TranslatableCaption.of("errors.error_create"),
                                                Template.of("world", area.getWorldName())
                                        );
                                    }
                                };
                                if (hasConfirmation(player)) {
                                    CmdConfirm.addPending(player, getCommandString() + " create pos2 (Creates world)", run);
                                } else {
                                    run.run();
                                }
                                return true;
                            }
                        }
                    default: // Start creation
                        String[] split = args[1].split(":");
                        String id;
                        if (split.length == 2) {
                            id = split[1];
                        } else {
                            id = null;
                        }
                        PlotAreaBuilder builder = PlotAreaBuilder.newBuilder();
                        builder.worldName(split[0]);
                        final HybridPlotWorld pa =
                                this.hybridPlotWorldFactory.create(
                                        builder.worldName(),
                                        id,
                                        PlotSquared.platform().defaultGenerator(),
                                        null,
                                        null
                                );
                        PlotArea other = this.plotAreaManager.getPlotArea(pa.getWorldName(), id);
                        if (other != null && Objects.equals(pa.getId(), other.getId())) {
                            player.sendMessage(
                                    TranslatableCaption.of("setup.setup_world_taken"),
                                    Template.of("value", pa.toString())
                            );
                            return false;
                        }
                        Set<PlotArea> areas = this.plotAreaManager.getPlotAreasSet(pa.getWorldName());
                        if (!areas.isEmpty()) {
                            PlotArea area = areas.iterator().next();
                            pa.setType(area.getType());
                        }
                        pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                        for (int i = 2; i < args.length; i++) {
                            String[] pair = args[i].split("=");
                            if (pair.length != 2) {
                                player.sendMessage(
                                        TranslatableCaption.of("commandconfig.command_syntax_extended"),
                                        Template.of("value1,", getCommandString()),
                                        Template.of("value2", " create [world[:id]] [<modifier>=<value>]...")
                                );
                                return false;
                            }
                            switch (pair[0].toLowerCase()) {
                                case "s", "size" -> {
                                    pa.PLOT_WIDTH = Integer.parseInt(pair[1]);
                                    pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                                }
                                case "g", "gap" -> {
                                    pa.ROAD_WIDTH = Integer.parseInt(pair[1]);
                                    pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                                }
                                case "h", "height" -> {
                                    int value = Integer.parseInt(pair[1]);
                                    pa.PLOT_HEIGHT = value;
                                    pa.ROAD_HEIGHT = value;
                                    pa.WALL_HEIGHT = value;
                                }
                                case "f", "floor" -> pa.TOP_BLOCK = ConfigurationUtil.BLOCK_BUCKET.parseString(pair[1]);
                                case "m", "main" -> pa.MAIN_BLOCK = ConfigurationUtil.BLOCK_BUCKET.parseString(pair[1]);
                                case "w", "wall" -> pa.WALL_FILLING = ConfigurationUtil.BLOCK_BUCKET.parseString(pair[1]);
                                case "b", "border" -> pa.WALL_BLOCK = ConfigurationUtil.BLOCK_BUCKET.parseString(pair[1]);
                                case "terrain" -> {
                                    pa.setTerrain(PlotAreaTerrainType.fromString(pair[1])
                                            .orElseThrow(() -> new IllegalArgumentException(pair[1] + " is not a valid terrain.")));
                                    builder.terrainType(pa.getTerrain());
                                }
                                case "type" -> {
                                    pa.setType(PlotAreaType.fromString(pair[1])
                                            .orElseThrow(() -> new IllegalArgumentException(pair[1] + " is not a valid type.")));
                                    builder.plotAreaType(pa.getType());
                                }
                                default -> {
                                    player.sendMessage(
                                            TranslatableCaption.of("commandconfig.command_syntax_extended"),
                                            Template.of("value1", getCommandString()),
                                            Template.of("value2", " create [world[:id]] [<modifier>=<value>]...")
                                    );
                                    return false;
                                }
                            }
                        }
                        if (pa.getType() != PlotAreaType.PARTIAL) {
                            if (this.worldUtil.isWorld(pa.getWorldName())) {
                                player.sendMessage(
                                        TranslatableCaption.of("setup.setup_world_taken"),
                                        Template.of("value", pa.getWorldName())
                                );
                                return false;
                            }
                            Runnable run = () -> {
                                String path = "worlds." + pa.getWorldName();
                                if (!this.worldConfiguration.contains(path)) {
                                    this.worldConfiguration.createSection(path);
                                }
                                ConfigurationSection section = this.worldConfiguration.getConfigurationSection(path);
                                pa.saveConfiguration(section);
                                pa.loadConfiguration(section);
                                builder.plotManager(PlotSquared.platform().pluginName());
                                builder.generatorName(PlotSquared.platform().pluginName());
                                String world = this.setupUtils.setupWorld(builder);
                                if (this.worldUtil.isWorld(world)) {
                                    player.teleport(this.worldUtil.getSpawn(world), TeleportCause.COMMAND_AREA_CREATE);
                                    player.sendMessage(TranslatableCaption.of("setup.setup_finished"));
                                } else {
                                    player.sendMessage(
                                            TranslatableCaption.of("errors.error_create"),
                                            Template.of("world", pa.getWorldName())
                                    );
                                }
                                try {
                                    this.worldConfiguration.save(this.worldFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            };
                            if (hasConfirmation(player)) {
                                CmdConfirm.addPending(player, getCommandString() + ' ' + StringMan.join(args, " "), run);
                            } else {
                                run.run();
                            }
                            return true;
                        }
                        if (pa.getId() == null) {
                            player.sendMessage(
                                    TranslatableCaption.of("commandconfig.command_syntax"),
                                    Template.of("value", getUsage())
                            );
                            player.sendMessage(
                                    TranslatableCaption.of("commandconfig.command_syntax_extended"),
                                    Template.of("value1", getCommandString()),
                                    Template.of("value2", " create [world[:id]] [<modifier>=<value>]...")
                            );
                            return false;
                        }
                        if (this.worldUtil.isWorld(pa.getWorldName())) {
                            if (!player.getLocation().getWorldName().equals(pa.getWorldName())) {
                                player.teleport(this.worldUtil.getSpawn(pa.getWorldName()), TeleportCause.COMMAND_AREA_CREATE);
                            }
                        } else {
                            builder.terrainType(PlotAreaTerrainType.NONE);
                            builder.plotAreaType(PlotAreaType.NORMAL);
                            this.setupUtils.setupWorld(builder);
                            player.teleport(this.worldUtil.getSpawn(pa.getWorldName()), TeleportCause.COMMAND_AREA_CREATE);
                        }
                        metaData.computeIfAbsent(player.getUUID(), missingUUID -> new HashMap<>()).put("area_create_area", pa);
                        player.sendMessage(
                                TranslatableCaption.of("single.get_position"),
                                Template.of("command", getCommandString())
                        );
                        break;
                }
                return true;
            }
            case "i", "info" -> {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_AREA_INFO)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", String.valueOf(Permission.PERMISSION_AREA_INFO))
                    );
                    return false;
                }
                PlotArea area;
                switch (args.length) {
                    case 1 -> area = player.getApplicablePlotArea();
                    case 2 -> area = this.plotAreaManager.getPlotAreaByString(args[1]);
                    default -> {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax_extended"),
                                Template.of("value1", getCommandString()),
                                Template.of("value2", " info [area]")
                        );
                        return false;
                    }
                }
                if (area == null) {
                    if (args.length == 2) {
                        player.sendMessage(TranslatableCaption.of("errors.not_valid_plot_world"), Template.of("value", args[1]));
                    } else {
                        player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                    }
                    return false;
                }
                String name;
                double percent;
                int claimed = area.getPlotCount();
                int clusters = area.getClusters().size();
                String region;
                String generator = String.valueOf(area.getGenerator());
                if (area.getType() == PlotAreaType.PARTIAL) {
                    PlotId min = area.getMin();
                    PlotId max = area.getMax();
                    name = area.getWorldName() + ';' + area.getId() + ';' + min + ';' + max;
                    int size = (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1);
                    percent = claimed == 0 ? 0 : size / (double) claimed;
                    region = area.getRegion().toString();
                } else {
                    name = area.getWorldName();
                    percent = claimed == 0 ? 0 : 100d * claimed / Integer.MAX_VALUE;
                    region = "N/A";
                }
                Template headerTemplate = Template.of(
                        "header",
                        TranslatableCaption.of("info.plot_info_header").getComponent(player)
                );
                Template nameTemplate = Template.of("name", name);
                Template typeTemplate = Template.of("type", area.getType().name());
                Template terrainTemplate = Template.of("terrain", area.getTerrain().name());
                Template usageTemplate = Template.of("usage", String.format("%.2f", percent));
                Template claimedTemplate = Template.of("claimed", String.valueOf(claimed));
                Template clustersTemplate = Template.of("clusters", String.valueOf(clusters));
                Template regionTemplate = Template.of("region", region);
                Template generatorTemplate = Template.of("generator", generator);
                Template footerTemplate = Template.of(
                        "footer",
                        TranslatableCaption.of("info.plot_info_footer").getComponent(player)
                );
                player.sendMessage(
                        TranslatableCaption.of("info.area_info_format"),
                        headerTemplate,
                        nameTemplate,
                        typeTemplate,
                        terrainTemplate,
                        usageTemplate,
                        claimedTemplate,
                        clustersTemplate,
                        regionTemplate,
                        generatorTemplate,
                        footerTemplate
                );
                return true;
            }
            case "l", "list" -> {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_AREA_LIST)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", String.valueOf(Permission.PERMISSION_AREA_LIST))
                    );
                    return false;
                }
                int page;
                switch (args.length) {
                    case 1:
                        page = 0;
                        break;
                    case 2:
                        if (MathMan.isInteger(args[1])) {
                            page = Integer.parseInt(args[1]) - 1;
                            break;
                        }
                    default:
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax_extended"),
                                Template.of("value1", getCommandString()),
                                Template.of("value2", " list [#]")
                        );
                        return false;
                }
                final List<PlotArea> areas = new ArrayList<>(Arrays.asList(this.plotAreaManager.getAllPlotAreas()));
                paginate(player, areas, 8, page, new RunnableVal3<Integer, PlotArea, CaptionHolder>() {
                    @Override
                    public void run(Integer i, PlotArea area, CaptionHolder caption) {
                        String name;
                        double percent;
                        int claimed = area.getPlotCount();
                        int clusters = area.getClusters().size();
                        String region;
                        String generator = String.valueOf(area.getGenerator());
                        if (area.getType() == PlotAreaType.PARTIAL) {
                            PlotId min = area.getMin();
                            PlotId max = area.getMax();
                            name = area.getWorldName() + ';' + area.getId() + ';' + min + ';' + max;
                            int size = (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1);
                            percent = claimed == 0 ? 0 : claimed / (double) size;
                            region = area.getRegion().toString();
                        } else {
                            name = area.getWorldName();
                            percent = claimed == 0 ? 0 : (double) claimed / Short.MAX_VALUE * Short.MAX_VALUE;
                            region = "N/A";
                        }
                        Template claimedTemplate = Template.of("claimed", String.valueOf(claimed));
                        Template usageTemplate = Template.of("usage", String.format("%.2f", percent) + "%");
                        Template clustersTemplate = Template.of("clusters", String.valueOf(clusters));
                        Template regionTemplate = Template.of("region", region);
                        Template generatorTemplate = Template.of("generator", generator);
                        String tooltip = MINI_MESSAGE.serialize(MINI_MESSAGE
                                .parse(
                                        TranslatableCaption.of("info.area_list_tooltip").getComponent(player),
                                        claimedTemplate,
                                        usageTemplate,
                                        clustersTemplate,
                                        regionTemplate,
                                        generatorTemplate
                                ));
                        Template tooltipTemplate = Template.of("hover_info", tooltip);
                        Template visitcmdTemplate = Template.of("command_tp", "/plot area tp " + area);
                        Template infocmdTemplate = Template.of("command_info", "/plot area info " + area);
                        Template numberTemplate = Template.of("number", String.valueOf(i));
                        Template nameTemplate = Template.of("area_name", name);
                        Template typeTemplate = Template.of("area_type", area.getType().name());
                        Template terrainTemplate = Template.of("area_terrain", area.getTerrain().name());
                        caption.set(TranslatableCaption.of("info.area_list_item"));
                        caption.setTemplates(
                                tooltipTemplate,
                                visitcmdTemplate,
                                numberTemplate,
                                nameTemplate,
                                typeTemplate,
                                terrainTemplate,
                                infocmdTemplate
                        );
                    }
                }, "/plot area list", TranslatableCaption.of("list.area_list_header_paged"));
                return true;
            }
            case "regen", "clear", "reset", "regenerate" -> {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_AREA_REGEN)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", String.valueOf(Permission.PERMISSION_AREA_REGEN))
                    );
                    return false;
                }
                final PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                    return false;
                }
                if (area.getType() != PlotAreaType.PARTIAL) {
                    player.sendMessage(
                            TranslatableCaption.of("single.delete_world_region"),
                            Template.of("world", area.getWorldName())
                    );
                    return false;
                }
                QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(area.getWorldName()));
                queue.setChunkConsumer(chunk -> AugmentedUtils.generate(
                        null,
                        area.getWorldName(),
                        chunk.getX(),
                        chunk.getZ(),
                        null
                ));
                queue.addReadChunks(area.getRegion().getChunks());
                queue.setCompleteTask(() -> player.sendMessage(TranslatableCaption.of("single.regeneration_complete")));
                queue.enqueue();
                return true;
            }
            case "goto", "v", "teleport", "visit", "tp" -> {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_AREA_TP)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", String.valueOf(Permission.PERMISSION_AREA_TP))
                    );
                    return false;
                }
                if (args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot area tp [area]")
                    );
                    return false;
                }
                PlotArea area = this.plotAreaManager.getPlotAreaByString(args[1]);
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_valid_plot_world"), Template.of("value", args[1]));
                    return false;
                }
                Location center;
                if (area instanceof SinglePlotArea) {
                    ((SinglePlotArea) area).loadWorld(PlotId.of(0, 0));
                    center = this.worldUtil.getSpawn(PlotId.of(0, 0).toUnderscoreSeparatedString());
                    player.teleport(center, TeleportCause.COMMAND_AREA_TELEPORT);
                } else if (area.getType() != PlotAreaType.PARTIAL) {
                    center = this.worldUtil.getSpawn(area.getWorldName());
                    player.teleport(center, TeleportCause.COMMAND_AREA_TELEPORT);
                } else {
                    CuboidRegion region = area.getRegion();
                    center = Location.at(area.getWorldName(),
                            region.getMinimumPoint().getX() + (region.getMaximumPoint().getX() - region
                                    .getMinimumPoint()
                                    .getX()) / 2, 0,
                            region.getMinimumPoint().getZ() + (region.getMaximumPoint().getZ() - region
                                    .getMinimumPoint()
                                    .getZ()) / 2
                    );
                    this.worldUtil.getHighestBlock(area.getWorldName(), center.getX(), center.getZ(),
                            y -> player.teleport(center.withY(1 + y), TeleportCause.COMMAND_AREA_TELEPORT)
                    );
                }
                return true;
            }
            case "delete", "remove" -> {
                player.sendMessage(TranslatableCaption.of("single.worldcreation_location"));
                return true;
            }
        }
        sendUsage(player);
        return false;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        if (args.length == 1) {
            final List<String> completions = new LinkedList<>();
            if (Permissions.hasPermission(player, Permission.PERMISSION_AREA_CREATE)) {
                completions.add("create");
            }
            if (Permissions.hasPermission(player, Permission.PERMISSION_AREA_CREATE)) {
                completions.add("single");
            }
            if (Permissions.hasPermission(player, Permission.PERMISSION_AREA_LIST)) {
                completions.add("list");
            }
            if (Permissions.hasPermission(player, Permission.PERMISSION_AREA_INFO)) {
                completions.add("info");
            }
            if (Permissions.hasPermission(player, Permission.PERMISSION_AREA_TP)) {
                completions.add("tp");
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
            if (Permissions.hasPermission(player, Permission.PERMISSION_AREA) && args[0].length() > 0) {
                commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
            }
            return commands;
        }
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

}
