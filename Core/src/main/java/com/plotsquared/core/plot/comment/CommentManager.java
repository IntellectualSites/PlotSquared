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
package com.plotsquared.core.plot.comment;

import com.google.common.annotations.Beta;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Beta public class CommentManager {

    public static final HashMap<String, CommentInbox> inboxes = new HashMap<>();

    public static void sendTitle(final PlotPlayer player, final Plot plot) {
        if (!Settings.Enabled_Components.COMMENT_NOTIFIER || !plot.isOwner(player.getUUID())) {
            return;
        }
        TaskManager.runTaskLaterAsync(() -> {
            Collection<CommentInbox> boxes = CommentManager.inboxes.values();
            final AtomicInteger count = new AtomicInteger(0);
            final AtomicInteger size = new AtomicInteger(boxes.size());
            for (final CommentInbox inbox : inboxes.values()) {
                inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
                    @Override public void run(List<PlotComment> value) {
                        int total;
                        if (value != null) {
                            int num = 0;
                            for (PlotComment comment : value) {
                                if (comment.timestamp > getTimestamp(player, inbox.toString())) {
                                    num++;
                                }
                            }
                            total = count.addAndGet(num);
                        } else {
                            total = count.get();
                        }
                        if ((size.decrementAndGet() == 0) && (total > 0)) {
                            player.sendTitle("", Captions.INBOX_NOTIFICATION.getTranslated()
                                .replaceAll("%s", "" + total));
                        }
                    }
                });
            }
        }, TaskTime.seconds(1L));
    }

    public static long getTimestamp(PlotPlayer<?> player, String inbox) {
        return player.getMeta("inbox:" + inbox, player.getLastPlayed());
    }

    public static void addInbox(CommentInbox inbox) {
        inboxes.put(inbox.toString().toLowerCase(), inbox);
    }

    public static void registerDefaultInboxes() {
        addInbox(new InboxReport());
        addInbox(new InboxPublic());
        addInbox(new InboxOwner());
    }
}
