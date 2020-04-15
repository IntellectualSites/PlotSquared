package com.plotsquared.core.util.helpmenu;

import com.plotsquared.core.command.Command;
import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.player.PlotPlayer;

import java.util.List;

public class HelpMenu {

    private static final int PER_PAGE = 5;

    private final PlotPlayer commandCaller;
    private HelpPage page = new HelpPage(CommandCategory.INFO, 0, 0);
    private int maxPage;
    private CommandCategory commandCategory;
    private List<Command> commands;

    public HelpMenu(PlotPlayer commandCaller) {
        this.commandCaller = commandCaller;
    }

    public HelpMenu setCategory(CommandCategory commandCategory) {
        this.commandCategory = commandCategory;
        return this;
    }

    public HelpMenu getCommands() {
        this.commands =
            MainCommand.getInstance().getCommands(this.commandCategory, this.commandCaller);
        return this;
    }

    public HelpMenu setCommands(final List<Command> commands) {
        this.commands = commands;
        return this;
    }

    public HelpMenu generateMaxPages() {
        this.maxPage = Math.max((this.commands.size() - 1) / PER_PAGE, 0);
        return this;
    }

    public HelpMenu generatePage(int currentPage, String label) {
        if (currentPage > this.maxPage) {
            currentPage = this.maxPage;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        this.page = new HelpPage(this.commandCategory, currentPage, this.maxPage);
        int max = Math.min((currentPage * PER_PAGE) + (PER_PAGE - 1), this.commands.size());
        for (int i = currentPage * PER_PAGE; i < max; i++) {
            this.page.addHelpItem(new HelpObject(this.commands.get(i), label));
        }
        return this;
    }

    public void render() {
        this.page.render(this.commandCaller);
    }

}
