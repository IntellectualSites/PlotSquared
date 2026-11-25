/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.listener;

import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.plot.flag.implementations.FlyFlag;
import com.plotsquared.core.plot.flag.implementations.GamemodeFlag;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * Covers what the fly, gamemode and guest-gamemode flags do to a player who enters and leaves a plot, in particular
 * when the flag is removed while the player is still standing in the plot.
 */
class PlotListenerFlightAndGameModeTest {

    private static final GameMode AREA_GAME_MODE = GameModes.SURVIVAL;
    private static final GameMode NO_GAME_MODE = GamemodeFlag.DEFAULT;
    private static final FlyFlag.FlyStatus NO_FLY = FlyFlag.FlyStatus.DEFAULT;
    private static final String BYPASS = "plots.gamemode.bypass";
    private static final String BYPASS_MESSAGE = "gamemode.gamemode_was_bypassed";

    @Test
    @DisplayName("Entering a plot with a gamemode flag switches the player to that game mode")
    void enteringPlotWithGameModeFlagSwitchesGameMode() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        Assertions.assertEquals(GameModes.CREATIVE, player.getGameMode(), "the gamemode flag must be applied on entry");
    }

    @Test
    @DisplayName("Entering a plot with a gamemode flag remembers that the plot changed the game mode")
    void enteringPlotWithGameModeFlagRemembersTheChange() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        Assertions.assertTrue(gameModeRemembered(player), "a game mode change made by a plot flag must be remembered");
    }

    @Test
    @DisplayName("Leaving a plot whose gamemode flag was removed meanwhile restores the area game mode")
    void leavingPlotAfterGameModeFlagWasRemovedRestoresAreaGameMode() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        leaveOwnedPlot(player, NO_FLY, NO_GAME_MODE, NO_GAME_MODE);
        Assertions.assertEquals(
                AREA_GAME_MODE,
                player.getGameMode(),
                "the game mode set by a plot flag must not survive leaving the plot after the flag was removed"
        );
    }

    @Test
    @DisplayName("Leaving a plot whose guest-gamemode flag was removed meanwhile restores the area game mode of a visitor")
    void leavingPlotAfterGuestGameModeFlagWasRemovedRestoresAreaGameModeOfVisitor() {
        final FakePlotPlayer visitor = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(visitor, NO_FLY, NO_GAME_MODE, GameModes.CREATIVE, false);
        Assertions.assertEquals(GameModes.CREATIVE, visitor.getGameMode(), "the guest-gamemode flag must be applied to a visitor");
        leaveOwnedPlot(visitor, NO_FLY, NO_GAME_MODE, NO_GAME_MODE);
        Assertions.assertEquals(
                AREA_GAME_MODE,
                visitor.getGameMode(),
                "the game mode set by a guest-gamemode flag must not survive leaving the plot after the flag was removed"
        );
    }

    @Test
    @DisplayName("Entering the plot again after its gamemode flag was removed restores the area game mode at once")
    void reenteringPlotAfterGameModeFlagWasRemovedRestoresAreaGameMode() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        enter(player, NO_FLY, NO_GAME_MODE, NO_GAME_MODE, true);
        Assertions.assertEquals(
                AREA_GAME_MODE,
                player.getGameMode(),
                "entering a plot without gamemode flags must undo the game mode an earlier plot flag set"
        );
    }

    @Test
    @DisplayName("Leaving a plot that lost its owner restores the area game mode")
    void leavingUnownedPlotRestoresAreaGameMode() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        leaveUnownedPlot(player);
        Assertions.assertEquals(
                AREA_GAME_MODE,
                player.getGameMode(),
                "the game mode set by a plot flag must not survive the plot being unclaimed or deleted"
        );
    }

    @Test
    @DisplayName("Leaving a plot that still carries its gamemode flag restores the area game mode")
    void leavingPlotWithGameModeFlagRestoresAreaGameMode() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        leaveOwnedPlot(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE);
        Assertions.assertEquals(AREA_GAME_MODE, player.getGameMode(), "leaving a plot with a gamemode flag must restore the area game mode");
    }

    @Test
    @DisplayName("A restored game mode is no longer remembered")
    void restoredGameModeIsNoLongerRemembered() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        leaveOwnedPlot(player, NO_FLY, NO_GAME_MODE, NO_GAME_MODE);
        Assertions.assertFalse(gameModeRemembered(player), "nothing must stay remembered once the game mode was restored");
    }

    @Test
    @DisplayName("The guest-gamemode flag is not applied to a player who is added to the plot")
    void guestGameModeFlagIsNotAppliedToAddedPlayer() {
        final FakePlotPlayer member = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(member, NO_FLY, NO_GAME_MODE, GameModes.ADVENTURE, true);
        Assertions.assertEquals(GameModes.SURVIVAL, member.getGameMode(), "the guest-gamemode flag must only affect players who are not added");
    }

    @Test
    @DisplayName("A player with the bypass permission keeps their game mode when entering a plot with a gamemode flag")
    void playerWithBypassKeepsGameModeOnEntry() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        player.grantPermission(BYPASS);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        Assertions.assertEquals(GameModes.SURVIVAL, player.getGameMode(), "the bypass permission must keep the gamemode flag from being applied");
    }

    @Test
    @DisplayName("A player with the bypass permission is told that the gamemode flag was bypassed")
    void playerWithBypassIsToldAboutTheBypass() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        player.grantPermission(BYPASS);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        Assertions.assertEquals(List.of(BYPASS_MESSAGE), player.receivedMessages(), "a bypassed gamemode flag must be reported to the player");
    }

    @Test
    @DisplayName("A bypassed gamemode flag is not remembered as a change")
    void bypassedGameModeFlagIsNotRemembered() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        player.grantPermission(BYPASS);
        enter(player, NO_FLY, GameModes.CREATIVE, NO_GAME_MODE, true);
        Assertions.assertFalse(gameModeRemembered(player), "a game mode the plot did not change must not be remembered");
    }

    @Test
    @DisplayName("A player whose game mode no plot changed keeps it when entering a plot without flags")
    void untouchedPlayerKeepsGameModeWhenEnteringPlotWithoutFlags() {
        final FakePlotPlayer admin = new FakePlotPlayer(GameModes.CREATIVE);
        enter(admin, NO_FLY, NO_GAME_MODE, NO_GAME_MODE, false);
        Assertions.assertEquals(GameModes.CREATIVE, admin.getGameMode(), "a plot without gamemode flags must not change a game mode it never set");
    }

    @Test
    @DisplayName("A player whose game mode no plot changed keeps it when leaving a plot without flags")
    void untouchedPlayerKeepsGameModeWhenLeavingPlotWithoutFlags() {
        final FakePlotPlayer admin = new FakePlotPlayer(GameModes.CREATIVE);
        enter(admin, NO_FLY, NO_GAME_MODE, NO_GAME_MODE, false);
        leaveOwnedPlot(admin, NO_FLY, NO_GAME_MODE, NO_GAME_MODE);
        Assertions.assertEquals(GameModes.CREATIVE, admin.getGameMode(), "leaving a plot without gamemode flags must not change a game mode it never set");
    }

    @Test
    @DisplayName("A player whose game mode no plot changed keeps it when leaving a plot without owner")
    void untouchedPlayerKeepsGameModeWhenLeavingUnownedPlot() {
        final FakePlotPlayer admin = new FakePlotPlayer(GameModes.CREATIVE);
        leaveUnownedPlot(admin);
        Assertions.assertEquals(GameModes.CREATIVE, admin.getGameMode(), "leaving an unowned plot must not change a game mode no plot set");
    }

    @Test
    @DisplayName("Entering a plot with fly enabled lets the player fly")
    void enteringPlotWithFlyEnabledEnablesFlight() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        Assertions.assertTrue(player.getFlight(), "the fly flag must be applied on entry");
    }

    @Test
    @DisplayName("Entering a plot with fly enabled remembers the flight state the player had before")
    void enteringPlotWithFlyEnabledRemembersPreviousFlight() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        Assertions.assertEquals(Optional.of(false), rememberedFlight(player), "the flight state before the fly flag was applied must be remembered");
    }

    @Test
    @DisplayName("Leaving a plot whose fly flag was removed meanwhile restores the flight state from before")
    void leavingPlotAfterFlyFlagWasRemovedRestoresFlight() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        leaveOwnedPlot(player, NO_FLY, NO_GAME_MODE, NO_GAME_MODE);
        Assertions.assertFalse(player.getFlight(), "flight granted by a plot flag must not survive leaving the plot after the flag was removed");
    }

    @Test
    @DisplayName("Entering the plot again after its fly flag was removed restores the flight state at once")
    void reenteringPlotAfterFlyFlagWasRemovedRestoresFlight() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        enter(player, NO_FLY, NO_GAME_MODE, NO_GAME_MODE, true);
        Assertions.assertFalse(player.getFlight(), "entering a plot without fly flag must undo the flight an earlier plot flag granted");
    }

    @Test
    @DisplayName("Leaving a plot that lost its owner restores the flight state from before")
    void leavingUnownedPlotRestoresFlight() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        leaveUnownedPlot(player);
        Assertions.assertFalse(player.getFlight(), "flight granted by a plot flag must not survive the plot being unclaimed or deleted");
    }

    @Test
    @DisplayName("Leaving a plot that still carries its fly flag restores the flight state from before")
    void leavingPlotWithFlyFlagRestoresFlight() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        leaveOwnedPlot(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE);
        Assertions.assertFalse(player.getFlight(), "leaving a plot with a fly flag must restore the flight state from before");
    }

    @Test
    @DisplayName("Entering the plot a second time does not overwrite the remembered flight state")
    void rememberedFlightIsNotOverwrittenOnSecondEntry() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        Assertions.assertEquals(
                Optional.of(false),
                rememberedFlight(player),
                "the flight state from before the first entry must stay remembered, not the one the flag granted"
        );
    }

    @Test
    @DisplayName("A flight state remembered earlier is not overwritten by a plot that disables flight")
    void rememberedFlightIsNotOverwrittenByDisablingPlot() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        enter(player, FlyFlag.FlyStatus.DISABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        Assertions.assertEquals(Optional.of(false), rememberedFlight(player), "the first remembered flight state must be kept");
    }

    @Test
    @DisplayName("A restored flight state is no longer remembered")
    void restoredFlightIsNoLongerRemembered() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, NO_GAME_MODE, NO_GAME_MODE, true);
        leaveOwnedPlot(player, NO_FLY, NO_GAME_MODE, NO_GAME_MODE);
        Assertions.assertEquals(Optional.empty(), rememberedFlight(player), "nothing must stay remembered once flight was restored");
    }

    @Test
    @DisplayName("A player with the admin flight permission is not affected by the fly flag")
    void playerWithAdminFlightPermissionIsNotAffectedByFlyFlag() {
        final FakePlotPlayer admin = new FakePlotPlayer(GameModes.CREATIVE);
        admin.grantPermission(Permission.PERMISSION_ADMIN_FLIGHT.toString());
        enter(admin, FlyFlag.FlyStatus.DISABLED, NO_GAME_MODE, NO_GAME_MODE, false);
        Assertions.assertTrue(admin.getFlight(), "the fly flag must not change flight of a player with the admin flight permission");
    }

    @Test
    @DisplayName("Game mode and flight from one plot are both restored after both flags were removed")
    void gameModeAndFlightAreBothRestoredAfterBothFlagsWereRemoved() {
        final FakePlotPlayer player = new FakePlotPlayer(GameModes.SURVIVAL);
        enter(player, FlyFlag.FlyStatus.ENABLED, GameModes.CREATIVE, NO_GAME_MODE, true);
        leaveOwnedPlot(player, NO_FLY, NO_GAME_MODE, NO_GAME_MODE);
        Assertions.assertEquals(
                List.of(AREA_GAME_MODE, false),
                List.of(player.getGameMode(), player.getFlight()),
                "game mode and flight set by plot flags must both be undone after the flags were removed"
        );
    }

    /**
     * What {@link PlotListener#plotEntry} does with the fly and gamemode flags of an owned plot.
     */
    private static void enter(
            final FakePlotPlayer player,
            final FlyFlag.FlyStatus flyStatus,
            final GameMode gameMode,
            final GameMode guestGameMode,
            final boolean added
    ) {
        PlotListener.applyFlightAndGameMode(player, flyStatus, gameMode, guestGameMode, added, AREA_GAME_MODE, "1;1", "world;1;1");
    }

    /**
     * What {@link PlotListener#plotExit} does with the fly and gamemode flags of an owned plot, for a player who walks
     * onto the road.
     */
    private static void leaveOwnedPlot(
            final FakePlotPlayer player,
            final FlyFlag.FlyStatus flyStatus,
            final GameMode gameMode,
            final GameMode guestGameMode
    ) {
        PlotListener.resetGameModeOnExit(player, gameMode, guestGameMode, AREA_GAME_MODE, "world;1;1");
        PlotListener.restoreFlightOnExit(player, flyStatus, FlyFlag.FlyStatus.DEFAULT);
    }

    /**
     * What {@link PlotListener#plotExit} does for a plot without owner.
     */
    private static void leaveUnownedPlot(final FakePlotPlayer player) {
        PlotListener.restoreRememberedState(player, AREA_GAME_MODE, "world;1;1");
    }

    private static boolean gameModeRemembered(final FakePlotPlayer player) {
        try (final MetaDataAccess<Boolean> access = player.accessPersistentMetaData(PlayerMetaDataKeys.PERSISTENT_PLOT_GAMEMODE)) {
            return access.isPresent();
        }
    }

    private static Optional<Boolean> rememberedFlight(final FakePlotPlayer player) {
        try (final MetaDataAccess<Boolean> access = player.accessPersistentMetaData(PlayerMetaDataKeys.PERSISTENT_FLIGHT)) {
            return access.get();
        }
    }

}
