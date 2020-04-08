package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class FlyFlag extends PlotFlag<FlyFlag.FlyStatus, FlyFlag> {

    public static final FlyFlag FLIGHT_FLAG_DISABLED = new FlyFlag(FlyStatus.DISABLED);
    public static final FlyFlag FLIGHT_FLAG_ENABLED = new FlyFlag(FlyStatus.ENABLED);
    public static final FlyFlag FLIGHT_FLAG_DEFAULT = new FlyFlag(FlyStatus.DEFAULT);

    protected FlyFlag(final FlyStatus value) {
        super(value, Captions.FLAG_CATEGORY_BOOLEAN, Captions.FLAG_DESCRIPTION_FLIGHT);
    }

    @Override public FlyFlag parse(@NotNull final String input) {
        switch (input.toLowerCase()) {
            case "true":
            case "enabled":
            case "allow":
                return FLIGHT_FLAG_ENABLED;
            case "false":
            case "disabled":
            case "disallow":
                return FLIGHT_FLAG_DISABLED;
            default:
                return FLIGHT_FLAG_DEFAULT;
        }
    }

    @Override public FlyFlag merge(@NotNull final FlyStatus newValue) {
        if (newValue == FlyStatus.ENABLED || this.getValue() == FlyStatus.ENABLED) {
            return FLIGHT_FLAG_ENABLED;
        }
        return flagOf(newValue);
    }

    @Override public String toString() {
        return this.getValue().name().toLowerCase();
    }

    @Override public String getExample() {
        return "true";
    }

    @Override protected FlyFlag flagOf(@NotNull final FlyStatus value) {
        switch (value) {
            case ENABLED:
                return FLIGHT_FLAG_ENABLED;
            case DISABLED:
                return FLIGHT_FLAG_DISABLED;
            default:
                return FLIGHT_FLAG_DEFAULT;
        }
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("true", "false", "default");
    }

    public enum FlyStatus {
        ENABLED,
        DISABLED,
        DEFAULT
    }

}
