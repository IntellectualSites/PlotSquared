package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.HashSet;

public class PlotBlockListFlag extends ListFlag<HashSet<PlotBlock>> {

    public PlotBlockListFlag(String name) {
        super(name);
    }

    @Override public String valueToString(Object value) {
        return StringMan.join((HashSet<PlotBlock>) value, ",");
    }

    @Override public HashSet<PlotBlock> parseValue(final String value) {
        final HashSet<PlotBlock> list = new HashSet<>();
        for (final String item : value.split(",")) {
            final PlotBlock block = PlotSquared.get().IMP.getLegacyMappings().fromAny(item);
            if (block != null) {
                list.add(block);
            }
        }
        return list;
    }

    @Override public String getValueDescription() {
        return "Flag value must be a block list";
    }
}
