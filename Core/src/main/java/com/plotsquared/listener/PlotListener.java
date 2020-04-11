package com.plotsquared.listener;

import com.plotsquared.PlotSquared;
import com.plotsquared.plot.PlotWeather;
import com.plotsquared.config.Captions;
import com.plotsquared.config.Settings;
import com.plotsquared.events.PlotFlagRemoveEvent;
import com.plotsquared.events.Result;
import com.plotsquared.plot.flags.GlobalFlagContainer;
import com.plotsquared.plot.flags.PlotFlag;
import com.plotsquared.plot.flags.implementations.DenyExitFlag;
import com.plotsquared.plot.flags.implementations.FarewellFlag;
import com.plotsquared.plot.flags.implementations.FeedFlag;
import com.plotsquared.plot.flags.implementations.FlyFlag;
import com.plotsquared.plot.flags.implementations.GamemodeFlag;
import com.plotsquared.plot.flags.implementations.GreetingFlag;
import com.plotsquared.plot.flags.implementations.GuestGamemodeFlag;
import com.plotsquared.plot.flags.implementations.HealFlag;
import com.plotsquared.plot.flags.implementations.MusicFlag;
import com.plotsquared.plot.flags.implementations.NotifyEnterFlag;
import com.plotsquared.plot.flags.implementations.NotifyLeaveFlag;
import com.plotsquared.plot.flags.implementations.TimeFlag;
import com.plotsquared.plot.flags.implementations.TitlesFlag;
import com.plotsquared.plot.flags.implementations.WeatherFlag;
import com.plotsquared.plot.flags.types.TimedFlag;
import com.plotsquared.location.Location;
import com.plotsquared.plot.Plot;
import com.plotsquared.plot.PlotArea;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.plot.comment.CommentManager;
import com.plotsquared.util.ByteArrayUtilities;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.Permissions;
import com.plotsquared.util.StringMan;
import com.plotsquared.util.WorldUtil;
import com.plotsquared.util.uuid.UUIDHandler;
import com.plotsquared.util.tasks.RunnableVal;
import com.plotsquared.plot.expiration.ExpireManager;
import com.plotsquared.util.tasks.TaskManager;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PlotListener {

    private static final HashMap<UUID, Interval> feedRunnable = new HashMap<>();
    private static final HashMap<UUID, Interval> healRunnable = new HashMap<>();

    public static void startRunnable() {
        TaskManager.runTaskRepeat(() -> {
            if (!healRunnable.isEmpty()) {
                for (Iterator<Map.Entry<UUID, Interval>> iterator =
                     healRunnable.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<UUID, Interval> entry = iterator.next();
                    Interval value = entry.getValue();
                    ++value.count;
                    if (value.count == value.interval) {
                        value.count = 0;
                        PlotPlayer player = WorldUtil.IMP.wrapPlayer(entry.getKey());
                        if (player == null) {
                            iterator.remove();
                            continue;
                        }
                        double level = WorldUtil.IMP.getHealth(player);
                        if (level != value.max) {
                            WorldUtil.IMP.setHealth(player, Math.min(level + value.amount, value.max));
                        }
                    }
                }
            }
            if (!feedRunnable.isEmpty()) {
                for (Iterator<Map.Entry<UUID, Interval>> iterator =
                     feedRunnable.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<UUID, Interval> entry = iterator.next();
                    Interval value = entry.getValue();
                    ++value.count;
                    if (value.count == value.interval) {
                        value.count = 0;
                        PlotPlayer player = WorldUtil.IMP.wrapPlayer(entry.getKey());
                        if (player == null) {
                            iterator.remove();
                            continue;
                        }
                        int level = WorldUtil.IMP.getFoodLevel(player);
                        if (level != value.max) {
                            WorldUtil.IMP.setFoodLevel(player, Math.min(level + value.amount, value.max));
                        }
                    }
                }
            }
        }, 20);
    }

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
        PlotSquared.get().getEventDispatcher().callEntry(player, plot);
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

            final FlyFlag.FlyStatus flyStatus = plot.getFlag(FlyFlag.class);
            if (flyStatus != FlyFlag.FlyStatus.DEFAULT) {
                boolean flight = player.getFlight();
                GameMode gamemode = player.getGameMode();
                if (flight != (gamemode == GameModes.CREATIVE
                    || gamemode == GameModes.SPECTATOR)) {
                    player.setPersistentMeta("flight",
                        ByteArrayUtilities.booleanToBytes(player.getFlight()));
                }
                player.setFlight(flyStatus == FlyFlag.FlyStatus.ENABLED);
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
                    PlotFlag<?, ?> plotFlag =
                        GlobalFlagContainer.getInstance().getFlag(TimeFlag.class);
                    PlotFlagRemoveEvent event =
                        PlotSquared.get().getEventDispatcher().callFlagRemove(plotFlag, plot);
                    if (event.getEventResult() != Result.DENY) {
                        plot.removeFlag(event.getFlag());
                    }
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

            TimedFlag.Timed<Integer> feed = plot.getFlag(FeedFlag.class);
            if (feed != null && feed.getInterval() != 0 && feed.getValue() != 0) {
                feedRunnable.put(player.getUUID(), new Interval(feed.getInterval(), feed.getValue(), 20));
            }
            TimedFlag.Timed<Integer> heal = plot.getFlag(HealFlag.class);
            if (heal != null && heal.getInterval() != 0 && heal.getValue() != 0) {
                healRunnable.put(player.getUUID(), new Interval(heal.getInterval(), heal.getValue(), 20));
            }
            return true;
        }
        return true;
    }

    public static boolean plotExit(final PlotPlayer player, Plot plot) {
        Object previous = player.deleteMeta(PlotPlayer.META_LAST_PLOT);
        PlotSquared.get().getEventDispatcher().callLeave(player, plot);
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
                if (player.getGameMode() != pw.getGameMode()) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(pw.getGameMode());
                    } else {
                        MainUtil.sendMessage(player, StringMan
                            .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(), "{plot}",
                                plot.toString(), "{gamemode}",
                                pw.getGameMode().getName().toLowerCase()));
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

            final FlyFlag.FlyStatus flyStatus = plot.getFlag(FlyFlag.class);
            if (flyStatus != FlyFlag.FlyStatus.DEFAULT) {
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
            if (plotWeather != PlotWeather.CLEAR) {
                player.setWeather(PlotWeather.RESET);
            }

            Location lastLoc = player.getMeta("music");
            if (lastLoc != null) {
                player.deleteMeta("music");
                player.playMusic(lastLoc, ItemTypes.AIR);
            }

            feedRunnable.remove(player.getUUID());
            healRunnable.remove(player.getUUID());
        }
        return true;
    }

    public static void logout(UUID uuid) {
        feedRunnable.remove(uuid);
        healRunnable.remove(uuid);
    }

    private static class Interval {

        final int interval;
        final int amount;
        final int max;
        int count = 0;

        Interval(int interval, int amount, int max) {
            this.interval = interval;
            this.amount = amount;
            this.max = max;
        }
    }
}
