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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlotFlagAddEvent;
import com.intellectualcrafters.plot.events.PlotFlagRemoveEvent;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.StringComparison;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Citymonstret
 */
public class Set extends SubCommand {

    public final static String[] values = new String[]{"biome", "wall", "wall_filling", "floor", "alias", "home", "flag"};
    public final static String[] aliases = new String[]{"b", "w", "wf", "f", "a", "h", "fl"};

    public Set() {
        super(Command.SET, "Set a plot value", "set {arg} {value...}", CommandCategory.ACTIONS, true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (!plot.hasOwner()) {
            sendMessage(plr, C.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.hasRights(plr) && !PlotMain.hasPermission(plr, "plots.admin")) {
            PlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length < 1) {
            PlayerFunctions.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + getArgumentList(values));
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
        if (!PlotMain.hasPermission(plr, "plots.set." + args[0].toLowerCase())) {
            PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.set." + args[0].toLowerCase());
            return false;
        }

        if (args[0].equalsIgnoreCase("flag")) {
            if (args.length < 2) {
                String message = StringUtils.join(FlagManager.getFlags(plr), "&c, &6");
                if (PlotMain.worldGuardListener != null) {
                    if (message.equals("")) {
                        message = StringUtils.join(PlotMain.worldGuardListener.str_flags, "&c, &6");
                    } else {
                        message += "," + StringUtils.join(PlotMain.worldGuardListener.str_flags, "&c, &6");
                    }
                }
                PlayerFunctions.sendMessage(plr, C.NEED_KEY.s().replaceAll("%values%", message));
                return false;
            }

            AbstractFlag af;

            try {
                af = FlagManager.getFlag(args[1].toLowerCase());
            } catch (final Exception e) {
                af = new AbstractFlag(args[1].toLowerCase());
            }

            if (!FlagManager.getFlags().contains(af) && ((PlotMain.worldGuardListener == null) || !PlotMain.worldGuardListener.str_flags.contains(args[1].toLowerCase()))) {
                PlayerFunctions.sendMessage(plr, C.NOT_VALID_FLAG);
                return false;
            }
            if (!PlotMain.hasPermission(plr, "plots.set.flag." + args[1].toLowerCase())) {
                PlayerFunctions.sendMessage(plr, C.NO_PERMISSION);
                return false;
            }
            if (args.length == 2) {
                if (FlagManager.getPlotFlagAbs(plot, args[1].toLowerCase()) == null) {
                    if (PlotMain.worldGuardListener != null) {
                        if (PlotMain.worldGuardListener.str_flags.contains(args[1].toLowerCase())) {
                            PlotMain.worldGuardListener.removeFlag(plr, plr.getWorld(), plot, args[1]);
                            return false;
                        }
                    }
                    PlayerFunctions.sendMessage(plr, C.FLAG_NOT_IN_PLOT);
                    return false;
                }
                
                boolean result = FlagManager.removePlotFlag(plot, args[1].toLowerCase());
                
                if (!result) {
                    PlayerFunctions.sendMessage(plr, C.FLAG_NOT_REMOVED);
                    return false;
                }
                PlayerFunctions.sendMessage(plr, C.FLAG_REMOVED);
                PlotListener.plotEntry(plr, plot);
                return true;
            }
            try {
                String value = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                value = af.parseValue(value);
                if (value == null) {
                    PlayerFunctions.sendMessage(plr, af.getValueDesc());
                    return false;
                }

                if ((FlagManager.getFlag(args[1].toLowerCase()) == null) && (PlotMain.worldGuardListener != null)) {
                    PlotMain.worldGuardListener.addFlag(plr, plr.getWorld(), plot, args[1], value);
                    return false;
                }

                final Flag flag = new Flag(FlagManager.getFlag(args[1].toLowerCase(), true), value);
                boolean result = FlagManager.addPlotFlag(plot, flag);
                if (!result) {
                    PlayerFunctions.sendMessage(plr, C.FLAG_NOT_ADDED);
                    return false;
                }
                PlayerFunctions.sendMessage(plr, C.FLAG_ADDED);
                PlotListener.plotEntry(plr, plot);
                return true;
            } catch (final Exception e) {
                PlayerFunctions.sendMessage(plr, "&c" + e.getMessage());
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("home")) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("none")) {
                    plot.settings.setPosition(null);
                    DBFunc.setPosition(plr.getWorld().getName(), plot, "");
                    return true;
                }
                return PlayerFunctions.sendMessage(plr, C.HOME_ARGUMENT);
            }
            //set to current location
            World world = plr.getWorld();
            Location base = PlotHelper.getPlotBottomLoc(world, plot.id);
            int y = PlotHelper.getHeighestBlock(world, base.getBlockX(), base.getBlockZ());
            base.setY(y);
            Location relative = plr.getLocation().subtract(base);
            BlockLoc blockloc = new BlockLoc(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ());
            plot.settings.setPosition(blockloc);
            DBFunc.setPosition(plr.getWorld().getName(), plot, relative.getBlockX() + "," + relative.getBlockY() + "," + relative.getBlockZ());
            return PlayerFunctions.sendMessage(plr, C.POSITION_SET);
        }

        if (args[0].equalsIgnoreCase("alias")) {
            if (args.length < 2) {
                PlayerFunctions.sendMessage(plr, C.MISSING_ALIAS);
                return false;
            }
            final String alias = args[1];
            if (alias.length() >= 50) {
                PlayerFunctions.sendMessage(plr, C.ALIAS_TOO_LONG);
                return false;
            }
            for (final Plot p : PlotMain.getPlots(plr.getWorld()).values()) {
                if (p.settings.getAlias().equalsIgnoreCase(alias)) {
                    PlayerFunctions.sendMessage(plr, C.ALIAS_IS_TAKEN);
                    return false;
                }
                if (Bukkit.getOfflinePlayer(alias).hasPlayedBefore()) {
                    PlayerFunctions.sendMessage(plr, C.ALIAS_IS_TAKEN);
                    return false;
                }
            }
            DBFunc.setAlias(plr.getWorld().getName(), plot, alias);
            PlayerFunctions.sendMessage(plr, C.ALIAS_SET_TO.s().replaceAll("%alias%", alias));
            return true;
        }
        if (args[0].equalsIgnoreCase("biome")) {
            if (args.length < 2) {
                PlayerFunctions.sendMessage(plr, C.NEED_BIOME);
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
                PlayerFunctions.sendMessage(plr, getBiomeList(Arrays.asList(Biome.values())));
                return true;
            }
            PlotHelper.setBiome(plr.getWorld(), plot, biome);
            PlayerFunctions.sendMessage(plr, C.BIOME_SET_TO.s() + biome.toString().toLowerCase());
            return true;
        }
        if (args[0].equalsIgnoreCase("wall")) {
            final PlotWorld plotworld = PlotMain.getWorldSettings(plr.getWorld());
            if (plotworld == null) {
                PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
                return true;
            }
            if (args.length < 2) {
                PlayerFunctions.sendMessage(plr, C.NEED_BLOCK);
                return true;
            }
            if (args[1].length() < 2) {
                sendMessage(plr, C.NAME_LITTLE, "Material", args[1].length() + "", "2");
                return true;
            }
            Material material;
            try {
                material = getMaterial(args[1], PlotWorld.BLOCKS);
            } catch (final NullPointerException e) {
                material = null;
            }
            /*
             * for (Material m : PlotWorld.BLOCKS) {
             * if (m.toString().equalsIgnoreCase(args[1])) {
             * material = m;
             * break;
             * }
             * }
             */
            if (material == null) {
                PlayerFunctions.sendMessage(plr, getBlockList(PlotWorld.BLOCKS));
                return true;
            }
            byte data = 0;

            if (args.length > 2) {
                try {
                    data = (byte) Integer.parseInt(args[2]);
                } catch (final Exception e) {
                    PlayerFunctions.sendMessage(plr, C.NOT_VALID_DATA);
                    return true;
                }
            }
            PlayerFunctions.sendMessage(plr, C.GENERATING_WALL);
            PlotHelper.adjustWall(plr, plot, new PlotBlock((short) material.getId(), data));
            return true;
        }
        if (args[0].equalsIgnoreCase("floor")) {
            if (args.length < 2) {
                PlayerFunctions.sendMessage(plr, C.NEED_BLOCK);
                return true;
            }
            final PlotWorld plotworld = PlotMain.getWorldSettings(plr.getWorld());
            if (plotworld == null) {
                PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
                return true;
            }
            //
            @SuppressWarnings("unchecked") final ArrayList<Material> materials = (ArrayList<Material>) ((ArrayList<Material>) PlotWorld.BLOCKS).clone();
            materials.add(Material.AIR);
            //
            final String[] strings = args[1].split(",");
            //
            int index = 0;
            //
            Material m;
            //
            final PlotBlock[] blocks = new PlotBlock[strings.length];

            for (String s : strings) {
                s = s.replaceAll(",", "");
                final String[] ss = s.split(";");
                ss[0] = ss[0].replaceAll(";", "");
                if (ss[0].length() < 2) {
                    sendMessage(plr, C.NAME_LITTLE, "Material", ss[0].length() + "", "2");
                    return true;
                }
                m = getMaterial(ss[0], materials);
                /*
                 * for (Material ma : materials) {
                 * if (ma.toString().equalsIgnoreCase(ss[0])) {
                 * m = ma;
                 * }
                 * }
                 */
                if (m == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_VALID_BLOCK);
                    return true;
                }
                if (ss.length == 1) {

                    blocks[index] = new PlotBlock((short) m.getId(), (byte) 0);
                } else {
                    byte b;
                    try {
                        b = (byte) Integer.parseInt(ss[1]);
                    } catch (final Exception e) {
                        PlayerFunctions.sendMessage(plr, C.NOT_VALID_DATA);
                        return true;
                    }
                    blocks[index] = new PlotBlock((short) m.getId(), b);
                }
                index++;
            }
            PlotHelper.setFloor(plr, plot, blocks);
            return true;
        }
        if (args[0].equalsIgnoreCase("wall_filling")) {
            if (args.length < 2) {
                PlayerFunctions.sendMessage(plr, C.NEED_BLOCK);
                return true;
            }
            final PlotWorld plotworld = PlotMain.getWorldSettings(plr.getWorld());
            if (plotworld == null) {
                PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
                return true;
            }
            if (args[1].length() < 2) {
                sendMessage(plr, C.NAME_LITTLE, "Material", args[1].length() + "", "2");
                return true;
            }
            final Material material = getMaterial(args[1], PlotWorld.BLOCKS);
            /*
             * for (Material m : PlotWorld.BLOCKS) {
             * if (m.toString().equalsIgnoreCase(args[1])) {
             * material = m;
             * break;
             * }
             * }
             */

            if (material == null) {
                PlayerFunctions.sendMessage(plr, getBlockList(PlotWorld.BLOCKS));
                return true;
            }
            byte data = 0;

            if (args.length > 2) {
                try {
                    data = (byte) Integer.parseInt(args[2]);
                } catch (final Exception e) {
                    PlayerFunctions.sendMessage(plr, C.NOT_VALID_DATA);
                    return true;
                }
            }
            PlotHelper.adjustWallFilling(plr, plot, new PlotBlock((short) material.getId(), data));
            return true;
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
        PlayerFunctions.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + getArgumentList(values));
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

    private String getBlockList(final List<Material> blocks) {
        final StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.translateAlternateColorCodes('&', C.NOT_VALID_BLOCK_LIST_HEADER.s()));
        for (final Material b : blocks) {
            builder.append(getMaterial(b));
        }
        return builder.toString().substring(1, builder.toString().length() - 1);
    }

}
