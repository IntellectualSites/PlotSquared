package com.intellectualcrafters.plot.uuid;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.StringWrapper;

import java.util.UUID;

/**
 * Created by Citymonstret on 2014-10-13.
 */
public abstract class UUIDSaver {
    public abstract void globalPopulate();
    public abstract void globalSave(BiMap<StringWrapper, UUID> biMap);
    public abstract void save(UUIDSet set);
    public abstract UUIDSet get(String name);
    public abstract UUIDSet get(UUID uuid);
}