package com.intellectualcrafters.plot.commands.callers;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualsites.commands.Argument;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.callers.CommandCaller;

public class ConsoleCaller implements CommandCaller<PS> {
    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public void message(String message) {
        MainUtil.sendConsoleMessage(message);
    }

    @Override
    public PS getSuperCaller() {
        return PS.get();
    }

    @Override
    public void message(C c, String... args) {
        MainUtil.sendConsoleMessage(c, args);
    }

    @Override
    public void sendRequiredArgumentsList(CommandManager manager, Command cmd, Argument[] required) {
        message(C.COMMAND_SYNTAX, cmd.getUsage());
    }
}
