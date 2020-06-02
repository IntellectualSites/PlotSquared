package com.plotsquared.core.setup;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SettingsNodeStep implements SetupStep {
    @Getter private final ConfigurationNode configurationNode;
    @Getter private final int id;
    private final SettingsNodesWrapper wrapper;

    public SettingsNodeStep(ConfigurationNode configurationNode, int id, SettingsNodesWrapper wrapper) {
        this.configurationNode = configurationNode;
        this.id = id;
        this.wrapper = wrapper;
    }

    @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument) {
        if (this.configurationNode.isValid(argument)) {
            this.configurationNode.setValue(argument);
        }
        return this.wrapper.hasNext() ? wrapper.next() : wrapper.getAfterwards();
    }

    @NotNull @Override public Collection<String> getSuggestions() {
        return this.configurationNode.getSuggestions();
    }

    @Nullable @Override public String getDefaultValue() {
        return String.valueOf(this.configurationNode.getDefaultValue());
    }

    @Override
    public void announce(PlotPlayer plotPlayer) {
        MainUtil.sendMessage(plotPlayer, Captions.SETUP_STEP, this.getId() + 1,
                this.configurationNode.getDescription(), this.configurationNode.getType().getType(),
                String.valueOf(this.configurationNode.getDefaultValue()));
    }
}
