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
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.setup.SetupProcess;
import com.plotsquared.core.setup.SetupStep;
import com.plotsquared.core.util.SetupUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

@CommandDeclaration(command = "setup",
        permission = "plots.admin.command.setup",
        usage = "/plot setup",
        aliases = {"create"},
        category = CommandCategory.ADMINISTRATION)
public class Setup extends SubCommand {

    private final SetupUtils setupUtils;

    @Inject
    public Setup(final @NonNull SetupUtils setupUtils) {
        this.setupUtils = setupUtils;
    }

    public void displayGenerators(PlotPlayer<?> player) {
        StringBuilder message = new StringBuilder();
        message.append(TranslatableCaption.of("setup.choose_generator").getComponent(player));
        for (Entry<String, GeneratorWrapper<?>> entry : SetupUtils.generators.entrySet()) {
            if (entry.getKey().equals(PlotSquared.platform().pluginName())) {
                message.append("\n<dark_gray> - </dark_gray><dark_green>").append(entry.getKey()).append(
                        " (Default Generator)</dark_green>");
            } else if (entry.getValue().isFull()) {
                message.append("\n<dark_gray> - </dark_gray><gray>").append(entry.getKey()).append(" (Plot Generator)</gray>");
            } else {
                message.append("\n<dark_gray> - </dark_gray><gray>").append(entry.getKey()).append(" (Unknown structure)</gray>");
            }
        }
        player.sendMessage(StaticCaption.of(message.toString()));
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        try (final MetaDataAccess<SetupProcess> metaDataAccess =
                     player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_SETUP)) {
            SetupProcess process = metaDataAccess.get().orElse(null);
            if (process == null) {
                if (args.length > 0) {
                    player.sendMessage(TranslatableCaption.of("setup.setup_not_started"));
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver(
                                    "value",
                                    Tag.inserting(Component.text("Use /plot setup to start a setup process."))
                            )
                    );
                    return true;
                }
                process = new SetupProcess();
                metaDataAccess.set(process);
                this.setupUtils.updateGenerators(false);
                SetupStep step = process.getCurrentStep();
                step.announce(player);
                displayGenerators(player);
                return true;
            }
            if (args.length == 1) {
                if ("back".equalsIgnoreCase(args[0])) {
                    process.back();
                    process.getCurrentStep().announce(player);
                } else if ("cancel".equalsIgnoreCase(args[0])) {
                    metaDataAccess.remove();
                    player.sendMessage(TranslatableCaption.of("setup.setup_cancelled"));
                } else {
                    process.handleInput(player, args[0]);
                    if (process.getCurrentStep() != null) {
                        process.getCurrentStep().announce(player);
                    }
                }
            } else {
                process.getCurrentStep().announce(player);
            }
            return true;
        }
    }

    @Override
    public Collection<Command> tab(PlotPlayer<?> player, String[] args, boolean space) {
        SetupProcess process;
        try (final MetaDataAccess<SetupProcess> metaDataAccess =
                     player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_SETUP)) {
            process = metaDataAccess.get().orElse(null);
        }
        if (process == null) {
            return Collections.emptyList();
        }
        // player already provided too many arguments
        if (args.length > 1 || (args.length == 1 && space)) {
            return Collections.emptyList();
        }
        SetupStep setupStep = process.getCurrentStep();
        List<Command> commands = new ArrayList<>(setupStep.createSuggestions(player, space ? "" : args[0]));
        tryAddSubCommand("back", args[0], commands);
        tryAddSubCommand("cancel", args[0], commands);
        return commands;
    }

    private void tryAddSubCommand(String subCommand, String argument, List<Command> suggestions) {
        if (!argument.isEmpty() && subCommand.startsWith(argument)) {
            suggestions.add(new Command(null, false, subCommand, "", RequiredType.NONE, null) {
            });
        }
    }

}
