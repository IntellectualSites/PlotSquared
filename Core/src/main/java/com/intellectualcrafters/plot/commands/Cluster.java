package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "cluster",
        aliases = {"clusters"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster",
        usage = "/plot cluster <create|delete|helpers|invite|kick|leave|list|resize|sethome|tp>",
        description = "Plot cluster commands")
public class Cluster extends Command {

    public Cluster() { super(MainCommand.getInstance(), true); }

}
