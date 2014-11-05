package com.intellectualcrafters.plot.database.sqlobjects;

/**
 * Created by Citymonstret on 2014-10-28.
 */
public class SQLField {

    private final SQLType type;
    private final Object  value;

    public SQLField(final SQLType type, final Object value) {
        this.type = type;
        this.value = value;
    }

    public SQLType getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

}
