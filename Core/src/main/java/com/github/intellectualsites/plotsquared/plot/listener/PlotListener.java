package com.github.intellectualsites.plotsquared.plot.listener;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DenyExitFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.FarewellFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.FlightFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.GamemodeFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.GreetingFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.GuestGamemodeFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MusicFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.NotifyEnterFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.NotifyLeaveFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.WeatherFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.TimeFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.TitlesFlag;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.ByteArrayUtilities;
import com.github.intellectualsites.plotsquared.plot.util.CommentManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotListener {

    public static boolean plotEntry(final PlotPlayer player, final Plot plot) {
        if (plot.isDenied(player.getUUID()) && !Permissions
            .hasPermission(player, "plots.admin.entry.denied")) {
            return false;
        }
        Plot last = player.getMeta(PlotPlayer.META_LAST_PLOT);
        if ((last != null) && !last.getId().equals(plot.getId())) {
            plotExit(player, last);
        }
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.handleEntry(player, plot);
        }
        player.setMeta(PlotPlayer.META_LAST_PLOT, plot);
        PlotSquared.get().getEventUtil().callEntry(player, plot);
        if (plot.hasOwner()) {
            // This will inherit values from PlotArea
            final TitlesFlag.TitlesFlagValue titleFlag = plot.getFlag(TitlesFlag.class);
            final boolean titles;
            if (titleFlag == TitlesFlag.TitlesFlagValue.NONE) {
                titles = Settings.TITLES;
            } else {
                titles = titleFlag == TitlesFlag.TitlesFlagValue.TRUE;
            }

            final String greeting = plot.getFlag(GreetingFlag.class);
            if (!greeting.isEmpty()) {
                MainUtil
                    .format(Captions.PREFIX_GREETING.getTranslated() + greeting, plot, player,
                        false, new RunnableVal<String>() {
                            @Override public void run(String value) {
                                MainUtil.sendMessage(player, value);
                            }
                        });
            }

            if (plot.getFlag(NotifyEnterFlag.class)) {
                if (!Permissions.hasPermission(player, "plots.flag.notify-enter.bypass")) {
                    for (UUID uuid : plot.getOwners()) {
                        PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                        if (owner != null && !owner.getUUID().equals(player.getUUID())) {
                            MainUtil.sendMessage(owner, Captions.NOTIFY_ENTER.getTranslated()
                                .replace("%player", player.getName())
                                .replace("%plot", plot.getId().toString()));
                        }
                    }
                }
            }

            if (plot.getFlag(FlightFlag.class)) {
                boolean flight = player.getFlight();
                GameMode gamemode = player.getGameMode();
                if (flight != (gamemode == GameModes.CREATIVE
                    || gamemode == GameModes.SPECTATOR)) {
                    player.setPersistentMeta("flight",
                        ByteArrayUtilities.booleanToBytes(player.getFlight()));
                }
                player.setFlight(true);
            }

            final GameMode gameMode = plot.getFlag(GamemodeFlag.class);
            if (!gameMode.equals(GamemodeFlag.DEFAULT)) {
                if (player.getGameMode() != gameMode) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(gameMode);
                    } else {
                        MainUtil.sendMessage(player, StringMan
                            .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(),
                                "{plot}", plot.getId(), "{gamemode}", gameMode));
                    }
                }
            }

            final GameMode guestGameMode = plot.getFlag(GuestGamemodeFlag.class);
            if (!guestGameMode.equals(GamemodeFlag.DEFAULT)) {
                if (player.getGameMode() != guestGameMode && !plot.isAdded(player.getUUID())) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(guestGameMode);
                    } else {
                        MainUtil.sendMessage(player, StringMan
                            .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(),
                                "{plot}", plot.getId(), "{gamemode}", guestGameMode));
                    }
                }
            }

            long time = plot.getFlag(TimeFlag.class);
            if (time != TimeFlag.TIME_DISABLED.getValue() && !player
                .getAttribute("disabletime")) {
                try {
                    player.setTime(time);
                } catch (Exception ignored) {
                    plot.removeFlag(TimeFlag.class);
                }
            }

            player.setWeather(plot.getFlag(WeatherFlag.class));

            ItemType musicFlag = plot.getFlag(MusicFlag.class);
            if (musicFlag != null) {
                final String rawId = musicFlag.getId();
                if (rawId.contains("disc") || musicFlag == ItemTypes.AIR) {
                    Location location = player.getLocation();
                    Location lastLocation = player.getMeta("music");
                    if (lastLocation != null) {
                        player.playMusic(lastLocation, musicFlag);
                        if (musicFlag == ItemTypes.AIR) {
                            player.deleteMeta("music");
                        }
                    }
                    if (musicFlag != ItemTypes.AIR) {
                        try {
                            player.setMeta("music", location);
                            player.playMusic(location, musicFlag);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                Location lastLoc = player.getMeta("music");
                if (lastLoc != null) {
                    player.deleteMeta("music");
                    player.playMusic(lastLoc, ItemTypes.AIR);
                }
            }
            CommentManager.sendTitle(player, plot);

            if (titles && !player.getAttribute("disabletitles")) {
                if (!Captions.TITLE_ENTERED_PLOT.getTranslated().isEmpty()
                    || !Captions.TITLE_ENTERED_PLOT_SUB.getTranslated().isEmpty()) {
                    TaskManager.runTaskLaterAsync(() -> {
                        Plot lastPlot = player.getMeta(PlotPlayer.META_LAST_PLOT);
                        if ((lastPlot != null) && plot.getId().equals(lastPlot.getId())) {
                            Map<String, String> replacements = new HashMap<>();
                            replacements.put("%x%", String.valueOf(lastPlot.getId().x));
                            replacements.put("%z%", lastPlot.getId().y + "");
                            replacements.put("%world%", plot.getArea().toString());
                            replacements.put("%greeting%", greeting);
                            replacements.put("%alias", plot.toString());
                            replacements.put("%s", MainUtil.getName(plot.getOwner()));
                            String main = StringMan
                                .replaceFromMap(Captions.TITLE_ENTERED_PLOT.getTranslated(),
                                    replacements);
                            String sub = StringMan
                                .replaceFromMap(Captions.TITLE_ENTERED_PLOT_SUB.getTranslated(),
                                    replacements);
                            player.sendTitle(main, sub);
                        }
                    }, 20);
                }
            }
            return true;
        }
        return true;
    }

    public static boolean plotExit(final PlotPlayer player, Plot plot) {
        Object previous = player.deleteMeta(PlotPlayer.META_LAST_PLOT);
        PlotSquared.get().getEventUtil().callLeave(player, plot);
        if (plot.hasOwner()) {
            PlotArea pw = plot.getArea();
            if (pw == null) {
                return true;
            }
            if (plot.getFlag(DenyExitFlag.class) && !Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_EXIT_DENIED) && !player
                .getMeta("kick", false)) {
                if (previous != null) {
                    player.setMeta(PlotPlayer.META_LAST_PLOT, previous);
                }
                return false;
            }
            if (!plot.getFlag(GamemodeFlag.class).equals(GamemodeFlag.DEFAULT) || !plot
                .getFlag(GuestGamemodeFlag.class).equals(GamemodeFlag.DEFAULT)) {
                if (player.getGameMode() != pw.GAMEMODE) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(pw.GAMEMODE);
                    } else {
                        MainUtil.sendMessage(player, StringMan
                            .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(), "{plot}",
                                plot.toString(), "{gamemode}",
                                pw.GAMEMODE.getName().toLowerCase()));
                    }
                }
            }

            final String farewell = plot.getFlag(FarewellFlag.class);
            if (!farewell.isEmpty()) {
                MainUtil.format(Captions.PREFIX_FAREWELL.getTranslated() + farewell, plot, player,
                    false, new RunnableVal<String>() {
                        @Override public void run(String value) {
                            MainUtil.sendMessage(player, value);
                        }
                    });
            }

            if (plot.getFlag(NotifyLeaveFlag.class)) {
                if (!Permissions.hasPermission(player, "plots.flag.notify-enter.bypass")) {
                    for (UUID uuid : plot.getOwners()) {
                        PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                        if ((owner != null) && !owner.getUUID().equals(player.getUUID())) {
                            MainUtil.sendMessage(owner, Captions.NOTIFY_LEAVE.getTranslated()
                                .replace("%player", player.getName())
                                .replace("%plot", plot.getId().toString()));
                        }
                    }
                }
            }

            if (plot.getFlag(FlightFlag.class)) {
                if (player.hasPersistentMeta("flight")) {
                    player.setFlight(
                        ByteArrayUtilities.bytesToBoolean(player.getPersistentMeta("flight")));
                    player.removePersistentMeta("flight");
                } else {
                    GameMode gameMode = player.getGameMode();
                    if (gameMode == GameModes.SURVIVAL || gameMode == GameModes.ADVENTURE) {
                        player.setFlight(false);
                    } else if (!player.getFlight()) {
                        player.setFlight(true);
                    }
                }
            }

            if (plot.getFlag(TimeFlag.class) != TimeFlag.TIME_DISABLED.getValue().longValue()) {
                player.setTime(Long.MAX_VALUE);
            }

            final PlotWeather plotWeather = plot.getFlag(WeatherFlag.class);
            if (plotWeather != PlotWeather.RESET) {
                player.setWeather(PlotWeather.RESET);
            }

            Location lastLoc = player.getMeta("music");
            if (lastLoc != null) {
                player.deleteMeta("music");
                player.playMusic(lastLoc, ItemTypes.AIR);
            }
        }
        return true;
    }
}
