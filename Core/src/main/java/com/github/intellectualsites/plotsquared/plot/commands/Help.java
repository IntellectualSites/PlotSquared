package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandCaller;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.helpmenu.HelpMenu;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "help", description = "Get this help menu", aliases = "?",
    category = CommandCategory.INFO, usage = "help [category|#]", permission = "plots.use")
public class Help extends Command {
    public Help(Command parent) {
        super(parent, true);
    }

    @Override public boolean canExecute(CommandCaller player, boolean message) {
        return true;
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        switch (args.length) {
            case 0:
                return displayHelp(player, null, 0);
            case 1:
                if (MathMan.isInteger(args[0])) {
                    try {
                        return displayHelp(player, null, Integer.parseInt(args[0]));
                    } catch (NumberFormatException ignored) {
                        return displayHelp(player, null, 1);
                    }
                } else {
                    return displayHelp(player, args[0], 1);
                }
            case 2:
                if (MathMan.isInteger(args[1])) {
                    try {
                        return displayHelp(player, args[0], Integer.parseInt(args[1]));
                    } catch (NumberFormatException ignored) {
                        return displayHelp(player, args[0], 1);
                    }
                }
                return CompletableFuture.completedFuture(false);
            default:
                Captions.COMMAND_SYNTAX.send(player, getUsage());
        }
        return CompletableFuture.completedFuture(true);
    }

    public CompletableFuture<Boolean> displayHelp(final CommandCaller player, final String catRaw, final int page) {
        return CompletableFuture.supplyAsync(() -> {
            String cat = catRaw;

            CommandCategory catEnum = null;
            if (cat != null) {
                if (!"all".equalsIgnoreCase(cat)) {
                    for (CommandCategory c : CommandCategory.values()) {
                        if (StringMan.isEqualIgnoreCaseToAny(cat, c.name(), c.toString())) {
                            catEnum = c;
                            cat = c.name();
                            break;
                        }
                    }
                    if (catEnum == null) {
                        cat = null;
                    }
                }
            }
            if (cat == null && page == 0) {
                StringBuilder builder = new StringBuilder();
                builder.append(Captions.HELP_HEADER.getTranslated());
                for (CommandCategory c : CommandCategory.values()) {
                    builder.append("\n").append(StringMan
                        .replaceAll(Captions.HELP_INFO_ITEM.getTranslated(), "%category%",
                            c.toString().toLowerCase(),
                            "%category_desc%", c.toString()));
                }
                builder.append("\n")
                    .append(Captions.HELP_INFO_ITEM.getTranslated().replaceAll("%category%", "all")
                    .replaceAll("%category_desc%", "Display all commands"));
                builder.append("\n").append(Captions.HELP_FOOTER.getTranslated());
                MainUtil.sendMessage(player, builder.toString(), false);
                return true;
            }
            new HelpMenu(player).setCategory(catEnum).getCommands().generateMaxPages()
                .generatePage(page - 1, getParent().toString()).render();
            return true;
        });
    }
}
