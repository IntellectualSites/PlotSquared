package com.intellectualcrafters.plot.titles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DefaultTitle_183 extends AbstractTitle {
    @Override
    public void sendTitle(final Player player, final String head, final String sub, final ChatColor head_color, final ChatColor sub_color) {
        try {
            final DefaultTitleManager_183 title = new DefaultTitleManager_183(head, sub, 1, 2, 1);
            title.setTitleColor(head_color);
            title.setSubtitleColor(sub_color);
            title.send(player);
        } catch (final Throwable e) {
            AbstractTitle.TITLE_CLASS = new HackTitle();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, head_color, sub_color);
        }
    }
}
