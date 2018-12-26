package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.CommandCategory;
import com.github.intellectualsites.plotsquared.plot.commands.MainCommand;
import com.github.intellectualsites.plotsquared.plot.commands.RequiredType;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.PlotMessage;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class Command {

    // May be none
    private final ArrayList<Command> allCommands = new ArrayList<>();
    private final ArrayList<Command> dynamicCommands = new ArrayList<>();
    private final HashMap<String, Command> staticCommands = new HashMap<>();

    // Parent command (may be null)
    private final Command parent;
    private final boolean isStatic;
    // The command ID
    private String id;
    private List<String> aliases;
    private RequiredType required;
    private String usage;
    private String description;
    private String perm;
    private boolean confirmation;
    private CommandCategory category;
    private Argument[] arguments;

    public Command(Command parent, boolean isStatic, String id, String perm, RequiredType required,
        CommandCategory cat) {
        this.parent = parent;
        this.isStatic = isStatic;
        this.id = id;
        this.perm = perm;
        this.required = required;
        this.category = cat;
        this.aliases = Collections.singletonList(id);
        if (this.parent != null) {
            this.parent.register(this);
        }
    }

    public Command(Command parent, boolean isStatic) {
        this.parent = parent;
        this.isStatic = isStatic;
        Annotation cdAnnotation = getClass().getAnnotation(CommandDeclaration.class);
        if (cdAnnotation != null) {
            CommandDeclaration declaration = (CommandDeclaration) cdAnnotation;
            init(declaration);
        }
        for (final Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandDeclaration.class)) {
                Class<?>[] types = method.getParameterTypes();
                // final PlotPlayer player, String[] args, RunnableVal3<Command,Runnable,Runnable> confirm, RunnableVal2<Command, CommandResult>
                // whenDone
                if (types.length == 5 && types[0] == Command.class && types[1] == PlotPlayer.class
                    && types[2] == String[].class && types[3] == RunnableVal3.class
                    && types[4] == RunnableVal2.class) {
                    Command tmp = new Command(this, true) {
                        @Override public void execute(PlotPlayer player, String[] args,
                            RunnableVal3<Command, Runnable, Runnable> confirm,
                            RunnableVal2<Command, CommandResult> whenDone) {
                            try {
                                method.invoke(Command.this, this, player, args, confirm, whenDone);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    tmp.init(method.getAnnotation(CommandDeclaration.class));
                }
            }
        }
    }

    public Command getParent() {
        return this.parent;
    }

    public String getId() {
        return this.id;
    }

    public String getFullId() {
        if (this.parent != null && this.parent.getParent() != null) {
            return this.parent.getFullId() + "." + this.id;
        }
        return this.id;
    }

    public List<Command> getCommands(PlotPlayer player) {
        List<Command> commands = new ArrayList<>();
        for (Command cmd : this.allCommands) {
            if (cmd.canExecute(player, false)) {
                commands.add(cmd);
            }
        }
        return commands;
    }

    public List<Command> getCommands(CommandCategory cat, PlotPlayer player) {
        List<Command> commands = getCommands(player);
        if (cat != null) {
            commands.removeIf(command -> command.category != cat);
        }
        return commands;
    }

    public List<Command> getCommands() {
        return this.allCommands;
    }

    public boolean hasConfirmation(PlotPlayer player) {
        return this.confirmation && !player.hasPermission(getPermission() + ".confirm.bypass");
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public String getDescription() {
        return this.description;
    }

    public RequiredType getRequiredType() {
        return this.required;
    }

    public Argument[] getRequiredArguments() {
        return this.arguments;
    }

    public void setRequiredArguments(Argument[] arguments) {
        this.arguments = arguments;
    }

    public void init(CommandDeclaration declaration) {
        this.id = declaration.command();
        this.perm = declaration.permission();
        this.required = declaration.requiredType();
        this.category = declaration.category();
        HashMap<String, Object> options = new HashMap<>();
        List<String> aliasOptions = new ArrayList<>();
        aliasOptions.add(this.id);
        aliasOptions.addAll(Arrays.asList(declaration.aliases()));
        options.put("aliases", aliasOptions);
        options.put("description", declaration.description());
        options.put("usage", declaration.usage());
        options.put("confirmation", declaration.confirmation());
        boolean set = false;
        YamlConfiguration commands =
            PlotSquared.get() == null ? new YamlConfiguration() : PlotSquared.get().commands;
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String key = this.getFullId() + "." + entry.getKey();
            if (!commands.contains(key)) {
                commands.set(key, entry.getValue());
                set = true;
            }
        }
        if (set && PlotSquared.get() != null) {
            try {
                commands.save(PlotSquared.get().commandsFile);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        this.aliases = commands.getStringList(this.getFullId() + ".aliases");
        this.description = commands.getString(this.getFullId() + ".description");
        this.usage = commands.getString(this.getFullId() + ".usage");
        this.confirmation = commands.getBoolean(this.getFullId() + ".confirmation");
        if (this.parent != null) {
            this.parent.register(this);
        }
    }

    public void register(Command command) {
        if (command.isStatic) {
            for (String alias : command.aliases) {
                this.staticCommands.put(alias.toLowerCase(), command);
            }
        } else {
            this.dynamicCommands.add(command);
        }
        this.allCommands.add(command);
    }

    public String getPermission() {
        if (this.perm != null && !this.perm.isEmpty()) {
            return this.perm;
        }
        if (this.parent == null) {
            return "plots.use";
        }
        return "plots." + getFullId();
    }

    public <T> void paginate(PlotPlayer player, List<T> c, int size, int page,
        RunnableVal3<Integer, T, PlotMessage> add, String baseCommand, String header) {
        // Calculate pages & index
        if (page < 0) {
            page = 0;
        }
        int totalPages = (int) Math.ceil(c.size() / size);
        if (page > totalPages) {
            page = totalPages;
        }
        int max = page * size + size;
        if (max > c.size()) {
            max = c.size();
        }
        // Send the header
        header = header.replaceAll("%cur", page + 1 + "").replaceAll("%max", totalPages + 1 + "")
            .replaceAll("%amount%", c.size() + "").replaceAll("%word%", "all");
        MainUtil.sendMessage(player, header);
        // Send the page content
        List<T> subList = c.subList(page * size, max);
        int i = page * size;
        for (T obj : subList) {
            i++;
            PlotMessage msg = new PlotMessage();
            add.run(i, obj, msg);
            msg.send(player);
        }
        // Send the footer
        if (page < totalPages && page > 0) { // Back | Next
            new PlotMessage().text("<-").color("$1").command(baseCommand + " " + page).text(" | ")
                .color("$3").text("->").color("$1").command(baseCommand + " " + (page + 2))
                .text(C.CLICKABLE.s()).color("$2").send(player);
            return;
        }
        if (page == 0 && totalPages != 0) { // Next
            new PlotMessage().text("<-").color("$3").text(" | ").color("$3").text("->").color("$1")
                .command(baseCommand + " " + 2).text(C.CLICKABLE.s()).color("$2").send(player);
            return;
        }
        if (page == totalPages && totalPages != 0) { // Back
            new PlotMessage().text("<-").color("$1").command(baseCommand + " " + page).text(" | ")
                .color("$3").text("->").color("$3").text(C.CLICKABLE.s()).color("$2").send(player);
        }
    }

    /**
     * @param player  Caller
     * @param args    Arguments
     * @param confirm Instance, Success, Failure
     * @return
     */
    public void execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 0 || args[0] == null) {
            if (this.parent == null) {
                MainCommand.getInstance().help.displayHelp(player, null, 0);
            } else {
                C.COMMAND_SYNTAX.send(player, getUsage());
            }
            return;
        }
        if (this.allCommands.isEmpty()) {
            player.sendMessage(
                "Not Implemented: https://github.com/IntellectualSites/PlotSquared/issues/new");
            return;
        }
        Command cmd = getCommand(args[0]);
        if (cmd == null) {
            if (this.parent != null) {
                C.COMMAND_SYNTAX.send(player, getUsage());
                return;
            }
            // Help command
            try {
                // This will default certain syntax to the help command
                // e.g. /plot, /plot 1, /plot claiming
                MainCommand.getInstance().help.execute(player, args, null, null);
                return;
            } catch (IllegalArgumentException ignored) {
            }
            // Command recommendation
            MainUtil.sendMessage(player, C.NOT_VALID_SUBCOMMAND);
            List<Command> commands = getCommands(player);
            if (commands.isEmpty()) {
                MainUtil
                    .sendMessage(player, C.DID_YOU_MEAN, MainCommand.getInstance().help.getUsage());
                return;
            }
            HashSet<String> setargs = new HashSet<>(args.length);
            for (String arg : args) {
                setargs.add(arg.toLowerCase());
            }
            String[] allargs = setargs.toArray(new String[setargs.size()]);
            int best = 0;
            for (Command current : commands) {
                int match = getMatch(allargs, current);
                if (match > best) {
                    cmd = current;
                }
            }
            if (cmd == null) {
                cmd = new StringComparison<>(args[0], this.allCommands).getMatchObject();
            }
            MainUtil.sendMessage(player, C.DID_YOU_MEAN, cmd.getUsage());
            return;
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        if (!cmd.checkArgs(player, newArgs) || !cmd.canExecute(player, true)) {
            return;
        }
        try {
            cmd.execute(player, newArgs, confirm, whenDone);
        } catch (CommandException e) {
            e.perform(player);
        }
    }

    public boolean checkArgs(PlotPlayer player, String[] args) {
        Argument<?>[] reqArgs = getRequiredArguments();
        if (reqArgs != null && reqArgs.length > 0) {
            boolean failed = args.length < reqArgs.length;
            String[] baseSplit = getCommandString().split(" ");
            String[] fullSplit = getUsage().split(" ");
            if (fullSplit.length - baseSplit.length < reqArgs.length) {
                String[] tmp = new String[baseSplit.length + reqArgs.length];
                System.arraycopy(fullSplit, 0, tmp, 0, fullSplit.length);
                fullSplit = tmp;
            }
            for (int i = 0; i < reqArgs.length; i++) {
                fullSplit[i + baseSplit.length] = reqArgs[i].getExample().toString();
                failed = failed || reqArgs[i].parse(args[i]) == null;
            }
            if (failed) {
                C.COMMAND_SYNTAX.send(player, StringMan.join(fullSplit, " "));
                return false;
            }
        }
        return true;
    }

    public int getMatch(String[] args, Command cmd) {
        int count = 0;
        String perm = cmd.getPermission();
        HashSet<String> desc = new HashSet<>();
        for (String alias : cmd.getAliases()) {
            if (alias.startsWith(args[0])) {
                count += 5;
            }
        }
        Collections.addAll(desc, cmd.getDescription().split(" "));
        for (String arg : args) {
            if (perm.startsWith(arg)) {
                count++;
            }
            if (desc.contains(arg)) {
                count++;
            }
        }
        String[] usage = cmd.getUsage().split(" ");
        for (int i = 0; i < Math.min(4, usage.length); i++) {
            int require;
            if (usage[i].startsWith("<")) {
                require = 1;
            } else {
                require = 0;
            }
            String[] split = usage[i].split("\\|| |\\>|\\<|\\[|\\]|\\{|\\}|\\_|\\/");
            for (String aSplit : split) {
                for (String arg : args) {
                    if (StringMan.isEqualIgnoreCase(arg, aSplit)) {
                        count += 5 - i + require;
                    }
                }
            }
        }
        count += StringMan.intersection(desc, args);
        return count;
    }

    public Command getCommand(String arg) {
        Command cmd = this.staticCommands.get(arg.toLowerCase());
        if (cmd == null) {
            for (Command command : this.dynamicCommands) {
                if (command.matches(arg)) {
                    return command;
                }
            }
        }
        return cmd;
    }

    public Command getCommand(Class clazz) {
        for (Command cmd : this.allCommands) {
            if (cmd.getClass() == clazz) {
                return cmd;
            }
        }
        return null;
    }

    public Command getCommandById(String id) {
        Command exact = this.staticCommands.get(id);
        if (exact != null) {
            return exact;
        }
        for (Command cmd : this.allCommands) {
            if (cmd.getId().equals(id)) {
                return cmd;
            }
        }
        return null;
    }

    public boolean canExecute(PlotPlayer player, boolean message) {
        if (player == null) {
            return true;
        }
        if (!this.required.allows(player)) {
            if (message) {
                MainUtil.sendMessage(player,
                    this.required == RequiredType.PLAYER ? C.IS_CONSOLE : C.NOT_CONSOLE);
            }
        } else if (!Permissions.hasPermission(player, getPermission())) {
            if (message) {
                C.NO_PERMISSION.send(player, getPermission());
            }
        } else {
            return true;
        }
        return false;
    }

    public boolean matches(String arg) {
        arg = arg.toLowerCase();
        return StringMan.isEqual(arg, this.id) || this.aliases.contains(arg);
    }

    public String getCommandString() {
        if (this.parent == null) {
            return "/" + toString();
        } else {
            return this.parent.getCommandString() + " " + toString();
        }
    }

    public String getUsage() {
        if (this.usage != null && !this.usage.isEmpty()) {
            if (this.usage.startsWith("/")) {
                return this.usage;
            }
            return getCommandString() + " " + this.usage;
        }
        if (this.allCommands.isEmpty()) {
            return getCommandString();
        }
        StringBuilder args = new StringBuilder("[");
        String prefix = "";
        for (Command cmd : this.allCommands) {
            args.append(prefix).append(cmd.isStatic ? cmd.toString() : "<" + cmd + ">");
            prefix = "|";
        }
        return getCommandString() + " " + args + "]";
    }

    public Collection<Command> tabOf(PlotPlayer player, String[] input, boolean space,
        String... args) {
        if (!space) {
            return null;
        }
        List<Command> result = new ArrayList<>();
        int index = input.length;
        for (String arg : args) {
            arg = arg.replace(getCommandString() + " ", "");
            String[] split = arg.split(" ");
            if (split.length <= index) {
                continue;
            }
            arg = StringMan.join(Arrays.copyOfRange(split, index, split.length), " ");
            Command cmd = new Command(null, false, arg, getPermission(), getRequiredType(), null) {
            };
            result.add(cmd);
        }
        return result;
    }

    public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        switch (args.length) {
            case 0:
                return this.allCommands;
            case 1:
                String arg = args[0].toLowerCase();
                if (space) {
                    Command cmd = getCommand(arg);
                    if (cmd != null && cmd.canExecute(player, false)) {
                        return cmd.tab(player, Arrays.copyOfRange(args, 1, args.length), space);
                    } else {
                        return null;
                    }
                } else {
                    Set<Command> commands = new HashSet<>();
                    for (Map.Entry<String, Command> entry : this.staticCommands.entrySet()) {
                        if (entry.getKey().startsWith(arg) && entry.getValue()
                            .canExecute(player, false)) {
                            commands.add(entry.getValue());
                        }
                    }
                    return commands;
                }
            default:
                Command cmd = getCommand(args[0]);
                if (cmd != null) {
                    return cmd.tab(player, Arrays.copyOfRange(args, 1, args.length), space);
                } else {
                    return null;
                }
        }
    }

    @Override public String toString() {
        return !this.aliases.isEmpty() ? this.aliases.get(0) : this.id;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Command other = (Command) obj;
        if (this.hashCode() != other.hashCode()) {
            return false;
        }
        return this.getFullId().equals(other.getFullId());
    }

    @Override public int hashCode() {
        return this.getFullId().hashCode();
    }

    public void checkTrue(boolean mustBeTrue, C message, Object... args) {
        if (!mustBeTrue) {
            throw new CommandException(message, args);
        }
    }

    public <T> T check(T object, C message, Object... args) {
        if (object == null) {
            throw new CommandException(message, args);
        }
        return object;
    }

    public enum CommandResult {
        FAILURE, SUCCESS
    }


    public static class CommandException extends RuntimeException {
        private final Object[] args;
        private final C message;

        public CommandException(C message, Object... args) {
            this.message = message;
            this.args = args;
        }

        public void perform(PlotPlayer player) {
            if (player != null && message != null) {
                message.send(player, args);
            }
        }
    }
}
