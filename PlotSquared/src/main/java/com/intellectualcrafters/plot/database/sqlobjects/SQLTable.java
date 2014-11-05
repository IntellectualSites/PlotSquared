package com.intellectualcrafters.plot.database.sqlobjects;

import com.intellectualcrafters.plot.Settings;

/**
 * Created by Citymonstret on 2014-10-28.
 */
public abstract class SQLTable {

    private final String     name;
    private final SQLField[] fields;
    
    public SQLTable(final String name, final String primaryKey, final SQLField... fields) {
        this.name = Settings.DB.PREFIX + name;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public SQLField[] getFields() {
        return this.fields;
    }

    public abstract void create();

}
