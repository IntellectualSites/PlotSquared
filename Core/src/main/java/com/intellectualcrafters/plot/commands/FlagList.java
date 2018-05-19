package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.*;

@CommandDeclaration(command = "list",
        permission = "plots.flag.list",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        description = "List all plot flags",
        usage = "/plot flag list")
public class FlagList extends FlagCommand {

    public FlagList(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        HashMap<String, ArrayList<String>> flags = new HashMap<>();
        for (Flag<?> flag1 : Flags.getFlags()) {
            String type = flag1.getClass().getSimpleName();
            if (!flags.containsKey(type)) {
                flags.put(type, new ArrayList<String>());
            }
            flags.get(type).add(flag1.getName());
        }
        String message = "";
        String prefix = "";
        for (Map.Entry<String, ArrayList<String>> stringArrayListEntry : flags.entrySet()) {
            message += prefix + "&6" + stringArrayListEntry.getKey() + ": &7" + StringMan.join(stringArrayListEntry.getValue(), ", ");
            prefix = "\n";
        }
        MainUtil.sendMessage(player, message);
        return true;
    }
}