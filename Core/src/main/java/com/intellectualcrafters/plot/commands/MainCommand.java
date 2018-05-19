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
import java.util.Collection;

/**
 * PlotSquared command class.
 */
@CommandDeclaration(
        command = "plot",
        aliases = {"plots", "p", "plotsquared", "plot2", "p2", "ps", "2", "plotme", "plotz", "ap"})
public class MainCommand extends Command {
    
    private static MainCommand instance;
    public Help help;

    private MainCommand() {
        super(null, true);
        instance = this;
    }

    public static MainCommand getInstance() {
        if (instance == null) {
            instance = new MainCommand();
            new Add();

            Alias aliasCmd = new Alias();
            new AliasSet(aliasCmd, true);
            new AliasRemove(aliasCmd, true);

            Area areaCmd = new Area();
            new AreaCreate(areaCmd, true);
            new AreaInfo(areaCmd, true);
            new AreaList(areaCmd, true);
            new AreaRegen(areaCmd, true);
            new AreaTeleport(areaCmd, true);

            new Auto();
            new Biome();

            BO3 bo3Cmd = new BO3();
            new BO3Export(bo3Cmd, true);
            new BO3Import(bo3Cmd, true);

            new Buy();
            new Changelog();
            new Chat();
            new Claim();
            new Clear();

            Cluster clusterCmd = new Cluster();
            new ClusterCreate(clusterCmd, true);
            new ClusterDelete(clusterCmd, true);
            ClusterHelpers clusterHelpersCmd = new ClusterHelpers(clusterCmd, true);
            new ClusterHelpersAdd(clusterHelpersCmd, true);
            new ClusterHelpersRemove(clusterHelpersCmd, true);
            new ClusterInfo(clusterCmd, true);
            new ClusterInvite(clusterCmd, true);
            new ClusterKick(clusterCmd, true);
            new ClusterLeave(clusterCmd, true);
            new ClusterList(clusterCmd, true);
            new ClusterResize(clusterCmd, true);
            new ClusterSethome(clusterCmd, true);
            new ClusterTeleport(clusterCmd, true);

            new Comment();
            new Condense();
            new Confirm();
            new Continue();
            new Copy();
            new CreateRoadSchematic();
            new Database();
            new Debug();
            new DebugAllowUnsafe();
            new DebugClaimTest();
            new DebugExec();
            new DebugFixFlags();
            new DebugImportWorlds();
            new DebugLoadTest();
            new DebugPaste();
            new DebugRoadRegen();
            new DebugSaveTest();
            new Delete();
            new Deny();
            new Desc();
            new Done();

            Download downloadCmd = new Download();
            new DownloadBO3(downloadCmd, true);
            new DownloadSchematic(downloadCmd, true);
            new DownloadWorld(downloadCmd, true);

            FlagCmd flagCmd = new FlagCmd();
            new FlagAdd(flagCmd, true);
            new FlagInfo(flagCmd, true);
            new FlagList(flagCmd, true);
            new FlagRemove(flagCmd, true);
            new FlagSet(flagCmd, true);

            Grant grantCmd = new Grant();
            new GrantAdd(grantCmd, true);
            new GrantCheck(grantCmd, true);

            instance.help = new Help(instance);
            new Home();
            new Inbox();
            new Info();
            new Kick();
            new Leave();
            new ListCmd();
            new Load();
            new Merge();
            new Middle();
            new Move();
            new Music();
            new Near();
            new Owner();
            new PluginCmd();
            new Purge();
            new Rate();
            new RegenAllRoads();
            new Relight();
            new Reload();
            new Remove();
            new Save();

            SchematicCmd schematicCmd = new SchematicCmd();
            new SchematicPaste(schematicCmd, true);
            new SchematicSave(schematicCmd, true);
            new SchematicSaveall(schematicCmd, true);
            new SchematicTest(schematicCmd, true);

            Set setCmd = new Set();
            new SetBiome(setCmd, true);
            new SetDescription(setCmd, true);
            new SetHome(setCmd, true);
            new SetOwner(setCmd, true);

            new Setflag();
            new Setup();
            new Swap();
            new Target();

            Template templateCmd = new Template();
            new TemplateExport(templateCmd, true);
            new TemplateImport(templateCmd, true);

            Toggle toggleCmd = new Toggle();
            new ToggleChat(toggleCmd, true);
            new ToggleChatspy(toggleCmd, true);
            new ToggleClearconfirm(toggleCmd, true);
            new ToggleTitles(toggleCmd, true);
            new ToggleWorldedit(toggleCmd, true);

            new Trim();
            new Trust();
            new Unlink();
            new Visit();
            new WE_Anywhere();
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
    public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        // Clear perm caching //
        player.deleteMeta("perm");

        return super.tab(player, args, space);
    }

    @Override
    public boolean canExecute(PlotPlayer player, boolean message) {
        return true;
    }
}
