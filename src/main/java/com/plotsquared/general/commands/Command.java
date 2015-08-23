package com.plotsquared.general.commands;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.commands.RequiredType;

public abstract class Command<E extends CommandCaller> extends CommandManager {

    private RequiredType requiredType = RequiredType.NONE;
    private String command, usage = "", description = "", permission = "";
    private Set<String> aliases = new HashSet<>();
    private CommandCategory category;
    protected Argument<?>[] requiredArguments;

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

    public Command(String command, String usage, String description, String permission, String[] aliases, RequiredType requiredType) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
        this.description = description;
        this.permission = permission;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
        this.requiredType = requiredType;
    }

    final public RequiredType getRequiredType() {
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

    public abstract boolean onCommand(E plr, String[] arguments);

    final public int handle(E plr, String[] args) {
        if (args.length == 0) {
            return super.handle(plr, "");
        }
        StringBuilder builder = new StringBuilder();
        for (String s : args) {
            builder.append(s).append(" ");
        }
        String s = builder.substring(0, builder.length() - 1);
        return super.handle(plr, s);
    }

    final public String getCommand() {
        return this.command;
    }

    public String getUsage() {
        if (this.usage.length() == 0) {
            return "/{label} " + command; 
        }
        return this.usage;
    }

    final public String getPermission() {
        if (this.permission == null || this.permission.length() == 0) {
            this.permission = "plots." + command.toLowerCase();
        }
        return this.permission;
    }

    public String getDescription() {
        return this.description;
    }

    final public Set<String> getAliases() {
        return this.aliases;
    }

    final public Argument<?>[] getRequiredArguments() {
        if (this.requiredArguments == null) {
            return new Argument<?>[0];
        }
        return this.requiredArguments;
    }

    final public CommandCategory getCategory() {
        if (category == null) {
            return CommandCategory.DEBUG;
        }
        return this.category;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Command<?> other = (Command<?>) obj;
        if (this.hashCode() != other.hashCode()) {
            return false;
        }
        return this.command.equals(other.command);
    }
    
    private int hash;
    
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = getCommand().hashCode();
        }
        return hash;
    }
}
