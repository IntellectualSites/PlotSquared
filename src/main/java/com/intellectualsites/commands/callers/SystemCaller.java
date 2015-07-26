package com.intellectualsites.commands.callers;

import com.intellectualsites.commands.Argument;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;

public class SystemCaller implements CommandCaller {

    public boolean hasPermission(String permission) {
        return true;
    }

    public void message(String message) {
        System.out.println(message);
    }

    public Object getSuperCaller() {
        return new Object();
    }

    public void sendRequiredArgumentsList(CommandManager manager, Command cmd, Argument[] required) {
        StringBuilder builder = new StringBuilder();
        builder.append(manager.getInitialCharacter()).append(cmd.getCommand()).append(" requires ");
        for (Argument argument : required) {
            builder.append(argument.getName()).append(" (").append(argument.getExample()).append("), ");
        }
        message(builder.substring(0, builder.length() - 2));
    }
}
