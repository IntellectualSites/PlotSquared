package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class NotifyEnterFlag extends BooleanFlag<NotifyEnterFlag> {

    public static final NotifyEnterFlag NOTIFY_ENTER_TRUE = new NotifyEnterFlag(true);
    public static final NotifyEnterFlag NOTIFY_ENTER_FALSE = new NotifyEnterFlag(false);

    private NotifyEnterFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_NOTIFY_ENTER);
    }

    @Override protected NotifyEnterFlag flagOf(@NotNull Boolean value) {
        return value ? NOTIFY_ENTER_TRUE : NOTIFY_ENTER_FALSE;
    }

}
