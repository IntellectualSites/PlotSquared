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
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.database.Database;
import com.plotsquared.core.database.MySQL;
import com.plotsquared.core.database.SQLManager;
import com.plotsquared.core.database.SQLite;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

@CommandDeclaration(command = "database",
        aliases = {"convert"},
        category = CommandCategory.ADMINISTRATION,
        permission = "plots.database",
        requiredType = RequiredType.CONSOLE,
        usage = "/plot database [area] <sqlite | mysql | import>")
public class DatabaseCommand extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final PlotListener plotListener;
    private final YamlConfiguration worldConfiguration;

    @Inject
    public DatabaseCommand(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull PlotListener plotListener,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration
    ) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.plotListener = plotListener;
        this.worldConfiguration = worldConfiguration;
    }

    public static void insertPlots(
            final SQLManager manager, final List<Plot> plots,
            final PlotPlayer<?> player
    ) {
        TaskManager.runTaskAsync(() -> {
            try {
                ArrayList<Plot> ps = new ArrayList<>(plots);
                player.sendMessage(TranslatableCaption.of("database.starting_conversion"));
                manager.createPlotsAndData(ps, () -> {
                    player.sendMessage(TranslatableCaption.of("database.conversion_done"));
                    manager.close();
                });
            } catch (Exception e) {
                player.sendMessage(TranslatableCaption.of("database.conversion_failed"));
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver(
                            "value",
                            Tag.inserting(Component.text("/plot database [area] <sqlite | mysql | import>"))
                    )
            );
            return false;
        }
        List<Plot> plots;
        PlotArea area = this.plotAreaManager.getPlotAreaByString(args[0]);
        if (area != null) {
            plots = PlotSquared.get().sortPlotsByTemp(area.getPlots());
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            plots = PlotSquared.get().sortPlotsByTemp(PlotQuery.newQuery().allPlots().asList());
        }
        if (args.length < 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("/plot database [area] <sqlite|mysql|import>")))
            );
            player.sendMessage(TranslatableCaption.of("database.arg"));
            return false;
        }
        try {
            Database implementation;
            String prefix = "";
            switch (args[0].toLowerCase()) {
                case "import" -> {
                    if (args.length < 2) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                TagResolver.resolver(
                                        "value",
                                        Tag.inserting(Component.text("/plot database import <sqlite file> [prefix]"))
                                )
                        );
                        return false;
                    }
                    File file = FileUtils.getFile(
                            PlotSquared.platform().getDirectory(),
                            args[1].endsWith(".db") ? args[1] : args[1] + ".db"
                    );
                    if (!file.exists()) {
                        player.sendMessage(
                                TranslatableCaption.of("database.does_not_exist"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(file.toString())))
                        );
                        return false;
                    }
                    player.sendMessage(TranslatableCaption.of("database.starting_conversion"));
                    implementation = new SQLite(file);
                    SQLManager manager = new SQLManager(implementation, args.length == 3 ? args[2] : "",
                            this.eventDispatcher, this.plotListener, this.worldConfiguration
                    );
                    HashMap<String, HashMap<PlotId, Plot>> map = manager.getPlots();
                    plots = new ArrayList<>();
                    for (Entry<String, HashMap<PlotId, Plot>> entry : map.entrySet()) {
                        String areaName = entry.getKey();
                        PlotArea pa = this.plotAreaManager.getPlotAreaByString(areaName);
                        if (pa != null) {
                            for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                                Plot plot = entry2.getValue();
                                if (pa.getOwnedPlotAbs(plot.getId()) != null) {
                                    if (pa instanceof SinglePlotArea) {
                                        Plot newPlot = pa.getNextFreePlot(null, plot.getId());
                                        if (newPlot != null) {
                                            PlotId newId = newPlot.getId();
                                            PlotId id = plot.getId();
                                            File worldFile =
                                                    new File(
                                                            PlotSquared.platform().worldContainer(),
                                                            id.toCommaSeparatedString()
                                                    );
                                            if (worldFile.exists()) {
                                                File newFile =
                                                        new File(
                                                                PlotSquared.platform().worldContainer(),
                                                                newId.toCommaSeparatedString()
                                                        );
                                                worldFile.renameTo(newFile);
                                            }
                                            plot.setId(newId);
                                            plot.setArea(pa);
                                            plots.add(plot);
                                            continue;
                                        }
                                    }
                                    player.sendMessage(
                                            TranslatableCaption.of("database.skipping_duplicated_plot"),
                                            TagResolver.builder()
                                                    .tag("plot", Tag.inserting(Component.text(plot.toString())))
                                                    .tag("id", Tag.inserting(Component.text(plot.temp)))
                                                    .build()
                                    );
                                    continue;
                                }
                                plot.setArea(pa);
                                plots.add(plot);
                            }
                        } else {
                            HashMap<PlotId, Plot> plotMap = PlotSquared.get().plots_tmp
                                    .computeIfAbsent(areaName, k -> new HashMap<>());
                            plotMap.putAll(entry.getValue());
                        }
                    }
                    DBFunc.createPlotsAndData(
                            plots,
                            () -> player.sendMessage(TranslatableCaption.of("database.conversion_done"))
                    );
                    return true;
                }
                case "mysql" -> {
                    if (args.length < 6) {
                        player.sendMessage(StaticCaption.of(
                                "/plot database mysql [host] [port] [username] [password] [database] {prefix}"));
                        return false;
                    }
                    String host = args[1];
                    String port = args[2];
                    String username = args[3];
                    String password = args[4];
                    String database = args[5];
                    if (args.length > 6) {
                        prefix = args[6];
                    }
                    implementation = new MySQL(host, port, database, username, password);
                }
                case "sqlite" -> {
                    if (args.length < 2) {
                        player.sendMessage(StaticCaption.of("/plot database sqlite [file]"));
                        return false;
                    }
                    File sqliteFile =
                            FileUtils.getFile(PlotSquared.platform().getDirectory(), args[1] + ".db");
                    implementation = new SQLite(sqliteFile);
                }
                default -> {
                    player.sendMessage(StaticCaption.of("/plot database [sqlite/mysql]"));
                    return false;
                }
            }
            try {
                SQLManager manager = new SQLManager(
                        implementation,
                        prefix,
                        this.eventDispatcher,
                        this.plotListener,
                        this.worldConfiguration
                );
                DatabaseCommand.insertPlots(manager, plots, player);
                return true;
            } catch (ClassNotFoundException | SQLException e) {
                player.sendMessage(TranslatableCaption.of("database.failed_to_save_plots"));
                player.sendMessage(TranslatableCaption.of("errors.stacktrace_begin"));
                e.printStackTrace();
                player.sendMessage(TranslatableCaption.of("errors.stacktrace_end"));
                player.sendMessage(TranslatableCaption.of("database.invalid_args"));
                return false;
            }
        } catch (ClassNotFoundException | SQLException e) {
            player.sendMessage(TranslatableCaption.of("database.failed_to_open"));
            player.sendMessage(TranslatableCaption.of("errors.stacktrace_begin"));
            e.printStackTrace();
            player.sendMessage(TranslatableCaption.of("errors.stacktrace_end"));
            player.sendMessage(TranslatableCaption.of("database.invalid_args"));
            return false;
        }
    }

}
