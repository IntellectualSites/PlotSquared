/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *               Copyright (C) 2014 - 2022 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util.fawe;

import com.fastasyncworldedit.bukkit.regions.plotsquared.FaweDelegateSchematicHandler;
import com.google.inject.Inject;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.sk89q.jnbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class FaweSchematicHandler extends SchematicHandler {

    private final FaweDelegateSchematicHandler delegate = new FaweDelegateSchematicHandler();

    @Inject
    public FaweSchematicHandler(@NotNull WorldUtil worldUtil, @NotNull ProgressSubscriberFactory subscriberFactory) {
        super(worldUtil, subscriberFactory);
    }

    @Override
    public boolean restoreTile(QueueCoordinator queue, CompoundTag tag, int x, int y, int z) {
        return false;
    }

    @Override
    public void paste(
            final Schematic schematic,
            final Plot plot,
            final int xOffset,
            final int yOffset,
            final int zOffset,
            final boolean autoHeight,
            final PlotPlayer<?> actor,
            final RunnableVal<Boolean> whenDone
    ) {
        delegate.paste(schematic, plot, xOffset, yOffset, zOffset, autoHeight, whenDone);
    }

    @Override
    public boolean save(CompoundTag tag, String path) {
        return delegate.save(tag, path);
    }

    @SuppressWarnings("removal") // Just the override
    @Override
    public void upload(final CompoundTag tag, final UUID uuid, final String file, final RunnableVal<URL> whenDone) {
        delegate.upload(tag, uuid, file, whenDone);
    }

    @Override
    public Schematic getSchematic(@NotNull InputStream is) {
        return delegate.getSchematic(is);
    }

}

