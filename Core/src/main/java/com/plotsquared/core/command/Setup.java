/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.setup.SetupProcess;
import com.plotsquared.core.setup.SetupStep;
import com.plotsquared.core.util.SetupUtils;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

@CommandDeclaration(command = "setup",
    permission = "plots.admin.command.setup",
    description = "Setup wizard for plot worlds",
    usage = "/plot setup",
    aliases = {"create"},
    category = CommandCategory.ADMINISTRATION)
public class Setup extends SubCommand {

    private final SetupUtils setupUtils;

    @Inject public Setup(@Nonnull final SetupUtils setupUtils) {
        this.setupUtils = setupUtils;
    }

    public void displayGenerators(PlotPlayer<?> player) {
        StringBuilder message = new StringBuilder();
        message.append("&6What generator do you want?");
        for (Entry<String, GeneratorWrapper<?>> entry : SetupUtils.generators.entrySet()) {
            if (entry.getKey().equals(PlotSquared.platform().getPluginName())) {
                message.append("\n&8 - &2").append(entry.getKey()).append(" (Default Generator)");
            } else if (entry.getValue().isFull()) {
                message.append("\n&8 - &7").append(entry.getKey()).append(" (Plot Generator)");
            } else {
                message.append("\n&8 - &7").append(entry.getKey()).append(" (Unknown structure)");
            }
        }
        player.sendMessage(StaticCaption.of(message.toString()));
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        SetupProcess process = player.getMeta("setup");
        if (process == null) {
            if (args.length > 0) {
                player.sendMessage(TranslatableCaption.of("setup.setup_not_started"));
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        Template.of("value", "Use /plot setup to start a setup process.")
                );
                return true;
            }
            process = new SetupProcess();
            player.setMeta("setup", process);
            this.setupUtils.updateGenerators();
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
                player.deleteMeta("setup");
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

    @Override public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        SetupProcess process = (SetupProcess) player.getMeta("setup"); // TODO use generics -> auto cast
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
            suggestions.add(new Command(null, false, subCommand, "", RequiredType.NONE, null) {});
        }
    }
}
