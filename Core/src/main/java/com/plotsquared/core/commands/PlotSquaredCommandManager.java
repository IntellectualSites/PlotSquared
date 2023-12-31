package com.plotsquared.core.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.injection.GuiceInjectionService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.plotsquared.core.commands.injection.PlotInjector;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

@Singleton
public final class PlotSquaredCommandManager {

    private final CommandManager<PlotPlayer<?>> commandManager;
    private final Injector injector;

    @Inject
    public PlotSquaredCommandManager(
            final @NonNull CommandManager<PlotPlayer<?>> commandManager,
            final @NonNull Injector injector
    ) {
        this.commandManager = commandManager;
        this.injector = injector;
        this.registerInjectors();
    }

    /**
     * Registers the commands that are shipped with PlotSquared.
     */
    public void registerDefaultCommands() {
        final Set<PlotSquaredCommandBean> commands =
                this.injector.getInstance(Key.get(new TypeLiteral<Set<PlotSquaredCommandBean>>() {}));
        commands.forEach(command -> this.commandManager().command(command));
    }

    /**
     * Returns the command manager.
     *
     * @return the command manager
     */
    public @NonNull CommandManager<PlotPlayer<?>> commandManager() {
        return this.commandManager;
    }

    private void registerInjectors() {
        this.commandManager.parameterInjectorRegistry().registerInjector(Plot.class,
                this.injector.getInstance(PlotInjector.class));
        this.commandManager.parameterInjectorRegistry().registerInjectionService(GuiceInjectionService.create(this.injector));
    }
}
