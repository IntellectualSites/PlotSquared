package com.intellectualcrafters.plot.util.helpmenu;

import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualsites.commands.Command;
import com.plotsquared.bukkit.util.bukkit.BukkitUtil;

import java.util.List;

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
        this._maxPage = Math.min(_commands.size() / PER_PAGE, 1);
        return this;
    }

    public HelpMenu generatePage(int currentPage) {
        if (currentPage > _maxPage) {
            currentPage = _maxPage;
        }
        _page = new HelpPage(_commandCategory, currentPage, _maxPage);
        int max = Math.min((currentPage * PER_PAGE) + PER_PAGE, _commands.size());
        for (int i = currentPage * PER_PAGE; i < max; i++) {
            _page.addHelpItem(new HelpObject(_commands.get(i)));
        }
        return this;
    }

    public void render() {
        _page.render(_player);
    }

}
