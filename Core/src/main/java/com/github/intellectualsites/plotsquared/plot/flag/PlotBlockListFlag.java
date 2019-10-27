package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.LegacyMappings;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PlotBlockListFlag extends ListFlag<HashSet<PlotBlock>> {

    public PlotBlockListFlag(String name) {
        super(Captions.FLAG_CATEGORY_BLOCK_LIST, name);
    }

    @Override public String valueToString(Object value) {
        return StringMan.join((HashSet<PlotBlock>) value, ",");
    }

    @Override public HashSet<PlotBlock> parseValue(final String value) {
        final LegacyMappings legacyMappings = PlotSquared.get().IMP.getLegacyMappings();
        return Arrays.stream(value.split(",")).map(legacyMappings::fromAny).filter(Objects::nonNull)
            .collect(Collectors.toCollection(HashSet::new));
    }

    @Override public String getValueDescription() {
        return "Flag value must be a block list";
    }
}
