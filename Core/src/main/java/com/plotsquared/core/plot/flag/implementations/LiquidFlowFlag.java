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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;

public class LiquidFlowFlag extends PlotFlag<LiquidFlowFlag.FlowStatus, LiquidFlowFlag> {

    public static final LiquidFlowFlag LIQUID_FLOW_ENABLED = new LiquidFlowFlag(FlowStatus.ENABLED);
    public static final LiquidFlowFlag LIQUID_FLOW_DISABLED =
            new LiquidFlowFlag(FlowStatus.DISABLED);
    public static final LiquidFlowFlag LIQUID_FLOW_DEFAULT = new LiquidFlowFlag(FlowStatus.DEFAULT);

    private LiquidFlowFlag(FlowStatus value) {
        super(
                value,
                TranslatableCaption.of("flags.flag_category_boolean"),
                TranslatableCaption.of("flags.flag_description_liquid_flow")
        );
    }

    @Override
    public LiquidFlowFlag parse(final @NonNull String input) {
        return switch (input.toLowerCase()) {
            case "true", "enabled", "allow" -> LIQUID_FLOW_ENABLED;
            case "false", "disabled", "disallow" -> LIQUID_FLOW_DISABLED;
            default -> LIQUID_FLOW_DEFAULT;
        };
    }

    @Override
    public LiquidFlowFlag merge(final @NonNull FlowStatus newValue) {
        if (newValue == FlowStatus.ENABLED || this.getValue() == FlowStatus.ENABLED) {
            return LIQUID_FLOW_ENABLED;
        }
        return flagOf(newValue);
    }

    @Override
    public String toString() {
        return this.getValue().name().toLowerCase();
    }

    @Override
    public String getExample() {
        return "true";
    }

    @Override
    protected LiquidFlowFlag flagOf(final @NonNull FlowStatus value) {
        return switch (value) {
            case ENABLED -> LIQUID_FLOW_ENABLED;
            case DISABLED -> LIQUID_FLOW_DISABLED;
            default -> LIQUID_FLOW_DEFAULT;
        };
    }

    @Override
    public Collection<String> getTabCompletions() {
        return Arrays.asList("true", "false", "default");
    }

    public enum FlowStatus {
        ENABLED,
        DISABLED,
        DEFAULT
    }

}
