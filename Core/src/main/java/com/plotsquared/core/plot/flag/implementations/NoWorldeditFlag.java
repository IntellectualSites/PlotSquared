package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class NoWorldeditFlag extends BooleanFlag<NoWorldeditFlag> {

    public static final NoWorldeditFlag NO_WORLDEDIT_TRUE = new NoWorldeditFlag(true);
    public static final NoWorldeditFlag NO_WORLDEDIT_FALSE = new NoWorldeditFlag(false);

    private NoWorldeditFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_NO_WORLDEDIT);
    }

    @Override protected NoWorldeditFlag flagOf(@NotNull Boolean value) {
        return value ? NO_WORLDEDIT_TRUE : NO_WORLDEDIT_FALSE;
    }

}
