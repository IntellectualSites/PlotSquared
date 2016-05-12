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
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.CommentManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotListener {

    public static boolean plotEntry(final PlotPlayer pp, final Plot plot) {
        if (plot.isDenied(pp.getUUID()) && !Permissions.hasPermission(pp, "plots.admin.entry.denied")) {
            return false;
        }
        Plot last = pp.getMeta("lastplot");
        if ((last != null) && !last.getId().equals(plot.getId())) {
            plotExit(pp, last);
        }
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.handleEntry(pp, plot);
        }
        pp.setMeta("lastplot", plot);
        EventUtil.manager.callEntry(pp, plot);
        if (plot.hasOwner()) {
            Map<Flag<?>, Object> flags = FlagManager.getPlotFlags(plot);
            boolean titles = Settings.TITLES;
            final String greeting;

            if (flags.isEmpty()) {
                if (titles) {
                    greeting = "";
                } else {
                    return true;
                }
            } else {
                Optional<Boolean> titleFlag = plot.getFlag(Flags.TITLES);
                if (titleFlag.isPresent()) {
                    titles = titleFlag.get();
                }
                Optional<String> greetingFlag = plot.getFlag(Flags.GREETING);
                if (greetingFlag.isPresent()) {
                    greeting = greetingFlag.get();
                    MainUtil.format(C.PREFIX_GREETING.s() + greeting, plot, pp, false, new RunnableVal<String>() {
                        @Override
                        public void run(String value) {
                            MainUtil.sendMessage(pp, value);
                        }
                    });
                } else {
                    greeting = "";
                }
                Optional<Boolean> enter = plot.getFlag(Flags.NOTIFY_ENTER);
                if (enter.isPresent() && enter.get()) {
                    if (!Permissions.hasPermission(pp, "plots.flag.notify-enter.bypass")) {
                        for (UUID uuid : plot.getOwners()) {
                            PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                            if (owner != null && !owner.getUUID().equals(pp.getUUID())) {
                                MainUtil.sendMessage(owner,
                                        C.NOTIFY_ENTER.s().replace("%player", pp.getName()).replace("%plot", plot.getId().toString()));
                            }
                        }
                    }
                }
                Optional<PlotGameMode> gamemodeFlag = plot.getFlag(Flags.GAMEMODE);
                if (gamemodeFlag.isPresent()) {
                    if (pp.getGameMode() != gamemodeFlag.get()) {
                        if (!Permissions.hasPermission(pp, "plots.gamemode.bypass")) {
                            pp.setGameMode(gamemodeFlag.get());
                        } else {
                            MainUtil.sendMessage(pp,
                                    StringMan.replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.getId(), "{gamemode}", gamemodeFlag.get()));
                        }
                    }
                }
                Optional<Boolean> flyFlag = plot.getFlag(Flags.FLY);
                if (flyFlag.isPresent()) {
                    pp.setFlight(flyFlag.get());
                }
                Optional<Long> timeFlag = plot.getFlag(Flags.TIME);
                if (timeFlag.isPresent()) {
                    try {
                        long time = timeFlag.get();
                        pp.setTime(time);
                    } catch (Exception ignored) {
                        FlagManager.removePlotFlag(plot, Flags.TIME);
                    }
                }
                Optional<PlotWeather> weatherFlag = plot.getFlag(Flags.WEATHER);
                if (weatherFlag.isPresent()) {
                    pp.setWeather(weatherFlag.get());
                }

                Optional<Integer> musicFlag = plot.getFlag(Flags.MUSIC);
                if (musicFlag.isPresent()) {
                    Integer id = musicFlag.get();
                    if ((id >= 2256 && id <= 2267) || (id == 0)) {
                        Location loc = pp.getLocation();
                        Location lastLoc = pp.getMeta("music");
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
                            } catch (Exception ignored) {}
                        }
                    }
                } else {
                    Location lastLoc = pp.getMeta("music");
                    if (lastLoc != null) {
                        pp.deleteMeta("music");
                        pp.playMusic(lastLoc, 0);
                    }
                }
                CommentManager.sendTitle(pp, plot);
            }
            if (titles) {
                if (!C.TITLE_ENTERED_PLOT.s().isEmpty() || !C.TITLE_ENTERED_PLOT_SUB.s().isEmpty()) {
                    TaskManager.runTaskLaterAsync(new Runnable() {
                        @Override
                        public void run() {
                            Plot lastPlot = pp.getMeta("lastplot");
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

    public static boolean plotExit(final PlotPlayer pp, Plot plot) {
        pp.deleteMeta("lastplot");
        EventUtil.manager.callLeave(pp, plot);
        if (plot.hasOwner()) {
            PlotArea pw = plot.getArea();
            if (pw == null) {
                return true;
            }
            if (plot.getFlag(Flags.GAMEMODE).isPresent()) {
                if (pp.getGameMode() != pw.GAMEMODE) {
                    if (!Permissions.hasPermission(pp, "plots.gamemode.bypass")) {
                        pp.setGameMode(pw.GAMEMODE);
                    } else {
                        MainUtil.sendMessage(pp, StringMan
                                .replaceAll(C.GAMEMODE_WAS_BYPASSED.s(), "{plot}", plot.toString(), "{gamemode}", pw.GAMEMODE.name().toLowerCase()));
                    }
                }
            }
            Optional<String> farewell = plot.getFlag(Flags.FAREWELL);
            if (farewell.isPresent()) {
                MainUtil.format(C.PREFIX_FAREWELL.s() + farewell.get(), plot, pp, false, new RunnableVal<String>() {
                    @Override
                    public void run(String value) {
                        MainUtil.sendMessage(pp, value);
                    }
                });
            }
            Optional<Boolean> leave = plot.getFlag(Flags.NOTIFY_LEAVE);
            if (leave.isPresent() && leave.get()) {
                if (!Permissions.hasPermission(pp, "plots.flag.notify-enter.bypass")) {
                    for (UUID uuid : plot.getOwners()) {
                        PlotPlayer owner = UUIDHandler.getPlayer(uuid);
                        if ((owner != null) && !owner.getUUID().equals(pp.getUUID())) {
                            MainUtil.sendMessage(pp, C.NOTIFY_LEAVE.s().replace("%player", pp.getName()).replace("%plot", plot.getId().toString()));
                        }
                    }
                }
            }
            if (plot.getFlag(Flags.FLY).isPresent()) {
                PlotGameMode gamemode = pp.getGameMode();
                if (gamemode == PlotGameMode.SURVIVAL || (gamemode == PlotGameMode.ADVENTURE)) {
                    pp.setFlight(false);
                }
            }
            if (plot.getFlag(Flags.TIME).isPresent()) {
                pp.setTime(Long.MAX_VALUE);
            }
            if (plot.getFlag(Flags.WEATHER).isPresent()) {
                pp.setWeather(PlotWeather.RESET);
            }
            Location lastLoc = pp.getMeta("music");
            if (lastLoc != null) {
                pp.deleteMeta("music");
                pp.playMusic(lastLoc, 0);
            }
        }
        return true;
    }
}
