/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
