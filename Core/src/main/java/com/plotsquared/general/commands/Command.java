package com.plotsquared.general.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Command {

    // May be none
    private ArrayList<Command> allCommands = new ArrayList<>();
    private ArrayList<Command> dynamicCommands = new ArrayList<>();
    private HashMap<String, Command> staticCommands = new HashMap<>();

    // Parent command (may be null)
    private Command parent;

    // The command ID
    private String id;
    private List<String> aliases;
    private RequiredType required;
    private String usage;
    private String description;
    private boolean isStatic;
    private String perm;
    private boolean confirmation;
    private CommandCategory category;
    private Argument[] arguments;

    public Command getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public List<Command> getCommands(PlotPlayer player) {
        List<Command> commands = new ArrayList<>();
        for (Command cmd : allCommands) {
            if (cmd.canExecute(player, false)) {
                commands.add(cmd);
            }
        }
        return commands;
    }

    public List<Command> getCommands(CommandCategory cat, PlotPlayer player) {
        List<Command> cmds = getCommands(player);
        if (cat != null) {
            Iterator<Command> iter = cmds.iterator();
            while (iter.hasNext()) {
                if (iter.next().category != cat) {
                    iter.remove();
                }
            }
        }
        return cmds;
    }

    public List<Command> getCommands() {
        return allCommands;
    }

    public boolean hasConfirmation(PlotPlayer player) {
        return confirmation && !player.hasPermission(getPermission() + ".confirm.bypass");
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public RequiredType getRequiredType() {
        return required;
    }

    public Argument[] getRequiredArguments() {
        return arguments;
    }

    public void setRequiredArguments(Argument[] arguments) {
        this.arguments = arguments;
    }

    public Command(Command parent, boolean isStatic, String id, String perm, RequiredType required, CommandCategory cat) {
        this.parent = parent;
        this.isStatic = isStatic;
        this.id = id;
        this.perm = perm;
        this.required = required;
        this.category = cat;
        this.aliases = Arrays.asList(id);
    }

    public Command(Command parent, boolean isStatic) {
        this.parent = parent;
        this.isStatic = isStatic;
        final Annotation cdAnnotation = getClass().getAnnotation(CommandDeclaration.class);
        if (cdAnnotation != null) {
            final CommandDeclaration declaration = (CommandDeclaration) cdAnnotation;
            init(declaration);
        }
        for (final Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandDeclaration.class)) {
                Class<?>[] types = method.getParameterTypes();
                // final PlotPlayer player, String[] args, RunnableVal3<Command,Runnable,Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone
                if (types.length == 5 && types[0] == Command.class && types[1] == PlotPlayer.class && types[2] == String[].class && types[3] == RunnableVal3.class && types[4] == RunnableVal2.class) {
                    Command tmp = new Command(this, true) {
                        @Override
                        public void execute(PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone) {
                            try {
                                method.invoke(Command.this, this, player, args, confirm, whenDone);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    tmp.init(method.getAnnotation(CommandDeclaration.class));
                }
            }
        }
    }

    public void init(CommandDeclaration declaration) {
        this.id = declaration.command();
        this.perm = declaration.permission();
        this.required = declaration.requiredType();
        this.category = declaration.category();
        HashMap<String, Object> options = new HashMap<>();
        List<String> aliasOptions = new ArrayList<>();
        aliasOptions.add(id);
        aliasOptions.addAll(Arrays.asList(declaration.aliases()));
        options.put("aliases", aliasOptions);
        options.put("description", declaration.description());
        options.put("usage", declaration.usage());
        options.put("confirmation", declaration.confirmation());
        boolean set = false;
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String key = id + "." + entry.getKey();
            if (!PS.get().commands.contains(key)) {
                PS.get().commands.set(key, entry.getValue());
                set = true;
            }
        }
        if (set) {
            try {
                PS.get().commands.save(PS.get().commandsFile);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        aliases = PS.get().commands.getStringList(id + ".aliases");
        description = PS.get().commands.getString(id + ".description");
        usage = PS.get().commands.getString(id + ".usage");
        confirmation = PS.get().commands.getBoolean(id + ".confirmation");
        if (parent != null) {
            parent.register(this);
        }
    }

    public void register(Command command) {
        if (command.isStatic) {
            for (String alias : command.aliases) {
                staticCommands.put(alias.toLowerCase(), command);
            }
        } else {
            dynamicCommands.add(command);
        }
        allCommands.add(command);
    }

    public enum CommandResult {
        FAILURE,
        SUCCESS
    }

    public String getPermission() {
        if (perm != null && perm.length() != 0) {
            return perm;
        }
        if (parent == null) {
            return "plots.use";
        }
        if (parent.parent == null) {
            return "plots." + id;
        }
        return parent.getPermission() + "." + id;
    }

    public <T> void paginate(PlotPlayer player, List<T> c, int size, int page, RunnableVal3<Integer, T, PlotMessage> add, String baseCommand, String header) {
        // Calculate pages & index
        if (page < 0) {
            page = 0;
        }
        final int totalPages = (int) Math.ceil(c.size() / size);
        if (page > totalPages) {
            page = totalPages;
        }
        int max = page * size + size;
        if (max > c.size()) {
            max = c.size();
        }
        // Send the header
        header = header.replaceAll("%cur", page + 1 + "").replaceAll("%max", totalPages + 1 + "").replaceAll("%amount%", c.size() + "").replaceAll("%word%", "all");
        MainUtil.sendMessage(player, header);
        // Send the page content
        final List<T> subList = c.subList(page * size, max);
        int i = page * size;
        for (final T obj : subList) {
            i++;
            PlotMessage msg = new PlotMessage();
            add.run(i, obj, msg);
            msg.send(player);
        }
        // Send the footer
        if (page < totalPages && page > 0) { // Back | Next
            new PlotMessage().text("<-").color("$1").command(baseCommand + " " + page).text(" | ").color("$3").text("->").color("$1")
                    .command(baseCommand + " " + (page + 2))
                    .text(C.CLICKABLE.s()).color("$2").send(player);
            return;
        }
        if (page == 0 && totalPages != 0) { // Next
            new PlotMessage().text("<-").color("$3").text(" | ").color("$3").text("->").color("$1").command(baseCommand + " " + (page + 2)).text(C.CLICKABLE.s()).color("$2").send(player);
            return;
        }
        if (page == totalPages && totalPages != 0) { // Back
            new PlotMessage().text("<-").color("$1").command(baseCommand + " " + page).text(" | ").color("$3").text("->").color("$3")
                    .text(C.CLICKABLE.s()).color("$2").send(player);
        }
    }

    /**
     *
     * @param player Caller
     * @param args Arguments
     * @param confirm Instance, Success, Failure
     * @return
     */
    public void execute(final PlotPlayer player, String[] args, RunnableVal3<Command,Runnable,Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone) {
        if (args.length == 0 || args[0] == null) {
            if (parent == null) {
                MainCommand.getInstance().help.displayHelp(player, null, 0);
            } else {
                C.COMMAND_SYNTAX.send(player, getUsage());
            }
            return;
        }
        if (allCommands == null || allCommands.size() == 0) {
            player.sendMessage("Not Implemented: https://github.com/IntellectualSites/PlotSquared/issues/new");
            return;
        }
        Command cmd = getCommand(args[0]);
        if (cmd == null) {
            if (parent != null) {
                C.COMMAND_SYNTAX.send(player, getUsage());
                return;
            }
            // Help command
            try {
                if (args.length == 0 || MathMan.isInteger(args[0]) || CommandCategory.valueOf(args[0].toUpperCase()) != null) {
                    // This will default certain syntax to the help command
                    // e.g. /plot, /plot 1, /plot claiming
                    MainCommand.getInstance().help.execute(player, args, null, null);
                    return;
                }
            } catch (IllegalArgumentException e) {}
            // Command recommendation
            MainUtil.sendMessage(player, C.NOT_VALID_SUBCOMMAND);
            {
                List<Command> cmds = getCommands(player);
                if (cmds.isEmpty()) {
                    MainUtil.sendMessage(player, C.DID_YOU_MEAN, MainCommand.getInstance().help.getUsage());
                    return;
                }
                HashSet<String> setargs = new HashSet<>(args.length);
                for (String arg : args) {
                    setargs.add(arg.toLowerCase());
                }
                String[] allargs = setargs.toArray(new String[setargs.size()]);
                int best = 0;
                for (Command current : cmds) {
                    int match = getMatch(allargs, current);
                    if (match > best) {
                        cmd = current;
                    }
                }
                if (cmd == null) {
                    cmd = new StringComparison<>(args[0], allCommands).getMatchObject();
                }
                MainUtil.sendMessage(player, C.DID_YOU_MEAN, cmd.getUsage());
            }
            return;
        }
        Argument<?>[] reqArgs = cmd.getRequiredArguments();
        if ((reqArgs != null) && (reqArgs.length > 0)) {
            boolean failed = args.length > reqArgs.length;
            String[] baseSplit = getCommandString().split(" ");
            String[] fullSplit = getUsage().split(" ");
            String base = getCommandString();
            for (int i = 0; i < reqArgs.length; i++) {
                fullSplit[i + baseSplit.length] = reqArgs[i].getExample().toString();
                failed = failed || reqArgs[i].parse(args[i]) == null;
            }
            if (failed) {
                C.COMMAND_SYNTAX.send(player, StringMan.join(fullSplit, " "));
                return;
            }
        }
        if (!cmd.canExecute(player, true)) {
            return;
        }
        cmd.execute(player, Arrays.copyOfRange(args, 1, args.length), confirm, whenDone);
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
        Command cmd = staticCommands.get(arg.toLowerCase());
        if (cmd == null) {
            for (Command command : dynamicCommands) {
                if (command.matches(arg)) {
                    return command;
                }
            }
        }
        return cmd;
    }

    public boolean canExecute(PlotPlayer player, boolean message) {
        if (!required.allows(player)) {
            if (message) {
                MainUtil.sendMessage(player, required == RequiredType.PLAYER ? C.IS_CONSOLE : C.NOT_CONSOLE);
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
        return StringMan.isEqual(arg, id) || aliases.contains(arg);
    }

    public String getCommandString() {
        String base;
        if (parent == null) {
            return "/" + toString();
        } else {
            return parent.getCommandString() + " " + toString();
        }
    }

    public String getUsage() {
        if (usage != null && usage.length() != 0) {
            if (usage.startsWith("/")) {
                return usage;
            }
            return getCommandString() + " " + usage;
        }
        if (allCommands.size() == 0) {
            return getCommandString();
        }
        StringBuilder args = new StringBuilder("[");
        String prefix = "";
        for (Command cmd : allCommands) {
            args.append(prefix).append(cmd.isStatic ? cmd.toString() : "<" + cmd + ">");
            prefix = "|";
        }
        return getCommandString() + " " + args + "]";
    }

    public Collection tab(PlotPlayer player, String[] args, boolean space) {
        switch (args.length) {
            case 0:
                return allCommands;
            case 1:
                String arg = args[0].toLowerCase();
                if (space) {
                    Command cmd = getCommand(arg);
                    return (cmd != null && cmd.canExecute(player, false)) ? (cmd.tab(player, Arrays.copyOfRange(args, 1, args.length), space)) : null;
                } else {
                    Set<Command> commands = new HashSet<Command>();
                    for (Map.Entry<String, Command> entry : staticCommands.entrySet()) {
                        if (entry.getKey().startsWith(arg) && entry.getValue().canExecute(player, false)) {
                            commands.add(entry.getValue());
                        }
                    }
                    return commands;
                }
            default:
                Command cmd = getCommand(args[0]);
                return cmd != null ? cmd.tab(player, Arrays.copyOfRange(args, 1, args.length), space) : null;
        }
    }

    @Override
    public String toString() {
        return aliases.size() > 0 ? aliases.get(0) : id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Command other = (Command) obj;
        if (this.hashCode() != other.hashCode()) {
            return false;
        }
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
