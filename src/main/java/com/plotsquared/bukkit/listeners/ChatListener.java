package com.plotsquared.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;

import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.util.BukkitUtil;

/**
 * Created 2015-07-13 for PlotSquaredGit
 *
 * @author Citymonstret
 */
public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotPlayer plr = BukkitUtil.getPlayer(player);
        if (!plotworld.PLOT_CHAT && (plr.getAttribute("chat"))) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(player);
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        event.setCancelled(true);
        final String message = event.getMessage();
        final String sender = event.getPlayer().getDisplayName();
        final PlotId id = plot.id;
        String toSend = StringMan.replaceAll(C.PLOT_CHAT_FORMAT.s(), "%plot_id%", id.x + ";" + id.y, "%sender%", sender, "%msg%", message);
        PS.debug("FORMAT: " + event.getFormat());
        PS.debug("MESSAGE: " + event.getMessage());
        for (PlotPlayer recipient : UUIDHandler.getPlayers().values()) {
            if (plot.equals(recipient.getCurrentPlot())) {
                recipient.sendMessage(toSend);
            }
            else if (Permissions.hasPermission(recipient, C.PERMISSION_COMMANDS_CHAT)) {
                recipient.sendMessage(toSend);
            }
        }
    }

}
