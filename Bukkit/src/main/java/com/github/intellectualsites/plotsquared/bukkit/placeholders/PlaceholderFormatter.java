package com.github.intellectualsites.plotsquared.bukkit.placeholders;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.config.ChatFormatter;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderFormatter implements ChatFormatter {

    @Override public void format(final ChatContext context) {
        final PlotPlayer recipient = context.getRecipient();
        if (recipient instanceof BukkitPlayer) {
            if (context.isRawOutput()) {
                context.setMessage(context.getMessage().replace('%', '\u2010'));
            } else {
                final Player player = ((BukkitPlayer) recipient).player;
                context.setMessage(PlaceholderAPI.setPlaceholders(player, context.getMessage()));
            }
         }
    }

}
