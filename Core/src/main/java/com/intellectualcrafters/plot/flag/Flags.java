package com.intellectualcrafters.plot.flag;

import com.google.common.collect.Sets;

import java.util.HashSet;

public class Flags {

    public static final NumericFlag<Integer> MUSIC = new NumericFlag<>("music");
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
    public static final NumericFlag<Long> TIME = new NumericFlag<>("time");
    public static final PlotWeatherFlag WEATHER = new PlotWeatherFlag("weather");
    public static final Flag<Object> KEEP = new Flag<>("keep");
    public static final NumericFlag<Double> PRICE = new NumericFlag<>("price");
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
    public static final NumericFlag<Integer> MISC_CAP = new NumericFlag<>("misc-cap");
    public static final NumericFlag<Integer> ENTITY_CAP = new NumericFlag<>("entity-cap");
    public static final NumericFlag<Integer> MOB_CAP = new NumericFlag<>("mob-cap");
    public static final NumericFlag<Integer> ANIMAL_CAP = new NumericFlag<>("animal-cap");
    public static final NumericFlag<Integer> HOSTILE_CAP = new NumericFlag<>("hostile-cap");
    public static final NumericFlag<Integer> VEHICLE_CAP = new NumericFlag<>("vehicle-cap");
    static final HashSet<? extends Flag<?>> flags = Sets.newHashSet(MUSIC, ANIMAL_CAP, HOSTILE_CAP, PVP, PVE, NO_WORLDEDIT);

    /**
     * Get a list of registered AbstractFlag objects
     *
     * @return List (AbstractFlag)
     */
    public static HashSet<? extends Flag<?>> getFlags() {
        return flags;
    }
}
