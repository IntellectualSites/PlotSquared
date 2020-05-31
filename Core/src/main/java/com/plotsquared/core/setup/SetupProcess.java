package com.plotsquared.core.setup;

import com.plotsquared.core.player.PlotPlayer;

import java.util.ArrayDeque;
import java.util.Queue;

public class SetupProcess {
    private final PlotAreaBuilder builder;
    private final Queue<SetupStep> history;
    private SetupStep current;

    public SetupProcess() {
        this.builder = new PlotAreaBuilder();
        this.history = new ArrayDeque<>();
        this.current = CommonSetupSteps.GENERATOR;
    }

    public SetupStep getCurrentStep() {
        return this.current;
    }

    public void handleInput(PlotPlayer plotPlayer, String argument) {
        // TODO null check?
        this.current = this.current.handleInput(plotPlayer, this.builder, argument);
    }

    public void back() {
        if (!this.history.isEmpty()) {
            this.current.onBack(this.builder);
            this.current = this.history.poll();
        }
    }
}
