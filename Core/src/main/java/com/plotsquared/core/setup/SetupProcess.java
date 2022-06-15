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

import com.plotsquared.core.player.PlotPlayer;

import java.util.Stack;

/**
 * This class keeps track of a setup process.
 * It holds the history and the current setup state.
 */
public class SetupProcess {

    private final PlotAreaBuilder builder;
    private final Stack<SetupStep> history;
    private SetupStep current;

    public SetupProcess() {
        this.builder = PlotAreaBuilder.newBuilder();
        this.history = new Stack<>();
        this.current = CommonSetupSteps.CHOOSE_GENERATOR;
    }

    public SetupStep getCurrentStep() {
        return this.current;
    }

    public void handleInput(PlotPlayer<?> plotPlayer, String argument) {
        SetupStep previous = this.current;
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
