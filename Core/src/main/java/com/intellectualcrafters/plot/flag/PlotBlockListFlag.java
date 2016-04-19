package com.intellectualcrafters.plot.flag;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.WorldUtil;

import java.util.HashSet;

public class PlotBlockListFlag extends ListFlag<HashSet<PlotBlock>> {

    public PlotBlockListFlag(String name) {
        super(name);
    }

    @Override public String valueToString(Object value) {
        return StringMan.join((HashSet<PlotBlock>) value, ",");
    }

    @Override public HashSet<PlotBlock> parseValue(String value) {
        HashSet<PlotBlock> list = new HashSet<>();
        for (String item : value.split(",")) {
            PlotBlock block;
            try {
                String[] split = item.split(":");
                byte data;
                if (split.length == 2) {
                    if ("*".equals(split[1])) {
                        data = -1;
                    } else {
                        data = Byte.parseByte(split[1]);
                    }
                } else {
                    data = -1;
                }
                short id = Short.parseShort(split[0]);
                block = new PlotBlock(id, data);
            } catch (NumberFormatException e) {
                StringComparison<PlotBlock>.ComparisonResult str = WorldUtil.IMP.getClosestBlock(value);
                if (str == null || str.match > 1) {
                    continue;
                }
                block = str.best;
            }
            list.add(block);
        }
        return list;
    }

    @Override public String getValueDescription() {
        return null;
    }
}
