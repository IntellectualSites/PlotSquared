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
 *                  Copyright (C) 2020 IntellectualSites
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.plot.util.world;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class OperationUtil {
    private static final boolean ASYNC;

    static {
        boolean hasFawe = true;
        try {
            Class.forName("com.boydti.fawe.Fawe");
        } catch (ClassNotFoundException ignore) {
            hasFawe = false;
        }
        ASYNC = hasFawe;
    }

    private static World getWorld(String worldName) {
        Platform platform =
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
        List<? extends World> worlds = platform.getWorlds();
        for (World current : worlds) {
            if (current.getName().equals(worldName)) {
                return current;
            }
        }
        return null;
    }

    private static World getWorld(PlotPlayer plotPlayer, Actor actor) {
        World weWorld;
        if (actor instanceof Player) {
            weWorld = ((Player) actor).getWorld();
        } else {
            @NotNull Location loc = plotPlayer.getLocation();
            String world = loc.getWorld();
            weWorld = getWorld(world);
        }
        return weWorld;
    }

    private static EditSession createEditSession(PlotPlayer plotPlayer) {
        Actor actor = plotPlayer.toActor();
        World weWorld = getWorld(plotPlayer, actor);
        return createEditSession(weWorld, actor);
    }

    private static LocalSession getSession(Actor actor) {
        return WorldEdit.getInstance().getSessionManager().get(actor);
    }

    private static EditSession createEditSession(World world, Actor actor) {
        return createEditSession(world, actor, getSession(actor));
    }

    private static EditSession createEditSession(World world, Actor actor, LocalSession session) {
        EditSession editSession;
        Player player = actor.isPlayer() ? (Player) actor : null;
        editSession =
            WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1, null, player);

        editSession.setFastMode(!actor.isPlayer());
        editSession.setReorderMode(EditSession.ReorderMode.FAST);
        return editSession;
    }

    public Future<?> withEditSession(@NotNull PlotPlayer plotPlayer,
        @NotNull Consumer<EditSession> consumer, @Nullable Consumer<Throwable> exceptionHandler) {
        if (ASYNC) {
            ListeningExecutorService exec = WorldEdit.getInstance().getExecutorService();
            return exec
                .submit(() -> withEditSessionOnThread(plotPlayer, consumer, exceptionHandler));
        } else {
            withEditSessionOnThread(plotPlayer, consumer, exceptionHandler);
        }
        return Futures.immediateFuture(true);
    }

    private void withEditSessionOnThread(PlotPlayer plotPlayer, Consumer<EditSession> consumer,
        Consumer<Throwable> exceptionHandler) {
        Actor actor = plotPlayer.toActor();
        World weWorld = getWorld(plotPlayer, actor);
        LocalSession session = getSession(actor);
        try (EditSession ess = createEditSession(weWorld, actor, session)) {
            try {
                consumer.accept(ess);
            } finally {
                ess.close();
                session.remember(ess);
            }
        } catch (Throwable e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            } else {
                e.printStackTrace();
            }
        }
    }
}
