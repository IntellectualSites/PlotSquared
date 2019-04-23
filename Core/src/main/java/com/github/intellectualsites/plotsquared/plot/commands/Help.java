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

@CommandDeclaration(command = "help", description = "Get this help menu", aliases = {"he", "?"},
    category = CommandCategory.INFO, usage = "help [category|#]", permission = "plots.use")
public class Help extends Command {
    public Help(Command parent) {
        super(parent, true);
    }

    @Override public boolean canExecute(CommandCaller player, boolean message) {
        return true;
    }

    @Override public void execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        switch (args.length) {
            case 0:
                displayHelp(player, null, 0);
                return;
            case 1:
                if (MathMan.isInteger(args[0])) {
                    try {
                        displayHelp(player, null, Integer.parseInt(args[0]));
                    } catch (NumberFormatException ignored) {
                        displayHelp(player, null, 1);
                    }
                } else {
                    displayHelp(player, args[0], 1);
                }
                return;
            case 2:
                if (MathMan.isInteger(args[1])) {
                    try {
                        displayHelp(player, args[0], Integer.parseInt(args[1]));
                    } catch (NumberFormatException ignored) {
                        displayHelp(player, args[0], 1);
                    }
                }
                return;
            default:
                Captions.COMMAND_SYNTAX.send(player, getUsage());
        }
    }

    public void displayHelp(CommandCaller player, String cat, int page) {
        CommandCategory catEnum = null;
        if (cat != null) {
            if (StringMan.isEqualIgnoreCase(cat, "all")) {
                catEnum = null;
            } else {
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
            builder.append(Captions.HELP_HEADER.s());
            for (CommandCategory c : CommandCategory.values()) {
                builder.append("\n" + StringMan
                    .replaceAll(Captions.HELP_INFO_ITEM.s(), "%category%",
                        c.toString().toLowerCase(), "%category_desc%", c.toString()));
            }
            builder.append("\n").append(Captions.HELP_INFO_ITEM.s().replaceAll("%category%", "all")
                .replaceAll("%category_desc%", Captions.HELP_DISPLAY_ALL_COMMANDS.s()));
            builder.append("\n" + Captions.HELP_FOOTER.s());
            MainUtil.sendMessage(player, builder.toString(), false);
            return;
        }
        page--;
        new HelpMenu(player).setCategory(catEnum).getCommands().generateMaxPages()
            .generatePage(page, getParent().toString()).render();
    }
}
