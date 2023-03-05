/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.CaptionHolder;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.PermissionHolder;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.StringComparison;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class Command {

    static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

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
    private Caption description;
    private String permission;
    private boolean confirmation;
    private CommandCategory category;
    private Argument<?>[] arguments;

    public Command(
            Command parent, boolean isStatic, String id, String permission,
            RequiredType required, CommandCategory category
    ) {
        this.parent = parent;
        this.isStatic = isStatic;
        this.id = id;
        this.permission = permission;
        this.required = required;
        this.category = category;
        this.aliases = Collections.singletonList(id);
        if (this.parent != null) {
            this.parent.register(this);
        }
    }

    public Command(Command parent, boolean isStatic) {
        this.parent = parent;
        this.isStatic = isStatic;
        CommandDeclaration cdAnnotation = getClass().getAnnotation(CommandDeclaration.class);
        if (cdAnnotation != null) {
            init(cdAnnotation);
        }
        for (final Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandDeclaration.class)) {
                Class<?>[] types = method.getParameterTypes();
                // final PlotPlayer<?> player, String[] args, RunnableVal3<Command,Runnable,Runnable> confirm, RunnableVal2<Command, CommandResult>
                // whenDone
                if (types.length == 5 && types[0] == Command.class && types[1] == PlotPlayer.class
                        && types[2] == String[].class && types[3] == RunnableVal3.class
                        && types[4] == RunnableVal2.class) {
                    Command tmp = new Command(this, true) {
                        @Override
                        public CompletableFuture<Boolean> execute(
                                PlotPlayer<?> player, String[] args,
                                RunnableVal3<Command, Runnable, Runnable> confirm,
                                RunnableVal2<Command, CommandResult> whenDone
                        ) {
                            try {
                                method.invoke(Command.this, this, player, args, confirm, whenDone);
                                return CompletableFuture.completedFuture(true);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            return CompletableFuture.completedFuture(false);
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

    public List<Command> getCommands(PlotPlayer<?> player) {
        List<Command> commands = new ArrayList<>();
        for (Command cmd : this.allCommands) {
            if (cmd.canExecute(player, false)) {
                commands.add(cmd);
            }
        }
        return commands;
    }

    public List<Command> getCommands(CommandCategory category, PlotPlayer<?> player) {
        List<Command> commands = getCommands(player);
        if (category != null) {
            commands.removeIf(command -> command.category != category);
        }
        return commands;
    }

    public List<Command> getCommands() {
        return this.allCommands;
    }

    public boolean hasConfirmation(PermissionHolder player) {
        return this.confirmation && !player.hasPermission(getPermission() + ".confirm.bypass");
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public Caption getDescription() {
        return this.description;
    }

    public RequiredType getRequiredType() {
        return this.required;
    }

    public Argument<?>[] getRequiredArguments() {
        return this.arguments;
    }

    public void setRequiredArguments(Argument<?>[] arguments) {
        this.arguments = arguments;
    }

    public void init(CommandDeclaration declaration) {
        this.id = declaration.command();
        this.permission = declaration.permission();
        this.required = declaration.requiredType();
        this.category = declaration.category();

        List<String> aliasOptions = new ArrayList<>();
        aliasOptions.add(this.id);
        aliasOptions.addAll(Arrays.asList(declaration.aliases()));

        this.aliases = aliasOptions;
        if (declaration.description().isEmpty()) {
            Command parent = getParent();
            // we're collecting the "path" of the command
            List<String> path = new ArrayList<>();
            path.add(this.id);
            while (parent != null && !parent.equals(MainCommand.getInstance())) {
                path.add(parent.getId());
                parent = parent.getParent();
            }
            Collections.reverse(path);
            String descriptionKey = String.join(".", path);
            this.description = TranslatableCaption.of(String.format("commands.description.%s", descriptionKey));
        } else {
            this.description = StaticCaption.of(declaration.description());
        }
        this.usage = declaration.usage();
        this.confirmation = declaration.confirmation();

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
        if (this.permission != null && !this.permission.isEmpty()) {
            return this.permission;
        }
        if (this.parent == null) {
            return "plots.use";
        }
        return "plots." + getFullId();
    }

    public <T> void paginate(
            PlotPlayer<?> player, List<T> c, int size, int page,
            RunnableVal3<Integer, T, CaptionHolder> add, String baseCommand, Caption header
    ) {
        // Calculate pages & index
        if (page < 0) {
            page = 0;
        }
        int totalPages = (int) Math.floor((double) c.size() / size);
        if (page > totalPages) {
            page = totalPages;
        }
        int max = page * size + size;
        if (max > c.size()) {
            max = c.size();
        }
        // Send the header
        player.sendMessage(
                header,
                TagResolver.builder()
                        .tag("cur", Tag.inserting(Component.text(page + 1)))
                        .tag("max", Tag.inserting(Component.text(totalPages + 1)))
                        .tag("amount", Tag.inserting(Component.text(c.size())))
                        .build()
        );
        // Send the page content
        List<T> subList = c.subList(page * size, max);
        int i = page * size;
        for (T obj : subList) {
            i++;
            final CaptionHolder msg = new CaptionHolder();
            add.run(i, obj, msg);
            player.sendMessage(msg.get(), msg.getTagResolvers());
        }
        // Send the footer
        player.sendMessage(
                TranslatableCaption.of("list.page_turn"),
                TagResolver.builder()
                        .tag("cur", Tag.inserting(Component.text(page + 1)))
                        .tag(
                                "command1",
                                Tag.preProcessParsed(baseCommand + " " + page)
                        )
                        .tag("command2", Tag.preProcessParsed(baseCommand + " " + (page + 2)))
                        .tag(
                                "clickable",
                                Tag.inserting(TranslatableCaption.of("list.clickable").toComponent(player))
                        )
                        .build()
        );
    }

    /**
     * @param player   Caller
     * @param args     Arguments
     * @param confirm  Instance, Success, Failure
     * @param whenDone task to run when done
     * @return CompletableFuture {@code true} if the command executed fully, {@code false} in
     *         any other case
     */
    public CompletableFuture<Boolean> execute(
            PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) throws CommandException {
        if (args.length == 0 || args[0] == null) {
            if (this.parent == null) {
                MainCommand.getInstance().help.displayHelp(player, null, 0);
            } else {
                sendUsage(player);
            }
            return CompletableFuture.completedFuture(false);
        }
        if (this.allCommands.isEmpty()) {
            player.sendMessage(
                    StaticCaption.of("Not Implemented: https://github.com/IntellectualSites/PlotSquared/issues"));
            return CompletableFuture.completedFuture(false);
        }
        Command cmd = getCommand(args[0]);
        if (cmd == null) {
            if (this.parent != null) {
                sendUsage(player);
                return CompletableFuture.completedFuture(false);
            }
            // Help command
            try {
                if (!MathMan.isInteger(args[0])) {
                    CommandCategory.valueOf(args[0].toUpperCase());
                }
                // This will default certain syntax to the help command
                // e.g. /plot, /plot 1, /plot claiming
                MainCommand.getInstance().help.execute(player, args, null, null);
                return CompletableFuture.completedFuture(false);
            } catch (IllegalArgumentException ignored) {
            }
            // Command recommendation
            player.sendMessage(TranslatableCaption.of("commandconfig.not_valid_subcommand"));
            List<Command> commands = getCommands(player);
            if (commands.isEmpty()) {
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.did_you_mean"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(MainCommand.getInstance().help.getUsage())))
                );
                return CompletableFuture.completedFuture(false);
            }
            HashSet<String> setArgs = new HashSet<>(args.length);
            for (String arg : args) {
                setArgs.add(arg.toLowerCase());
            }
            String[] allArgs = setArgs.toArray(new String[0]);
            int best = 0;
            for (Command current : commands) {
                int match = getMatch(allArgs, current, player);
                if (match > best) {
                    cmd = current;
                }
            }
            if (cmd == null) {
                cmd = new StringComparison<>(args[0], this.allCommands).getMatchObject();
            }
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.did_you_mean"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(cmd.getUsage())))
            );
            return CompletableFuture.completedFuture(false);
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        if (!cmd.checkArgs(player, newArgs) || !cmd.canExecute(player, true)) {
            return CompletableFuture.completedFuture(false);
        }
        try {
            cmd.execute(player, newArgs, confirm, whenDone);
        } catch (CommandException e) {
            e.perform(player);
        }
        return CompletableFuture.completedFuture(true);
    }

    public boolean checkArgs(PlotPlayer<?> player, String[] args) {
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
                // TODO improve or remove the Argument system
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(StringMan.join(fullSplit, " "))))
                );
                return false;
            }
        }
        return true;
    }

    public int getMatch(String[] args, Command cmd, PlotPlayer<?> player) {
        String perm = cmd.getPermission();
        int count = cmd.getAliases().stream().filter(alias -> alias.startsWith(args[0]))
                .mapToInt(alias -> 5).sum();
        HashSet<String> desc = new HashSet<>();
        Collections.addAll(desc, cmd.getDescription().getComponent(player).split(" "));
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
                    if (arg.equalsIgnoreCase(aSplit)) {
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

    public Command getCommand(Class<?> clazz) {
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

    public boolean canExecute(PlotPlayer<?> player, boolean message) {
        if (player == null) {
            return true;
        }
        if (!this.required.allows(player)) {
            if (message) {
                player.sendMessage(this.required.getErrorMessage());
            }
        } else if (!player.hasPermission(getPermission())) {
            if (message) {
                player.sendMessage(
                        TranslatableCaption.of("permission.no_permission"),
                        TagResolver.resolver("node", Tag.inserting(Component.text(getPermission())))
                );
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

    public void sendUsage(PlotPlayer<?> player) {
        player.sendMessage(
                TranslatableCaption.of("commandconfig.command_syntax"),
                TagResolver.resolver("value", Tag.inserting(Component.text(getUsage())))
        );
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

    public Collection<Command> tabOf(
            PlotPlayer<?> player, String[] input, boolean space,
            String... args
    ) {
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

    public Collection<Command> tab(PlotPlayer<?> player, String[] args, boolean space) {
        switch (args.length) {
            case 0 -> {
                return this.allCommands;
            }
            case 1 -> {
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
            }
            default -> {
                Command cmd = getCommand(args[0]);
                if (cmd != null) {
                    return cmd.tab(player, Arrays.copyOfRange(args, 1, args.length), space);
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public String toString() {
        return !this.aliases.isEmpty() ? this.aliases.get(0) : this.id;
    }

    @Override
    public boolean equals(Object obj) {
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

    @Override
    public int hashCode() {
        return this.getFullId().hashCode();
    }

    public void checkTrue(boolean mustBeTrue, Caption message, TagResolver... args) {
        if (!mustBeTrue) {
            throw new CommandException(message, args);
        }
    }

    public <T> T check(T object, Caption message, TagResolver... args) {
        if (object == null) {
            throw new CommandException(message, args);
        }
        return object;
    }


    public enum CommandResult {
        FAILURE,
        SUCCESS
    }


    public static class CommandException extends RuntimeException {

        private final Caption message;
        private final TagResolver[] args;

        public CommandException(final @Nullable Caption message, final TagResolver... args) {
            this.message = message;
            this.args = args;
        }

        public void perform(final @Nullable PlotPlayer<?> player) {
            if (player != null && message != null) {
                player.sendMessage(message, args);
            }
        }

    }

}
