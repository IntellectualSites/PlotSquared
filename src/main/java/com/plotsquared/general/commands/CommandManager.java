package com.plotsquared.general.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.util.Permissions;

@SuppressWarnings("unused")
public class CommandManager<T extends CommandCaller> {
    
    final public ConcurrentHashMap<String, Command<T>> commands;
    final private Character initialCharacter;
    
    public CommandManager() {
        this('/', new ArrayList<Command<T>>());
    }
    
    public CommandManager(final Character initialCharacter, final List<Command<T>> commands) {
        this.commands = new ConcurrentHashMap<>();
        for (final Command<T> command : commands) {
            addCommand(command);
        }
        this.initialCharacter = initialCharacter;
    }
    
    final public void addCommand(final Command<T> command) {
        if (command.getCommand() == null) {
            command.create();
        }
        this.commands.put(command.getCommand().toLowerCase(), command);
        for (final String alias : command.getAliases()) {
            this.commands.put(alias.toLowerCase(), command);
        }
    }
    
    final public Command<T> getCommand(final String command) {
        return commands.get(command);
    }
    
    final public boolean createCommand(final Command<T> command) {
        try {
            command.create();
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        if (command.getCommand() != null) {
            addCommand(command);
            return true;
        }
        return false;
    }
    
    final public ArrayList<Command<T>> getCommands() {
        final HashSet<Command<T>> set = new HashSet<>(this.commands.values());
        final ArrayList<Command<T>> result = new ArrayList<>(set);
        Collections.sort(result, new Comparator<Command<T>>() {
            @Override
            public int compare(final Command<T> a, final Command<T> b) {
                if (a == b) {
                    return 0;
                }
                if (a == null) {
                    return -1;
                }
                if (b == null) {
                    return 1;
                }
                return a.getCommand().compareTo(b.getCommand());
            }
        });
        return result;
    }
    
    final public ArrayList<String> getCommandLabels(final ArrayList<Command<T>> cmds) {
        final ArrayList<String> labels = new ArrayList<>(cmds.size());
        for (final Command<T> cmd : cmds) {
            labels.add(cmd.getCommand());
        }
        return labels;
    }
    
    public int handle(final T plr, String input) {
        if ((initialCharacter != null) && !input.startsWith(initialCharacter + "")) {
            return CommandHandlingOutput.NOT_COMMAND;
        }
        input = initialCharacter == null ? input : input.substring(1);
        final String[] parts = input.split(" ");
        String[] args;
        final String command = parts[0].toLowerCase();
        if (parts.length == 1) {
            args = new String[0];
        } else {
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
        }
        Command<T> cmd = null;
        cmd = commands.get(command);
        if (cmd == null) {
            return CommandHandlingOutput.NOT_FOUND;
        }
        if (!cmd.getRequiredType().allows(plr)) {
            return CommandHandlingOutput.CALLER_OF_WRONG_TYPE;
        }
        if (!Permissions.hasPermission(plr, cmd.getPermission())) {
            return CommandHandlingOutput.NOT_PERMITTED;
        }
        final Argument<?>[] requiredArguments = cmd.getRequiredArguments();
        if ((requiredArguments != null) && (requiredArguments.length > 0)) {
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
                cmd.getUsage().replaceAll("\\{label\\}", parts[0]);
                C.COMMAND_SYNTAX.send(plr, cmd.getUsage());
                return CommandHandlingOutput.WRONG_USAGE;
            }
        }
        try {
            final boolean a = cmd.onCommand(plr, args);
            if (!a) {
                final String usage = cmd.getUsage();
                if ((usage != null) && !usage.isEmpty()) {
                    plr.sendMessage(usage);
                }
                return CommandHandlingOutput.WRONG_USAGE;
            }
        } catch (final Throwable t) {
            t.printStackTrace();
            return CommandHandlingOutput.ERROR;
        }
        return CommandHandlingOutput.SUCCESS;
    }
    
    final public char getInitialCharacter() {
        return this.initialCharacter;
    }
}
