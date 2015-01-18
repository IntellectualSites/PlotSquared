package com.intellectualcrafters.plot.titles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DefaultTitle extends AbstractTitle {
	@Override
	public void sendTitle(Player player, String head, String sub, ChatColor head_color, ChatColor sub_color) {
		DefaultTitleManager title = new DefaultTitleManager(head,sub,1, 2, 1);
		title.setTitleColor(head_color);
		title.setSubtitleColor(sub_color);
		title.send(player);
	}
}
