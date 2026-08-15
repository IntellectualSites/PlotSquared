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
package com.plotsquared.core.setup;

import com.plotsquared.core.command.Command;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.TabCompletions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * A SettingsNodeStep is a step wrapping a {@link ConfigurationNode}.
 */
public class SettingsNodeStep implements SetupStep {

    private final ConfigurationNode configurationNode;
    private final int id;
    private final SetupStep next;

    public SettingsNodeStep(
            final ConfigurationNode configurationNode, final int id,
            final SettingsNodesWrapper wrapper
    ) {
        this.configurationNode = configurationNode;
        this.id = id;
        if (wrapper.settingsNodes().length > id + 1) {
            this.next = new SettingsNodeStep(wrapper.settingsNodes()[id + 1], id + 1, wrapper);
        } else {
            this.next = wrapper.afterwards();
        }
    }

    @Override
    public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
        if (this.configurationNode.isValid(argument)) {
            this.configurationNode.setValue(argument);
        }
        return this.next;
    }

    @NonNull
    @Override
    public Collection<String> getSuggestions() {
        return this.configurationNode.getSuggestions();
    }

    @Nullable
    @Override
    public String getDefaultValue() {
        return String.valueOf(this.configurationNode.getDefaultValue());
    }

    @Override
    public void announce(PlotPlayer<?> plotPlayer) {
        plotPlayer.sendMessage(
                TranslatableCaption.of("setup.setup_step"),
                TagResolver.builder()
                        .tag("step", Tag.inserting(Component.text(this.getId() + 1)))
                        .tag(
                                "description",
                                Tag.inserting(this.configurationNode.getDescription().toComponent(plotPlayer))
                        )
                        .tag("type", Tag.inserting(Component.text(this.configurationNode.getType().getType())))
                        .tag("value", Tag.inserting(Component.text(this.configurationNode.getDefaultValue().toString())))
                        .build()
        );
    }

    @Override
    public Collection<Command> createSuggestions(PlotPlayer<?> plotPlayer, String argument) {
        switch (this.configurationNode.getType().getType()) {
            case "BLOCK_BUCKET":
                return TabCompletions.completePatterns(argument);
            case "INTEGER":
                if (getDefaultValue() != null && getDefaultValue().startsWith(argument)) {
                    return Collections.singletonList(new Command(null, false,
                            getDefaultValue(), "", RequiredType.NONE, null
                    ) {
                    });
                }
            case "BOOLEAN":
                return TabCompletions.completeBoolean(argument);
        }
        return Collections.emptyList();
    }

    public ConfigurationNode getConfigurationNode() {
        return this.configurationNode;
    }

    public int getId() {
        return this.id;
    }

}
