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
package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.world.PatternUtil;
import com.sk89q.worldedit.function.pattern.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@CommandDeclaration(command = "set",
    description = "Set a plot value",
    aliases = {"s"},
    usage = "/plot set <biome|alias|home|flag> <value...>",
    permission = "plots.set",
    category = CommandCategory.APPEARANCE,
    requiredType = RequiredType.NONE)
public class Set extends SubCommand {

    public static final String[] values = new String[] {"biome", "alias", "home"};
    public static final String[] aliases = new String[] {"b", "w", "wf", "a", "h"};

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
                        if (!Permissions.hasPermission(player, CaptionUtility
                            .format(player, Captions.PERMISSION_SET_COMPONENT.getTranslated(),
                                component))) {
                            MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                                .format(player, Captions.PERMISSION_SET_COMPONENT.getTranslated(),
                                    component));
                            return false;
                        }
                        if (args.length < 2) {
                            MainUtil.sendMessage(player, Captions.NEED_BLOCK);
                            return true;
                        }

                        Pattern pattern = PatternUtil.parse(player, material);
                        if (plot.getRunning() > 0) {
                            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
                            return false;
                        }
                        plot.addRunning();
                        for (Plot current : plot.getConnectedPlots()) {
                            current.setComponent(component, pattern);
                        }
                        MainUtil.sendMessage(player, Captions.GENERATING_COMPONENT);
                        GlobalBlockQueue.IMP.addEmptyTask(plot::removeRunning);
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public boolean noArgs(PlotPlayer player) {
        ArrayList<String> newValues = new ArrayList<>(Arrays.asList("biome", "alias", "home"));
        Plot plot = player.getCurrentPlot();
        if (plot != null) {
            newValues.addAll(Arrays.asList(plot.getManager().getPlotComponents(plot.getId())));
        }
        MainUtil.sendMessage(player,
            Captions.SUBCOMMAND_SET_OPTIONS_HEADER.getTranslated() + StringMan
                .join(newValues, Captions.BLOCK_LIST_SEPARATOR.formatted()));
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
        HashSet<String> components =
            new HashSet<>(Arrays.asList(plot.getManager().getPlotComponents(plot.getId())));
        if (components.contains(args[0].toLowerCase())) {
            return this.component.onCommand(player, Arrays.copyOfRange(args, 0, args.length));
        }
        return noArgs(player);
    }
}
