package com.plotsquared.sponge.util;

import com.plotsquared.core.location.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.world.server.ServerWorld;

public class SpongeWorld implements World<ServerWorld> {

    private final ServerWorld world;

    private SpongeWorld(final ServerWorld world) {
        this.world = world;
    }

    @Override
    public @NonNull ServerWorld getPlatformWorld() {
        return this.world;
    }

    @Override
    public @NonNull String getName() {
        return this.world.key().asString();
    }

}
