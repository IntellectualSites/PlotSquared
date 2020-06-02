package com.plotsquared.core.setup;

import com.plotsquared.core.player.PlotPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface SetupStep {

    /**
     * Handles the input for this setup step.
     *
     * @param plotPlayer the plot player executing the command
     * @param builder    the plot area builder to work on
     * @param argument   the argument given as input
     * @return the next step if input was valid, this setup step otherwise
     */
    SetupStep handleInput(final PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument);

    @NotNull Collection<String> getSuggestions();

    @Nullable String getDefaultValue();

    /**
     * Announces this step to the player.
     *
     * @param plotPlayer the player to announce this step to.
     */
    void announce(PlotPlayer plotPlayer);

    /**
     * This method is called when the SetupProcess reverts to a previous step.
     *
     * @param builder the builder associated with the setup process.
     */
    default void onBack(PlotAreaBuilder builder) {

    }
}
