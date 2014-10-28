package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

/**
 * Created by Citymonstret on 2014-10-13.
 */
public class UUIDSet {
    private String name;
    private UUID uuid;

    public UUIDSet(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }
}