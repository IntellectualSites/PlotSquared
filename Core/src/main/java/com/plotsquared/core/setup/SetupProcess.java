package com.plotsquared.core.setup;

import com.plotsquared.core.player.PlotPlayer;

import java.util.Stack;

public class SetupProcess {
    private final PlotAreaBuilder builder;
    private final Stack<SetupStep> history;
    private SetupStep current;

    public SetupProcess() {
        this.builder = new PlotAreaBuilder();
        this.history = new Stack<>();
        this.current = CommonSetupSteps.CHOOSE_GENERATOR;
    }

    public SetupStep getCurrentStep() {
        return this.current;
    }

    public void handleInput(PlotPlayer<?> plotPlayer, String argument) {
        SetupStep previous = this.current;
        // TODO null check?
        this.current = this.current.handleInput(plotPlayer, this.builder, argument);
        // push previous step into history
        if (this.current != previous && this.current != null) {
            this.history.push(previous);
        }
    }

    public void back() {
        if (!this.history.isEmpty()) {
            this.current.onBack(this.builder);
            this.current = this.history.pop();
        }
    }
}
