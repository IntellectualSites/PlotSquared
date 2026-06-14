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
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.PatternUtil;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@CommandDeclaration(command = "set",
        aliases = {"s"},
        usage = "/plot set <biome | alias | home | flag> <value...>",
        permission = "plots.set",
        category = CommandCategory.APPEARANCE,
        requiredType = RequiredType.NONE)
public class Set extends SubCommand {

    public static final String[] values = new String[]{"biome", "alias", "home"};
    public static final String[] aliases = new String[]{"b", "w", "wf", "a", "h"};

    private final SetCommand component;

    @Inject
    public Set(final @NonNull WorldUtil worldUtil) {
        this.component = new SetCommand() {

            @Override
            public String getId() {
                return "set.component";
            }

            @Override
            public boolean set(PlotPlayer<?> player, final Plot plot, String value) {
                final PlotArea plotArea = player.getContextualPlotArea();
                if (plotArea == null) {
                    return false;
                }
                final PlotManager manager = plotArea.getPlotManager();

                String[] components = manager.getPlotComponents(plot.getId());

                String[] args = value.split(" ");
                String material =
                        StringMan.join(Arrays.copyOfRange(args, 1, args.length), ",").trim();

                final List<String> forbiddenTypes = new ArrayList<>(Settings.General.INVALID_BLOCKS);

                if (Settings.Enabled_Components.CHUNK_PROCESSOR) {
                    forbiddenTypes.addAll(worldUtil.getTileEntityTypes().stream().map(
                            BlockType::getName).toList());
                }

                if (!player.hasPermission(Permission.PERMISSION_ADMIN_ALLOW_UNSAFE) &&
                        !forbiddenTypes.isEmpty()) {
                    for (String forbiddenType : forbiddenTypes) {
                        forbiddenType = forbiddenType.toLowerCase(Locale.ENGLISH);
                        if (forbiddenType.startsWith("minecraft:")) {
                            forbiddenType = forbiddenType.substring(10);
                        }
                        for (String blockType : material.split(",")) {
                            blockType = blockType.toLowerCase(Locale.ENGLISH);
                            if (blockType.startsWith("minecraft:")) {
                                blockType = blockType.substring(10);
                            }

                            if (blockType.startsWith("##")) {
                                try {
                                    final BlockCategory category = BlockCategory.REGISTRY.get(blockType.substring(2)
                                            .replaceAll("[*^|]+", "").toLowerCase(Locale.ENGLISH));
                                    if (category == null || !category.contains(BlockTypes.get(forbiddenType))) {
                                        continue;
                                    }
                                } catch (final Throwable ignored) {
                                }
                            } else if (!blockType.contains(forbiddenType)) {
                                continue;
                            }
                            player.sendMessage(
                                    TranslatableCaption.of("invalid.component_illegal_block"),
                                    TagResolver.resolver("value", Tag.inserting(Component.text(forbiddenType)))
                            );
                            return true;
                        }
                    }
                }

                for (String component : components) {
                    if (component.equalsIgnoreCase(args[0])) {
                        if (!player.hasPermission(Permission.PERMISSION_SET_COMPONENT.format(component))) {
                            player.sendMessage(
                                    TranslatableCaption.of("permission.no_permission"),
                                    TagResolver.resolver(
                                            "node",
                                            Tag.inserting(Component.text(Permission.PERMISSION_SET_COMPONENT.format(component)))
                                    )
                            );
                            return false;
                        }
                        if (args.length < 2) {
                            player.sendMessage(TranslatableCaption.of("need.need_block"));
                            return true;
                        }

                        Pattern pattern = PatternUtil.parse(player, material, false);

                        if (plot.getRunning() > 0) {
                            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
                            return false;
                        }

                        BackupManager.backup(player, plot, () -> {
                            plot.addRunning();
                            QueueCoordinator queue = plotArea.getQueue();
                            queue.setCompleteTask(() -> {
                                plot.removeRunning();
                                player.sendMessage(
                                        TranslatableCaption.of("working.component_complete"),
                                        TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                                );
                            });
                            if (Settings.QUEUE.NOTIFY_PROGRESS) {
                                queue.addProgressSubscriber(
                                        PlotSquared
                                                .platform()
                                                .injector()
                                                .getInstance(ProgressSubscriberFactory.class)
                                                .createWithActor(player));
                            }
                            for (final Plot current : plot.getConnectedPlots()) {
                                current.getPlotModificationManager().setComponent(component, pattern, player, queue);
                            }
                            queue.enqueue();
                            player.sendMessage(TranslatableCaption.of("working.generating_component"));
                        });
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Collection<Command> tab(
                    final PlotPlayer<?> player, final String[] args,
                    final boolean space
            ) {
                return TabCompletions.completePatterns(StringMan.join(args, ","));
            }
        };
    }

    public boolean noArgs(PlotPlayer<?> player) {
        ArrayList<String> newValues = new ArrayList<>(Arrays.asList("biome", "alias", "home"));
        Plot plot = player.getCurrentPlot();
        if (plot != null) {
            newValues.addAll(Arrays.asList(plot.getManager().getPlotComponents(plot.getId())));
        }
        player.sendMessage(StaticCaption.of(TranslatableCaption
                .of("commandconfig.subcommand_set_options_header_only")
                .getComponent(player) + StringMan
                .join(newValues, TranslatableCaption.of("blocklist.block_list_separator").getComponent(player))));
        return false;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        if (args.length == 0) {
            return noArgs(player);
        }
        Command cmd = MainCommand.getInstance().getCommand("set" + args[0]);
        if (cmd != null) {
            if (!player.hasPermission(cmd.getPermission(), true)) {
                return false;
            }
            cmd.execute(player, Arrays.copyOfRange(args, 1, args.length), null, null);
            return true;
        }
        // Additional checks
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
            return false;
        }
        // components
        HashSet<String> components =
                new HashSet<>(Arrays.asList(plot.getManager().getPlotComponents(plot.getId())));
        if (components.contains(args[0].toLowerCase())) {
            return this.component.onCommand(player, Arrays.copyOfRange(args, 0, args.length));
        }
        return noArgs(player);
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, String[] args, boolean space) {
        if (args.length == 1) {
            final List<String> completions = new LinkedList<>();

            if (player.hasPermission(Permission.PERMISSION_SET_BIOME)) {
                completions.add("biome");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_ALIAS)) {
                completions.add("alias");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_HOME)) {
                completions.add("home");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_MAIN)) {
                completions.add("main");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_FLOOR)) {
                completions.add("floor");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_AIR)) {
                completions.add("air");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_ALL)) {
                completions.add("all");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_BORDER)) {
                completions.add("border");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_WALL)) {
                completions.add("wall");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_OUTLINE)) {
                completions.add("outline");
            }
            if (player.hasPermission(Permission.PERMISSION_SET_MIDDLE)) {
                completions.add("middle");
            }
            final List<Command> commands = completions.stream().filter(completion -> completion
                            .toLowerCase()
                            .startsWith(args[0].toLowerCase()))
                    .map(completion -> new Command(null, true, completion, "", RequiredType.NONE, CommandCategory.APPEARANCE) {
                    }).collect(Collectors.toCollection(LinkedList::new));

            if (player.hasPermission(Permission.PERMISSION_SET) && args[0].length() > 0) {
                commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
            }
            return commands;
        } else if (args.length > 1) {
            // Additional checks
            Plot plot = player.getCurrentPlot();
            if (plot == null) {
                return new ArrayList<>();
            }

            final String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);

            final Command cmd = MainCommand.getInstance().getCommand("set" + args[0]);
            if (cmd != null) {
                if (!player.hasPermission(cmd.getPermission(), true)) {
                    return new ArrayList<>();
                }
                return cmd.tab(player, newArgs, space);
            }

            // components
            HashSet<String> components =
                    new HashSet<>(Arrays.asList(plot.getManager().getPlotComponents(plot.getId())));
            if (components.contains(args[0].toLowerCase())) {
                return this.component.tab(player, newArgs, space);
            }
        }
        return tabOf(player, args, space);
    }

}
