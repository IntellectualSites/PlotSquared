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
package com.plotsquared.core.queue.subscriber;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.queue.ChunkCoordinator;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The default PlotSquared Progress Subscriber. Can be used for both console and player tasks.
 * It is the {@link ProgressSubscriber} returned by {@link com.plotsquared.core.inject.factory.ProgressSubscriberFactory}.
 * Runs a repeating synchronous task notifying the given actor about any updates, saving updates notified by the ChunkCoordinator.
 */
public class DefaultProgressSubscriber implements ProgressSubscriber {

    @NonNull
    private final AtomicDouble progress = new AtomicDouble(0);
    @NonNull
    private final AtomicBoolean started = new AtomicBoolean(false);
    @NonNull
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    @NonNull
    private final TaskTime interval;
    @NonNull
    private final TaskTime wait;
    @NonNull
    private final PlotPlayer<?> actor;
    @NonNull
    private final Caption caption;
    private PlotSquaredTask task;

    @AssistedInject
    public DefaultProgressSubscriber() {
        throw new UnsupportedOperationException("DefaultProgressSubscriber cannot be used without an actor.");
    }

    @AssistedInject
    public DefaultProgressSubscriber(@Nullable @Assisted("subscriber") final PlotPlayer<?> actor) {
        Preconditions.checkNotNull(
                actor,
                "Actor cannot be null when using DefaultProgressSubscriber! Make sure if attempting to use custom Subscribers it is correctly parsed to the queue!"
        );
        this.actor = actor;
        this.interval = TaskTime.ms(Settings.QUEUE.NOTIFY_INTERVAL);
        this.wait = TaskTime.ms(Settings.QUEUE.NOTIFY_WAIT);
        this.caption = TranslatableCaption.of("working.progress");
    }

    @AssistedInject
    public DefaultProgressSubscriber(
            @Nullable @Assisted("subscriber") final PlotPlayer<?> actor,
            @Assisted("progressInterval") final long interval,
            @Assisted("waitBeforeStarting") final long wait,
            @Nullable @Assisted("caption") final Caption caption
    ) {
        Preconditions.checkNotNull(
                actor,
                "Actor cannot be null when using DefaultProgressSubscriber! Make sure if attempting to use custom Subscribers it is correctly parsed to the queue!"
        );
        this.actor = actor;
        this.interval = TaskTime.ms(interval);
        this.wait = TaskTime.ms(wait);
        this.caption = Objects.requireNonNullElseGet(caption, () -> TranslatableCaption.of("working.progress"));
    }

    @Override
    public void notifyProgress(@NonNull ChunkCoordinator coordinator, double progress) {
        this.progress.set(progress);
        if (started.compareAndSet(false, true)) {
            TaskManager.getPlatformImplementation().taskLater(() -> task = TaskManager
                    .getPlatformImplementation()
                    .taskRepeat(() -> {
                        if (!started.get()) {
                            return;
                        }
                        if (cancelled.get()) {
                            task.cancel();
                            return;
                        }
                        actor.sendMessage(
                                caption,
                                TagResolver.resolver(
                                        "progress",
                                        Tag.inserting(Component.text(String.format("%.2f", this.progress.doubleValue() * 100)))
                                )
                        );
                    }, interval), wait);
        }
    }

    public void notifyEnd() {
        cancel();
    }

    public void cancel() {
        this.cancelled.set(true);
        if (this.task != null) {
            task.cancel();
        }
    }

}
