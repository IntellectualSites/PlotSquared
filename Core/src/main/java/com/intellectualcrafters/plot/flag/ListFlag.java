package com.intellectualcrafters.plot.flag;

import java.util.Collection;

public abstract class ListFlag<V extends Collection<?>> extends Flag<V> {

    public ListFlag(String name) {
        super(name);
    }
}
