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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandDeclaration(command = "schematic",
        permission = "plots.schematic",
        aliases = "schem",
        category = CommandCategory.SCHEMATIC,
        usage = "/plot schematic <save | saveall | paste | list>")
public class SchematicCmd extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final SchematicHandler schematicHandler;
    private boolean running = false;

    @Inject
    public SchematicCmd(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull SchematicHandler schematicHandler
    ) {
        this.plotAreaManager = plotAreaManager;
        this.schematicHandler = schematicHandler;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Possible values: save, paste, exportall, list")))
            );
            return true;
        }
        String arg = args[0].toLowerCase();
        switch (arg) {
            case "paste" -> {
                if (!player.hasPermission(Permission.PERMISSION_SCHEMATIC_PASTE)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_SCHEMATIC_PASTE)
                            )
                    );
                    return false;
                }
                if (args.length < 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver(
                                    "value",
                                    Tag.inserting(Component.text("Possible values: save, paste, exportall, list"))
                            )
                    );
                    break;
                }
                final Plot plot = player.getCurrentPlot();
                if (plot == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
                    return false;
                }
                if (!plot.hasOwner()) {
                    player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
                    return false;
                }
                if (!plot.isOwner(player.getUUID()) && !player.hasPermission("plots.admin.command.schematic.paste")) {
                    player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
                    return false;
                }
                if (plot.getVolume() > Integer.MAX_VALUE) {
                    player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
                    return false;
                }
                if (this.running) {
                    player.sendMessage(TranslatableCaption.of("error.task_in_process"));
                    return false;
                }
                final String location = args[1];
                this.running = true;
                TaskManager.runTaskAsync(() -> {
                    Schematic schematic = null;
                    if (location.startsWith("url:")) {
                        try {
                            UUID uuid = UUID.fromString(location.substring(4));
                            URL url = URI.create(Settings.Web.URL + "uploads/" + uuid + ".schematic").toURL();
                            schematic = this.schematicHandler.getSchematic(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(
                                    TranslatableCaption.of("schematics.schematic_invalid"),
                                    TagResolver.resolver(
                                            "reason",
                                            Tag.inserting(Component.text("non-existent url: " + location))
                                    )
                            );
                            SchematicCmd.this.running = false;
                            return;
                        }
                    } else {
                        try {
                            schematic = this.schematicHandler.getSchematic(location);
                        } catch (SchematicHandler.UnsupportedFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if (schematic == null) {
                        SchematicCmd.this.running = false;
                        player.sendMessage(
                                TranslatableCaption.of("schematics.schematic_invalid"),
                                TagResolver.resolver(
                                        "reason",
                                        Tag.inserting(Component.text("non-existent or not in gzip format"))
                                )
                        );
                        return;
                    }
                    this.schematicHandler.paste(
                            schematic,
                            plot,
                            0,
                            plot.getArea().getMinBuildHeight(),
                            0,
                            false,
                            player,
                            new RunnableVal<>() {
                                @Override
                                public void run(Boolean value) {
                                    SchematicCmd.this.running = false;
                                    if (value) {
                                        player.sendMessage(TranslatableCaption.of("schematics.schematic_paste_success"));
                                    } else {
                                        player.sendMessage(TranslatableCaption.of("schematics.schematic_paste_failed"));
                                    }
                                }
                            }
                    );
                });
            }
            case "saveall", "exportall" -> {
                if (!(player instanceof ConsolePlayer)) {
                    player.sendMessage(TranslatableCaption.of("console.not_console"));
                    return false;
                }
                if (args.length != 2) {
                    player.sendMessage(TranslatableCaption.of("schematics.schematic_exportall_world_args"));
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver(
                                    "value",
                                    Tag.inserting(Component.text("Use /plot schematic exportall <area>"))
                            )
                    );
                    return false;
                }
                PlotArea area = this.plotAreaManager.getPlotAreaByString(args[1]);
                if (area == null) {
                    player.sendMessage(
                            TranslatableCaption.of("errors.not_valid_plot_world"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(args[1])))
                    );
                    return false;
                }
                Collection<Plot> plots = area.getPlots();
                if (plots.isEmpty()) {
                    player.sendMessage(TranslatableCaption.of("schematic.schematic_exportall_world"));
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver("value", Tag.inserting(Component.text("Use /plot sch exportall <area>")))
                    );
                    return false;
                }
                boolean result = this.schematicHandler.exportAll(plots, null, null,
                        () -> player.sendMessage(TranslatableCaption.of("schematics.schematic_exportall_finished"))
                );
                if (!result) {
                    player.sendMessage(TranslatableCaption.of("error.task_in_process"));
                    return false;
                } else {
                    player.sendMessage(TranslatableCaption.of("schematics.schematic_exportall_started"));
                    player.sendMessage(
                            TranslatableCaption.of("schematics.plot_to_schem"),
                            TagResolver.resolver("amount", Tag.inserting(Component.text(plots.size())))
                    );
                }
            }
            case "export", "save" -> {
                if (!player.hasPermission(Permission.PERMISSION_SCHEMATIC_SAVE)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_SCHEMATIC_SAVE)
                            )
                    );
                    return false;
                }
                if (this.running) {
                    player.sendMessage(TranslatableCaption.of("error.task_in_process"));
                    return false;
                }
                Plot plot = player.getCurrentPlot();
                if (plot == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
                    return false;
                }
                if (!plot.hasOwner()) {
                    player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
                    return false;
                }
                if (plot.getVolume() > Integer.MAX_VALUE) {
                    player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
                    return false;
                }
                if (!plot.isOwner(player.getUUID()) && !player.hasPermission("plots.admin.command.schematic.save")) {
                    player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
                    return false;
                }
                ArrayList<Plot> plots = Lists.newArrayList(plot);
                boolean result = this.schematicHandler.exportAll(plots, null, null, () -> {
                    player.sendMessage(TranslatableCaption.of("schematics.schematic_exportall_single_finished"));
                    SchematicCmd.this.running = false;
                });
                if (!result) {
                    player.sendMessage(TranslatableCaption.of("error.task_in_process"));
                    return false;
                } else {
                    player.sendMessage(TranslatableCaption.of("schematics.schematic_exportall_started"));
                }
            }
            case "list" -> {
                if (!player.hasPermission(Permission.PERMISSION_SCHEMATIC_LIST)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Permission.PERMISSION_SCHEMATIC_LIST)
                            )
                    );
                    return false;
                }
                final String string = StringMan.join(this.schematicHandler.getSchematicNames(), "$2, $1");
                player.sendMessage(
                        TranslatableCaption.of("schematics.schematic_list"),
                        TagResolver.resolver("list", Tag.inserting(Component.text(string)))
                );
            }
            default -> player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Possible values: save, paste, exportall, list")))
            );
        }
        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        if (args.length == 1) {
            final List<String> completions = new LinkedList<>();
            if (player.hasPermission(Permission.PERMISSION_SCHEMATIC_SAVE)) {
                completions.add("save");
            }
            if (player.hasPermission(Permission.PERMISSION_SCHEMATIC_LIST)) {
                completions.add("list");
            }
            if (player.hasPermission(Permission.PERMISSION_SCHEMATIC_PASTE)) {
                completions.add("paste");
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
            if (player.hasPermission(Permission.PERMISSION_SCHEMATIC) && args[0].length() > 0) {
                commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
            }
            return commands;
        }
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

}
