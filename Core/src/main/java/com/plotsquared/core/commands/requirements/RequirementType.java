package com.plotsquared.core.commands.requirements;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum RequirementType {
    PLAYER(""),
    IN_PLOT("errors.not_in_plot"),
    PLOT_HAS_OWNER("info.plot_unowned", IN_PLOT),
    IS_OWNER("permission.no_plot_perms", PLOT_HAS_OWNER);

    private final Caption caption;
    private @NonNull Set<@NonNull RequirementType> inheritedRequirements;

    RequirementType(
            final String caption,
            final @NonNull RequirementType... inheritedRequirements
    ) {
        this.caption = TranslatableCaption.of(caption);
        this.inheritedRequirements = EnumSet.copyOf(Arrays.asList(inheritedRequirements));
    }

    public @NonNull Set<@NonNull RequirementType> inheritedRequirements() {
        return Collections.unmodifiableSet(this.inheritedRequirements);
    }

    public @NonNull Caption caption() {
        return this.caption;
    }
}
