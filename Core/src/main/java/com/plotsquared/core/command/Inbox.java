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
package com.plotsquared.core.command;

import com.google.inject.TypeLiteral;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.MetaDataKey;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.comment.CommentInbox;
import com.plotsquared.core.plot.comment.CommentManager;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.task.RunnableVal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@CommandDeclaration(command = "inbox",
        usage = "/plot inbox [inbox] [delete <index> | clear | page]",
        permission = "plots.inbox",
        category = CommandCategory.CHAT,
        requiredType = RequiredType.PLAYER)
public class Inbox extends SubCommand {

    public void displayComments(PlotPlayer<?> player, List<PlotComment> oldComments, int page) {
        if (oldComments == null || oldComments.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("comment.inbox_empty"));
            return;
        }
        PlotComment[] comments = oldComments.toArray(new PlotComment[0]);
        if (page < 0) {
            page = 0;
        }
        // Get the total pages
        // int totalPages = ((int) Math.ceil(12 *
        int totalPages = (int) Math.ceil(comments.length / 12);
        if (page > totalPages) {
            page = totalPages;
        }
        // Only display 12 per page
        int max = page * 12 + 12;
        if (max > comments.length) {
            max = comments.length;
        }
        TextComponent.Builder builder = Component.text();
        builder.append(MINI_MESSAGE.deserialize(
                TranslatableCaption.of("list.comment_list_header_paged").getComponent(player) + '\n',
                TagResolver.builder()
                        .tag("amount", Tag.inserting(Component.text(comments.length)))
                        .tag("cur", Tag.inserting(Component.text(page + 1)))
                        .tag("max", Tag.inserting(Component.text(totalPages + 1)))
                        .tag("word", Tag.inserting(Component.text("all")))
                        .build()
        ));

