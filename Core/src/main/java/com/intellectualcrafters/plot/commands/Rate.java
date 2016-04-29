package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

@CommandDeclaration(command = "rate",
        permission = "plots.rate",
        description = "Rate the plot",
        usage = "/plot rate [#|next]",
        aliases = "rt",
        category = CommandCategory.INFO,
        requiredType = RequiredType.NONE)
public class Rate extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("next")) {
                ArrayList<Plot> plots = new ArrayList<>(PS.get().getBasePlots());
                Collections.sort(plots, new Comparator<Plot>() {
                    @Override
                    public int compare(Plot p1, Plot p2) {
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
                    }
                });
                UUID uuid = player.getUUID();
                for (Plot p : plots) {
                    if ((!Settings.REQUIRE_DONE || p.hasFlag(Flags.DONE)) && p.isBasePlot() && (p.hasRatings() || !p.getRatings()
                            .containsKey(uuid)) && !p.isAdded(uuid)) {
                        p.teleportPlayer(player);
                        MainUtil.sendMessage(player, C.RATE_THIS);
                        return true;
                    }
                }
                MainUtil.sendMessage(player, C.FOUND_NO_PLOTS);
                return false;
            }
        }
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            sendMessage(player, C.RATING_NOT_OWNED);
            return false;
        }
        if (plot.isOwner(player.getUUID())) {
            sendMessage(player, C.RATING_NOT_YOUR_OWN);
            return false;
        }
        if (Settings.REQUIRE_DONE && !plot.hasFlag(Flags.DONE)) {
            sendMessage(player, C.RATING_NOT_DONE);
            return false;
        }
        if (Settings.RATING_CATEGORIES != null && !Settings.RATING_CATEGORIES.isEmpty()) {
            final Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (plot.getRatings().containsKey(player.getUUID())) {
                        sendMessage(player, C.RATING_ALREADY_EXISTS, plot.getId().toString());
                        return;
                    }
                    final MutableInt index = new MutableInt(0);
                    final MutableInt rating = new MutableInt(0);
                    String title = Settings.RATING_CATEGORIES.get(0);
                    PlotInventory inventory = new PlotInventory(player, 1, title) {
                        @Override
                        public boolean onClick(int i) {
                            rating.add((i + 1) * Math.pow(10, index.getValue()));
                            index.increment();
                            if (index.getValue() >= Settings.RATING_CATEGORIES.size()) {
                                int rV = rating.getValue();
                                Rating result = EventUtil.manager.callRating(this.player, plot, new Rating(rV));
                                plot.addRating(this.player.getUUID(), result);
                                sendMessage(this.player, C.RATING_APPLIED, plot.getId().toString());
                                if (Permissions.hasPermission(this.player, "plots.comment")) {
                                    Command command = MainCommand.getInstance().getCommand(Comment.class);
                                    if (command != null) {
                                        MainUtil.sendMessage(this.player, C.COMMENT_THIS, command.getUsage());
                                    }
                                }
                                return false;
                            }
                            setTitle(Settings.RATING_CATEGORIES.get(index.getValue()));
                            return true;
                        }
                    };
                    inventory.setItem(0, new PlotItemStack(35, (short) 12, 0, "0/8"));
                    inventory.setItem(1, new PlotItemStack(35, (short) 14, 1, "1/8"));
                    inventory.setItem(2, new PlotItemStack(35, (short) 1, 2, "2/8"));
                    inventory.setItem(3, new PlotItemStack(35, (short) 4, 3, "3/8"));
                    inventory.setItem(4, new PlotItemStack(35, (short) 5, 4, "4/8"));
                    inventory.setItem(5, new PlotItemStack(35, (short) 9, 5, "5/8"));
                    inventory.setItem(6, new PlotItemStack(35, (short) 11, 6, "6/8"));
                    inventory.setItem(7, new PlotItemStack(35, (short) 10, 7, "7/8"));
                    inventory.setItem(8, new PlotItemStack(35, (short) 2, 8, "8/8"));
                    inventory.openInventory();
                }
            };
            if (plot.getSettings().ratings == null) {
                if (!Settings.CACHE_RATINGS) {
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            plot.getSettings().ratings = DBFunc.getRatings(plot);
                            run.run();
                        }
                    });
                    return true;
                }
                plot.getSettings().ratings = new HashMap<>();
            }
            run.run();
            return true;
        }
        if (args.length < 1) {
            sendMessage(player, C.RATING_NOT_VALID);
            return true;
        }
        String arg = args[0];
        final int rating;
        if (MathMan.isInteger(arg) && arg.length() < 3 && !arg.isEmpty()) {
            rating = Integer.parseInt(arg);
            if (rating > 10 || rating < 1) {
                sendMessage(player, C.RATING_NOT_VALID);
                return false;
            }
        } else {
            sendMessage(player, C.RATING_NOT_VALID);
            return false;
        }
        final UUID uuid = player.getUUID();
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if (plot.getRatings().containsKey(uuid)) {
                    sendMessage(player, C.RATING_ALREADY_EXISTS, plot.getId().toString());
                    return;
                }
                Rating result = EventUtil.manager.callRating(player, plot, new Rating(rating));
                plot.addRating(uuid, result);
                sendMessage(player, C.RATING_APPLIED, plot.getId().toString());
            }
        };
        if (plot.getSettings().ratings == null) {
            if (!Settings.CACHE_RATINGS) {
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        plot.getSettings().ratings = DBFunc.getRatings(plot);
                        run.run();
                    }
                });
                return true;
            }
            plot.getSettings().ratings = new HashMap<>();
        }
        run.run();
        return true;
    }

    private class MutableInt {

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
