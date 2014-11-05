package com.intellectualcrafters.plot.commands;

import static com.intellectualcrafters.plot.PlotSelection.currentSelection;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotSelection;

/**
 * Created by Citymonstret on 2014-10-13.
 */
public class Clipboard extends SubCommand {

    public Clipboard() {
        super(Command.CLIPBOARD, "View information about your current copy", "clipboard", CommandCategory.INFO, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!currentSelection.containsKey(plr.getName())) {
            sendMessage(plr, C.NO_CLIPBOARD);
            return true;
        }
        final PlotSelection selection = currentSelection.get(plr.getName());

        final PlotId plotId = selection.getPlot().getId();
        final int width = selection.getWidth();
        final int total = selection.getBlocks().length;

        String message = C.CLIPBOARD_INFO.s();

        message = message.replace("%id", plotId.toString()).replace("%width", width + "").replace("%total", total + "");

        PlayerFunctions.sendMessage(plr, message);

        return true;
    }
}
