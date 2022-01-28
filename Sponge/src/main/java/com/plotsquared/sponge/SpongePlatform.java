package com.plotsquared.sponge;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.plotsquared.core.PlotPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.modules.PlotSquaredModule;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.plotsquared.sponge.inject.SpongeModule;
import com.plotsquared.sponge.util.task.SpongeTaskManager;
import com.plotsquared.sponge.util.task.SpongeTimeConverter;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.nio.file.Path;

/**
 * The main class of your Sponge plugin.
 *
 * <p>All methods are optional -- some common event registrations are included as a jumping-off point.</p>
 */
@Plugin("sponge")
public class SpongePlatform implements PlotPlatform<ServerPlayer> {

    public final PluginContainer container;
    private final Logger logger;
    private final Game game;


    @Inject
    private PlotAreaManager plotAreaManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private int[] version;
    private Injector injector;

    @Inject
    SpongePlatform(final PluginContainer container, final Logger logger, final Game game) {
        this.container = container;
        this.logger = logger;
        this.game = game;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        // Perform any one-time setup
        this.logger.info("Constructing sponge");
        final TaskTime.TimeConverter timeConverter = new SpongeTimeConverter();
        TaskManager.setPlatformImplementation(new SpongeTaskManager(this, timeConverter));

        final PlotSquared plotSquared = new PlotSquared(this, "Sponge");

        if (Settings.FAWE_Components.FAWE_HOOK) {
            this.logger.info("FAWE Hooks do not work on Sponge. 😞");
        }

        // We create the injector after PlotSquared has been initialized, so that we have access
        // to generated instances and settings
        this.injector = Guice
                .createInjector(
                        Stage.PRODUCTION,
                        new PermissionModule(),
                        new WorldManagerModule(),
                        new PlotSquaredModule(),
                        new SpongeModule(this),
                        new BackupModule()
                );

    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        // Any setup per-game instance. This can run multiple times when
        // using the integrated (single-player) server.
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {
        // Any tear down per-game instance. This can run multiple times when
        // using the integrated (single-player) server.
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        // Register a simple command
        // When possible, all commands should be registered within a command register event
        final Parameter.Value<String> nameParam = Parameter.string().key("name").build();
        event.register(this.container, Command.builder()
                .addParameter(nameParam)
                .permission("sponge.command.greet")
                .executor(ctx -> {
                    final String name = ctx.requireOne(nameParam);
                    ctx.sendMessage(Identity.nil(), LinearComponents.linear(
                            NamedTextColor.AQUA,
                            Component.text("Hello "),
                            Component.text(name, Style.style(TextDecoration.BOLD)),
                            Component.text("!")
                    ));

                    return CommandResult.success();
                })
                .build(), "greet", "wave");
    }

    @Override
    public int[] serverVersion() {
        if (this.version == null) {
            try {
                this.version = new int[3];
                String[] split = Sponge.platform().minecraftVersion().name().split("\\.");
                this.version[0] = Integer.parseInt(split[0]);
                this.version[1] = Integer.parseInt(split[1]);
                if (split.length == 3) {
                    this.version[2] = Integer.parseInt(split[2]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new int[]{1, 16, 5};
            }
        }
        return this.version;
    }

    @Override
    public @NonNull String serverImplementation() {
        return "Sponge"; //TODO Possibly revaluate the this method name
    }

    @Override
    public @NonNull File getDirectory() {
        return configDir.toFile();
    }

    @Override
    public @NonNull File worldContainer() {
        return game.gameDirectory().toFile(); //TODO This may be incorrect. Behavior needs verification.
    }

    public Logger getLogger() {
        return logger;
    }

}
