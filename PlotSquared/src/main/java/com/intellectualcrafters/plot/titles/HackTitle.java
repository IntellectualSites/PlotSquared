package com.intellectualcrafters.plot.titles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.Settings;

public class HackTitle extends AbstractTitle {
	@Override
	public void sendTitle(Player player, String head, String sub, ChatColor head_color, ChatColor sub_color) {
		try {
			HackTitleManager title = new HackTitleManager(head,sub,1, 2, 1);
			title.setTitleColor(head_color);
			title.setSubtitleColor(sub_color);
			title.send(player);
		}
		catch (Throwable e) {
			PlotSquared.log("&cYour server version does not support titles!");
			Settings.TITLES = false;
			AbstractTitle.TITLE_CLASS = null;
		}
	}
}
