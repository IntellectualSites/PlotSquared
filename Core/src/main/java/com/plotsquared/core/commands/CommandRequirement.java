package com.plotsquared.core.commands;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKeyHolder;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Something that is required for a command to be executed.
 */
public interface CommandRequirement extends CloudKeyHolder<Boolean> {

    /**
     * Returns the caption sent when the requirement is not met.
     *
     * @return the caption
     */
    @NonNull TranslatableCaption failureCaption();

    /**
     * Evaluates whether the requirement is met.
     *
     * @param context command context to evaluate
     * @return {@code true} if the requirement is met, else {@code false}
     */
    boolean evaluate(final @NonNull CommandContext<PlotPlayer<?>> context);
}
