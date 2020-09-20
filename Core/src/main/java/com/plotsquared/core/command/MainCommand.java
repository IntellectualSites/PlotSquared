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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.command;

import com.google.inject.Injector;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * PlotSquared command class.
 */
@CommandDeclaration(command = "plot",
    aliases = {"plots", "p", "plotsquared", "plot2", "p2", "ps", "2", "plotme", "plotz", "ap"})
public class MainCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + MainCommand.class.getSimpleName());

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

            final Injector injector = PlotSquared.platform().getInjector();
            final List<Class<? extends Command>> commands = new LinkedList<>();
            commands.add(Caps.class);
            commands.add(Buy.class);
            commands.add(Save.class);
            commands.add(Load.class);
            commands.add(Confirm.class);
            commands.add(Template.class);
            commands.add(Download.class);
            commands.add(Setup.class);
            commands.add(Area.class);
            commands.add(DebugSaveTest.class);
            commands.add(DebugLoadTest.class);
            commands.add(CreateRoadSchematic.class);
            commands.add(DebugAllowUnsafe.class);
            commands.add(RegenAllRoads.class);
            commands.add(Claim.class);
            commands.add(Auto.class);
            commands.add(HomeCommand.class);
            commands.add(Visit.class);
            commands.add(Set.class);
            commands.add(Clear.class);
            commands.add(Delete.class);
            commands.add(Trust.class);
            commands.add(Add.class);
            commands.add(Leave.class);
            commands.add(Deny.class);
            commands.add(Remove.class);
            commands.add(Info.class);
            commands.add(Near.class);
            commands.add(ListCmd.class);
            commands.add(Debug.class);
            commands.add(SchematicCmd.class);
            commands.add(PluginCmd.class);
            commands.add(Purge.class);
            commands.add(Reload.class);
            commands.add(Relight.class);
            commands.add(Merge.class);
            commands.add(DebugPaste.class);
            commands.add(Unlink.class);
            commands.add(Kick.class);
            commands.add(Inbox.class);
            commands.add(Comment.class);
            commands.add(DatabaseCommand.class);
            commands.add(Swap.class);
            commands.add(Music.class);
            commands.add(DebugRoadRegen.class);
            commands.add(DebugExec.class);
            commands.add(FlagCommand.class);
            commands.add(Target.class);
            commands.add(Move.class);
            commands.add(Condense.class);
            commands.add(Copy.class);
            commands.add(Chat.class);
            commands.add(Trim.class);
            commands.add(Done.class);
            commands.add(Continue.class);
            commands.add(Middle.class);
            commands.add(Grant.class);
            commands.add(Owner.class);
            commands.add(Desc.class);
            commands.add(Biome.class);
            commands.add(Alias.class);
            commands.add(SetHome.class);
            commands.add(Cluster.class);
            commands.add(DebugImportWorlds.class);
            commands.add(Backup.class);

            if (Settings.Ratings.USE_LIKES) {
                commands.add(Like.class);
                commands.add(Dislike.class);
            } else {
                commands.add(Rate.class);
            }

            for (final Class<? extends Command> command : commands) {
                try {
                    injector.getInstance(command);
                } catch (final Exception e) {
                    logger.error("Failed to register command {}", command.getCanonicalName());
                    e.printStackTrace();
                }
            }

            // Referenced commands
            instance.toggle = injector.getInstance(Toggle.class);
            instance.help = new Help(instance);
        }
        return instance;
    }

    public static boolean onCommand(final PlotPlayer<?> player, String... args) {
        final EconHandler econHandler = PlotSquared.platform().getEconHandler();
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
                            PlotArea area = player.getApplicablePlotArea();
                            if (area != null && econHandler.isEnabled(area)) {
                                Expression<Double> priceEval =
                                    area.getPrices().get(cmd.getFullId());
                                Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                if (price != null
                                    && econHandler.getMoney(player) < price) {
                                    if (failure != null) {
                                        failure.run();
                                    }
                                    return;
                                }
                            }
                            if (success != null) {
                                success.run();
                            }
                        });
                        return;
                    }
                    if (econHandler != null) {
                        PlotArea area = player.getApplicablePlotArea();
                        if (area != null) {
                            Expression<Double> priceEval = area.getPrices().get(cmd.getFullId());
                            Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                            if (price != 0d && econHandler.getMoney(player) < price) {
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
        // Optional command scope //
        Location location = null;
        Plot plot = null;
        boolean tp = false;
        if (args.length >= 2) {
            PlotArea area = player.getApplicablePlotArea();
            Plot newPlot = Plot.fromString(area, args[0]);
            if (newPlot != null && (player instanceof ConsolePlayer || newPlot.getArea()
                .equals(area) || Permissions.hasPermission(player, Permission.PERMISSION_ADMIN)
                || Permissions.hasPermission(player, Permission.PERMISSION_ADMIN_AREA_SUDO))
                && !newPlot.isDenied(player.getUUID())) {
                Location newLoc = newPlot.getCenterSynchronous();
                if (player.canTeleport(newLoc)) {
                    // Save meta
                    try (final MetaDataAccess<Location> locationMetaDataAccess
                        = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
                        location = locationMetaDataAccess.get().orElse(null);
                        locationMetaDataAccess.set(newLoc);
                    }
                    try (final MetaDataAccess<Plot> plotMetaDataAccess
                        = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                        plot = plotMetaDataAccess.get().orElse(null);
                        plotMetaDataAccess.set(newPlot);
                    }
                    tp = true;
                } else {
                    player.sendMessage(TranslatableCaption.of("border.border"));
                }
                // Trim command
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            if (args.length >= 2 && !args[0].isEmpty() && args[0].charAt(0) == '-') {
                if ("f".equals(args[0].substring(1))) {
                    confirm = new RunnableVal3<Command, Runnable, Runnable>() {
                        @Override public void run(Command cmd, Runnable success, Runnable failure) {
                            if (PlotSquared.platform().getEconHandler() != null) {
                                PlotArea area = player.getApplicablePlotArea();
                                if (area != null) {
                                    Expression<Double> priceEval =
                                        area.getPrices().get(cmd.getFullId());
                                    Double price = priceEval != null ? priceEval.evaluate(0d) : 0d;
                                    if (price != 0d
                                        && PlotSquared.platform().getEconHandler().getMoney(player) < price) {
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
                    player.sendMessage(TranslatableCaption.of("errors.invalid_command_flag"));
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
            String message = e.getMessage();
            if (message != null) {
                player.sendMessage(TranslatableCaption.of("errors.error"),
                        net.kyori.adventure.text.minimessage.Template.of("value", message));
            } else {
                player.sendMessage(
                        TranslatableCaption.of("errors.error_console"));
            }
        }
        // Reset command scope //
        if (tp && !(player instanceof ConsolePlayer)) {
            try (final MetaDataAccess<Location> locationMetaDataAccess
                = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
                if (location == null) {
                    locationMetaDataAccess.remove();
                } else {
                    locationMetaDataAccess.set(location);
                }
            }
            try (final MetaDataAccess<Plot> plotMetaDataAccess
                = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                if (plot == null) {
                    plotMetaDataAccess.remove();
                } else {
                    plotMetaDataAccess.set(plot);
                }
            }
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override public boolean canExecute(PlotPlayer player, boolean message) {
        return true;
    }
}
