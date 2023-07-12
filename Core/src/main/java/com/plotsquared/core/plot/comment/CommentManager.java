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
package com.plotsquared.core.plot.comment;

import com.google.inject.TypeLiteral;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.MetaDataKey;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CommentManager {

    public static final HashMap<String, CommentInbox> inboxes = new HashMap<>();

    public static void sendTitle(final PlotPlayer<?> player, final Plot plot) {
        if (!Settings.Enabled_Components.COMMENT_NOTIFIER || !plot.isOwner(player.getUUID())) {
            return;
        }
        TaskManager.runTaskLaterAsync(() -> {
            Collection<CommentInbox> boxes = CommentManager.inboxes.values();
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger size = new AtomicInteger(boxes.size());
            for (final CommentInbox inbox : inboxes.values()) {
                inbox.getComments(plot, new RunnableVal<>() {
                    @Override
                    public void run(List<PlotComment> value) {
                        int total;
                        if (value != null) {
                            int num = 0;
                            for (PlotComment comment : value) {
                                if (comment.timestamp() > getTimestamp(player, inbox.toString())) {
                                    num++;
                                }
                            }
                            total = count.addAndGet(num);
                        } else {
                            total = count.get();
                        }
                        if ((size.decrementAndGet() == 0) && (total > 0)) {
                            player.sendTitle(
                                    StaticCaption.of(""),
                                    TranslatableCaption.of("comment.inbox_notification"),
                                    TagResolver.builder()
                                            .tag("amount", Tag.inserting(Component.text(total)))
                                            .tag("command", Tag.inserting(Component.text("/plot inbox")))
                                            .build()
                            );
                        }
                    }
                });
            }
        }, TaskTime.seconds(1L));
    }

    /**
     * @param player The player the inbox belongs to
     * @param inbox  the inbox
     * @return the time in milliseconds when the player was last seen online
     */
    public static long getTimestamp(PlotPlayer<?> player, String inbox) {
        final MetaDataKey<Long> inboxKey = MetaDataKey.of(String.format("inbox:%s", inbox), new TypeLiteral<>() {
        });
        try (final MetaDataAccess<Long> inboxAccess = player.accessTemporaryMetaData(inboxKey)) {
            return inboxAccess.get().orElse(player.getLastPlayed());
        }
    }

    /**
     * @param inbox the inbox to add
     */
    public static void addInbox(CommentInbox inbox) {
        inboxes.put(inbox.toString().toLowerCase(), inbox);
    }

    public static void registerDefaultInboxes() {
        addInbox(new InboxReport());
        addInbox(new InboxPublic());
        addInbox(new InboxOwner());
    }

}