        // This might work xD
        for (int x = page * 12; x < max; x++) {
            PlotComment comment = comments[x];
            Component commentColored;
            if (player.getName().equals(comment.senderName())) {
                commentColored = MINI_MESSAGE
                        .deserialize(
                                TranslatableCaption.of("list.comment_list_by_lister").getComponent(player),
                                TagResolver.resolver("comment", Tag.inserting(Component.text(comment.comment())))
                        );
            } else {
                commentColored = MINI_MESSAGE
                        .deserialize(
                                TranslatableCaption.of("list.comment_list_by_other").getComponent(player),
                                TagResolver.resolver("comment", Tag.inserting(Component.text(comment.comment())))
                        );
            }
            TagResolver resolver = TagResolver.builder()
                    .tag("number", Tag.inserting(Component.text(x)))
                    .tag("world", Tag.inserting(Component.text(comment.world())))
                    .tag("plot_id", Tag.inserting(Component.text(comment.id().getX() + ";" + comment.id().getY())))
                    .tag("commenter", Tag.inserting(Component.text(comment.senderName())))
                    .tag("comment", Tag.inserting(commentColored))
                    .build();
            builder.append(MINI_MESSAGE
                    .deserialize(
                            TranslatableCaption.of("list.comment_list_comment").getComponent(player),
                            resolver
                    ));
        }
        player.sendMessage(StaticCaption.of(MINI_MESSAGE.serialize(builder.build())));
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (args.length == 0) {
            sendUsage(player);
            for (final CommentInbox inbox : CommentManager.inboxes.values()) {
                if (inbox.canRead(plot, player)) {
                    if (!inbox.getComments(plot, new RunnableVal<>() {
                        @Override
                        public void run(List<PlotComment> value) {
                            if (value != null) {
                                int total = 0;
                                int unread = 0;
                                for (PlotComment comment : value) {
                                    total++;
                                    if (comment.timestamp() > CommentManager
                                            .getTimestamp(player, inbox.toString())) {
                                        unread++;
                                    }
                                }
                                if (total != 0) {
                                    player.sendMessage(
                                            TranslatableCaption.of("comment.inbox_item"),
                                            TagResolver.resolver(
                                                    "value",
                                                    Tag.inserting(Component.text(inbox + " (" + total + '/' + unread + ')'))
                                            )
                                    );
                                    return;
                                }
                            }
                            player.sendMessage(
                                    TranslatableCaption.of("comment.inbox_item"),
                                    TagResolver.resolver("value", Tag.inserting(Component.text(inbox.toString())))
                            );
                        }
                    })) {
                        player.sendMessage(
                                TranslatableCaption.of("comment.inbox_item"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(inbox.toString())))
                        );
                    }
                }
            }
            return false;
        }
        final CommentInbox inbox = CommentManager.inboxes.get(args[0].toLowerCase());
        if (inbox == null) {
            player.sendMessage(
                    TranslatableCaption.of("comment.invalid_inbox"),
                    TagResolver.resolver(
                            "list",
                            Tag.inserting(Component.text(StringMan.join(CommentManager.inboxes.keySet(), ", ")))
                    )
            );
            return false;
        }
        final MetaDataKey<Long> metaDataKey = MetaDataKey.of(
                String.format("inbox:%s", inbox),
                new TypeLiteral<>() {
                }
        );
        try (final MetaDataAccess<Long> metaDataAccess = player.accessTemporaryMetaData(metaDataKey)) {
            metaDataAccess.set(System.currentTimeMillis());
        }
        final int page;
        if (args.length > 1) {
            switch (args[1].toLowerCase()) {
                case "delete" -> {
                    if (!inbox.canModify(plot, player)) {
                        player.sendMessage(TranslatableCaption.of("comment.no_perm_inbox_modify"));
                        return false;
                    }
                    if (args.length != 3) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                TagResolver.resolver(
                                        "value",
                                        Tag.inserting(Component.text("/plot inbox " + inbox + " delete <index>"))
                                )
                        );
                        return true;
                    }
                    final int index;
                    try {
                        index = Integer.parseInt(args[2]);
                        if (index < 1) {
                            player.sendMessage(
                                    TranslatableCaption.of("comment.not_valid_inbox_index"),
                                    TagResolver.resolver("number", Tag.inserting(Component.text(index)))
                            );
                            return false;
                        }
                    } catch (NumberFormatException ignored) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                TagResolver.resolver(
                                        "value",
                                        Tag.inserting(Component.text("/plot inbox " + inbox + " delete <index>"))
                                )
                        );
                        return false;
                    }
                    if (!inbox.getComments(plot, new RunnableVal<>() {
                        @Override
                        public void run(List<PlotComment> value) {
                            if (index > value.size()) {
                                player.sendMessage(
                                        TranslatableCaption.of("comment.not_valid_inbox_index"),
                                        TagResolver.resolver("number", Tag.inserting(Component.text(index)))
                                );
                                return;
                            }
                            PlotComment comment = value.get(index - 1);
                            inbox.removeComment(plot, comment);
                            boolean success = plot.getPlotCommentContainer().removeComment(comment);
                            if (success) {
                                player.sendMessage(
                                        TranslatableCaption.of("comment.comment_removed_success"),
                                        TagResolver.resolver("value", Tag.inserting(Component.text(comment.comment())))
                                );
                            } else {
                                player.sendMessage(
                                        TranslatableCaption.of("comment.comment_removed_failure"));
                            }
                        }
                    })) {
                        player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
                        return false;
                    }
                    return true;
                }
                case "clear" -> {
                    if (!inbox.canModify(plot, player)) {
                        player.sendMessage(TranslatableCaption.of("comment.no_perm_inbox_modify"));
                    }
                    inbox.clearInbox(plot);
                    List<PlotComment> comments = plot.getPlotCommentContainer().getComments(inbox.toString());
                    if (!comments.isEmpty()) {
                        player.sendMessage(
                                TranslatableCaption.of("comment.comment_removed_success"),
                                TagResolver.resolver("value", Tag.inserting(Component.text("*")))
                        );
                        plot.getPlotCommentContainer().removeComments(comments);
                    }
                    return true;
                }
                default -> {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                        sendUsage(player);
                        return false;
                    }
                }
            }
        } else {
            page = 1;
        }
        if (!inbox.canRead(plot, player)) {
            player.sendMessage(TranslatableCaption.of("comment.no_perm_inbox"));
            return false;
        }
        if (!inbox.getComments(plot, new RunnableVal<>() {
            @Override
            public void run(List<PlotComment> value) {
                displayComments(player, value, page);
            }
        })) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        if (args.length == 1) {
            final List<String> completions = new LinkedList<>();
            if (player.hasPermission(Permission.PERMISSION_INBOX_READ_OWNER)) {
                completions.add("owner");
            }
            if (player.hasPermission(Permission.PERMISSION_INBOX_READ_PUBLIC)) {
                completions.add("public");
            }
            if (player.hasPermission(Permission.PERMISSION_INBOX_READ_REPORT)) {
                completions.add("report");
            }
            final List<Command> commands = completions.stream().filter(completion -> completion
                            .toLowerCase()
                            .startsWith(args[0].toLowerCase()))
                    .map(completion -> new Command(null, true, completion, "", RequiredType.PLAYER, CommandCategory.CHAT) {
                    }).collect(Collectors.toCollection(LinkedList::new));
            if (player.hasPermission(Permission.PERMISSION_INBOX) && args[0].length() > 0) {
                commands.addAll(TabCompletions.completePlayers(player, args[0], Collections.emptyList()));
            }
            return commands;
        }
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

}
