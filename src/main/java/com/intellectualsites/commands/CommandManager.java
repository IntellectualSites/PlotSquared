package com.intellectualsites.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualsites.commands.util.StringUtil;

@SuppressWarnings("unused")
public class CommandManager {

    protected final ConcurrentHashMap<String, Command> commands;
    protected final Character initialCharacter;

    public CommandManager() {
        this('/', new ArrayList<Command>());
    }

    public CommandManager(Character initialCharacter, List<Command> commands) {
        this.commands = new ConcurrentHashMap<>();
        for (Command command : commands) {
            addCommand(command);
        }
        this.initialCharacter = initialCharacter;
    }

    final public void addCommand(final Command command) {
        this.commands.put(command.getCommand().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            this.commands.put(alias.toLowerCase(), command);
        }
    }

    final public boolean createCommand(final Command command) {
        try {
            command.create();
        } catch(final Exception e) {
            e.printStackTrace();
            return false;
        }
        if (command.getCommand() != null) {
            addCommand(command);
            return true;
        }
        return false;
    }

    final public Collection<Command> getCommands() {
        return this.commands.values();
    }

    public int handle(CommandCaller caller, String input) {
        if (initialCharacter != null && !StringUtil.startsWith(initialCharacter, input)) {
            return CommandHandlingOutput.NOT_COMMAND;
        }
        input = initialCharacter == null ? input : input.substring(1);
        String[] parts = input.split(" ");
        String[] args;
        String command = parts[0].toLowerCase();
        if (parts.length == 1) {
            args = new String[0];
        } else {
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
        }
        Command cmd = null;
        cmd = commands.get(command);
        if (cmd == null) {
            return CommandHandlingOutput.NOT_FOUND;
        }
        if (!cmd.getRequiredType().isInstance(caller.getSuperCaller())) {
            return CommandHandlingOutput.CALLER_OF_WRONG_TYPE;
        }
        if (!caller.hasPermission(cmd.getPermission())) {
            return CommandHandlingOutput.NOT_PERMITTED;
        }
        Argument[] requiredArguments = cmd.getRequiredArguments();
        if (requiredArguments != null && requiredArguments.length > 0) {
            boolean success = true;
            if (args.length < requiredArguments.length) {
                success = false;
            } else {
                for (int i = 0; i < requiredArguments.length; i++) {
                    if (requiredArguments[i].parse(args[i]) == null) {
                        success = false;
                        break;
                    }
                }
            }
            if (!success) {
                String usage = cmd.getUsage();
                caller.sendRequiredArgumentsList(this, cmd, requiredArguments);
                return CommandHandlingOutput.WRONG_USAGE;
            }
        }
        try {
            boolean a = cmd.onCommand(caller, args);
            if (!a) {
                String usage = cmd.getUsage();
                if (usage != null && !usage.isEmpty()) {
                    caller.message(usage);
                }
                return CommandHandlingOutput.WRONG_USAGE;
            }
        } catch(final Throwable t) {
            t.printStackTrace();
            return CommandHandlingOutput.ERROR;
        }
        return CommandHandlingOutput.SUCCESS;
    }

    final public char getInitialCharacter() {
        return this.initialCharacter;
    }
}
