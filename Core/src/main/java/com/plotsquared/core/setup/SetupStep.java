/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.setup;

import com.plotsquared.core.command.Command;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.player.PlotPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface SetupStep {

    /**
     * Handles the input for this setup step.
     *
     * @param plotPlayer the plot player executing the command
     * @param builder    the plot area builder to work on
     * @param argument   the argument given as input
     * @return the next step if input was valid, this setup step otherwise
     */
    SetupStep handleInput(final PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument);

    @Nonnull Collection<String> getSuggestions();

    @Nullable String getDefaultValue();

    /**
     * Announces this step to the player.
     *
     * @param plotPlayer the player to announce this step to.
     */
    void announce(PlotPlayer<?> plotPlayer);

    /**
     * Creates a collection of suggestions for the current input.
     *
     * @param plotPlayer the player to receive the suggestions.
     * @param argument the argument already typed.
     * @return a collection of suggestions.
     */
    default Collection<Command> createSuggestions(final PlotPlayer<?> plotPlayer, String argument) {
        List<Command> result = new ArrayList<>(getSuggestions().size());
        for (String suggestion : getSuggestions()) {
            if (suggestion.startsWith(argument)) {
                result.add(new Command(null, false, suggestion, "", RequiredType.NONE, null) {});
            }
        }
        return result;
    }

    /**
     * This method is called when the SetupProcess reverts to a previous step.
     *
     * @param builder the builder associated with the setup process.
     */
    default void onBack(PlotAreaBuilder builder) {

    }
}
