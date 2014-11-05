package com.intellectualcrafters.plot.database.sqlobjects;

/**
 * Created by Citymonstret on 2014-10-28.
 */
public enum SQLType {

    INTEGER(0, "integer", Integer.class, 11),
    VARCHAR("", "varchar", String.class, 300),
    BOOL(false, "bool", Boolean.class, 1);

    private Object defaultValue;
    private String sqlName;
    private Class  javaClass;
    private int    length;

    SQLType(final Object defaultValue, final String sqlName, final Class javaClass, final int length) {
        this.defaultValue = defaultValue;
        this.sqlName = sqlName;
        this.javaClass = javaClass;
        this.length = length;
    }

    public int getLength() {
        return this.length;
    }

    @Override
    public String toString() {
        return this.sqlName;
    }

    public Class getJavaClass() {
        return this.javaClass;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }
}
