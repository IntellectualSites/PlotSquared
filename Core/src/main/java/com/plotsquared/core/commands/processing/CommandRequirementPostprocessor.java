package com.plotsquared.core.commands.processing;

import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.services.types.ConsumerService;
import com.plotsquared.core.commands.CommandRequirement;
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Processor that evaluates registered {@link CommandRequirement command requirements} before a command is executed.
 */
public final class CommandRequirementPostprocessor implements CommandPostprocessor<PlotPlayer<?>> {

    private final Collection<@NonNull CommandRequirement> requirements = new ArrayList<>();

    /**
     * Requires a single requirement.
     *
     * @param requirement the requirement
     */
    public void registerRequirement(final @NonNull CommandRequirement requirement) {
        this.requirements.add(Objects.requireNonNull(requirement, "requirement"));
    }

    /**
     * Registers the given {@code requirements}.
     *
     * @param requirements the requirements
     */
    public void registerRequirements(final @NonNull Collection<@NonNull CommandRequirement> requirements) {
        requirements.forEach(this::registerRequirement);
    }

    /**
     * Registers the given {@code requirements}.
     *
     * @param requirements the requirements
     */
    public void registerRequirements(final @NonNull CommandRequirement @NonNull... requirements) {
        this.registerRequirements(Arrays.asList(requirements));
    }

    @Override
    public void accept(final @NonNull CommandPostprocessingContext<PlotPlayer<?>> processingContext) {
        for (final CommandRequirement requirement : this.requirements) {
            if (!processingContext.command().commandMeta().getOrDefault(requirement.key(), false)) {
                continue;
            }
            if (requirement.evaluate(processingContext.commandContext())) {
                continue;
            }
            processingContext.commandContext().sender().sendMessage(requirement.failureCaption());
            // Not allowed :(
            ConsumerService.interrupt();
        }
    }
}
