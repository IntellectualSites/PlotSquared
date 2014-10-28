package com.intellectualcrafters.plot.database.sqlobjects;

import com.intellectualcrafters.plot.Settings;

/**
 * Created by Citymonstret on 2014-10-28.
 */
public abstract class SQLTable {

    private String name;
    private SQLField[] fields;
    private String primaryKey;

    public SQLTable(String name, String primaryKey, SQLField ... fields) {
        this.name = Settings.DB.PREFIX + name;
        this.fields = fields;
        this.primaryKey = primaryKey;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public SQLField[] getFields() {
        return this.fields;
    }

    private void createFromFields() {
        StringBuilder statement = new StringBuilder();
        statement.append("CREATE TABLE `" + name + "` IF NOT EXISTS (");
        for(SQLField field : fields) {
            switch(field.getType()) {
                case INTEGER:
                    break;
                case VARCHAR:
                    break;
                case BOOL:
                    break;
                default:
                    break;
            }
        }
    }

    public abstract void create();

}
