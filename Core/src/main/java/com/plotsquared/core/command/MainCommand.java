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
 *                  Copyright (C) 2020 IntellectualSites
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * PlotSquared command class.
 */
@CommandDeclaration(command = "plot",
    aliases = {"plots", "p", "plotsquared", "plot2", "p2", "ps", "2", "plotme", "plotz", "ap"})
public class MainCommand extends Command {

    private static MainCommand instance;
    public Help help;
    public Toggle toggle;

    private MainCommand() {
        super(null, true);
        instance = this;
    }

    public static MainCommand getInstance() {
        if (instance == null) {
            instance = new MainCommand();

            final PlotAreaManager plotAreaManager = PlotSquared.get().getPlotAreaManager();
            final EventDispatcher eventDispatcher = PlotSquared.get().getEventDispatcher();
            final PlotListener plotListener = PlotSquared.get().getPlotListener();

            new Caps();
            new Buy(eventDispatcher);
            new Save(plotAreaManager);
            new Load(plotAreaManager);
            new Confirm();
            new Template(plotAreaManager);
            new Download(plotAreaManager);
            new Template(plotAreaManager);
            new Setup();
            new Area(plotAreaManager, eventDispatcher, plotListener);
            new DebugSaveTest();
            new DebugLoadTest();
            new CreateRoadSchematic();
            new DebugAllowUnsafe();
            new RegenAllRoads(plotAreaManager);
            new Claim(eventDispatcher);
            new Auto(plotAreaManager, eventDispatcher);
            new HomeCommand(plotAreaManager);
            new Visit(plotAreaManager);
            new Set();
            new Clear(eventDispatcher);
            new Delete(eventDispatcher);
            new Trust(eventDispatcher);
            new Add(eventDispatcher);
            new Leave(eventDispatcher);
            new Deny(plotAreaManager, eventDispatcher);
            new Remove(eventDispatcher);
            new Info();
            new Near();
            new ListCmd(plotAreaManager);
            new Debug(plotAreaManager);
            new SchematicCmd(plotAreaManager);
            new PluginCmd();
            new Purge(plotAreaManager, plotListener);
            new Reload(plotAreaManager);
            new Relight();
            new Merge(eventDispatcher);
            new DebugPaste();
            new Unlink(eventDispatcher);
            new Kick(plotAreaManager);
            new Inbox();
            new Comment();
            new DatabaseCommand(plotAreaManager, eventDispatcher, plotListener);
            new Swap();
            new Music();
            new DebugRoadRegen();
            new Trust(eventDispatcher);
            new DebugExec(plotAreaManager, eventDispatcher);
            new FlagCommand();
            new Target();
            new Move(plotAreaManager);
            new Condense(plotAreaManager);
            new Copy();
            new Chat();
            new Trim(plotAreaManager);
            new Done(eventDispatcher);
            new Continue(eventDispatcher);
            new Middle();
            new Grant();
            // Set commands
            new Owner(eventDispatcher);
            new Desc(eventDispatcher);
            new Biome();
            new Alias();
            new SetHome();
            new Cluster();
            new DebugImportWorlds(plotAreaManager);
            new Backup();

            if (Settings.Ratings.USE_LIKES) {
                final Like like = new Like(eventDispatcher);
                new Dislike(like);
            } else {
                new Rate(eventDispatcher);
            }

            // Referenced commands
            instance.toggle = new Toggle();
            instance.help = new Help(instance);
        }
        return instance;
    }

