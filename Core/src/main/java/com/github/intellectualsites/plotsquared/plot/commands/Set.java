package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.Configuration.UnknownBlockException;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@CommandDeclaration(command = "set", description = "Set a plot value", aliases = {
    "s"}, usage = "/plot set <biome|alias|home|flag> <value...>", permission = "plots.set", category = CommandCategory.APPEARANCE, requiredType = RequiredType.NONE)
public class Set extends SubCommand {

    public static final String[] values = new String[] {"biome", "alias", "home", "flag"};
    public static final String[] aliases = new String[] {"b", "w", "wf", "f", "a", "h", "fl"};

    private final SetCommand component;

    public Set() {
        this.component = new SetCommand() {

            @Override public String getId() {
                return "set.component";
            }

            @Override public boolean set(PlotPlayer player, final Plot plot, String value) {
                PlotArea plotArea = player.getLocation().getPlotArea();
                PlotManager manager = player.getLocation().getPlotManager();
                String[] components = manager.getPlotComponents(plotArea, plot.getId());
                boolean allowUnsafe = DebugAllowUnsafe.unsafeAllowed.contains(player.getUUID());

                String[] args = value.split(" ");
                String material =
                    StringMan.join(Arrays.copyOfRange(args, 1, args.length), ",").trim();

                for (String component : components) {
                    if (component.equalsIgnoreCase(args[0])) {
                        if (!Permissions
                            .hasPermission(player, C.PERMISSION_SET_COMPONENT.f(component))) {
                            MainUtil.sendMessage(player, C.NO_PERMISSION,
                                C.PERMISSION_SET_COMPONENT.f(component));
                            return false;
                        }
                        // PlotBlock[] blocks;
                        BlockBucket bucket;
                        try {
                            if (args.length < 2) {
                                MainUtil.sendMessage(player, C.NEED_BLOCK);
                                return true;
                            }
                            String[] split = material.split(",");
                            // blocks = Configuration.BLOCKLIST.parseString(material);

                            try {
                                bucket = Configuration.BLOCK_BUCKET.parseString(material);
                            } catch (final UnknownBlockException unknownBlockException) {
                                final String unknownBlock = unknownBlockException.getUnknownValue();
                                C.NOT_VALID_BLOCK.send(player, unknownBlock);
                                StringComparison<PlotBlock>.ComparisonResult match =
                                    WorldUtil.IMP.getClosestBlock(unknownBlock);
                                if (match != null) {
                                    final String found = WorldUtil.IMP.getClosestMatchingName(match.best);
                                    if (found != null) {
                                        MainUtil.sendMessage(player, C.DID_YOU_MEAN,
                                            found.toLowerCase());
                                    }
                                }
                                return false;
                            }

                            if (!allowUnsafe) {
                                for (final PlotBlock block : bucket.getBlocks()) {
                                    if (!block.isAir() && !WorldUtil.IMP.isBlockSolid(block)) {
                                        C.NOT_ALLOWED_BLOCK.send(player, block.toString());
                                        return false;
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                            MainUtil.sendMessage(player, C.NOT_VALID_BLOCK, material);
                            return false;
                        }
                        if (plot.getRunning() > 0) {
                            MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
                            return false;
                        }
                        plot.addRunning();
                        for (Plot current : plot.getConnectedPlots()) {
                            current.setComponent(component, bucket);
                        }
                        MainUtil.sendMessage(player, C.GENERATING_COMPONENT);
                        GlobalBlockQueue.IMP.addTask(new Runnable() {
                            @Override public void run() {
                                plot.removeRunning();
                            }
                        });
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public boolean noArgs(PlotPlayer player) {
        ArrayList<String> newValues = new ArrayList<>();
        newValues.addAll(Arrays.asList("biome", "alias", "home", "flag"));
        Plot plot = player.getCurrentPlot();
        if (plot != null) {
            newValues.addAll(
                Arrays.asList(plot.getManager().getPlotComponents(plot.getArea(), plot.getId())));
        }
        MainUtil.sendMessage(player, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + StringMan
            .join(newValues, C.BLOCK_LIST_SEPARATER.formatted()));
        return false;
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        if (args.length == 0) {
            return noArgs(player);
        }
        Command cmd = MainCommand.getInstance().getCommand("set" + args[0]);
        if (cmd != null) {
            if (!Permissions.hasPermission(player, cmd.getPermission(), true)) {
                return false;
            }
            cmd.execute(player, Arrays.copyOfRange(args, 1, args.length), null, null);
            return true;
        }
        // Additional checks
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            MainUtil.sendMessage(player, C.NOT_IN_PLOT);
            return false;
        }
        // components
        HashSet<String> components = new HashSet<>(
            Arrays.asList(plot.getManager().getPlotComponents(plot.getArea(), plot.getId())));
        if (components.contains(args[0].toLowerCase())) {
            return this.component.onCommand(player, Arrays.copyOfRange(args, 0, args.length));
        }
        // flag
        Flag<?> flag = FlagManager.getFlag(args[0].toLowerCase());
        if (Flags.getFlags().contains(flag)) {
            StringBuilder a = new StringBuilder();
            if (args.length > 1) {
                for (int x = 1; x < args.length; x++) {
                    a.append(" ").append(args[x]);
                }
            }
            MainCommand.onCommand(player, ("flag set " + args[0] + a.toString()).split(" "));
            return true;
        }
        return noArgs(player);
    }
}
