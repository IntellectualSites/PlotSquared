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

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.comment.CommentManager;
import com.intellectualcrafters.plot.titles.AbstractTitle;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 * @author Empire92
 */
public class PlotListener extends APlotListener {
    public void textures(final Player p) {
        if ((Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1) && PS.get().isPlotWorld(p.getWorld().getName())) {
            p.setResourcePack(Settings.PLOT_SPECIFIC_RESOURCE_PACK);
        }
    }

    private String getName(final UUID id) {
        if (id == null) {
            return "none";
        }
        final String name = UUIDHandler.getName(id);
        if (name == null) {
            return "unknown";
        }
        return name;
    }

    private void setWeather(Player player, String str) {
        switch (str.toLowerCase()) {
            case "clear": {
                player.setPlayerWeather(WeatherType.CLEAR);
                return;
            }
            case "rain": {
                player.setPlayerWeather(WeatherType.DOWNFALL);
                return;
            }
        }
    }

    private GameMode getGameMode(final String str) {
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

    public void plotEntry(final PlotPlayer pp, final Plot plot) {
        final Player player = ((BukkitPlayer) pp).player;
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
                    player.setPlayerTime(time, false);
                } catch (final Exception e) {
                    FlagManager.removePlotFlag(plot, "time");
                }
            }
            final Flag weatherFlag = FlagManager.getPlotFlag(plot, "weather");
            if (weatherFlag != null) {
                setWeather(player, weatherFlag.getValueString());
            }
            if ((FlagManager.isBooleanFlag(plot, "titles", Settings.TITLES)) && (C.TITLE_ENTERED_PLOT.s().length() > 2)) {
                Flag greetingFlag = FlagManager.getPlotFlag(plot, "greeting");
                String greeting;
                if (greetingFlag != null) {
                    greeting = greetingFlag.getValue() + "";
                }
                else {
                    greeting = "";
                }
                String alias = plot.settings.getAlias();
                if (alias.length() == 0) {
                    alias = plot.toString();
                }
                final String sTitleMain = C.TITLE_ENTERED_PLOT.s().replaceAll("%x%", plot.id.x + "").replaceAll("%z%", plot.id.y + "").replaceAll("%world%", plot.world + "").replaceAll("%greeting%", greeting).replaceAll("%s", getName(plot.owner)).replaceAll("%alias%", alias);
                final String sTitleSub = C.TITLE_ENTERED_PLOT_SUB.s().replaceAll("%x%", plot.id.x + "").replaceAll("%z%", plot.id.y + "").replaceAll("%world%", plot.world + "").replaceAll("%greeting%", greeting).replaceAll("%s", getName(plot.owner)).replaceAll("%alias%", alias);
                AbstractTitle.sendTitle(pp, sTitleMain, sTitleSub, ChatColor.valueOf(C.TITLE_ENTERED_PLOT_COLOR.s()), ChatColor.valueOf(C.TITLE_ENTERED_PLOT_SUB_COLOR.s()));
            }
            {
                final PlayerEnterPlotEvent callEvent = new PlayerEnterPlotEvent(player, plot);
                Bukkit.getPluginManager().callEvent(callEvent);
            }
            Flag musicFlag = FlagManager.getPlotFlag(plot, "music");
            if (musicFlag != null) {
                final Integer id = (Integer) musicFlag.getValue();
                if ((id >= 2256 && id <= 2267) || id == 0) {
                    final org.bukkit.Location loc = player.getLocation();
                    org.bukkit.Location lastLoc = (org.bukkit.Location) pp.getMeta("music");
                    if (lastLoc != null) {
                        player.playEffect(lastLoc, Effect.RECORD_PLAY, 0);
                        if (id == 0) {
                            pp.deleteMeta("music");
                            return;
                        }
                    }
                    try {
                        pp.setMeta("music", loc);
                        player.playEffect(loc, Effect.RECORD_PLAY, Material.getMaterial(id));
                    }
                    catch (Exception e) {}
                }
            }
            else {
                org.bukkit.Location lastLoc = (org.bukkit.Location) pp.getMeta("music");
                if (lastLoc != null) {
                    pp.deleteMeta("music");
                    player.playEffect(lastLoc, Effect.RECORD_PLAY, 0);
                }
            }
            CommentManager.sendTitle(pp, plot);
        }
    }

    public void plotExit(final PlotPlayer pp, final Plot plot) {
        Player player = ((BukkitPlayer) pp).player;
        final PlayerLeavePlotEvent callEvent = new PlayerLeavePlotEvent(player, plot);
        Bukkit.getPluginManager().callEvent(callEvent);
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
        org.bukkit.Location lastLoc = (org.bukkit.Location) pp.getMeta("music");
        if (lastLoc != null) {
            pp.deleteMeta("music");
            player.playEffect(lastLoc, Effect.RECORD_PLAY, 0);
        }
    }

    public boolean getFlagValue(final String value) {
        return Arrays.asList("true", "on", "enabled", "yes").contains(value.toLowerCase());
    }
}
