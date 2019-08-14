package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.Configuration.UnknownBlockException;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CommandDeclaration(command = "set", description = "Set a plot value", aliases = {"s"},
    usage = "/plot set <biome|alias|home|flag> <value...>", permission = "plots.set",
    category = CommandCategory.APPEARANCE, requiredType = RequiredType.NONE) public class Set
    extends SubCommand {

    public static final String[] values = new String[] {"biome", "alias", "home", "flag"};
    public static final String[] aliases = new String[] {"b", "w", "wf", "f", "a", "h", "fl"};

    private final SetCommand component;

    public Set() {
        this.component = new SetCommand() {

            @Override public String getId() {
                return "set.component";
            }

            @Override public boolean set(PlotPlayer player, final Plot plot, String value) {
                PlotManager manager = player.getLocation().getPlotManager();
                String[] components = manager.getPlotComponents(plot.getId());
                boolean allowUnsafe = DebugAllowUnsafe.unsafeAllowed.contains(player.getUUID());

                String[] args = value.split(" ");
                String material =
                    StringMan.join(Arrays.copyOfRange(args, 1, args.length), ",").trim();

                for (String component : components) {
                    if (component.equalsIgnoreCase(args[0])) {
                        if (!Permissions.hasPermission(player,
                            Captions.PERMISSION_SET_COMPONENT.f(component))) {
                            MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                                Captions.PERMISSION_SET_COMPONENT.f(component));
                            return false;
                        }
                        // PlotBlock[] blocks;
                        BlockBucket bucket;
                        try {
                            if (args.length < 2) {
                                MainUtil.sendMessage(player, Captions.NEED_BLOCK);
                                return true;
                            }

                            try {
                                bucket = Configuration.BLOCK_BUCKET.parseString(material);
                            } catch (final UnknownBlockException unknownBlockException) {
                                final String unknownBlock = unknownBlockException.getUnknownValue();
                                Captions.NOT_VALID_BLOCK.send(player, unknownBlock);
                                StringComparison<PlotBlock>.ComparisonResult match =
                                    WorldUtil.IMP.getClosestBlock(unknownBlock);
                                if (match != null) {
                                    final String found =
                                        WorldUtil.IMP.getClosestMatchingName(match.best);
                                    if (found != null) {
                                        MainUtil.sendMessage(player, Captions.DID_YOU_MEAN,
                                            found.toLowerCase());
                                    }
                                }
                                return false;
                            }

                            if (!allowUnsafe) {
                                for (final PlotBlock block : bucket.getBlocks()) {
                                    if (!block.isAir() && !WorldUtil.IMP.isBlockSolid(block)) {
                                        Captions.NOT_ALLOWED_BLOCK.send(player, block.toString());
                                        return false;
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                            MainUtil.sendMessage(player, Captions.NOT_VALID_BLOCK, material);
                            return false;
                        }
                        if (plot.getRunning() > 0) {
                            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
                            return false;
                        }
                        plot.addRunning();
                        for (Plot current : plot.getConnectedPlots()) {
                            current.setComponent(component, bucket);
                        }
                        MainUtil.sendMessage(player, Captions.GENERATING_COMPONENT);
                        GlobalBlockQueue.IMP.addTask(plot::removeRunning);
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public boolean noArgs(PlotPlayer player) {
        ArrayList<String> newValues =
            new ArrayList<>(Arrays.asList("biome", "alias", "home", "flag"));
        Plot plot = player.getCurrentPlot();
        if (plot != null) {
            newValues.addAll(
                Arrays.asList(plot.getManager().getPlotComponents(plot.getId())));
        }
        MainUtil
            .sendMessage(player, Captions.SUBCOMMAND_SET_OPTIONS_HEADER.getTranslated() + StringMan
            .join(newValues, Captions.BLOCK_LIST_SEPARATER.formatted()));
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
            MainUtil.sendMessage(player, Captions.NOT_IN_PLOT);
            return false;
        }
        // components
        HashSet<String> components = new HashSet<>(
            Arrays.asList(plot.getManager().getPlotComponents(plot.getId())));
        if (components.contains(args[0].toLowerCase())) {
            return this.component.onCommand(player, Arrays.copyOfRange(args, 0, args.length));
        }
        // flag
        Flag<?> flag = FlagManager.getFlag(args[0].toLowerCase());
        if (Flags.getFlags().contains(flag)) {
            String a = "";
            if (args.length > 1) {
                a = IntStream.range(1, args.length).mapToObj(x -> " " + args[x])
                    .collect(Collectors.joining());
            }
            MainCommand.onCommand(player, ("flag set " + args[0] + a).split(" "));
            return true;
        }
        return noArgs(player);
    }
}
