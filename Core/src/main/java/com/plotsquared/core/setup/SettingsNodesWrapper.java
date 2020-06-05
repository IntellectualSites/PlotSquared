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

    public SetupStep getFirstStep() {
        return this.settingsNodes.length == 0 ? this.afterwards : new SettingsNodeStep(this.settingsNodes[0], 0, this);
    }
}
