package com.intellectualcrafters.plot.object;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.util.bukkit.BukkitUtil;

public class ConsolePlayer implements PlotPlayer {

    private static ConsolePlayer instance;
    private Location loc;
    private final HashMap<String, Object> meta;
    
    public static ConsolePlayer getConsole() {
        if (instance == null) {
            instance = new ConsolePlayer();
        }
        return instance;
    }
    
    private ConsolePlayer() {
        String world;
        Set<String> plotworlds = PS.get().getPlotWorlds();
        if (plotworlds.size() > 0) {
            world = plotworlds.iterator().next();
        }
        else {
            world = "world";
        }
        this.loc = new Location(world, 0, 0, 0);
        this.meta = new HashMap<>();
    }
    
    public static boolean isConsole(PlotPlayer plr) {
        return instance == plr; 
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
    public boolean hasPermission(String perm) {
        return true;
    }

    @Override
    public void sendMessage(String message) {
        PS.log(message);
    }
    
    @Override
    public void sendMessage(C c, String... args) {
        MainUtil.sendMessage(this, c, args);
    }

    @Override
    public void teleport(Location loc) {
        this.loc = loc;
    }

    @Override
    public boolean isOp() {
        return true;
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
    public void setCompassTarget(Location loc) {}

    @Override
    public void loadData() {}

    @Override
    public void saveData() {}

    @Override
    public void setAttribute(String key) {}

    @Override
    public boolean getAttribute(String key) {
        return false;
    }

    @Override
    public void removeAttribute(String key) {}

    @Override
    public void setMeta(String key, Object value) {
        this.meta.put(key, value);
    }

    @Override
    public Object getMeta(String key) {
        return this.meta.get(key);
    }

    @Override
    public void deleteMeta(String key) {
        this.meta.remove(key);
    }

    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.CONSOLE;
    }
    
}
