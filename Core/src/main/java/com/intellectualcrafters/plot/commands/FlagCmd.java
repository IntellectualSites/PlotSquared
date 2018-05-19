package com.intellectualcrafters.plot.commands;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.flag.ListFlag;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@CommandDeclaration(
        command = "flag",
        aliases = {"f"},
        usage = "/plot flag <set|remove|add|list|info>",
        description = "Plot flag commands",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag")
public class FlagCmd extends Command {

    public FlagCmd() { super(MainCommand.getInstance(), true); }

}
