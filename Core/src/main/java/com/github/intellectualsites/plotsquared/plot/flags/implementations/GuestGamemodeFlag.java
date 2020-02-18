package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import org.jetbrains.annotations.NotNull;

public class GuestGamemodeFlag extends PlotFlag<GameMode, GuestGamemodeFlag> {

    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_CREATIVE =
        new GuestGamemodeFlag(GameModes.CREATIVE);
    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_ADVENTURE =
        new GuestGamemodeFlag(GameModes.ADVENTURE);
    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_SPECTATOR =
        new GuestGamemodeFlag(GameModes.SPECTATOR);
    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_SURVIVAL =
        new GuestGamemodeFlag(GameModes.SURVIVAL);
    public static final GuestGamemodeFlag GUEST_GAMEMODE_FLAG_DEFAULT =
        new GuestGamemodeFlag(GamemodeFlag.DEFAULT);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected GuestGamemodeFlag(@NotNull GameMode value) {
        super(value, Captions.FLAG_CATEGORY_GAMEMODE, Captions.FLAG_DESCRIPTION_GUEST_GAMEMODE);
    }

    @Override public GuestGamemodeFlag parse(@NotNull String input) throws FlagParseException {
        switch (input) {
            case "creative":
            case "c":
            case "1":
                return flagOf(GameModes.CREATIVE);
            case "adventure":
            case "a":
            case "2":
                return flagOf(GameModes.ADVENTURE);
            case "spectator":
            case "sp":
            case "3":
                return flagOf(GameModes.SPECTATOR);
            case "survival":
            case "s":
            case "0":
                return flagOf(GameModes.SURVIVAL);
            default:
                return flagOf(GamemodeFlag.DEFAULT);
        }
    }

    @Override public GuestGamemodeFlag merge(@NotNull GameMode newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return getValue().getId();
    }

    @Override public String getExample() {
        return "survival";
    }

    @Override protected GuestGamemodeFlag flagOf(@NotNull GameMode value) {
        switch (value.getId()) {
            case "creative":
                return GUEST_GAMEMODE_FLAG_CREATIVE;
            case "adventure":
                return GUEST_GAMEMODE_FLAG_ADVENTURE;
            case "spectator":
                return GUEST_GAMEMODE_FLAG_SPECTATOR;
            case "survival":
                return GUEST_GAMEMODE_FLAG_SURVIVAL;
            default:
                return GUEST_GAMEMODE_FLAG_DEFAULT;
        }
    }

}
