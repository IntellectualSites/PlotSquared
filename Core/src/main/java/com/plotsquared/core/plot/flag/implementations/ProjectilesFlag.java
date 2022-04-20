package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ProjectilesFlag extends BooleanFlag<ProjectilesFlag> {

    public static final ProjectilesFlag PROJECTILES_TRUE = new ProjectilesFlag(true);
    public static final ProjectilesFlag PROJECTILES_FALSE = new ProjectilesFlag(false);

    private ProjectilesFlag(boolean value){
        super(value, TranslatableCaption.of("flags.flag_description_projectiles"));
    }

    @Override
    protected ProjectilesFlag flagOf(@NonNull final Boolean value) {
        return value ? PROJECTILES_TRUE : PROJECTILES_FALSE;
    }

}
