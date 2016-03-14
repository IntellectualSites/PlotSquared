package com.plotsquared.general.commands;

import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.commands.RequiredType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Command<E extends CommandCaller> extends CommandManager {

    protected Argument<?>[] requiredArguments;
    private RequiredType requiredType = RequiredType.NONE;
    private String command, usage = "", description = "", permission = "";
    private Set<String> aliases = new HashSet<>();
    private CommandCategory category;
    private int hash;

    public Command() {
        super(null, new ArrayList<Command>());
    }
    
    public Command(final String command) {
        super(null, new ArrayList<Command>());
        this.command = command;
    }
    
    public Command(final String command, final String usage) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
    }
    
    public Command(final String command, final String usage, final String description) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
        this.description = description;
    }
    
    public Command(final String command, final String usage, final String description, final String permission) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
        this.description = description;
        this.permission = permission;
    }
    
    public Command(final String command, final String[] aliases, final String usage) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
        this.usage = usage;
    }
    
    public Command(final String command, final String[] aliases) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
    }
    
    public Command(final String command, final String usage, final String description, final String permission, final String[] aliases, final RequiredType requiredType) {
        super(null, new ArrayList<Command>());
        this.command = command;
        this.usage = usage;
        this.description = description;
        this.permission = permission;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
        this.requiredType = requiredType;
    }
    
    public RequiredType getRequiredType() {
        return this.requiredType;
    }
    
    public void create() {
        final Annotation annotation = getClass().getAnnotation(CommandDeclaration.class);
        if (annotation == null) {
            throw new RuntimeException("Command does not have a CommandDeclaration");
        }
        final CommandDeclaration declaration = (CommandDeclaration) annotation;
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
    public String toString() {
        return this.command;
    }
    
    public abstract boolean onCommand(final E plr, final String[] arguments);
    
    public int handle(final E plr, final String[] args) {
        if (args.length == 0) {
            return super.handle(plr, "");
        }
        final StringBuilder builder = new StringBuilder();
        for (final String s : args) {
            builder.append(s).append(" ");
        }
        final String s = builder.substring(0, builder.length() - 1);
        return super.handle(plr, s);
    }
    
    public String getCommand() {
        return this.command;
    }
    
    public String getUsage() {
        if (this.usage.isEmpty()) {
            return "/{label} " + command;
        }
        return this.usage;
    }
    
    public String getPermission() {
        if ((this.permission == null) || (this.permission.isEmpty())) {
            this.permission = "plots." + command.toLowerCase();
        }
        return this.permission;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public Set<String> getAliases() {
        return this.aliases;
    }
    
    public Argument<?>[] getRequiredArguments() {
        if (this.requiredArguments == null) {
            return new Argument<?>[0];
        }
        return this.requiredArguments;
    }
    
    public CommandCategory getCategory() {
        if (category == null) {
            return CommandCategory.DEBUG;
        }
        return this.category;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
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
    
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = getCommand().hashCode();
        }
        return hash;
    }
}
