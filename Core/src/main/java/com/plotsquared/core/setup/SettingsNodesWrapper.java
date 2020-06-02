package com.plotsquared.core.setup;

import com.plotsquared.core.configuration.ConfigurationNode;
import lombok.Getter;

public class SettingsNodesWrapper {
    @Getter private final ConfigurationNode[] settingsNodes;
    @Getter private final SetupStep afterwards;
    private int current;

    public SettingsNodesWrapper(ConfigurationNode[] settingsNodes, SetupStep afterwards) {
        this.settingsNodes = settingsNodes;
        this.afterwards = afterwards;
        this.current = 0;
    }


    public SettingsNodeStep next() {
        if (this.settingsNodes.length <= this.current) {
            throw new IllegalStateException("No step left");
        } else {
            int temp = this.current;
            this.current++;
            return new SettingsNodeStep(this.settingsNodes[temp], temp, this);
        }
    }
    public boolean hasNext() {
        return this.current < this.settingsNodes.length;
    }
}
