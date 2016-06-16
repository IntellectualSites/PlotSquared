package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;

import java.util.UUID;

public class ConsolePlayer extends PlotPlayer {
    
    private static ConsolePlayer instance;

    private ConsolePlayer() {
        PlotArea area = PS.get().getFirstPlotArea();
        Location loc;
        if (area != null) {
            RegionWrapper region = area.getRegion();
            loc = new Location(area.worldname, region.minX + region.maxX / 2, 0, region.minZ + region.maxZ / 2);
        } else {
            loc = new Location("world", 0, 0, 0);
        }
        setMeta("location", loc);
    }

    public static ConsolePlayer getConsole() {
        if (instance == null) {
            instance = new ConsolePlayer();
            instance.teleport(instance.getLocation());
        }
        return instance;
    }

    @Override
    public Location getLocation() {
        return this.<Location>getMeta("location");
    }
    
    @Override
    public Location getLocationFull() {
        return getLocation();
    }
    
    @Override
    public UUID getUUID() {
        return DBFunc.everyone;
    }

    @Override public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }
    
    @Override
    public void sendMessage(String message) {
        PS.log(message);
    }
    
    @Override
    public void teleport(Location location) {
        setMeta("lastplot", location.getPlot());
        setMeta("location", location);
    }
    
    @Override
    public boolean isOnline() {
        return true;
    }
    
    @Override
    public String getName() {
        return "*";
    }
    
    @Override
    public void setCompassTarget(Location location) {}

    @Override
    public void setAttribute(String key) {}
    
    @Override
    public boolean getAttribute(String key) {
        return false;
    }
    
    @Override
    public void removeAttribute(String key) {}
    
    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.CONSOLE;
    }
    
    @Override
    public void setWeather(PlotWeather weather) {}
    
    @Override
    public PlotGameMode getGameMode() {
        return PlotGameMode.NOT_SET;
    }
    
    @Override
    public void setGameMode(PlotGameMode gameMode) {}
    
    @Override
    public void setTime(long time) {}
    
    @Override
    public void setFlight(boolean fly) {}

    @Override
    public boolean getFlight() {
        return true;
    }

    @Override
    public void playMusic(Location location, int id) {}
    
    @Override
    public void kick(String message) {}

    @Override public void stopSpectating() {}

    @Override
    public boolean isBanned() {
        return false;
    }
    
}
