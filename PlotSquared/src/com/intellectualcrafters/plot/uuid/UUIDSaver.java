package com.intellectualcrafters.plot.uuid;

import com.google.common.collect.BiMap;

import java.util.UUID;

/**
 * Created by Citymonstret on 2014-10-13.
 */
public abstract class UUIDSaver {
    public abstract void globalSave(BiMap<String, UUID> map);
    public abstract void save(UUIDSet set);
    public abstract UUIDSet get(String name);
    public abstract UUIDSet get(UUID uuid);
}