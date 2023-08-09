package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EditSignFlag extends BooleanFlag<EditSignFlag> {
    public static final EditSignFlag EDIT_SIGN_TRUE = new EditSignFlag(true);
    public static final EditSignFlag EDIT_SIGN_FALSE = new EditSignFlag(false);

    private EditSignFlag(final boolean value) {
        super(value, TranslatableCaption.of("flags.flag_description_edit_sign"));
    }

    @Override
    protected EditSignFlag flagOf(@NonNull final Boolean value) {
        return value ? EDIT_SIGN_TRUE : EDIT_SIGN_FALSE;
    }

}
