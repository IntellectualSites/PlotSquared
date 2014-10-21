package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.*;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Citymonstret on 2014-10-21.
 */
public class PlotListener {

    public void textures(Player p) {
        if ((Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1) && isPlotWorld(p.getWorld())) {
            p.setResourcePack(Settings.PLOT_SPECIFIC_RESOURCE_PACK);
        }
    }

    public boolean isInPlot(Player player) {
        return PlayerFunctions.isInPlot(player);
    }

    public Plot getPlot(Player player) {
        return PlayerFunctions.getCurrentPlot(player);
    }

    public boolean isPlotWorld(World world) {
        return PlotMain.isPlotWorld(world);
    }

    public PlotWorld getPlotWorld(World world) {
        return PlotMain.getWorldSettings(world);
    }

    public String getName(UUID uuid) {
        return UUIDHandler.getName(uuid);
    }

    public UUID getUUID(String name) {
        return UUIDHandler.getUUID(name);
    }

    public boolean enteredPlot(Location l1, Location l2) {
        PlotId p1 = PlayerFunctions.getPlot(new Location(l1.getWorld(), l1.getBlockX(), 64, l1.getBlockZ()));
        PlotId p2 = PlayerFunctions.getPlot(new Location(l2.getWorld(), l2.getBlockX(), 64, l2.getBlockZ()));
        if (p2 == null) {
            return false;
        }
        if (p1 == null) {
            return true;
        }
        if (p1.equals(p2)) {
            return false;
        }
        return true;
    }

    public boolean leftPlot(Location l1, Location l2) {
        PlotId p1 = PlayerFunctions.getPlot(new Location(l1.getWorld(), l1.getBlockX(), 64, l1.getBlockZ()));
        PlotId p2 = PlayerFunctions.getPlot(new Location(l2.getWorld(), l2.getBlockX(), 64, l2.getBlockZ()));
        if (p1 == null) {
            return false;
        }
        if (p2 == null) {
            return true;
        }
        if (p1.equals(p2)) {
            return false;
        }
        return true;
    }

    public boolean isPlotWorld(Location l) {
        return PlotMain.isPlotWorld(l.getWorld());
    }

    public static boolean isInPlot(Location loc) {
        return getCurrentPlot(loc) != null;
    }

    public static Plot getCurrentPlot(Location loc) {
        PlotId id = PlayerFunctions.getPlot(loc);
        if (id == null) {
            return null;
        }
        World world = loc.getWorld();
        if (PlotMain.getPlots(world).containsKey(id)) {
            return PlotMain.getPlots(world).get(id);
        }
        return new Plot(id, null, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), loc.getWorld().getName());
    }

    private WeatherType getWeatherType(String str) {
        str = str.toLowerCase();
        if(str.equals("rain")) {
            return WeatherType.DOWNFALL;
        } else {
            return WeatherType.CLEAR;
        }
    }

    private GameMode getGameMode(String str) {
        if (str.equals("creative")) {
            return GameMode.CREATIVE;
        } else if (str.equals("survival")) {
            return GameMode.SURVIVAL;
        } else if (str.equals("adventure")) {
            return GameMode.ADVENTURE;
        } else {
            return Bukkit.getDefaultGameMode();
        }
    }

    public void plotEntry(Player player, Plot plot) {
        if (plot.hasOwner()) {
            if(plot.settings.getFlag("gamemode") != null) {
                player.setGameMode(getGameMode(plot.settings.getFlag("gamemode").getValue()));
            }
            if(plot.settings.getFlag("fly") != null) {
                player.setAllowFlight(getFlagValue(plot.settings.getFlag("fly").getValue()));
            }
            if(plot.settings.getFlag("time") != null) {
                try {
                    Long time = Long.parseLong(plot.settings.getFlag("time").getValue());
                    player.setPlayerTime(time, true);
                } catch(Exception e) {
                    plot.settings.setFlags(FlagManager.removeFlag(plot.settings.getFlags(), "time"));
                }
            }
            if(plot.settings.getFlag("weather") != null) {
                player.setPlayerWeather(getWeatherType(plot.settings.getFlag("weather").getValue()));
            }
            if (C.TITLE_ENTERED_PLOT.s().length() > 2) {
                String sTitleMain = C.TITLE_ENTERED_PLOT.s().replaceFirst("%s", plot.getDisplayName());
                String sTitleSub = C.TITLE_ENTERED_PLOT_SUB.s().replaceFirst("%s", getName(plot.owner));
                ChatColor sTitleMainColor = ChatColor.valueOf(C.TITLE_ENTERED_PLOT_COLOR.s());
                ChatColor sTitleSubColor = ChatColor.valueOf(C.TITLE_ENTERED_PLOT_SUB_COLOR.s());
                Title title = new Title(sTitleMain, sTitleSub, 10, 20, 10);
                title.setTitleColor(sTitleMainColor);
                title.setSubtitleColor(sTitleSubColor);
                title.setTimingsToTicks();
                title.send(player);
            }
            {
                PlayerEnterPlotEvent callEvent = new PlayerEnterPlotEvent(player, plot);
                Bukkit.getPluginManager().callEvent(callEvent);
            }
            PlayerFunctions.sendMessage(player, plot.settings.getJoinMessage());
        }
    }

    public void plotExit(Player player, Plot plot) {
        {
            PlayerLeavePlotEvent callEvent = new PlayerLeavePlotEvent(player, plot);
            Bukkit.getPluginManager().callEvent(callEvent);
        }
        player.setGameMode(Bukkit.getDefaultGameMode());
        player.resetPlayerTime();
        player.resetPlayerWeather();
        PlayerFunctions.sendMessage(player, plot.settings.getLeaveMessage());
    }

    public boolean getFlagValue(String value) {
        return Arrays.asList("true", "on", "enabled", "yes").contains(value.toLowerCase());
    }
}
