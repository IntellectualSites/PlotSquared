package com.intellectualcrafters.plot.commands;

public abstract class NamedSubCommand extends SubCommand {

    public NamedSubCommand(Command command, String description, String usage, CommandCategory category, boolean isPlayer) {
        super(command, description, usage, category, isPlayer);
    }
    public NamedSubCommand(String cmd, String permission, String description, String usage, CommandCategory category, boolean isPlayer, String[] aliases) {
        super(cmd, permission, description, usage, category, isPlayer, aliases);
    }
    public NamedSubCommand(String cmd, String permission, String description, String usage, String alias, CommandCategory category, boolean isPlayer) {
        super(cmd, permission, description, usage, alias, category, isPlayer);
    }
}
