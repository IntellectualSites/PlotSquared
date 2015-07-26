package com.intellectualcrafters.plot.commands.callers;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualsites.commands.Argument;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.callers.CommandCaller;

public class PlotPlayerCaller implements CommandCaller<PlotPlayer> {

    private final PlotPlayer player;

    public PlotPlayerCaller(final PlotPlayer player) {
        this.player = player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void message(String message) {
        player.sendMessage(message);
    }

    @Override
    public PlotPlayer getSuperCaller() {
        return player;
    }

    @Override
    public void message(C c, String... args) {
        MainUtil.sendMessage(getSuperCaller(), c, args);
    }

    @Override
    public void sendRequiredArgumentsList(CommandManager manager, Command cmd, Argument[] required) {
        message(C.COMMAND_SYNTAX, cmd.getUsage());
        message("Argument list is yet to be implemented");
    }
}
