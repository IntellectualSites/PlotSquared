package com.plotsquared.core.setup;

import com.plotsquared.core.configuration.ConfigurationNode;

public class SettingsNodesWrapper {
    private final ConfigurationNode[] settingsNodes;
    private int current;

    public SettingsNodesWrapper(ConfigurationNode[] settingsNodes) {
        this.settingsNodes = settingsNodes;
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
