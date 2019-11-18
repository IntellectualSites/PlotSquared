package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.ArrayList;
import java.util.List;


public class IntegerListFlag extends ListFlag<List<Integer>> {

    public IntegerListFlag(String name) {
        super(Captions.FLAG_CATEGORY_INTEGER_LIST, name);
    }

    @Override public String valueToString(List<Integer> value) {
        return StringMan.join(value, ",");
    }

    @Override public List<Integer> parseValue(String value) {
        String[] split = value.split(",");
        ArrayList<Integer> numbers = new ArrayList<>();
        for (String element : split) {
            numbers.add(Integer.parseInt(element));
        }
        return numbers;
    }

    @Override public String getValueDescription() {
        return Captions.FLAG_ERROR_INTEGER_LIST.getTranslated();
    }
}
