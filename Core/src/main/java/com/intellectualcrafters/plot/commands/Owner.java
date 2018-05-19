package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Set;
import java.util.UUID;

@CommandDeclaration(
        command = "setowner",
        permission = "plots.set.owner",
        description = "Set the plot owner",
        usage = "/plot setowner <player>",
        aliases = {"owner", "so", "seto"},
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE,
        confirmation = true)
public class Owner extends SetOwner {

    public Owner() { super(MainCommand.getInstance(), true); }

    @Override
    public boolean set(PlotPlayer player, Plot plot, String value) {
        return super.set(player, plot, value);
    }
}
