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
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlotRateEvent;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.plot.Rating;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandDeclaration(command = "rate",
        permission = "plots.rate",
        usage = "/plot rate [# | next | purge]",
        aliases = "rt",
        category = CommandCategory.INFO,
        requiredType = RequiredType.PLAYER)
public class Rate extends SubCommand {

    private final EventDispatcher eventDispatcher;
    private final InventoryUtil inventoryUtil;

    @Inject
    public Rate(
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull InventoryUtil inventoryUtil
    ) {
        this.eventDispatcher = eventDispatcher;
        this.inventoryUtil = inventoryUtil;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "next" -> {
                    final List<Plot> plots = PlotQuery.newQuery().whereBasePlot().asList();
                    plots.sort((p1, p2) -> {
                        double v1 = 0;
                        if (!p1.getRatings().isEmpty()) {
                            for (Entry<UUID, Rating> entry : p1.getRatings().entrySet()) {
                                v1 -= 11 - entry.getValue().getAverageRating();
                            }
                        }
                        double v2 = 0;
                        if (!p2.getRatings().isEmpty()) {
                            for (Entry<UUID, Rating> entry : p2.getRatings().entrySet()) {
                                v2 -= 11 - entry.getValue().getAverageRating();
                            }
                        }
                        if (v1 == v2) {
                            return -0;
                        }
                        return v2 > v1 ? 1 : -1;
                    });
                    UUID uuid = player.getUUID();
                    for (Plot p : plots) {
                        if ((!Settings.Done.REQUIRED_FOR_RATINGS || DoneFlag.isDone(p)) && p
                                .isBasePlot() && !p.getRatings().containsKey(uuid) && !p
                                .isAdded(uuid)) {
                            p.teleportPlayer(player, TeleportCause.COMMAND_RATE, result -> {
                            });
                            player.sendMessage(TranslatableCaption.of("tutorial.rate_this"));
                            return true;
                        }
                    }
                    player.sendMessage(TranslatableCaption.of("invalid.found_no_plots"));
                    return false;
                }
                case "purge" -> {
                    final Plot plot = player.getCurrentPlot();
                    if (plot == null) {
                        player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
                        return false;
                    }
                    if (!player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_PURGE_RATINGS, true)) {
                        return false;
                    }
                    plot.clearRatings();
                    player.sendMessage(TranslatableCaption.of("ratings.ratings_purged"));
                    return true;
                }
            }
        }
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("ratings.rating_not_owned"));
            return false;
        }
        if (plot.isOwner(player.getUUID())) {
            player.sendMessage(TranslatableCaption.of("ratings.rating_not_your_own"));
            return false;
        }
        if (Settings.Done.REQUIRED_FOR_RATINGS && !DoneFlag.isDone(plot)) {
            player.sendMessage(TranslatableCaption.of("ratings.rating_not_done"));
            return false;
        }
        if (Settings.Ratings.CATEGORIES != null && !Settings.Ratings.CATEGORIES.isEmpty()) {
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (plot.getRatings().containsKey(player.getUUID())) {
                        player.sendMessage(
                                TranslatableCaption.of("ratings.rating_already_exists"),
                                TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                        );
                        return;
                    }
                    final MutableInt index = new MutableInt(0);
                    final MutableInt rating = new MutableInt(0);
                    String title = Settings.Ratings.CATEGORIES.get(0);
                    PlotInventory inventory = new PlotInventory(inventoryUtil, player, 1, title) {
                        @Override
                        public boolean onClick(int i) {
                            rating.add((i + 1) * Math.pow(10, index.getValue()));
                            index.increment();
                            if (index.getValue() >= Settings.Ratings.CATEGORIES.size()) {
                                int rV = rating.getValue();
                                PlotRateEvent event = Rate.this.eventDispatcher
                                        .callRating(this.getPlayer(), plot, new Rating(rV));
                                if (event.getRating() != null) {
                                    plot.addRating(this.getPlayer().getUUID(), event.getRating());
                                    getPlayer().sendMessage(
                                            TranslatableCaption.of("ratings.rating_applied"),
                                            TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                                    );
                                }
                                return false;
                            }
                            setTitle(Settings.Ratings.CATEGORIES.get(index.getValue()));
                            return true;
                        }
                    };
                    inventory.setItem(0, new PlotItemStack(Settings.Ratings.BLOCK_0, 1,
                            TranslatableCaption.of("ratings.0-8").getComponent(player)
                    ));
                    inventory.setItem(1, new PlotItemStack(Settings.Ratings.BLOCK_1, 1,
                            TranslatableCaption.of("ratings.1-8").getComponent(player)
                    ));
                    inventory.setItem(2, new PlotItemStack(Settings.Ratings.BLOCK_2, 2,
                            TranslatableCaption.of("ratings.2-8").getComponent(player)
                    ));
                    inventory.setItem(3, new PlotItemStack(Settings.Ratings.BLOCK_3, 3,
                            TranslatableCaption.of("ratings.3-8").getComponent(player)
                    ));
                    inventory.setItem(4, new PlotItemStack(Settings.Ratings.BLOCK_4, 4,
                            TranslatableCaption.of("ratings.4-8").getComponent(player)
                    ));
                    inventory.setItem(5, new PlotItemStack(Settings.Ratings.BLOCK_5, 5,
                            TranslatableCaption.of("ratings.5-8").getComponent(player)
                    ));
                    inventory.setItem(6, new PlotItemStack(Settings.Ratings.BLOCK_6, 6,
                            TranslatableCaption.of("ratings.6-8").getComponent(player)
                    ));
                    inventory.setItem(7, new PlotItemStack(Settings.Ratings.BLOCK_7, 7,
                            TranslatableCaption.of("ratings.7-8").getComponent(player)
                    ));
                    inventory.setItem(8, new PlotItemStack(Settings.Ratings.BLOCK_8, 8,
                            TranslatableCaption.of("ratings.8-8").getComponent(player)
                    ));
                    inventory.openInventory();
                }
            };
            if (plot.getSettings().getRatings() == null) {
                if (!Settings.Enabled_Components.RATING_CACHE) {
                    TaskManager.runTaskAsync(() -> {
                        plot.getSettings().setRatings(DBFunc.getRatings(plot));
                        run.run();
                    });
                    return true;
                }
                plot.getSettings().setRatings(new HashMap<>());
            }
            run.run();
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(TranslatableCaption.of("ratings.rating_not_valid"));
            return true;
        }
        String arg = args[0];
        final int rating;
        if (MathMan.isInteger(arg) && arg.length() < 3 && !arg.isEmpty()) {
            rating = Integer.parseInt(arg);
            if (rating > 10 || rating < 1) {
                player.sendMessage(TranslatableCaption.of("ratings.rating_not_valid"));
                return false;
            }
        } else {
            player.sendMessage(TranslatableCaption.of("ratings.rating_not_valid"));
            return false;
        }
        final UUID uuid = player.getUUID();
        final Runnable run = () -> {
            if (plot.getRatings().containsKey(uuid)) {
                player.sendMessage(
                        TranslatableCaption.of("ratings.rating_already_exists"),
                        TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                );
                return;
            }
            PlotRateEvent event =
                    this.eventDispatcher.callRating(player, plot, new Rating(rating));
            if (event.getRating() != null) {
                plot.addRating(uuid, event.getRating());
                player.sendMessage(
                        TranslatableCaption.of("ratings.rating_applied"),
                        TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                );
            }
        };
        if (plot.getSettings().getRatings() == null) {
            if (!Settings.Enabled_Components.RATING_CACHE) {
                TaskManager.runTaskAsync(() -> {
                    plot.getSettings().setRatings(DBFunc.getRatings(plot));
                    run.run();
                });
                return true;
            }
            plot.getSettings().setRatings(new HashMap<>());
        }
        run.run();
        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        if (args.length == 1) {
            final List<String> completions = new LinkedList<>();
            if (player.hasPermission(Permission.PERMISSION_RATE)) {
                completions.add("1 - 10");
            }
            if (player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_PURGE_RATINGS)) {
                completions.add("purge");
            }
            final List<Command> commands = completions.stream().filter(completion -> completion
                            .toLowerCase()
                            .startsWith(args[0].toLowerCase()))
                    .map(completion -> new Command(null, true, completion, "", RequiredType.PLAYER, CommandCategory.INFO) {
                    }).collect(Collectors.toCollection(LinkedList::new));
            if (player.hasPermission(Permission.PERMISSION_RATE) && args[0].length() > 0) {
                commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
            }
            return commands;
        }
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

    private static class MutableInt {

        private int value;

        MutableInt(int i) {
            this.value = i;
        }

        void increment() {
            this.value++;
        }

        void decrement() {
            this.value--;
        }

        int getValue() {
            return this.value;
        }

        void add(Number v) {
            this.value += v.intValue();
        }

    }

}
