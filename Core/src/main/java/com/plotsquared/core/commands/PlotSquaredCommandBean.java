package com.plotsquared.core.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandBean;
import cloud.commandframework.CommandProperties;
import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public abstract class PlotSquaredCommandBean extends CommandBean<PlotPlayer<?>> {

    /**
     * Returns the category of the command.
     *
     * @return the category
     */
    public abstract @NonNull CommandCategory category();

    /**
     * Returns the requirements for the command to be executable.
     *
     * @return the requirements
     */
    public abstract @NonNull Set<@NonNull CommandRequirement> requirements();

    /**
     * Prepares the given {@code builder}.
     *
     * <p>This should be implemented by abstract classes that extend {@link PlotSquaredCommandBean} to offer shared behavior
     * for a subset of plot commands.</p>
     *
     * @param builder the builder
     * @return the prepared builder
     */
    protected Command.@NonNull Builder<PlotPlayer<?>> prepare(final Command.@NonNull Builder<PlotPlayer<?>> builder) {
        return builder;
    }

    @Override
    protected final @NonNull CommandProperties properties() {
        return CommandProperties.of("platsquared", "plat");
    }

    @Override
    protected final Command.@NonNull Builder<PlotPlayer<?>> configure(final Command.@NonNull Builder<PlotPlayer<?>> builder) {
        Command.@NonNull Builder<PlotPlayer<?>> intermediaryBuilder =
                this.configurePlotCommand(this.prepare(builder.meta(PlotSquaredCommandMeta.META_CATEGORY, this.category())));
        for (final CommandRequirement requirement : this.requirements()) {
            intermediaryBuilder = intermediaryBuilder.meta(requirement.key(), true);
        }
        return intermediaryBuilder;
    }

    protected abstract Command.@NonNull Builder<PlotPlayer<?>> configurePlotCommand(
            Command.@NonNull Builder<PlotPlayer<?>> builder
    );
}
