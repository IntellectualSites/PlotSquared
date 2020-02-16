package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class NotifyLeaveFlag extends BooleanFlag<NotifyLeaveFlag> {

    public static final NotifyLeaveFlag NOTIFY_LEAVE_TRUE = new NotifyLeaveFlag(true);
    public static final NotifyLeaveFlag NOTIFY_LEAVE_FALSE = new NotifyLeaveFlag(false);

    private NotifyLeaveFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_NOTIFY_LEAVE);
    }

    @Override protected NotifyLeaveFlag flagOf(@NotNull Boolean value) {
        return value ? NOTIFY_LEAVE_TRUE : NOTIFY_LEAVE_FALSE;
    }

}