    public static boolean onCommand(final PlotPlayer<?> player, String... args) {
        if (args.length >= 1 && args[0].contains(":")) {
            String[] split2 = args[0].split(":");
            if (split2.length == 2) {
                // Ref: c:v, this will push value to the last spot in the array
                // ex. /p h:2 SomeUsername
                // > /p h SomeUsername 2
                String[] tmp = new String[args.length + 1];
                tmp[0] = split2[0];
                tmp[args.length] = split2[1];
                if (args.length >= 2) {
                    System.arraycopy(args, 1, tmp, 1, args.length - 1);
                }
                args = tmp;
            }
        }
        try {
            getInstance().execute(player, args, new RunnableVal3<Command, Runnable, Runnable>() {
                @Override
                public void run(final Command cmd, final Runnable success, final Runnable failure) {
                    if (cmd.hasConfirmation(player)) {
                        CmdConfirm.addPending(player, cmd.getUsage(), () -> {
                            if (EconHandler.getEconHandler() != null) {
                                PlotArea area = player.getApplicablePlotArea();
                                if (area != null) {
                                    Expression<Double> priceEval =
                                        area.getPrices().get(cmd.getFullId());
                                    Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                    if (price != null
                                        && EconHandler.getEconHandler().getMoney(player) < price) {
                                        if (failure != null) {
                                            failure.run();
                                        }
                                        return;
                                    }
                                }
                            }
                            if (success != null) {
                                success.run();
                            }
                        });
                        return;
                    }
                    if (EconHandler.getEconHandler() != null) {
                        PlotArea area = player.getApplicablePlotArea();
                        if (area != null) {
                            Expression<Double> priceEval = area.getPrices().get(cmd.getFullId());
                            Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                            if (price != 0d && EconHandler.getEconHandler().getMoney(player) < price) {
                                if (failure != null) {
                                    failure.run();
                                }
                                return;
                            }
                        }
                    }
                    if (success != null) {
                        success.run();
                    }
                }
            }, new RunnableVal2<Command, CommandResult>() {
                @Override public void run(Command cmd, CommandResult result) {
                    // Post command stuff!?
                }
            }).thenAccept(result -> {
                // TODO: Something with the command result
            });
        } catch (CommandException e) {
            e.perform(player);
        }
        // Always true
        return true;
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        // Clear perm caching //
        player.deleteMeta("perm");
        // Optional command scope //
        Location location = null;
        Plot plot = null;
        boolean tp = false;
        if (args.length >= 2) {
            PlotArea area = player.getApplicablePlotArea();
            Plot newPlot = Plot.fromString(area, args[0]);
            if (newPlot != null && (player instanceof ConsolePlayer || newPlot.getArea()
                .equals(area) || Permissions.hasPermission(player, Captions.PERMISSION_ADMIN)
                || Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_SUDO_AREA))
                && !newPlot.isDenied(player.getUUID())) {
                Location newLoc = newPlot.getCenterSynchronous();
                if (player.canTeleport(newLoc)) {
                    // Save meta
                    location = player.getMeta(PlotPlayer.META_LOCATION);
                    plot = player.getMeta(PlotPlayer.META_LAST_PLOT);
                    tp = true;
                    // Set loc
                    player.setMeta(PlotPlayer.META_LOCATION, newLoc);
                    player.setMeta(PlotPlayer.META_LAST_PLOT, newPlot);
                } else {
                    Captions.BORDER.send(player);
                }
                // Trim command
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            if (args.length >= 2 && !args[0].isEmpty() && args[0].charAt(0) == '-') {
                if ("f".equals(args[0].substring(1))) {
                    confirm = new RunnableVal3<Command, Runnable, Runnable>() {
                        @Override public void run(Command cmd, Runnable success, Runnable failure) {
                            if (EconHandler.getEconHandler() != null) {
                                PlotArea area = player.getApplicablePlotArea();
                                if (area != null) {
                                    Expression<Double> priceEval =
                                        area.getPrices().get(cmd.getFullId());
                                    Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                    if (price != 0d
                                        && EconHandler.getEconHandler().getMoney(player) < price) {
                                        if (failure != null) {
                                            failure.run();
                                        }
                                        return;
                                    }
                                }
                            }
                            if (success != null) {
                                success.run();
                            }
                        }
                    };
                    args = Arrays.copyOfRange(args, 1, args.length);
                } else {
                    Captions.INVALID_COMMAND_FLAG.send(player);
                    return CompletableFuture.completedFuture(false);
                }
            }
        }
        try {
            super.execute(player, args, confirm, whenDone);
        } catch (CommandException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            String message = e.getLocalizedMessage();
            if (message != null) {
                Captions.ERROR.send(player, message);
            } else {
                Captions.ERROR.send(player);
            }
        }
        // Reset command scope //
        if (tp && !(player instanceof ConsolePlayer)) {
            if (location == null) {
                player.deleteMeta(PlotPlayer.META_LOCATION);
            } else {
                player.setMeta(PlotPlayer.META_LOCATION, location);
            }
            if (plot == null) {
                player.deleteMeta(PlotPlayer.META_LAST_PLOT);
            } else {
                player.setMeta(PlotPlayer.META_LAST_PLOT, plot);
            }
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override public boolean canExecute(PlotPlayer player, boolean message) {
        return true;
    }
}
