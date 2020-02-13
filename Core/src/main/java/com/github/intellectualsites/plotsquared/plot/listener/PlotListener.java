package com.github.intellectualsites.plotsquared.plot.listener;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DenyExitFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.FlightFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MusicFlag;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.ByteArrayUtilities;
import com.github.intellectualsites.plotsquared.plot.util.CommentManager;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.github.intellectualsites.plotsquared.plot.util.world.ItemUtil;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        EventUtil.manager.callEntry(player, plot);
        if (plot.hasOwner()) {
            Map<Flag<?>, Object> flags = FlagManager.getPlotFlags(plot);

            boolean titles;
            if (!plot.getArea().DEFAULT_FLAGS.isEmpty()) {
                Boolean value = (Boolean) plot.getArea().DEFAULT_FLAGS.get(Flags.TITLES);
                titles = value != null ? value : Settings.TITLES;
            } else {
                titles = Settings.TITLES;
            }
            final String greeting;
            if (flags.isEmpty()) {
                if (titles) {
                    greeting = "";
                } else {
                    return true;
                }
            } else {
                titles = plot.getFlag(Flags.TITLES, titles);
                Optional<String> greetingFlag = plot.getFlag(Flags.GREETING);
                if (greetingFlag.isPresent()) {
                    greeting = greetingFlag.get();
                    MainUtil
                        .format(Captions.PREFIX_GREETING.getTranslated() + greeting, plot, player,
                            false,
                        new RunnableVal<String>() {
                            @Override public void run(String value) {
                                MainUtil.sendMessage(player, value);
                            }
                        });
                } else {
                    greeting = "";
                }
                Optional<Boolean> enter = plot.getFlag(Flags.NOTIFY_ENTER);
                if (enter.isPresent() && enter.get()) {
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

                Optional<GameMode> gamemodeFlag = plot.getFlag(Flags.GAMEMODE);
                if (gamemodeFlag.isPresent()) {
                    if (player.getGameMode() != gamemodeFlag.get()) {
                        if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                            player.setGameMode(gamemodeFlag.get());
                        } else {
                            MainUtil.sendMessage(player, StringMan
                                .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(),
                                    "{plot}",
                                    plot.getId(), "{gamemode}", gamemodeFlag.get()));
                        }
                    }
                }
                Optional<GameMode> guestGamemodeFlag = plot.getFlag(Flags.GUEST_GAMEMODE);
                if (guestGamemodeFlag.isPresent()) {
                    if (player.getGameMode() != guestGamemodeFlag.get() && !plot
                        .isAdded(player.getUUID())) {
                        if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                            player.setGameMode(guestGamemodeFlag.get());
                        } else {
                            MainUtil.sendMessage(player, StringMan
                                .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(),
                                    "{plot}",
                                    plot.getId(), "{gamemode}", guestGamemodeFlag.get()));
                        }
                    }
                }
                Optional<Long> timeFlag = plot.getFlag(Flags.TIME);
                if (timeFlag.isPresent() && !player.getAttribute("disabletime")) {
                    try {
                        long time = timeFlag.get();
                        player.setTime(time);
                    } catch (Exception ignored) {
                        FlagManager.removePlotFlag(plot, Flags.TIME);
                    }
                }

                Optional<PlotWeather> weatherFlag = plot.getFlag(Flags.WEATHER);
                weatherFlag.ifPresent(player::setWeather);

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
            }
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
        EventUtil.manager.callLeave(player, plot);
        if (plot.hasOwner()) {
            PlotArea pw = plot.getArea();
            if (pw == null) {
                return true;
            }
            if (plot.getFlag(DenyExitFlag.class)
                && !Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_EXIT_DENIED)
                && !player.getMeta("kick", false)) {
                if (previous != null) {
                    player.setMeta(PlotPlayer.META_LAST_PLOT, previous);
                }
                return false;
            }
            if (plot.getFlag(Flags.GAMEMODE).isPresent() || plot.getFlag(Flags.GUEST_GAMEMODE)
                .isPresent()) {
                if (player.getGameMode() != pw.GAMEMODE) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(pw.GAMEMODE);
                    } else {
                        MainUtil.sendMessage(player, StringMan
                            .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(), "{plot}",
                                plot.toString(), "{gamemode}", pw.GAMEMODE.getName().toLowerCase()));
                    }
                }
            }
            Optional<String> farewell = plot.getFlag(Flags.FAREWELL);
            farewell.ifPresent(s -> MainUtil
                .format(Captions.PREFIX_FAREWELL.getTranslated() + s, plot, player, false,
                    new RunnableVal<String>() {
                        @Override public void run(String value) {
                            MainUtil.sendMessage(player, value);
                        }
                    }));
            Optional<Boolean> leave = plot.getFlag(Flags.NOTIFY_LEAVE);
            if (leave.isPresent() && leave.get()) {
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

            if (plot.getFlag(Flags.TIME).isPresent()) {
                player.setTime(Long.MAX_VALUE);
            }

            if (plot.getFlag(Flags.WEATHER).isPresent()) {
                player.setWeather(PlotWeather.CLEAR);
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
