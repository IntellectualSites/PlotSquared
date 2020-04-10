package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.UntrustedVisitFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal2;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.object.TeleportCause;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.github.intellectualsites.plotsquared.util.MathMan;
import com.github.intellectualsites.plotsquared.util.Permissions;
import com.github.intellectualsites.plotsquared.util.uuid.UUIDHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "visit",
    permission = "plots.visit",
    description = "Visit someones plot",
    usage = "/plot visit [<player>|<alias>|<world>|<id>] [#]",
    aliases = {"v", "tp", "teleport", "goto", "home", "h", "warp"},
    requiredType = RequiredType.PLAYER,
    category = CommandCategory.TELEPORT)
public class Visit extends Command {

    public Visit() {
        super(MainCommand.getInstance(), true);
    }

    @Override public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        return tabOf(player, args, space, getUsage());
    }

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 1 && args[0].contains(":")) {
            args = args[0].split(":");
        }
        int page = Integer.MIN_VALUE;
        Collection<Plot> unsorted = null;
        PlotArea sortByArea = player.getApplicablePlotArea();
        boolean shouldSortByArea = Settings.Teleport.PER_WORLD_VISIT;
        switch (args.length) {
            case 3:
                if (!MathMan.isInteger(args[1])) {
                    Captions.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                    Captions.COMMAND_SYNTAX.send(player, getUsage());
                    return CompletableFuture.completedFuture(false);
                }
                page = Integer.parseInt(args[2]);
            case 2:
                if (page != Integer.MIN_VALUE || !MathMan.isInteger(args[1])) {
                    sortByArea = PlotSquared.get().getPlotAreaByString(args[1]);
                    if (sortByArea == null) {
                        Captions.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                        Captions.COMMAND_SYNTAX.send(player, getUsage());
                        return CompletableFuture.completedFuture(false);
                    }
                    UUID user = UUIDHandler.getUUIDFromString(args[0]);
                    if (user == null) {
                        Captions.COMMAND_SYNTAX.send(player, getUsage());
                        return CompletableFuture.completedFuture(false);
                    }
                    unsorted = PlotSquared.get().getBasePlots(user);
                    shouldSortByArea = true;
                    break;
                }
                page = Integer.parseInt(args[1]);
            case 1:
                UUID user = args[0].length() >= 2 ? UUIDHandler.getUUIDFromString(args[0]) : null;
                if (user != null && !PlotSquared.get().hasPlot(user)) {
                    user = null;
                }
                if (page == Integer.MIN_VALUE && user == null && MathMan.isInteger(args[0])) {
                    page = Integer.parseInt(args[0]);
                    unsorted = PlotSquared.get().getBasePlots(player);
                    break;
                }
                if (user != null) {
                    unsorted = PlotSquared.get().getBasePlots(user);
                } else {
                    Plot plot = MainUtil.getPlotFromString(player, args[0], true);
                    if (plot != null) {
                        unsorted = Collections.singletonList(plot.getBasePlot(false));
                    }
                }
                break;
            case 0:
                page = 1;
                unsorted = PlotSquared.get().getPlots(player);
                break;
            default:

        }
        if (page == Integer.MIN_VALUE) {
            page = 1;
        }
        if (unsorted == null || unsorted.isEmpty()) {
            Captions.FOUND_NO_PLOTS.send(player);
            return CompletableFuture.completedFuture(false);
        }
        unsorted = new ArrayList<>(unsorted);
        if (unsorted.size() > 1) {
            unsorted.removeIf(plot -> !plot.isBasePlot());
        }
        if (page < 1 || page > unsorted.size()) {
            Captions.NOT_VALID_NUMBER.send(player, "(1, " + unsorted.size() + ")");
            return CompletableFuture.completedFuture(false);
        }
        List<Plot> plots;
        if (shouldSortByArea) {
            plots = PlotSquared.get()
                .sortPlots(unsorted, PlotSquared.SortType.CREATION_DATE, sortByArea);
        } else {
            plots = PlotSquared.get().sortPlotsByTemp(unsorted);
        }
        final Plot plot = plots.get(page - 1);
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_UNOWNED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_UNOWNED);
                return CompletableFuture.completedFuture(false);
            }
        } else if (plot.isOwner(player.getUUID())) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OWNED) && !Permissions
                .hasPermission(player, Captions.PERMISSION_HOME)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OWNED);
                return CompletableFuture.completedFuture(false);
            }
        } else if (plot.isAdded(player.getUUID())) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_SHARED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_SHARED);
                return CompletableFuture.completedFuture(false);
            }
        } else {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OTHER)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OTHER);
                return CompletableFuture.completedFuture(false);
            }
            if (!plot.getFlag(UntrustedVisitFlag.class) &&
                       !Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_VISIT_UNTRUSTED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_ADMIN_VISIT_UNTRUSTED);
                return CompletableFuture.completedFuture(false);
            }
        }
        confirm.run(this, () ->
            plot.teleportPlayer(player, TeleportCause.COMMAND, result -> {
            if (result) {
                whenDone.run(Visit.this, CommandResult.SUCCESS);
            } else {
                whenDone.run(Visit.this, CommandResult.FAILURE);
            }
        }), () -> whenDone.run(Visit.this, CommandResult.FAILURE));

        return CompletableFuture.completedFuture(true);
    }

}
