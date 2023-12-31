package com.plotsquared.core.commands.injection;

import cloud.commandframework.annotations.AnnotationAccessor;
import cloud.commandframework.annotations.injection.ParameterInjector;
import cloud.commandframework.context.CommandContext;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link ParameterInjector} that returns the current plot of the player.
 */
public final class PlotInjector implements ParameterInjector<PlotPlayer<?>, Plot> {

    @Override
    public @Nullable Plot create(
            final @NonNull CommandContext<PlotPlayer<?>> context,
            final @NonNull AnnotationAccessor annotationAccessor
    ) {
        // TODO: Allow for overriding for console.
        return context.sender().getCurrentPlot();
    }
}
