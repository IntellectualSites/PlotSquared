package com.intellectualcrafters.plot.flag;

import com.google.common.collect.Sets;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Flags {

    public static final IntegerFlag MUSIC = new IntegerFlag("music");
    public static final StringFlag DESCRIPTION = new StringFlag("description");
    public static final IntegerListFlag ANALYSIS = new IntegerListFlag("analysis");
    public static final StringFlag GREETING = new StringFlag("greeting");
    public static final StringFlag FAREWELL = new StringFlag("farewell");
    public static final IntervalFlag FEED = new IntervalFlag("feed");
    public static final IntervalFlag HEAL = new IntervalFlag("heal");
    public static final GameModeFlag GAMEMODE = new GameModeFlag("gamemode");
    public static final StringFlag DONE = new StringFlag("done");
    public static final BooleanFlag REDSTONE = new BooleanFlag("redstone");
    public static final BooleanFlag FLY = new BooleanFlag("fly");
    public static final BooleanFlag NOTIFY_LEAVE = new BooleanFlag("notify-leave");
    public static final BooleanFlag TITLES = new BooleanFlag("titles");
    public static final BooleanFlag NOTIFY_ENTER = new BooleanFlag("notify-enter");
    public static final LongFlag TIME = new LongFlag("time");
    public static final PlotWeatherFlag WEATHER = new PlotWeatherFlag("weather");
    public static final DoubleFlag PRICE = new DoubleFlag("price") {
        @Override
        public Double parseValue(String input) {
            Double value = super.parseValue(input);
            return value != null && value > 0 ? value : null;
        }

        @Override
        public String getValueDescription() {
            return "Flag value must be a positive number.";
        }
    };
    public static final BooleanFlag EXPLOSION = new BooleanFlag("explosion");
    public static final BooleanFlag GRASS_GROW = new BooleanFlag("grass-grow");
    public static final BooleanFlag VINE_GROW = new BooleanFlag("vine-grow");
    public static final BooleanFlag MYCEL_GROW = new BooleanFlag("mycel-grow");
    public static final BooleanFlag DISABLE_PHYSICS = new BooleanFlag("disable-physics");
    public static final BooleanFlag SNOW_MELT = new BooleanFlag("snow-melt");
    public static final BooleanFlag ICE_MELT = new BooleanFlag("ice-melt");
    public static final BooleanFlag FIRE_SPREAD = new BooleanFlag("fire-spread");
    public static final BooleanFlag BLOCK_BURN = new BooleanFlag("block-burn");
    public static final BooleanFlag BLOCK_IGNITION = new BooleanFlag("block-ignition");
    public static final BooleanFlag SOIL_DRY = new BooleanFlag("soil-dry");
    public static final StringListFlag BLOCKED_CMDS = new StringListFlag("blocked-cmds");
    public static final PlotBlockListFlag USE = new PlotBlockListFlag("use");
    public static final PlotBlockListFlag BREAK = new PlotBlockListFlag("break");
    public static final PlotBlockListFlag PLACE = new PlotBlockListFlag("place");
    public static final BooleanFlag DEVICE_INTERACT = new BooleanFlag("device-interact");
    public static final BooleanFlag VEHICLE_BREAK = new BooleanFlag("vehicle-break");
    public static final BooleanFlag VEHICLE_PLACE = new BooleanFlag("vehicle-place");
    public static final BooleanFlag VEHICLE_USE = new BooleanFlag("vehicle-use");
    public static final BooleanFlag HANGING_BREAK = new BooleanFlag("hanging-break");
    public static final BooleanFlag HANGING_PLACE = new BooleanFlag("hanging-place");
    public static final BooleanFlag HANGING_INTERACT = new BooleanFlag("hanging-interact");
    public static final BooleanFlag MISC_PLACE = new BooleanFlag("misc-place");
    public static final BooleanFlag MISC_BREAK = new BooleanFlag("misc-break");
    public static final BooleanFlag MISC_INTERACT = new BooleanFlag("misc-interact");
    public static final BooleanFlag PLAYER_INTERACT = new BooleanFlag("player-interact");
    public static final BooleanFlag TAMED_ATTACK = new BooleanFlag("tamed-attack");
    public static final BooleanFlag TAMED_INTERACT = new BooleanFlag("tamed-interact");
    public static final BooleanFlag ANIMAL_ATTACK = new BooleanFlag("animal-attack");
    public static final BooleanFlag ANIMAL_INTERACT = new BooleanFlag("animal-interact");
    public static final BooleanFlag HOSTILE_ATTACK = new BooleanFlag("hostile-attack");
    public static final BooleanFlag HOSTILE_INTERACT = new BooleanFlag("hostile-interact");
    public static final BooleanFlag MOB_PLACE = new BooleanFlag("mob-place");
    public static final BooleanFlag FORCEFIELD = new BooleanFlag("forcefield");
    public static final BooleanFlag INVINCIBLE = new BooleanFlag("invincible");
    public static final BooleanFlag ITEM_DROP = new BooleanFlag("item-drop");
    public static final BooleanFlag INSTABREAK = new BooleanFlag("instabreak");
    public static final BooleanFlag DROP_PROTECTION = new BooleanFlag("drop-protection");
    public static final BooleanFlag PVP = new BooleanFlag("pvp");
    public static final BooleanFlag PVE = new BooleanFlag("pve");
    public static final BooleanFlag NO_WORLDEDIT = new BooleanFlag("no-worldedit");
    public static final IntegerFlag MISC_CAP = new IntegerFlag("misc-cap");
    public static final IntegerFlag ENTITY_CAP = new IntegerFlag("entity-cap");
    public static final IntegerFlag MOB_CAP = new IntegerFlag("mob-cap");
    public static final IntegerFlag ANIMAL_CAP = new IntegerFlag("animal-cap");
    public static final IntegerFlag HOSTILE_CAP = new IntegerFlag("hostile-cap");
    public static final IntegerFlag VEHICLE_CAP = new IntegerFlag("vehicle-cap");
    public static final Flag<?> KEEP = new Flag("keep") {
        @Override public String valueToString(Object value) {
            return value.toString();
        }

        @Override public Object parseValue(String value) {
            if (MathMan.isInteger(value)) {
                return Long.parseLong(value);
            }
            switch (value.toLowerCase()) {
                case "true":
                    return true;
                case "false":
                    return false;
                default:
                    return MainUtil.timeToSec(value) * 1000 + System.currentTimeMillis();
            }
        }

        @Override public String getValueDescription() {
            return "Flag value must a timestamp or a boolean";
        }
    };
    public static final BooleanFlag SLEEP = new BooleanFlag("sleep");

    private static final HashSet<Flag<?>> flags = Sets.newHashSet(MUSIC, DESCRIPTION, ANALYSIS, GREETING, FAREWELL, FEED, HEAL,
            GAMEMODE,
            DONE,
            REDSTONE,
            FLY, NOTIFY_LEAVE, NOTIFY_ENTER, TIME, WEATHER, KEEP, PRICE, EXPLOSION, GRASS_GROW, VINE_GROW, MYCEL_GROW, DISABLE_PHYSICS, SNOW_MELT,
            ICE_MELT,
            FIRE_SPREAD, BLOCK_BURN, BLOCK_IGNITION, SOIL_DRY, BLOCKED_CMDS, USE, BREAK, PLACE, DEVICE_INTERACT, VEHICLE_BREAK, VEHICLE_PLACE,
            VEHICLE_USE,
            HANGING_BREAK, HANGING_PLACE, HANGING_INTERACT, MISC_PLACE, MISC_BREAK, MISC_INTERACT, PLAYER_INTERACT, TAMED_ATTACK, TAMED_INTERACT,
            ANIMAL_ATTACK, ANIMAL_INTERACT, HOSTILE_ATTACK, HOSTILE_INTERACT, MOB_PLACE, FORCEFIELD, INVINCIBLE, ITEM_DROP, INSTABREAK,
            DROP_PROTECTION, PVP,
            PVE, NO_WORLDEDIT, MISC_CAP, ENTITY_CAP, MOB_CAP, ANIMAL_CAP, HOSTILE_CAP, VEHICLE_CAP);

    /**
     * Get an immutable set of registered flags.
     *
     * @return a set of registered flags.
     */
    public static Set<Flag<?>> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    public static void registerFlag(final Flag<?> flag) {
        Iterator<Flag<?>> iterator = flags.iterator();
        Flag<?> duplicate = null;
        while (iterator.hasNext()){
            duplicate = iterator.next();
            if (flag.getName().equalsIgnoreCase(duplicate.getName())) {
                iterator.remove();
                flags.add(flag);
                break;
            }
        }
        final Flag<?> dupFinal = duplicate;
        PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override public void run(PlotArea value) {
                if (dupFinal != null) {
                    Object remove = null;
                    if (value.DEFAULT_FLAGS.containsKey(dupFinal)) {
                        remove = value.DEFAULT_FLAGS.remove(dupFinal);
                    }
                    if (!(remove instanceof String)) {
                        //error message? maybe?
                        return;
                    }
                    value.DEFAULT_FLAGS.put(flag,flag.parseValue((String) remove));
                }
            }
        });
        PS.get().foreachPlotRaw(new RunnableVal<Plot>() {
            @Override public void run(Plot value) {
                if (dupFinal != null) {
                    Object remove = null;
                    if (value.getFlags().containsKey(dupFinal)) {
                        remove = value.getFlags().remove(dupFinal);
                    }
                    if (!(remove instanceof String)) {
                        //error message? maybe?
                        return;
                    }
                    value.getFlags().put(flag,flag.parseValue((String) remove));
                }
            }
        });
    }
}
