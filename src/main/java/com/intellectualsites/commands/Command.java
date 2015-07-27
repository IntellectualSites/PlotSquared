package com.intellectualsites.commands;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.intellectualcrafters.plot.commands.CommandCategory;

public abstract class Command extends CommandManager {

    private Class requiredType = Object.class;
    private String command, usage = "", description = "", permission = "";
    private Set<String> aliases = new HashSet<>();
    private CommandCategory category;
    protected Argument[] requiredArguments;

    public Command() {
        super(null, new ArrayList<Command>());
    }

    public Command(String command) {
        super(null, new ArrayList<Command>());
        this.command = command;
    }

    public Command(String command, String usage) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
    }

    public Command(String command, String usage, String description) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
        this.description = description;
    }

    public Command(String command, String usage, String description, String permission) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
        this.description = description;
        this.permission = permission;
    }

    public Command(String command, String[] aliases, String usage) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
        this.usage = usage;
    }

    public Command(String command, String[] aliases) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
    }

    public Command(String command, String usage, String description, String permission, String[] aliases, Class requiredType) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
        this.description = description;
        this.permission = permission;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
        this.requiredType = requiredType;
    }

    final public Class getRequiredType() {
        return this.requiredType;
    }

    final protected void create() {
        Annotation annotation = getClass().getAnnotation(CommandDeclaration.class);
        if (annotation == null) {
            throw new RuntimeException("Command does not have a CommandDeclaration");
        }
        CommandDeclaration declaration = (CommandDeclaration) annotation;
        this.command = declaration.command();
        this.usage = declaration.usage();
        this.description = declaration.description();
        this.usage = declaration.usage();
        this.permission = declaration.permission();
        this.aliases = new HashSet<>(Arrays.asList(declaration.aliases()));
        this.requiredType = declaration.requiredType();
        this.category = declaration.category();
    }

    @Override
    final public String toString() {
        return this.command;
    }
    @Override
    final public int hashCode() {
        return this.command.hashCode();
    }

    public abstract boolean onCommand(CommandCaller caller, String[] arguments);

    final public int handle(CommandCaller caller, String[] args) {
        if (args.length == 0) {
            return super.handle(caller, "");
        }
        StringBuilder builder = new StringBuilder();
        for (String s : args) {
            builder.append(s).append(" ");
        }
        String s = builder.substring(0, builder.length() - 1);
        return super.handle(caller, s);
    }

    final public String getCommand() {
        return this.command;
    }

    final public String getUsage() {
        return this.usage;
    }

    final public String getPermission() {
        return this.permission;
    }

    final public String getDescription() {
        return this.description;
    }

    final public Set<String> getAliases() {
        return this.aliases;
    }

    final public Argument[] getRequiredArguments() {
        return this.requiredArguments;
    }

    final public CommandCategory getCategory() {
        return this.category;
    }
}
