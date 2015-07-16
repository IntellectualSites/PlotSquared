package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

/**
 * Created 2015-07-13 for PlotSquaredGit
 *
 * @author Citymonstret
 */
public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotPlayer plr = BukkitUtil.getPlayer(player);
        if (!plotworld.PLOT_CHAT && (plr.getMeta("chat") == null || !(Boolean) plr.getMeta("chat"))) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(player);
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        final String message = event.getMessage();
        String format = C.PLOT_CHAT_FORMAT.s();
        final String sender = event.getPlayer().getDisplayName();
        final PlotId id = plot.id;
        final Set<Player> recipients = event.getRecipients();
        recipients.clear();
        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("plots.admin.command.chat") || plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(p)))) {
                recipients.add(p);
            }
        }
        format = format.replaceAll("%plot_id%", id.x + ";" + id.y).replaceAll("%sender%", sender).replaceAll("%msg%", message);
        format = ChatColor.translateAlternateColorCodes('&', format);
        event.setFormat(format);
    }

}
