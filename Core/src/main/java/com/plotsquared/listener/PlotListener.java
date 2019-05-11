package com.plotsquared.listener;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.expiry.ExpireManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotListener {

    public static boolean plotEntry(final PlotPlayer player, final Plot plot) {
        if (plot.isDenied(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.entry.denied")) {
            return false;
        }
        Plot last = player.getMeta("lastplot");
        if ((last != null) && !last.getId().equals(plot.getId())) {
            plotExit(player, last);
        }
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.handleEntry(player, plot);
        }
        player.setMeta("lastplot", plot);
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
                    MainUtil.format(C.PREFIX_GREETING.s() + greeting, plot, player, false, new RunnableVal<String>() {
                        @Override
                        public void run(String value) {
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
                                MainUtil.sendMessage(owner,
                                        C.NOTIFY_ENTER.s().replace("%player", player.getName()).replace("%plot", plot.getId().toString()));
                            }
                        }
                    }
                }
                Optional<Boolean> flyFlag = plot.getFlag(Flags.FLY);
                if (flyFlag.isPresent()) {
                    boolean flight = player.getFlight();
                    PlotGameMode gamemode = player.getGameMode();
                    if (flight != (gamemode == PlotGameMode.CREATIVE || gamemode == PlotGameMode.SPECTATOR)) {
                        player.setPersistentMeta("flight", ByteArrayUtilities.booleanToBytes(player.getFlight()));
                    }
                    if (flyFlag.get() != player.getFlight()) {
                        player.setFlight(flyFlag.get());
                    }
                }

                Optional<PlotGameMode> guestGamemodeFlag = plot.getFlag(Flags.GUEST_GAMEMODE);
                if (guestGamemodeFlag.isPresent() && !plot.isAdded(player.getUUID())) {
                    if (player.getGameMode() != guestGamemodeFlag.get()) {
                        if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                            player.setGameMode(guestGamemodeFlag.get());
                        } else {
                            MainUtil.sendMessage(player,
                                    StringMan.replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.getId(), "{gamemode}", guestGamemodeFlag.get()));
                        }
                    }
                } else {
                    Optional<PlotGameMode> gamemodeFlag = plot.getFlag(Flags.GAMEMODE);
                    if (gamemodeFlag.isPresent()) {
                        if (player.getGameMode() != gamemodeFlag.get()) {
                            if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                                player.setGameMode(gamemodeFlag.get());
                            } else {
                                MainUtil.sendMessage(player,
                                        StringMan.replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.getId(), "{gamemode}", gamemodeFlag.get()));
                            }
                        }
                    }
                }

                Optional<Long> timeFlag = plot.getFlag(Flags.TIME);
                if (timeFlag.isPresent()) {
                    try {
                        long time = timeFlag.get();
                        player.setTime(time);
                    } catch (Exception ignored) {
                        FlagManager.removePlotFlag(plot, Flags.TIME);
                    }
                }
                Optional<PlotWeather> weatherFlag = plot.getFlag(Flags.WEATHER);
                if (weatherFlag.isPresent()) {
                    player.setWeather(weatherFlag.get());
                }

                Optional<Integer> musicFlag = plot.getFlag(Flags.MUSIC);
                if (musicFlag.isPresent()) {
                    Integer id = musicFlag.get();
                    if ((id >= 2256 && id <= 2267) || (id == 0)) {
                        Location loc = player.getLocation();
                        Location lastLoc = player.getMeta("music");
                        if (lastLoc != null) {
                            player.playMusic(lastLoc, 0);
                            if (id == 0) {
                                player.deleteMeta("music");
                            }
                        }
                        if (id != 0) {
                            try {
                                player.setMeta("music", loc);
                                player.playMusic(loc, id);
                            } catch (Exception ignored) {}
                        }
                    }
                } else {
                    Location lastLoc = player.getMeta("music");
                    if (lastLoc != null) {
                        player.deleteMeta("music");
                        player.playMusic(lastLoc, 0);
                    }
                }
                CommentManager.sendTitle(player, plot);
            }
            if (titles) {
                if (!C.TITLE_ENTERED_PLOT.s().isEmpty() || !C.TITLE_ENTERED_PLOT_SUB.s().isEmpty()) {
                    TaskManager.runTaskLaterAsync(() -> {
                        Plot lastPlot = player.getMeta("lastplot");
                        if ((lastPlot != null) && plot.getId().equals(lastPlot.getId())) {
                            Map<String, String> replacements = new HashMap<>();
                            replacements.put("%x%", String.valueOf(lastPlot.getId().x));
                            replacements.put("%z%", lastPlot.getId().y + "");
                            replacements.put("%world%", plot.getArea().toString());
                            replacements.put("%greeting%", greeting);
                            replacements.put("%alias", plot.toString());
                            replacements.put("%s", MainUtil.getName(plot.owner));
                            String main = StringMan.replaceFromMap(C.TITLE_ENTERED_PLOT.s(), replacements);
                            String sub = StringMan.replaceFromMap(C.TITLE_ENTERED_PLOT_SUB.s(), replacements);
                            AbstractTitle.sendTitle(player, main, sub);
                        }
                    }, 20);
                }
            }
            return true;
        }
        return true;
    }

    public static boolean plotExit(final PlotPlayer player, Plot plot) {
        Object previous = player.deleteMeta("lastplot");
        EventUtil.manager.callLeave(player, plot);
        if (plot.hasOwner()) {
            PlotArea pw = plot.getArea();
            if (pw == null) {
                return true;
            }
            if (Flags.DENY_EXIT.isTrue(plot) && !player.getMeta("kick", false)) {
                if (previous != null) {
                    player.setMeta("lastplot", previous);
                }
                return false;
            }
            if (plot.getFlag(Flags.GAMEMODE).isPresent() || plot.getFlag(Flags.GUEST_GAMEMODE).isPresent()) {
                if (player.getGameMode() != pw.GAMEMODE) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(pw.GAMEMODE);
                    } else {
                        MainUtil.sendMessage(player, StringMan
                                .replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.toString(), "{gamemode}", pw.GAMEMODE.name().toLowerCase()));
                    }
                }
            }
            Optional<String> farewell = plot.getFlag(Flags.FAREWELL);
            if (farewell.isPresent()) {
                MainUtil.format(C.PREFIX_FAREWELL.s() + farewell.get(), plot, player, false, new RunnableVal<String>() {
                    @Override
                    public void run(String value) {
                        MainUtil.sendMessage(player, value);
                    }
                });
            }
            Optional<Boolean> leave = plot.getFlag(Flags.NOTIFY_LEAVE);
            if (leave.isPresent() && leave.get()) {
                if (!Permissions.hasPermission(player, "plots.flag.notify-enter.bypass")) {
                    for (UUID uuid : plot.getOwners()) {
                        PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                        if ((owner != null) && !owner.getUUID().equals(player.getUUID())) {
                            MainUtil.sendMessage(owner, C.NOTIFY_LEAVE.s().replace("%player", player.getName()).replace("%plot", plot.getId()
                                    .toString()));
                        }
                    }
                }
            }
            if (plot.getFlag(Flags.FLY).isPresent()) {
                if (player.hasPersistentMeta("flight")) {
                    player.setFlight(ByteArrayUtilities.bytesToBoolean(player.getPersistentMeta("flight")));
                    player.removePersistentMeta("flight");
                } else {
                    PlotGameMode gameMode = player.getGameMode();
                    if (gameMode == PlotGameMode.SURVIVAL || gameMode == PlotGameMode.ADVENTURE) {
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
                player.playMusic(lastLoc, 0);
            }
        }
        return true;
    }
}
