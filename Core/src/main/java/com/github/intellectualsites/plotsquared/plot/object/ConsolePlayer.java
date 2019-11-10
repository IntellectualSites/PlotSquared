package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.RequiredType;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.util.PlotGameMode;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.item.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ConsolePlayer extends PlotPlayer {

    private static ConsolePlayer instance;

    private ConsolePlayer() {
        PlotArea area = PlotSquared.get().getFirstPlotArea();
        Location location;
        if (area != null) {
            CuboidRegion region = area.getRegion();
            location = new Location(area.worldname, region.getMinimumPoint().getX() + region.getMaximumPoint().getX() / 2, 0,
                region.getMinimumPoint().getZ() + region.getMaximumPoint().getZ() / 2);
        } else {
            location = new Location("world", 0, 0, 0);
        }
        setMeta("location", location);
    }

    public static ConsolePlayer getConsole() {
        if (instance == null) {
            instance = new ConsolePlayer();
            instance.teleport(instance.getLocation());
        }
        return instance;
    }

    @Override public Actor toActor() {
        return PlotSquared.get().IMP.getConsole();
    }

    @Override public boolean canTeleport(@NotNull Location location) {
        return true;
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    }

    @NotNull @Override public Location getLocation() {
        return this.getMeta("location");
    }

    @Override public Location getLocationFull() {
        return getLocation();
    }

    @NotNull @Override public UUID getUUID() {
        return DBFunc.EVERYONE;
    }

    @Override public long getLastPlayed() {
        return 0;
    }

    @Override public boolean hasPermission(String permission) {
        return true;
    }

    @Override public boolean isPermissionSet(String permission) {
        return true;
    }

    @Override public void sendMessage(String message) {
        PlotSquared.log(message);
    }

    @Override public void teleport(Location location) {
        setMeta(PlotPlayer.META_LAST_PLOT, location.getPlot());
        setMeta(PlotPlayer.META_LOCATION, location);
    }

    @Override public boolean isOnline() {
        return true;
    }

    @Override public String getName() {
        return "*";
    }

    @Override public void setCompassTarget(Location location) {
    }

    @Override public void setAttribute(String key) {
    }

    @Override public boolean getAttribute(String key) {
        return false;
    }

    @Override public void removeAttribute(String key) {
    }

    @Override public RequiredType getSuperCaller() {
        return RequiredType.CONSOLE;
    }

    @Override public void setWeather(@NotNull PlotWeather weather) {
    }

    @NotNull @Override public PlotGameMode getGameMode() {
        return PlotGameMode.NOT_SET;
    }

    @Override public void setGameMode(@NotNull PlotGameMode gameMode) {
    }

    @Override public void setTime(long time) {
    }

    @Override public boolean getFlight() {
        return true;
    }

    @Override public void setFlight(boolean fly) {
    }

    @Override public void playMusic(@NotNull Location location, @NotNull ItemType id) {
    }

    @Override public void kick(String message) {
    }

    @Override public void stopSpectating() {
    }

    @Override public boolean isBanned() {
        return false;
    }

}
