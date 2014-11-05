package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

/**
 * Created by Citymonstret on 2014-10-13.
 */
public class UUIDSet {
    private final String name;
    private final UUID   uuid;

    public UUIDSet(final String name, final UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return this.name;
    }

    public UUID getUUID() {
        return this.uuid;
    }
}
