package com.intellectualcrafters.plot.util.helpmenu;

import java.util.ArrayList;
import java.util.List;

import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;

public class HelpPage {

    private final List<HelpObject> _helpObjecs;
    private final String _header;

    public HelpPage(CommandCategory category, int currentPage, int maxPages) {
        _helpObjecs = new ArrayList<>();
        _header = C.HELP_PAGE_HEADER.s()
                .replace("%category%", category == null ? "ALL" : category.toString())
                .replace("%current%", (currentPage + 1) + "")
                .replace("%max%", (maxPages + 1) + "");
    }

    public void render(final PlotPlayer player) {
        if (_helpObjecs.size() < 1) {
            MainUtil.sendMessage(player, C.NOT_VALID_NUMBER, "(0)");
        } else {
            MainUtil.sendMessage(player, C.HELP_HEADER.s(), false);
            MainUtil.sendMessage(player, _header, false);
            for (final HelpObject object : _helpObjecs) {
                MainUtil.sendMessage(player, object.toString(), false);
            }
            MainUtil.sendMessage(player, C.HELP_FOOTER.s(), false);
        }
    }

    public void addHelpItem(final HelpObject object) {
        _helpObjecs.add(object);
    }
}
