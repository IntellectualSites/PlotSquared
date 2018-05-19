package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "helpers",
        aliases = {"members","admin","helper"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.helpers",
        usage = "/plot cluster helpers <add|remove>",
        description = "Modify helpers on plot cluster")
public class ClusterHelpers extends Command {

    public ClusterHelpers(Command parent, boolean isStatic) { super(parent, isStatic); }

}
