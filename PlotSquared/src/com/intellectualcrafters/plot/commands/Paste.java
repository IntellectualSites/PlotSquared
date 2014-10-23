package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.*;

import org.bukkit.entity.Player;

/**
 * Created by Citymonstret on 2014-10-12.
 */
public class Paste extends SubCommand {

    public Paste() {
        super(Command.PASTE, "Paste a plot", "paste", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(Player plr, String... args) {
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }
        Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(plr.getUniqueId()))
                && !PlotMain.hasPermission(plr,"plots.admin")) {
            PlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (!PlayerFunctions.getTopPlot(plr.getWorld(), plot).equals(PlayerFunctions.getBottomPlot(plr.getWorld(), plot))) {
            PlayerFunctions.sendMessage(plr, C.UNLINK_REQUIRED);
            return false;
        }
        assert plot != null;
        int size = (PlotHelper.getPlotTopLocAbs(plr.getWorld(), plot.getId()).getBlockX() - PlotHelper.getPlotBottomLocAbs(plr.getWorld(), plot.getId()).getBlockX());

        if(PlotSelection.currentSelection.containsKey(plr.getName())) {
            PlotSelection selection = PlotSelection.currentSelection.get(plr.getName());
            if(size != selection.getWidth()) {
                sendMessage(plr, C.PASTE_FAILED, "The size of the current plot is not the same as the paste");
                return false;
            }
            selection.paste(plr.getWorld(), plot);
            sendMessage(plr, C.PASTED);
        } else {
            sendMessage(plr, C.NO_CLIPBOARD);
            return false;
        }
        PlotSelection.currentSelection.remove(plr.getName());
        return true;
    }
}
