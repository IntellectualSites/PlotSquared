package com.intellectualcrafters.plot.util.helpmenu;

import java.util.List;

import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.Command;

public class HelpMenu {
    
    public static final int PER_PAGE = 5;
    
    private final PlotPlayer _player;
    private HelpPage _page = new HelpPage(CommandCategory.ACTIONS, 0, 0);
    private int _maxPage;
    private CommandCategory _commandCategory;
    private List<Command<PlotPlayer>> _commands;
    
    public HelpMenu(final PlotPlayer player) {
        _player = player;
    }
    
    public HelpMenu setCategory(final CommandCategory commandCategory) {
        _commandCategory = commandCategory;
        return this;
    }
    
    public HelpMenu getCommands() {
        _commands = MainCommand.getCommands(_commandCategory, _player);
        return this;
    }
    
    public HelpMenu generateMaxPages() {
        _maxPage = Math.max((_commands.size() - 1) / PER_PAGE, 0);
        return this;
    }
    
    public HelpMenu generatePage(int currentPage, final String label) {
        if (currentPage > _maxPage) {
            currentPage = _maxPage;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        _page = new HelpPage(_commandCategory, currentPage, _maxPage);
        final int max = Math.min((currentPage * PER_PAGE) + (PER_PAGE - 1), _commands.size());
        for (int i = currentPage * PER_PAGE; i < max; i++) {
            _page.addHelpItem(new HelpObject(_commands.get(i), label));
        }
        return this;
    }
    
    public void render() {
        _page.render(_player);
    }
    
}
