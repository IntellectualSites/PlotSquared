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
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.titles.AbstractTitle;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 * @author Empire92
 */
public class PlotListener {
    public static void textures(final Player p) {
        if ((Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1) && isPlotWorld(p.getWorld().getName())) {
            p.setResourcePack(Settings.PLOT_SPECIFIC_RESOURCE_PACK);
        }
    }
    
    public static boolean booleanFlag(final Plot plot, final String key, final boolean defaultValue) {
        final Flag flag = FlagManager.getPlotFlag(plot, key);
        if (flag == null) {
            return defaultValue;
        }
        final Object value = flag.getValue();
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        return defaultValue;
    }
    
    public static boolean isInPlot(final String world, final int x, final int y, final int z) {
        return (MainUtil.getPlot(new Location(world, x, y, z)) != null);
    }
    
    public static boolean isPlotWorld(final String world) {
        return PlotSquared.isPlotWorld(world);
    }
    
    public static boolean isPlotArea(final Location location) {
        final PlotWorld plotworld = PlotSquared.getPlotWorld(location.getWorld());
        if (plotworld.TYPE == 2) {
            return ClusterManager.getCluster(location) != null;
        }
        return true;
    }
    
    private static String getName(final UUID id) {
        if (id == null) {
            return "none";
        }
        final String name = UUIDHandler.getName(id);
        if (name == null) {
            return "unknown";
        }
        return name;
    }
    
    public static UUID getUUID(final String name) {
        return UUIDHandler.getUUID(name);
    }
    
    public static boolean enteredPlot(final Location l1, final Location l2) {
        final PlotId p1 = MainUtil.getPlotId(l1);
        final PlotId p2 = MainUtil.getPlotId(l2);
        return (p2 != null) && ((p1 == null) || !p1.equals(p2));
    }
    
    public static boolean leftPlot(final Location l1, final Location l2) {
        final PlotId p1 = MainUtil.getPlotId(l1);
        final PlotId p2 = MainUtil.getPlotId(l2);
        return (p1 != null) && ((p2 == null) || !p1.equals(p2));
    }
    
    public static boolean isPlotWorld(final Location l) {
        return PlotSquared.isPlotWorld(l.getWorld());
    }
    
    public static boolean isInPlot(final Location loc) {
        return getCurrentPlot(loc) != null;
    }
    
    public static Plot getCurrentPlot(final Location loc) {
        final PlotId id = MainUtil.getPlotId(loc);
        if (id == null) {
            return null;
        }
        return MainUtil.getPlot(loc.getWorld(), id);
    }
    
    private static WeatherType getWeatherType(String str) {
        str = str.toLowerCase();
        if (str.equals("rain")) {
            return WeatherType.DOWNFALL;
        } else {
            return WeatherType.CLEAR;
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
            final Flag gamemodeFlag = FlagManager.getPlotFlag(plot, "gamemode");
            if (gamemodeFlag != null) {
                player.setGameMode(getGameMode(gamemodeFlag.getValueString()));
            }
            final Flag flyFlag = FlagManager.getPlotFlag(plot, "fly");
            if (flyFlag != null) {
                player.setAllowFlight((boolean) flyFlag.getValue());
            }
            final Flag timeFlag = FlagManager.getPlotFlag(plot, "time");
            if (timeFlag != null) {
                try {
                    final long time = (long) timeFlag.getValue();
                    player.setPlayerTime(time, true);
                } catch (final Exception e) {
                    FlagManager.removePlotFlag(plot, "time");
                }
            }
            final Flag weatherFlag = FlagManager.getPlotFlag(plot, "weather");
            if (weatherFlag != null) {
                player.setPlayerWeather(getWeatherType(weatherFlag.getValueString()));
            }
            if ((booleanFlag(plot, "titles", false) || Settings.TITLES) && (C.TITLE_ENTERED_PLOT.s().length() > 2)) {
                final String sTitleMain = C.TITLE_ENTERED_PLOT.s().replaceAll("%x%", plot.id.x + "").replaceAll("%z%", plot.id.y + "").replaceAll("%world%", plot.world + "");
                final String sTitleSub = C.TITLE_ENTERED_PLOT_SUB.s().replaceFirst("%s", getName(plot.owner));
                if (AbstractTitle.TITLE_CLASS != null) {
                    AbstractTitle.TITLE_CLASS.sendTitle(player, sTitleMain, sTitleSub, ChatColor.valueOf(C.TITLE_ENTERED_PLOT_COLOR.s()), ChatColor.valueOf(C.TITLE_ENTERED_PLOT_SUB_COLOR.s()));
                }
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
        if (FlagManager.getPlotFlag(plot, "fly") != null) {
            player.setAllowFlight(Bukkit.getAllowFlight());
        }
        if (FlagManager.getPlotFlag(plot, "gamemode") != null) {
            player.setGameMode(Bukkit.getDefaultGameMode());
        }
        if (FlagManager.getPlotFlag(plot, "time") != null) {
            player.resetPlayerTime();
        }
        if (FlagManager.getPlotFlag(plot, "weather") != null) {
            player.resetPlayerWeather();
        }
    }
    
    public static boolean getFlagValue(final String value) {
        return Arrays.asList("true", "on", "enabled", "yes").contains(value.toLowerCase());
    }
}
