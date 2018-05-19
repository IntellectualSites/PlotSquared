package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;

public abstract class ToggleCommand extends SubCommand {

    public ToggleCommand() { super(MainCommand.getInstance(), true); }

    public ToggleCommand(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        boolean toggleDisabled;
        String key = toggleKey();
        if (player.getAttribute(key)) {
            player.removeAttribute(key);
            toggleDisabled = true;
        } else {
            player.setAttribute(key);
            toggleDisabled = false;
        }

        if (key.equals("disabletitles")) {
            PlotArea area = player.getApplicablePlotArea();
            boolean chat = area != null && area.PLOT_CHAT;
            if (toggleDisabled != chat) {
                toggleDisabled = false;
            } else {
                toggleDisabled = true;
            }
        }

        if (toggleDisabled) {
            MainUtil.sendMessage(player, C.TOGGLE_DISABLED, getCommandString());
        } else {
            MainUtil.sendMessage(player, C.TOGGLE_ENABLED, getCommandString());
        }

        return true;
    }

    public abstract String toggleKey();

}
