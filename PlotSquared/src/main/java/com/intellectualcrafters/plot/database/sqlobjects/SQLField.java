package com.intellectualcrafters.plot.database.sqlobjects;

/**
 * Created by Citymonstret on 2014-10-28.
 */
public class SQLField {

    private SQLType type;
    private Object  value;

    public SQLField(SQLType type, Object value) {
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
