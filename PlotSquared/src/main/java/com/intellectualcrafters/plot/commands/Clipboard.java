package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotSelection;
import org.bukkit.entity.Player;

import static com.intellectualcrafters.plot.PlotSelection.currentSelection;
/**
 * Created by Citymonstret on 2014-10-13.
 */
public class Clipboard extends SubCommand {

    public Clipboard() {
        super(Command.CLIPBOARD, "View information about your current copy", "clipboard", CommandCategory.INFO, true);
    }

    @Override
    public boolean execute(Player plr, String... args) {
        if(!currentSelection.containsKey(plr.getName())) {
            sendMessage(plr, C.NO_CLIPBOARD);
            return true;
        }
        PlotSelection selection = currentSelection.get(plr.getName());

        PlotId plotId = selection.getPlot().getId();
        int width = selection.getWidth();
        int total = selection.getBlocks().length;

        String message = C.CLIPBOARD_INFO.s();

        message = message.replace("%id", plotId.toString()).replace("%width", width + "").replace("%total", total + "");

        PlayerFunctions.sendMessage(plr, message);

        return true;
    }
}
