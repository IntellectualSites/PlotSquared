package com.plotsquared.core.setup;

import com.plotsquared.core.configuration.ConfigurationNode;
import lombok.Getter;

public class SettingsNodesWrapper {
    @Getter private final ConfigurationNode[] settingsNodes;
    @Getter private final SetupStep afterwards;

    public SettingsNodesWrapper(ConfigurationNode[] settingsNodes, SetupStep afterwards) {
        this.settingsNodes = settingsNodes;
        this.afterwards = afterwards;
    }


    public SettingsNodeStep next(int current) {
        if (this.settingsNodes.length <= current + 1) {
            throw new IllegalStateException("No step left");
        } else {
            return new SettingsNodeStep(this.settingsNodes[current + 1], current + 1, this);
        }
    }

    public SettingsNodeStep first() {
        if (this.settingsNodes.length == 0) {
            throw new IllegalStateException("No step left");
        } else {
            return new SettingsNodeStep(this.settingsNodes[0], 0, this);
        }
    }

    public boolean hasNext(int current) {
        return current + 1 < this.settingsNodes.length;
    }

    public boolean hasStep() {
        return this.settingsNodes.length > 0;
    }
}
