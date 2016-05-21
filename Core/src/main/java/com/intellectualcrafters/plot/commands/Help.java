package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.helpmenu.HelpMenu;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "help",
        description = "Get this help menu",
        aliases = {"he", "?"},
        category = CommandCategory.INFO,
        usage="help [category|#]",
        permission="plots.use")
public class Help extends Command {
    public Help(Command parent) {
        super(parent, true);
    }

    @Override
    public boolean canExecute(PlotPlayer player, boolean message) {
        return true;
    }

    @Override
    public void execute(PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone) {
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
                        displayHelp(player, args[1], Integer.parseInt(args[1]));
                    } catch (NumberFormatException ignored) {
                        displayHelp(player, args[1], 1);
                    }
                }
                return;
            default:
                C.COMMAND_SYNTAX.send(player, getUsage());
        }
    }

    public void displayHelp(PlotPlayer player, String cat, int page) {
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
            builder.append(C.HELP_HEADER.s());
            for (CommandCategory c : CommandCategory.values()) {
                builder.append(
                        "\n" + StringMan.replaceAll(C.HELP_INFO_ITEM.s(), "%category%", c.toString().toLowerCase(), "%category_desc%", c.toString()));
            }
            builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", "all").replaceAll("%category_desc%", "Display all commands"));
            builder.append("\n" + C.HELP_FOOTER.s());
            MainUtil.sendMessage(player, builder.toString(), false);
            return;
        }
        page--;
        new HelpMenu(player).setCategory(catEnum).getCommands().generateMaxPages().generatePage(page, getParent().toString()).render();
    }
}
