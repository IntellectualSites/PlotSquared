package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringListFlag extends ListFlag<List<String>> {

    public StringListFlag(String name) {
        super(Captions.FLAG_CATEGORY_STRING_LIST, name);
    }

    @Override public String valueToString(Object value) {
        return StringMan.join((List<String>) value, ",");
    }

    @Override public List<String> parseValue(String value) {
        return new ArrayList<>(Arrays.asList(value.split(",")));
    }

    @Override public String getValueDescription() {
        return Captions.FLAG_ERROR_STRINGLIST.getTranslated();
    }
}
