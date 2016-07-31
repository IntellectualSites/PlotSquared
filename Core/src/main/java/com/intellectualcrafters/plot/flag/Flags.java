package com.intellectualcrafters.plot.flag;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public final class Flags {

    public static final IntegerFlag MUSIC = new IntegerFlag("music");
    public static final StringFlag DESCRIPTION = new StringFlag("description");
    public static final IntegerListFlag ANALYSIS = (IntegerListFlag) new IntegerListFlag("analysis").reserve();
    public static final StringFlag GREETING = new StringFlag("greeting");
    public static final StringFlag FAREWELL = new StringFlag("farewell");
    public static final IntervalFlag FEED = new IntervalFlag("feed");
    public static final IntervalFlag HEAL = new IntervalFlag("heal");
    public static final GameModeFlag GAMEMODE = new GameModeFlag("gamemode");
    public static final GameModeFlag GUEST_GAMEMODE = new GameModeFlag("guest-gamemode");
    public static final StringFlag DONE = (StringFlag) new StringFlag("done").reserve();
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
    public static final BooleanFlag LIQUID_FLOW = new BooleanFlag("liquid-flow");
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
    public static final TeleportDenyFlag DENY_TELEPORT = new TeleportDenyFlag("deny-teleport");


    private static final HashMap<String, Flag<?>> flags;
    static {
        flags = new HashMap<>();
        try {
            for (Field field : Flags.class.getFields()) {
                String fieldName = field.getName().replace("_","-").toLowerCase();
                Object fieldValue = field.get(null);
                if (!(fieldValue instanceof Flag)) {
                    continue;
                }
                Flag flag = (Flag) fieldValue;
                if (!flag.getName().equals(fieldName)) {
                    PS.debug(Flags.class + "Field doesn't match: " + fieldName + " != " + flag.getName());
                }
                flags.put(flag.getName(), flag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get an immutable collection of registered flags.
     *
     * @return a collection of registered flags.
     */
    public static Collection<Flag<?>> getFlags() {
        return Collections.unmodifiableCollection(flags.values());
    }

    public static Flag<?> getFlag(String flag) {
    return flags.get(flag);
}

    public static void registerFlag(final Flag<?> flag) {
        final Flag<?> duplicate = flags.put(flag.getName(), flag);
        if (duplicate != null) {
            PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
                @Override public void run(PlotArea value) {
                    Object remove;
                    if (value.DEFAULT_FLAGS.containsKey(duplicate)) {
                        remove = value.DEFAULT_FLAGS.remove(duplicate);
                        value.DEFAULT_FLAGS.put(flag,flag.parseValue("" + remove));
                    }
                }
            });
            PS.get().foreachPlotRaw(new RunnableVal<Plot>() {
                @Override public void run(Plot value) {
                    if (value.getFlags().containsKey(duplicate)) {
                        Object remove = value.getFlags().remove(duplicate);
                        value.getFlags().put(flag,flag.parseValue("" + remove));
                    }
                }
            });
        }
    }
}
