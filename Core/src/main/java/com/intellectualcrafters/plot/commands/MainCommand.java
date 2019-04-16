package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Expression;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Arrays;

/**
 * PlotSquared command class.
 */
@CommandDeclaration(
        command = "plot",
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
            new Rate();
            new DebugClaimTest();
            new Inbox();
            new Comment();
            new Database();
            new Swap();
            new Music();
            new DebugRoadRegen();
            new Trust();
            new DebugExec();
            new FlagCmd();
            new Target();
            new DebugFixFlags();
            new Move();
            new Condense();
            new Copy();
            new Chat();
            new Trim();
            new Done();
            new Continue();
            new BO3();
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
                        CmdConfirm.addPending(player, cmd.getUsage(), new Runnable() {
                            @Override
                            public void run() {
                                if (EconHandler.manager != null) {
                                    PlotArea area = player.getApplicablePlotArea();
                                    if (area != null) {
                                        Expression<Double> priceEval = area.PRICES.get(cmd.getFullId());
                                        Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                        if (price != null && EconHandler.manager.getMoney(player) < price) {
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
                        });
                        return;
                    }
                    if (EconHandler.manager != null) {
                        PlotArea area = player.getApplicablePlotArea();
                        if (area != null) {
                            Expression<Double> priceEval = area.PRICES.get(cmd.getFullId());
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
                @Override
                public void run(Command cmd, CommandResult result) {
                    // Post command stuff!?
                }
            });
        } catch (CommandException e) {
            e.perform(player);
        }
        // Always true
        return true;
    }

    @Deprecated
    /**
     * @Deprecated legacy
     */
    public void addCommand(SubCommand command) {
        PS.debug("Command registration is now done during instantiation");
    }

    @Override
    public void execute(final PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone) {
        // Clear perm caching //
        player.deleteMeta("perm");
        // Optional command scope //
        Location loc = null;
        Plot plot = null;
        boolean tp = false;
        if (args.length >= 2) {
            PlotArea area = player.getApplicablePlotArea();
            Plot newPlot = Plot.fromString(area, args[0]);
            if (newPlot != null && (player instanceof ConsolePlayer || newPlot.getArea().equals(area) || Permissions.hasPermission(player, C.PERMISSION_ADMIN)) && !newPlot.isDenied(player.getUUID())) {
                Location newLoc = newPlot.getCenter();
                if (player.canTeleport(newLoc)) {
                    // Save meta
                    loc = player.getMeta("location");
                    plot = player.getMeta("lastplot");
                    tp = true;
                    // Set loc
                    player.setMeta("location", newLoc);
                    player.setMeta("lastplot", newPlot);
                } else {
                    C.BORDER.send(player);
                }
                // Trim command
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            if (args.length >= 2 && !args[0].isEmpty() && args[0].charAt(0) == '-') {
                switch (args[0].substring(1)) {
                    case "f":
                        confirm = new RunnableVal3<Command, Runnable, Runnable>() {
                            @Override
                            public void run(Command cmd, Runnable success, Runnable failure) {
                                if (EconHandler.manager != null) {
                                    PlotArea area = player.getApplicablePlotArea();
                                    if (area != null) {
                                        Expression<Double> priceEval = area.PRICES.get(cmd.getFullId());
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
                        };
                        args = Arrays.copyOfRange(args, 1, args.length);
                        break;
                    default:
                        C.INVALID_COMMAND_FLAG.send(player);
                        return;
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
                C.ERROR.send(player, message);
            } else {
                C.ERROR.send(player);
            }
        }
        // Reset command scope //
        if (tp && !(player instanceof ConsolePlayer)) {
            if (loc == null) {
                player.deleteMeta("location");
            } else {
                player.setMeta("location", loc);
            }
            if (plot == null) {
                player.deleteMeta("lastplot");
            } else {
                player.setMeta("lastplot", plot);
            }
        }
    }

    @Override
    public boolean canExecute(PlotPlayer player, boolean message) {
        return true;
    }
}
