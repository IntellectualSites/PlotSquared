package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class DropProtectionFlag extends BooleanFlag<DropProtectionFlag> {

    public static final DropProtectionFlag DROP_PROTECTION_TRUE = new DropProtectionFlag(true);
    public static final DropProtectionFlag DROP_PROTECTION_FALSE = new DropProtectionFlag(false);

    private DropProtectionFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_DROP_PROTECTION);
    }

    @Override protected DropProtectionFlag flagOf(@NotNull Boolean value) {
        return value ? DROP_PROTECTION_TRUE : DROP_PROTECTION_FALSE;
    }

}
