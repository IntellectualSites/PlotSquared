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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class LiquidFlowFlag extends PlotFlag<LiquidFlowFlag.FlowStatus, LiquidFlowFlag> {

    public static final LiquidFlowFlag LIQUID_FLOW_ENABLED = new LiquidFlowFlag(FlowStatus.ENABLED);
    public static final LiquidFlowFlag LIQUID_FLOW_DISABLED =
        new LiquidFlowFlag(FlowStatus.DISABLED);
    public static final LiquidFlowFlag LIQUID_FLOW_DEFAULT = new LiquidFlowFlag(FlowStatus.DEFAULT);

    private LiquidFlowFlag(FlowStatus value) {
        super(value, Captions.FLAG_CATEGORY_BOOLEAN, Captions.FLAG_DESCRIPTION_LIQUID_FLOW);
    }

    @Override public LiquidFlowFlag parse(@NotNull final String input) {
        switch (input.toLowerCase()) {
            case "true":
            case "enabled":
            case "allow":
                return LIQUID_FLOW_ENABLED;
            case "false":
            case "disabled":
            case "disallow":
                return LIQUID_FLOW_DISABLED;
            default:
                return LIQUID_FLOW_DEFAULT;
        }
    }

    @Override public LiquidFlowFlag merge(@NotNull final FlowStatus newValue) {
        if (newValue == FlowStatus.ENABLED || this.getValue() == FlowStatus.ENABLED) {
            return LIQUID_FLOW_ENABLED;
        }
        return flagOf(newValue);
    }

    @Override public String toString() {
        return this.getValue().name().toLowerCase();
    }

    @Override public String getExample() {
        return "true";
    }

    @Override protected LiquidFlowFlag flagOf(@NotNull final FlowStatus value) {
        switch (value) {
            case ENABLED:
                return LIQUID_FLOW_ENABLED;
            case DISABLED:
                return LIQUID_FLOW_DISABLED;
            default:
                return LIQUID_FLOW_DEFAULT;
        }
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("true", "false", "default");
    }

    public enum FlowStatus {
        ENABLED, DISABLED, DEFAULT
    }

}
