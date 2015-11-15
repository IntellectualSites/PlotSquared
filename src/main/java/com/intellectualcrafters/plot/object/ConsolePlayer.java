package com.intellectualcrafters.plot.object;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotGamemode;
import com.intellectualcrafters.plot.util.PlotWeather;

public class ConsolePlayer extends PlotPlayer {
    
    private static ConsolePlayer instance;
    private Location loc;
    private final HashMap<String, Object> meta;
    
    public static ConsolePlayer getConsole() {
        if (instance == null) {
            instance = new ConsolePlayer();
            instance.teleport(instance.getLocation());
        }
        return instance;
    }

    /**
     * Direct access is deprecated
     */
    @Deprecated
    public ConsolePlayer() {
        String world;
        final Set<String> plotworlds = PS.get().getPlotWorlds();
        if (plotworlds.size() > 0) {
            world = plotworlds.iterator().next();
        } else {
            world = "world";
        }
        loc = new Location(world, 0, 0, 0);
        meta = new HashMap<>();
    }
    
    public static boolean isConsole(final PlotPlayer plr) {
        return plr instanceof ConsolePlayer;
    }
    
    @Override
    public long getPreviousLogin() {
        return 0;
    }
    
    @Override
    public Location getLocation() {
        return loc;
    }
    
    @Override
    public Location getLocationFull() {
        return loc;
    }
    
    @Override
    public UUID getUUID() {
        return DBFunc.everyone;
    }
    
    @Override
    public boolean hasPermission(final String perm) {
        return true;
    }
    
    @Override
    public void sendMessage(final String message) {
        PS.log(message);
    }
    
    @Override
    public void sendMessage(final C c, final String... args) {
        MainUtil.sendMessage(this, c, args);
    }
    
    @Override
    public void teleport(final Location loc) {
        final Plot plot = MainUtil.getPlot(loc);
        setMeta("lastplot", plot);
        this.loc = loc;
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
    public void setCompassTarget(final Location loc) {}
    
    @Override
    public void loadData() {}
    
    @Override
    public void saveData() {}
    
    @Override
    public void setAttribute(final String key) {}
    
    @Override
    public boolean getAttribute(final String key) {
        return false;
    }
    
    @Override
    public void removeAttribute(final String key) {}
    
    @Override
    public void setMeta(final String key, final Object value) {
        meta.put(key, value);
    }
    
    @Override
    public Object getMeta(final String key) {
        return meta.get(key);
    }
    
    @Override
    public Object deleteMeta(final String key) {
        return meta.remove(key);
    }
    
    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.CONSOLE;
    }
    
    @Override
    public void setWeather(final PlotWeather weather) {}
    
    @Override
    public PlotGamemode getGamemode() {
        return PlotGamemode.CREATIVE;
    }
    
    @Override
    public void setGamemode(final PlotGamemode gamemode) {}
    
    @Override
    public void setTime(final long time) {}
    
    @Override
    public void setFlight(final boolean fly) {}
    
    @Override
    public void playMusic(final Location loc, final int id) {}
    
    @Override
    public void kick(final String message) {}
    
    @Override
    public boolean isBanned() {
        return false;
    }
    
}
