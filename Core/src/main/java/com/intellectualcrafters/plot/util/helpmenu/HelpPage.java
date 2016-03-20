package com.intellectualcrafters.plot.util.helpmenu;

import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import java.util.ArrayList;
import java.util.List;

public class HelpPage {

    private final List<HelpObject> helpObjects;
    private final String _header;

    public HelpPage(final CommandCategory category, final int currentPage, final int maxPages) {
        helpObjects = new ArrayList<>();
        _header = C.HELP_PAGE_HEADER.s().replace("%category%", category == null ? "ALL" : category.toString()).replace("%current%", (currentPage + 1) + "").replace("%max%", (maxPages + 1) + "");
    }

    public void render(final PlotPlayer player) {
        if (helpObjects.size() < 1) {
            MainUtil.sendMessage(player, C.NOT_VALID_NUMBER, "(0)");
        } else {
            MainUtil.sendMessage(player, C.HELP_HEADER, false);
            MainUtil.sendMessage(player, _header, false);
            for (final HelpObject object : helpObjects) {
                MainUtil.sendMessage(player, object.toString(), false);
            }
            MainUtil.sendMessage(player, C.HELP_FOOTER, false);
        }
    }

    public void addHelpItem(final HelpObject object) {
        helpObjects.add(object);
    }
}
