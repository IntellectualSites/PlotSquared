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
package com.plotsquared.bukkit.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import com.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.plotsquared.bukkit.events.PlayerLeavePlotEvent;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.plotsquared.bukkit.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.object.comment.CommentManager;
import com.plotsquared.bukkit.titles.AbstractTitle;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;

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

    public boolean plotEntry(final PlotPlayer pp, final Plot plot) {
        if (plot.isDenied(pp.getUUID()) && !Permissions.hasPermission(pp, "plots.admin.entry.denied")) {
            return false;
        }
        Plot last = (Plot) pp.getMeta("lastplot");
        if (last != null && !last.id.equals(plot.id)) {
            plotExit(pp, last);
        }
        pp.setMeta("lastplot", plot);
        final Player player = ((BukkitPlayer) pp).player;
        final PlayerEnterPlotEvent callEvent = new PlayerEnterPlotEvent(player, plot);
        Bukkit.getPluginManager().callEvent(callEvent);
        if (plot.hasOwner()) {
            HashMap<String, Flag> flags = FlagManager.getPlotFlags(plot);
            int size = flags.size();
            boolean titles = Settings.TITLES;
            final String greeting;
            
            if (size != 0) {
                Flag titleFlag = flags.get("titles");
                    if (titleFlag != null) {
                    titles = (Boolean) titleFlag.getValue();
                }
                Flag greetingFlag = flags.get("greeting");
                if (greetingFlag != null) {
                    greeting = (String) greetingFlag.getValue();
                }
                else {
                    greeting = "";
                }
                
                final Flag gamemodeFlag = flags.get("gamemode");
                if (gamemodeFlag != null) {
                    if (player.getGameMode() != getGameMode(gamemodeFlag.getValueString())) {
                        if (!player.hasPermission("plots.gamemode.bypass")) {
                            player.setGameMode(getGameMode(gamemodeFlag.getValueString()));
                        }
                        else {
                            MainUtil.sendMessage(pp, StringMan.replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.id, "{gamemode}", gamemodeFlag.getValue()));
                        }
                    }
                }
                final Flag flyFlag = flags.get("fly");
                if (flyFlag != null) {
                    player.setAllowFlight((boolean) flyFlag.getValue());
                }
                final Flag timeFlag = flags.get("time");
                if (timeFlag != null) {
                    try {
                        final long time = (long) timeFlag.getValue();
                        player.setPlayerTime(time, false);
                    } catch (final Exception e) {
                        FlagManager.removePlotFlag(plot, "time");
                    }
                }
                final Flag weatherFlag = flags.get("weather");
                if (weatherFlag != null) {
                    setWeather(player, weatherFlag.getValueString());
                }
                
                Flag musicFlag = flags.get("music");
                if (musicFlag != null) {
                    final Integer id = (Integer) musicFlag.getValue();
                    if ((id >= 2256 && id <= 2267) || id == 0) {
                        final org.bukkit.Location loc = player.getLocation();
                        org.bukkit.Location lastLoc = (org.bukkit.Location) pp.getMeta("music");
                        if (lastLoc != null) {
                            player.playEffect(lastLoc, Effect.RECORD_PLAY, 0);
                            if (id == 0) {
                                pp.deleteMeta("music");
                            }
                        }
                        if (id != 0) {
                            try {
                                pp.setMeta("music", loc);
                                player.playEffect(loc, Effect.RECORD_PLAY, Material.getMaterial(id));
                            }
                            catch (Exception e) {}
                        }
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
            else if (titles) {
                greeting = "";
            }
            else {
                return true;
            }
            if (titles) { 
                if (C.TITLE_ENTERED_PLOT.s().length() != 0 || C.TITLE_ENTERED_PLOT_SUB.s().length() != 0) {
                    TaskManager.runTaskLaterAsync(new Runnable() {
                        @Override
                        public void run() {
                            Plot lastPlot = (Plot) pp.getMeta("lastplot");
                            if (lastPlot != null && plot.id.equals(lastPlot.id)) {
                                Map<String, String> replacements = new HashMap<>();
                                replacements.put("%x%", lastPlot.id.x + "");
                                replacements.put("%z%", lastPlot.id.y + "");
                                replacements.put("%world%", plot.world);
                                replacements.put("%greeting%", greeting);
                                replacements.put("%alias", plot.toString());
                                replacements.put("%s", getName(plot.owner));
                                String main = StringMan.replaceFromMap(C.TITLE_ENTERED_PLOT.s(), replacements);
                                String sub = StringMan.replaceFromMap(C.TITLE_ENTERED_PLOT_SUB.s(), replacements);
                                AbstractTitle.sendTitle(pp, main, sub, ChatColor.valueOf(C.TITLE_ENTERED_PLOT_COLOR.s()), ChatColor.valueOf(C.TITLE_ENTERED_PLOT_SUB_COLOR.s()));
                            }
                        }
                    }, 20);
                }
            }
            return true;
        }
        return true;
    }

    public boolean plotExit(final PlotPlayer pp, final Plot plot) {
        pp.deleteMeta("lastplot");
        Player player = ((BukkitPlayer) pp).player;
        final PlayerLeavePlotEvent callEvent = new PlayerLeavePlotEvent(player, plot);
        Bukkit.getPluginManager().callEvent(callEvent);
        if (plot.hasOwner()) {
            if (FlagManager.getPlotFlag(plot, "fly") != null) {
                player.setAllowFlight(Bukkit.getAllowFlight());
            }
            if (FlagManager.getPlotFlag(plot, "gamemode") != null) {
                if (player.getGameMode() != Bukkit.getDefaultGameMode()) {
                    if (!player.hasPermission("plots.gamemode.bypass")) {
                        player.setGameMode(Bukkit.getDefaultGameMode());
                    }
                    else {
                        MainUtil.sendMessage(pp, StringMan.replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.world, "{gamemode}", Bukkit.getDefaultGameMode().name().toLowerCase()));
                    }
                }
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
        return true;
    }

    public boolean getFlagValue(final String value) {
        return Arrays.asList("true", "on", "enabled", "yes").contains(value.toLowerCase());
    }
}
