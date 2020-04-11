package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class DeviceInteractFlag extends BooleanFlag<DeviceInteractFlag> {

    public static final DeviceInteractFlag DEVICE_INTERACT_TRUE = new DeviceInteractFlag(true);
    public static final DeviceInteractFlag DEVICE_INTERACT_FALSE = new DeviceInteractFlag(false);

    private DeviceInteractFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_DEVICE_INTERACT);
    }

    @Override protected DeviceInteractFlag flagOf(@NotNull Boolean value) {
        return value ? DEVICE_INTERACT_TRUE : DEVICE_INTERACT_FALSE;
    }

}
