package com.plotsquared.core.services.api;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Manages player metadata
 *
 * @version 1.0.0
 * @since 8.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
public interface PlayerMetaService {

    void addPersistentMeta(UUID uuid, String key, byte[] meta, boolean delete);

    void getPersistentMeta(UUID uuid, Consumer<Map<String, byte[]>> result);

    void removePersistentMeta(UUID uuid, String key);

}
