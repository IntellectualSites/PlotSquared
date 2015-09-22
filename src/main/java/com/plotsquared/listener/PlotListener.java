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
package com.plotsquared.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.CommentManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.PlotGamemode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;

/**
 */
public class PlotListener {
    
    public static boolean plotEntry(final PlotPlayer pp, final Plot plot) {
        if (plot.isDenied(pp.getUUID()) && !Permissions.hasPermission(pp, "plots.admin.entry.denied")) {
            return false;
        }
        final Plot last = (Plot) pp.getMeta("lastplot");
        if ((last != null) && !last.id.equals(plot.id)) {
            plotExit(pp, last);
        }
        pp.setMeta("lastplot", MainUtil.getPlot(plot));
        EventUtil.manager.callEntry(pp, plot);
        if (plot.hasOwner()) {
            final HashMap<String, Flag> flags = FlagManager.getPlotFlags(plot);
            final int size = flags.size();
            boolean titles = Settings.TITLES;
            final String greeting;
            
            if (size != 0) {
                final Flag titleFlag = flags.get("titles");
                if (titleFlag != null) {
                    titles = (Boolean) titleFlag.getValue();
                }
                final Flag greetingFlag = flags.get("greeting");
                if (greetingFlag != null) {
                    greeting = (String) greetingFlag.getValue();
                    pp.sendMessage(ChatColor.translateAlternateColorCodes('&', C.PREFIX_GREETING.s().replaceAll("%id%", plot.id + "") + greeting));
                } else {
                    greeting = "";
                }
                if (greeting != null) {
                    
                }
                final Flag enter = flags.get("notify-enter");
                if ((enter != null) && ((Boolean) enter.getValue())) {
                    if (!Permissions.hasPermission(pp, "plots.flag.notify-enter.bypass")) {
                        for (final UUID uuid : PlotHandler.getOwners(plot)) {
                            final PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                            if ((owner != null) && !owner.getUUID().equals(pp.getUUID())) {
                                MainUtil.sendMessage(pp, C.NOTIFY_ENTER.s().replace("%player", pp.getName()).replace("%plot", plot.getId().toString()));
                            }
                        }
                    }
                }
                final Flag gamemodeFlag = flags.get("gamemode");
                if (gamemodeFlag != null) {
                    if (pp.getGamemode() != gamemodeFlag.getValue()) {
                        if (!Permissions.hasPermission(pp, "plots.gamemode.bypass")) {
                            pp.setGamemode((PlotGamemode) gamemodeFlag.getValue());
                        } else {
                            MainUtil.sendMessage(pp, StringMan.replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.id, "{gamemode}", gamemodeFlag.getValue()));
                        }
                    }
                }
                final Flag flyFlag = flags.get("fly");
                if (flyFlag != null) {
                    pp.setFlight((boolean) flyFlag.getValue());
                }
                final Flag timeFlag = flags.get("time");
                if (timeFlag != null) {
                    try {
                        final long time = (long) timeFlag.getValue();
                        pp.setTime(time);
                    } catch (final Exception e) {
                        FlagManager.removePlotFlag(plot, "time");
                    }
                }
                final Flag weatherFlag = flags.get("weather");
                if (weatherFlag != null) {
                    pp.setWeather((PlotWeather) weatherFlag.getValue());
                }
                
                final Flag musicFlag = flags.get("music");
                if (musicFlag != null) {
                    final Integer id = (Integer) musicFlag.getValue();
                    if (((id >= 2256) && (id <= 2267)) || (id == 0)) {
                        final Location loc = pp.getLocation();
                        final Location lastLoc = (Location) pp.getMeta("music");
                        if (lastLoc != null) {
                            pp.playMusic(lastLoc, 0);
                            if (id == 0) {
                                pp.deleteMeta("music");
                            }
                        }
                        if (id != 0) {
                            try {
                                pp.setMeta("music", loc);
                                pp.playMusic(loc, id);
                            } catch (final Exception e) {}
                        }
                    }
                } else {
                    final Location lastLoc = (Location) pp.getMeta("music");
                    if (lastLoc != null) {
                        pp.deleteMeta("music");
                        pp.playMusic(lastLoc, 0);
                    }
                }
                CommentManager.sendTitle(pp, plot);
            } else if (titles) {
                greeting = "";
            } else {
                return true;
            }
            if (titles) {
                if ((C.TITLE_ENTERED_PLOT.s().length() != 0) || (C.TITLE_ENTERED_PLOT_SUB.s().length() != 0)) {
                    TaskManager.runTaskLaterAsync(new Runnable() {
                        @Override
                        public void run() {
                            final Plot lastPlot = (Plot) pp.getMeta("lastplot");
                            if ((lastPlot != null) && plot.id.equals(lastPlot.id)) {
                                final Map<String, String> replacements = new HashMap<>();
                                replacements.put("%x%", lastPlot.id.x + "");
                                replacements.put("%z%", lastPlot.id.y + "");
                                replacements.put("%world%", plot.world);
                                replacements.put("%greeting%", greeting);
                                replacements.put("%alias", plot.toString());
                                replacements.put("%s", MainUtil.getName(plot.owner));
                                final String main = StringMan.replaceFromMap(C.TITLE_ENTERED_PLOT.s(), replacements);
                                final String sub = StringMan.replaceFromMap(C.TITLE_ENTERED_PLOT_SUB.s(), replacements);
                                AbstractTitle.sendTitle(pp, main, sub);
                            }
                        }
                    }, 20);
                }
            }
            return true;
        }
        return true;
    }
    
    public static boolean plotExit(final PlotPlayer pp, final Plot plot) {
        pp.deleteMeta("lastplot");
        EventUtil.manager.callLeave(pp, plot);
        if (plot.hasOwner()) {
            final PlotWorld pw = PS.get().getPlotWorld(pp.getLocation().getWorld());
            if (pw == null) {
                return true;
            }
            if (FlagManager.getPlotFlag(plot, "gamemode") != null) {
                if (pp.getGamemode() != pw.GAMEMODE) {
                    if (!Permissions.hasPermission(pp, "plots.gamemode.bypass")) {
                        pp.setGamemode(pw.GAMEMODE);
                    } else {
                        MainUtil.sendMessage(pp, StringMan.replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.world, "{gamemode}", pw.GAMEMODE.name().toLowerCase()));
                    }
                }
            }
            final Flag farewell = FlagManager.getPlotFlag(plot, "farewell");
            if (farewell != null) {
                pp.sendMessage(ChatColor.translateAlternateColorCodes('&', C.PREFIX_FAREWELL.s().replaceAll("%id%", plot.id + "") + farewell.getValueString()));
            }
            final Flag leave = FlagManager.getPlotFlag(plot, "notify-leave");
            if ((leave != null) && ((Boolean) leave.getValue())) {
                if (!Permissions.hasPermission(pp, "plots.flag.notify-enter.bypass")) {
                    for (final UUID uuid : PlotHandler.getOwners(plot)) {
                        final PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                        if ((owner != null) && !owner.getUUID().equals(pp.getUUID())) {
                            MainUtil.sendMessage(pp, C.NOTIFY_LEAVE.s().replace("%player", pp.getName()).replace("%plot", plot.getId().toString()));
                        }
                    }
                }
            }
            if (FlagManager.getPlotFlag(plot, "fly") != null) {
                final PlotGamemode gamemode = pp.getGamemode();
                if ((gamemode == PlotGamemode.SURVIVAL) || (gamemode == PlotGamemode.ADVENTURE)) {
                    pp.setFlight(false);
                }
            }
            if (FlagManager.getPlotFlag(plot, "time") != null) {
                pp.setTime(Long.MAX_VALUE);
            }
            if (FlagManager.getPlotFlag(plot, "weather") != null) {
                pp.setWeather(PlotWeather.RESET);
            }
            final Location lastLoc = (Location) pp.getMeta("music");
            if (lastLoc != null) {
                pp.deleteMeta("music");
                pp.playMusic(lastLoc, 0);
            }
        }
        return true;
    }
    
    public boolean getFlagValue(final String value) {
        return Arrays.asList("true", "on", "enabled", "yes").contains(value.toLowerCase());
    }
}
