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

package com.intellectualcrafters.plot.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.Title;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.UUIDHandler;

/**
 * @author Citymonstret
 * @author Empire92
 */
@SuppressWarnings({ "unused", "deprecation" })
public class PlotListener {

    public static void textures(final Player p) {
        if ((Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1) && isPlotWorld(p.getWorld())) {
            p.setResourcePack(Settings.PLOT_SPECIFIC_RESOURCE_PACK);
        }
    }

    public static boolean isInPlot(final Player player) {
        return PlayerFunctions.isInPlot(player);
    }

    public static Plot getPlot(final Player player) {
        return PlayerFunctions.getCurrentPlot(player);
    }

    public static boolean isPlotWorld(final World world) {
        return PlotMain.isPlotWorld(world);
    }

    public static PlotWorld getPlotWorld(final World world) {
        return PlotMain.getWorldSettings(world);
    }

    public static String getName(final UUID uuid) {
        return UUIDHandler.getName(uuid);
    }

    public static UUID getUUID(final String name) {
        return UUIDHandler.getUUID(name);
    }

    // unused
    public static void blockChange(final Block block, final Cancellable event) {
        final Location loc = block.getLocation();
        final String world = loc.getWorld().getName();
        final PlotManager manager = PlotMain.getPlotManager(world);
        if (manager != null) {
            final PlotWorld plotworld = PlotMain.getWorldSettings(world);
            final PlotId id = manager.getPlotId(plotworld, loc);
            if (id == null) {
                event.setCancelled(true);
            }
        }
    }

    public static boolean enteredPlot(final Location l1, final Location l2) {
        final PlotId p1 = PlayerFunctions.getPlot(new Location(l1.getWorld(), l1.getBlockX(), 64, l1.getBlockZ()));
        final PlotId p2 = PlayerFunctions.getPlot(new Location(l2.getWorld(), l2.getBlockX(), 64, l2.getBlockZ()));
        return (p2 != null) && ((p1 == null) || !p1.equals(p2));

    }

    public static boolean leftPlot(final Location l1, final Location l2) {
        final PlotId p1 = PlayerFunctions.getPlot(new Location(l1.getWorld(), l1.getBlockX(), 64, l1.getBlockZ()));
        final PlotId p2 = PlayerFunctions.getPlot(new Location(l2.getWorld(), l2.getBlockX(), 64, l2.getBlockZ()));
        return (p1 != null) && ((p2 == null) || !p1.equals(p2));
    }

    public static boolean isPlotWorld(final Location l) {
        return PlotMain.isPlotWorld(l.getWorld());
    }

    public static boolean isInPlot(final Location loc) {
        return getCurrentPlot(loc) != null;
    }

    public static Plot getCurrentPlot(final Location loc) {
        final PlotId id = PlayerFunctions.getPlot(loc);
        if (id == null) {
            return null;
        }
        final World world = loc.getWorld();
        if (PlotMain.getPlots(world).containsKey(id)) {
            return PlotMain.getPlots(world).get(id);
        }
        return new Plot(id, null, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), loc.getWorld().getName());
    }

    private static WeatherType getWeatherType(String str) {
        str = str.toLowerCase();
        if (str.equals("rain")) {
            return WeatherType.DOWNFALL;
        }
        else {
            return WeatherType.CLEAR;
        }
    }

    public static boolean booleanFlag(final Plot plot, final String flag) {
        return (plot.settings.getFlag(flag) != null) && getBooleanFlag(plot.settings.getFlag(flag).getValue()).equals("true");
    }

    private static String getBooleanFlag(final String value) {
        switch (value) {
            case "on":
            case "1":
            case "true":
            case "enabled":
                return "true";
            case "off":
            case "0":
            case "false":
            case "disabled":
                return "false";
            default:
                return null;
        }
    }

    private static GameMode getGameMode(final String str) {
        switch (str) {
            case "creative":
                return GameMode.CREATIVE;
            case "survival":
                return GameMode.SURVIVAL;
            case "adventure":
                return GameMode.ADVENTURE;
            default:
                return Bukkit.getDefaultGameMode();
        }
    }

    public static void plotEntry(final Player player, final Plot plot) {
        if (plot.hasOwner()) {
            if (plot.settings.getFlag("gamemode") != null) {
                player.setGameMode(getGameMode(plot.settings.getFlag("gamemode").getValue()));
            }
            if (plot.settings.getFlag("fly") != null) {
                player.setAllowFlight(getFlagValue(plot.settings.getFlag("fly").getValue()));
            }
            if (plot.settings.getFlag("time") != null) {
                try {
                    final Long time = Long.parseLong(plot.settings.getFlag("time").getValue());
                    player.setPlayerTime(time, true);
                }
                catch (final Exception e) {
                    plot.settings.setFlags(FlagManager.removeFlag(plot.settings.getFlags(), "time"));
                }
            }
            if (plot.settings.getFlag("weather") != null) {
                player.setPlayerWeather(getWeatherType(plot.settings.getFlag("weather").getValue()));
            }
            if (booleanFlag(plot, "titles") && Settings.TITLES && (C.TITLE_ENTERED_PLOT.s().length() > 2)) {
                final String sTitleMain = C.TITLE_ENTERED_PLOT.s().replaceFirst("%s", plot.getDisplayName());
                final String sTitleSub = C.TITLE_ENTERED_PLOT_SUB.s().replaceFirst("%s", getName(plot.owner));
                final ChatColor sTitleMainColor = ChatColor.valueOf(C.TITLE_ENTERED_PLOT_COLOR.s());
                final ChatColor sTitleSubColor = ChatColor.valueOf(C.TITLE_ENTERED_PLOT_SUB_COLOR.s());
                final Title title = new Title(sTitleMain, sTitleSub, 10, 20, 10);
                title.setTitleColor(sTitleMainColor);
                title.setSubtitleColor(sTitleSubColor);
                title.setTimingsToTicks();
                title.send(player);
            }
            {
                final PlayerEnterPlotEvent callEvent = new PlayerEnterPlotEvent(player, plot);
                Bukkit.getPluginManager().callEvent(callEvent);
            }
        }
    }

    public static void plotExit(final Player player, final Plot plot) {
        {
            final PlayerLeavePlotEvent callEvent = new PlayerLeavePlotEvent(player, plot);
            Bukkit.getPluginManager().callEvent(callEvent);
        }
        if (plot.settings.getFlag("fly") != null) {
            player.setAllowFlight(Bukkit.getAllowFlight());
        }
        if (plot.settings.getFlag("gamemode") != null) {
            player.setGameMode(Bukkit.getDefaultGameMode());
        }
        if (plot.settings.getFlag("time") != null) {
            player.resetPlayerTime();
        }
        if (plot.settings.getFlag("weather") != null) {
            player.resetPlayerWeather();
        }
    }

    public static boolean getFlagValue(final String value) {
        return Arrays.asList("true", "on", "enabled", "yes").contains(value.toLowerCase());
    }
}
