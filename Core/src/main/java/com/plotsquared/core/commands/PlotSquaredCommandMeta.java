package com.plotsquared.core.commands;

import cloud.commandframework.keys.CloudKey;
import com.plotsquared.core.command.CommandCategory;

/**
 * Shared {@link cloud.commandframework.meta.CommandMeta command meta} keys.
 */
public final class PlotSquaredCommandMeta {

    /**
     * Key that determines what {@link CommandCategory category} a command belongs to.
     */
    public static final CloudKey<CommandCategory> META_CATEGORY = CloudKey.of("category", CommandCategory.class);

    private PlotSquaredCommandMeta() {
    }
}
