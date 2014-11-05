package com.intellectualcrafters.plot.uuid;

import java.util.UUID;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.StringWrapper;

/**
 * Created by Citymonstret on 2014-10-13.
 */
public abstract class UUIDSaver {
    public abstract void globalPopulate();

    public abstract void globalSave(final BiMap<StringWrapper, UUID> biMap);

    public abstract void save(final UUIDSet set);

    public abstract UUIDSet get(final String name);

    public abstract UUIDSet get(final UUID uuid);

    public abstract UUID mojangUUID(final String name) throws Exception;

    public abstract String mojangName(final UUID uuid) throws Exception;
}
