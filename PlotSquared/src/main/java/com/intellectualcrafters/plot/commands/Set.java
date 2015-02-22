////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 */
public class Set extends SubCommand {
    public final static String[] values = new String[] { "biome", "wall", "wall_filling", "floor", "alias", "home", "flag" };
    public final static String[] aliases = new String[] { "b", "w", "wf", "f", "a", "h", "fl" };
    
    public Set() {
        super(Command.SET, "Set a plot value", "set {arg} {value...}", CommandCategory.ACTIONS, true);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            sendMessage(plr, C.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.hasRights(plr) && !Permissions.hasPermission(plr, "plots.admin.command.set")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length < 1) {
            MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + getArgumentList(values));
            return false;
        }
        for (int i = 0; i < aliases.length; i++) {
            if (aliases[i].equalsIgnoreCase(args[0])) {
                args[0] = values[i];
                break;
            }
        }
        /* TODO: Implement option */
        // final boolean advanced_permissions = true;
        if (!Permissions.hasPermission(plr, "plots.set." + args[0].toLowerCase())) {
            MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.set." + args[0].toLowerCase());
            return false;
        }
        if (args[0].equalsIgnoreCase("flag")) {
            if (args.length < 2) {
                String message = StringUtils.join(FlagManager.getFlags(plr), "&c, &6");
                if (PlotSquared.worldGuardListener != null) {
                    if (message.equals("")) {
                        message = StringUtils.join(PlotSquared.worldGuardListener.str_flags, "&c, &6");
                    } else {
                        message += "," + StringUtils.join(PlotSquared.worldGuardListener.str_flags, "&c, &6");
                    }
                }
                MainUtil.sendMessage(plr, C.NEED_KEY.s().replaceAll("%values%", message));
                return false;
            }
            AbstractFlag af;
            try {
                af = FlagManager.getFlag(args[1].toLowerCase());
            } catch (final Exception e) {
                af = new AbstractFlag(args[1].toLowerCase());
            }
            if (!FlagManager.getFlags().contains(af) && ((PlotSquared.worldGuardListener == null) || !PlotSquared.worldGuardListener.str_flags.contains(args[1].toLowerCase()))) {
                MainUtil.sendMessage(plr, C.NOT_VALID_FLAG);
                return false;
            }
            if (!Permissions.hasPermission(plr, "plots.set.flag." + args[1].toLowerCase())) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION);
                return false;
            }
            if (args.length == 2) {
                if (FlagManager.getPlotFlagAbs(plot, args[1].toLowerCase()) == null) {
                    if (PlotSquared.worldGuardListener != null) {
                        if (PlotSquared.worldGuardListener.str_flags.contains(args[1].toLowerCase())) {
                            PlotSquared.worldGuardListener.removeFlag(plr, plr.getWorld(), plot, args[1]);
                            return false;
                        }
                    }
                    MainUtil.sendMessage(plr, C.FLAG_NOT_IN_PLOT);
                    return false;
                }
                final boolean result = FlagManager.removePlotFlag(plot, args[1].toLowerCase());
                if (!result) {
                    MainUtil.sendMessage(plr, C.FLAG_NOT_REMOVED);
                    return false;
                }
                MainUtil.sendMessage(plr, C.FLAG_REMOVED);
                PlotListener.plotEntry(plr, plot);
                return true;
            }
            try {
                final String value = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                final Object parsed_value = af.parseValueRaw(value);
                if (parsed_value == null) {
                    MainUtil.sendMessage(plr, af.getValueDesc());
                    return false;
                }
                if ((FlagManager.getFlag(args[1].toLowerCase()) == null) && (PlotSquared.worldGuardListener != null)) {
                    PlotSquared.worldGuardListener.addFlag(plr, plr.getWorld(), plot, args[1], af.toString(parsed_value));
                    return false;
                }
                final Flag flag = new Flag(FlagManager.getFlag(args[1].toLowerCase(), true), parsed_value);
                final boolean result = FlagManager.addPlotFlag(plot, flag);
                if (!result) {
                    MainUtil.sendMessage(plr, C.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(plr, C.FLAG_ADDED);
                PlotListener.plotEntry(plr, plot);
                return true;
            } catch (final Exception e) {
                MainUtil.sendMessage(plr, "&c" + e.getMessage());
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("home")) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("none")) {
                    plot.settings.setPosition(null);
                    DBFunc.setPosition(loc.getWorld(), plot, "");
                    return true;
                }
                return MainUtil.sendMessage(plr, C.HOME_ARGUMENT);
            }
            //set to current location
            final World world = plr.getWorld();
            final Location base = MainUtil.getPlotBottomLoc(world, plot.id);
            base.setY(0);
            final Location relative = plr.getLocation().subtract(base);
            final BlockLoc blockloc = new BlockLoc(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ());
            plot.settings.setPosition(blockloc);
            DBFunc.setPosition(loc.getWorld(), plot, relative.getBlockX() + "," + relative.getBlockY() + "," + relative.getBlockZ());
            return MainUtil.sendMessage(plr, C.POSITION_SET);
        }
        if (args[0].equalsIgnoreCase("alias")) {
            if (args.length < 2) {
                MainUtil.sendMessage(plr, C.MISSING_ALIAS);
                return false;
            }
            final String alias = args[1];
            if (alias.length() >= 50) {
                MainUtil.sendMessage(plr, C.ALIAS_TOO_LONG);
                return false;
            }
            for (final Plot p : PlotSquared.getPlots(plr.getWorld()).values()) {
                if (p.settings.getAlias().equalsIgnoreCase(alias)) {
                    MainUtil.sendMessage(plr, C.ALIAS_IS_TAKEN);
                    return false;
                }
                if (UUIDHandler.nameExists(new StringWrapper(alias))) {
                    MainUtil.sendMessage(plr, C.ALIAS_IS_TAKEN);
                    return false;
                }
            }
            DBFunc.setAlias(loc.getWorld(), plot, alias);
            MainUtil.sendMessage(plr, C.ALIAS_SET_TO.s().replaceAll("%alias%", alias));
            return true;
        }
        if (args[0].equalsIgnoreCase("biome")) {
            if (args.length < 2) {
                MainUtil.sendMessage(plr, C.NEED_BIOME);
                return true;
            }
            if (args[1].length() < 2) {
                sendMessage(plr, C.NAME_LITTLE, "Biome", args[1].length() + "", "2");
                return true;
            }
            final Biome biome = Biome.valueOf(new StringComparison(args[1], Biome.values()).getBestMatch());
            /*
             * for (Biome b : Biome.values()) {
             * if (b.toString().equalsIgnoreCase(args[1])) {
             * biome = b;
             * break;
             * }
             * }
             */
            if (biome == null) {
                MainUtil.sendMessage(plr, getBiomeList(Arrays.asList(Biome.values())));
                return true;
            }
            MainUtil.setBiome(plr.getWorld(), plot, biome);
            MainUtil.sendMessage(plr, C.BIOME_SET_TO.s() + biome.toString().toLowerCase());
            return true;
        }
        // Get components
        final World world = plr.getWorld();
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        final PlotManager manager = PlotSquared.getPlotManager(world);
        final String[] components = manager.getPlotComponents(world, plotworld, plot.id);
        for (final String component : components) {
            if (component.equalsIgnoreCase(args[0])) {
                if (args.length < 2) {
                    MainUtil.sendMessage(plr, C.NEED_BLOCK);
                    return true;
                }
                PlotBlock[] blocks;
                try {
                    blocks = (PlotBlock[]) Configuration.BLOCKLIST.parseObject(args[2]);
                } catch (final Exception e) {
                    try {
                        blocks = new PlotBlock[] { new PlotBlock((short) getMaterial(args[1], PlotWorld.BLOCKS).getId(), (byte) 0) };
                    } catch (final Exception e2) {
                        MainUtil.sendMessage(plr, C.NOT_VALID_BLOCK);
                        return false;
                    }
                }
                manager.setComponent(world, plotworld, plot.id, component, blocks);
                MainUtil.sendMessage(plr, C.GENERATING_COMPONENT);
                return true;
            }
        }
        {
            AbstractFlag af;
            try {
                af = new AbstractFlag(args[0].toLowerCase());
            } catch (final Exception e) {
                af = new AbstractFlag("");
            }
            if (FlagManager.getFlags().contains(af)) {
                final StringBuilder a = new StringBuilder();
                if (args.length > 1) {
                    for (int x = 1; x < args.length; x++) {
                        a.append(" ").append(args[x]);
                    }
                }
                plr.performCommand("plot set flag " + args[0] + a.toString());
                return true;
            }
        }
        MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + getArgumentList(values));
        return false;
    }
    
    private String getMaterial(final Material m) {
        return ChatColor.translateAlternateColorCodes('&', C.BLOCK_LIST_ITEM.s().replaceAll("%mat%", m.toString().toLowerCase()));
    }
    
    private String getBiome(final Biome b) {
        return ChatColor.translateAlternateColorCodes('&', C.BLOCK_LIST_ITEM.s().replaceAll("%mat%", b.toString().toLowerCase()));
    }
    
    private String getString(final String s) {
        return ChatColor.translateAlternateColorCodes('&', C.BLOCK_LIST_ITEM.s().replaceAll("%mat%", s));
    }
    
    private String getArgumentList(final String[] strings) {
        final StringBuilder builder = new StringBuilder();
        for (final String s : strings) {
            builder.append(getString(s));
        }
        return builder.toString().substring(1, builder.toString().length() - 1);
    }
    
    private String getBiomeList(final List<Biome> biomes) {
        final StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.translateAlternateColorCodes('&', C.NOT_VALID_BLOCK_LIST_HEADER.s()));
        for (final Biome b : biomes) {
            builder.append(getBiome(b));
        }
        return builder.toString().substring(1, builder.toString().length() - 1);
    }
    
    private Material getMaterial(final String input, final List<Material> blocks) {
        return Material.valueOf(new StringComparison(input, blocks.toArray()).getBestMatch());
    }
}
