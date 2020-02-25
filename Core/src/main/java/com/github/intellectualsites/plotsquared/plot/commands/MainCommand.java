package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;
import com.github.intellectualsites.plotsquared.plot.object.Expression;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.CmdConfirm;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;

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
            new Buy();
            new Save();
            new Load();
            new Confirm();
            new Template();
            new Download();
            new Template();
            new Setup();
            new Area();
            new DebugSaveTest();
            new DebugLoadTest();
            new CreateRoadSchematic();
            new DebugAllowUnsafe();
            new RegenAllRoads();
            new Claim();
            new Auto();
            new Visit();
            new Set();
            new Clear();
            new Delete();
            new Trust();
            new Add();
            new Leave();
            new Deny();
            new Remove();
            new Info();
            new Near();
            new ListCmd();
            new Debug();
            new SchematicCmd();
            new PluginCmd();
            new Purge();
            new Reload();
            new Relight();
            new Merge();
            new DebugPaste();
            new Unlink();
            new Kick();
            new DebugClaimTest();
            new Inbox();
            new Comment();
            new DatabaseCommand();
            new Swap();
            new Music();
            new DebugRoadRegen();
            new Trust();
            new DebugExec();
            new FlagCommand();
            new Target();
            new Move();
            new Condense();
            new Copy();
            new Chat();
            new Trim();
            new Done();
            new Continue();
            new Middle();
            new Grant();
            // Set commands
            new Owner();
            new Desc();
            new Biome();
            new Alias();
            new SetHome();
            new Cluster();
            new DebugImportWorlds();

            if (Settings.Ratings.USE_LIKES) {
                new Like();
                new Dislike();
            } else {
                new Rate();
            }

            // Referenced commands
            instance.toggle = new Toggle();
            instance.help = new Help(instance);
        }
        return instance;
    }

    public static boolean onCommand(final PlotPlayer player, String... args) {
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
                            if (EconHandler.manager != null) {
                                PlotArea area = player.getApplicablePlotArea();
                                if (area != null) {
                                    Expression<Double> priceEval = area.getPrices().get(cmd.getFullId());
                                    Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                    if (price != null
                                        && EconHandler.manager.getMoney(player) < price) {
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
                    if (EconHandler.manager != null) {
                        PlotArea area = player.getApplicablePlotArea();
                        if (area != null) {
                            Expression<Double> priceEval = area.getPrices().get(cmd.getFullId());
                            Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                            if (price != 0d && EconHandler.manager.getMoney(player) < price) {
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

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
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
                Location newLoc = newPlot.getCenter();
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
                            if (EconHandler.manager != null) {
                                PlotArea area = player.getApplicablePlotArea();
                                if (area != null) {
                                    Expression<Double> priceEval = area.getPrices().get(cmd.getFullId());
                                    Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                    if (price != 0d
                                        && EconHandler.manager.getMoney(player) < price) {
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
