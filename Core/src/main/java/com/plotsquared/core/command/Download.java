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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.PlotUploader;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@CommandDeclaration(usage = "/plot download [schem | world]",
        command = "download",
        aliases = {"dl"},
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE,
        permission = "plots.download")
public class Download extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final PlotUploader plotUploader;
    @NonNull
    private final SchematicHandler schematicHandler;
    private final WorldUtil worldUtil;

    @Inject
    public Download(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull PlotUploader plotUploader,
            final @NonNull SchematicHandler schematicHandler,
            final @NonNull WorldUtil worldUtil
    ) {
        this.plotAreaManager = plotAreaManager;
        this.plotUploader = plotUploader;
        this.schematicHandler = schematicHandler;
        this.worldUtil = worldUtil;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        String world = player.getCurrentPlot().getWorldName();
        if (!this.plotAreaManager.hasPlotArea(world)) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
            return false;
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
        if ((Settings.Done.REQUIRED_FOR_DOWNLOAD && !DoneFlag.isDone(plot)) && !player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_DOWNLOAD)) {
            player.sendMessage(TranslatableCaption.of("done.done_not_done"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN.toString())) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        if (plot.getRunning() > 0) {
            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            return false;
        }
        if (args.length == 0 || (args.length == 1 && StringMan
                .isEqualIgnoreCaseToAny(args[0], "sch", "schem", "schematic"))) {
            if (plot.getVolume() > Integer.MAX_VALUE) {
                player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
                return false;
            }
            plot.addRunning();
            upload(player, plot);
        } else if (args.length == 1 && StringMan
                .isEqualIgnoreCaseToAny(args[0], "mcr", "world", "mca")) {
            if (!player.hasPermission(Permission.PERMISSION_DOWNLOAD_WORLD)) {
                player.sendMessage(
                        TranslatableCaption.of("permission.no_permission"),
                        TagResolver.resolver(
                                "node",
                                Tag.inserting(Permission.PERMISSION_DOWNLOAD_WORLD)
                        )
                );
                return false;
            }
            player.sendMessage(TranslatableCaption.of("schematics.mca_file_size"));
            plot.addRunning();
            this.worldUtil.saveWorld(world);
            this.worldUtil.upload(plot, null, null, new RunnableVal<>() {
                @Override
                public void run(URL url) {
                    plot.removeRunning();
                    if (url == null) {
                        player.sendMessage(
                                TranslatableCaption.of("web.generating_link_failed"),
                                TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                        );
                        return;
                    }
                    player.sendMessage(
                            TranslatableCaption.of("web.generation_link_success_legacy_world"),
                            TagResolver.builder()
                                    .tag("url", Tag.preProcessParsed(url.toString()))
                                    .build()
                    );
                }
            });
        } else {
            sendUsage(player);
            return false;
        }
        player.sendMessage(
                TranslatableCaption.of("web.generating_link"),
                TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
        );
        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        if (args.length == 1) {
            final List<String> completions = new LinkedList<>();
            if (player.hasPermission(Permission.PERMISSION_DOWNLOAD)) {
                completions.add("schem");
            }
            if (player.hasPermission(Permission.PERMISSION_DOWNLOAD_WORLD)) {
                completions.add("world");
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
            if (player.hasPermission(Permission.PERMISSION_DOWNLOAD) && args[0].length() > 0) {
                commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
            }
            return commands;
        }
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

    private void upload(PlotPlayer<?> player, Plot plot) {
        if (Settings.Web.LEGACY_WEBINTERFACE) {
            schematicHandler
                    .getCompoundTag(plot)
                    .whenComplete((compoundTag, throwable) -> schematicHandler.upload(
                            compoundTag,
                            null,
                            null,
                            new RunnableVal<>() {
                                @Override
                                public void run(URL value) {
                                    plot.removeRunning();
                                    player.sendMessage(
                                            TranslatableCaption.of("web.generation_link_success"),
                                            TagResolver.builder()
                                                    .tag("download", Tag.preProcessParsed(value.toString()))
                                                    .tag("delete", Tag.preProcessParsed("Not available"))
                                                    .build()
                                    );
                                }
                            }
                    ));
            return;
        }
        // TODO legacy support
        this.plotUploader.upload(plot)
                .whenComplete((result, throwable) -> {
                    if (throwable != null || !result.isSuccess()) {
                        player.sendMessage(
                                TranslatableCaption.of("web.generating_link_failed"),
                                TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                        );
                    } else {
                        player.sendMessage(
                                TranslatableCaption.of("web.generation_link_success"),
                                TagResolver.builder()
                                        .tag("download", Tag.preProcessParsed(result.getDownloadUrl()))
                                        .tag("delete", Tag.preProcessParsed(result.getDeletionUrl()))
                                        .build()
                        );
                    }
                });
    }

}
