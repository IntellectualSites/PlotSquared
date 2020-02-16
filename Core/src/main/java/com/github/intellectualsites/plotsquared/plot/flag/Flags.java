package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;

public final class Flags {

    public static final IntervalFlag FEED = new IntervalFlag("feed");
    public static final IntervalFlag HEAL = new IntervalFlag("heal");
    public static final GameModeFlag GAMEMODE = new GameModeFlag("gamemode");
    public static final GameModeFlag GUEST_GAMEMODE = new GameModeFlag("guest-gamemode");
    public static final LongFlag TIME = new LongFlag("time");
    public static final DoubleFlag PRICE = new DoubleFlag("price") {
        @Override public Double parseValue(String input) {
            Double value = super.parseValue(input);
            return value != null && value > 0 ? value : null;
        }

        @Override public String getValueDescription() {
            return Captions.FLAG_ERROR_PRICE.getTranslated();
        }
    };
    public static final StringListFlag BLOCKED_CMDS = new StringListFlag("blocked-cmds");
    public static final BlockStateListFlag USE = new BlockStateListFlag("use");
    public static final BlockStateListFlag BREAK = new BlockStateListFlag("break");
    public static final BlockStateListFlag PLACE = new BlockStateListFlag("place");
    public static final IntegerFlag MISC_CAP = new IntegerFlag("misc-cap");
    public static final IntegerFlag ENTITY_CAP = new IntegerFlag("entity-cap");
    public static final IntegerFlag MOB_CAP = new IntegerFlag("mob-cap");
    public static final IntegerFlag ANIMAL_CAP = new IntegerFlag("animal-cap");
    public static final IntegerFlag HOSTILE_CAP = new IntegerFlag("hostile-cap");
    public static final IntegerFlag VEHICLE_CAP = new IntegerFlag("vehicle-cap");
    public static final Flag<?> KEEP = new Flag(Captions.FLAG_CATEGORY_MIXED, "keep") {
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
            return Captions.FLAG_ERROR_KEEP.getTranslated();
        }
    };

    public static final TeleportDenyFlag DENY_TELEPORT = new TeleportDenyFlag("deny-teleport");

}
