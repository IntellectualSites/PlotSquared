/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.inject;

import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.bukkit.commands.BukkitSenderMapper;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.commands.PlotSquaredCaptionProvider;
import com.plotsquared.core.commands.processing.CommandRequirementPostprocessor;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CloudModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + CloudModule.class.getSimpleName());

    private static @NonNull CommandSender convert(final @NonNull PlotPlayer<?> player) {
        if (player instanceof ConsolePlayer) {
            return Bukkit.getConsoleSender();
        }
        return (Player) player.getPlatformPlayer();
    }

    private static @NonNull PlotPlayer<?> convert (final @NonNull CommandSender sender) {
        if (sender instanceof Player player) {
            return BukkitUtil.adapt(player);
        }
        return ConsolePlayer.getConsole();
    }

    private final BukkitPlatform bukkitPlatform;

    public CloudModule(final @NonNull BukkitPlatform bukkitPlatform) {
        this.bukkitPlatform = bukkitPlatform;
    }

    @Override
    protected void configure() {
        final PaperCommandManager<PlotPlayer<?>> commandManager = new PaperCommandManager<PlotPlayer<?>>(
                this.bukkitPlatform,
                ExecutionCoordinator.asyncCoordinator(),
                new BukkitSenderMapper()
        );
        commandManager.captionRegistry().registerProvider(new PlotSquaredCaptionProvider());

        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier();
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions();
        }

        final CommandRequirementPostprocessor requirementPostprocessor = new CommandRequirementPostprocessor();
        commandManager.registerCommandPostProcessor(requirementPostprocessor);

        // TODO(City): Override parsing errors using MM parsing.
        MinecraftExceptionHandler.<PlotPlayer<?>>create(PlotPlayer::getAudience)
                .defaultHandlers()
                .decorator((ctx, component) -> TranslatableCaption.of("core.prefix").
                        toComponent(ctx.context().sender())
                        .append(component))
                .registerTo(commandManager);

        bind(Key.get(new TypeLiteral<CommandManager<PlotPlayer<?>>>() {})).toInstance(commandManager);
    }
}
