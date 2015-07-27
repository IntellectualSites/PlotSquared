package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualcrafters.plot.object.PlotPlayer;

@CommandDeclaration(
        command = "chat",
        description = "Toggle plto chant on or off",
        usage = "/plot chat [on|off]",
        permission = "plots.chat",
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.PLAYER
)
public class Chat extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String ... args) {
        
        final String world = plr.getLocation().getWorld();
        if (!PS.get().isPlotWorld(world)) {
            return !sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        boolean enable = !(plr.getMeta("chat") != null && (Boolean) plr.getMeta("chat"));
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("on")) {
                enable = true;
            } else if (args[0].equalsIgnoreCase("off")) {
                enable = false;
            }
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (!enable && plotworld.PLOT_CHAT) {
            return !sendMessage(plr, C.PLOT_CHAT_FORCED);
        }
        plr.setMeta("chat", enable);
        return sendMessage(plr, enable ? C.PLOT_CHAT_ON : C.PLOT_CHAT_OFF);
    }
}
