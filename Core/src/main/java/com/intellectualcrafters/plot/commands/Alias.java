package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import de.schlichtherle.util.Arrays;

@CommandDeclaration(
        command = "setalias",
        permission = "plots.alias",
        description = "Set the plot name",
        usage = "/plot alias <set|remove> <alias>",
        aliases = {"alias", "sa", "name", "rename", "setname", "seta", "nameplot"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Alias extends SubCommand {

	@Override
	public boolean onCommand(PlotPlayer player, String[] args) {	
		
		if (args.length == 0) {
			C.COMMAND_SYNTAX.send(player, "/plot alias <set> <value>");
            return false;
        }
		
		Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
		
        if (!plot.hasOwner()) {
            sendMessage(player, C.PLOT_NOT_CLAIMED);
            return false;
        }
        
        if (!plot.isOwner(player.getUUID())) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        
        boolean result = false;
        
		switch (args[0].toLowerCase()) {
			case "set":
				if (args.length != 2) {
					C.COMMAND_SYNTAX.send(player, "/plot alias <set> <value>");
					return false;
				}
				
				if(canExecuteCommand(player, C.PERMISSION_ALIAS_SET)) {
					result = setAlias(player, plot, args[1]);
				}
				break;
			case "remove":
				if(canExecuteCommand(player, C.PERMISSION_ALIAS_REMOVE)) {
					result = removeAlias(player, plot);
				}
				break;
			default:
				C.COMMAND_SYNTAX.send(player, "/plot alias <set|remove> <alias>");
				result = false;				
    	}
		
		return result;
	}
	
    
    private boolean setAlias(PlotPlayer player, Plot plot, String alias) {
    	
        if (alias.isEmpty()) {
            C.COMMAND_SYNTAX.send(player, "/plot alias <set> <value>");
            return false;
        }
        if (alias.length() >= 50) {
            MainUtil.sendMessage(player, C.ALIAS_TOO_LONG);
            return false;
        }
        if (alias.contains(" ")) {
            C.NOT_VALID_VALUE.send(player);
            return false;
        }
        for (Plot p : PS.get().getPlots(plot.getArea())) {
            if (p.getAlias().equalsIgnoreCase(alias)) {
                MainUtil.sendMessage(player, C.ALIAS_IS_TAKEN);
                return false;
            }
        }
        if (UUIDHandler.nameExists(new StringWrapper(alias)) || PS.get().hasPlotArea(alias)) {
            MainUtil.sendMessage(player, C.ALIAS_IS_TAKEN);
            return false;
        }
        plot.setAlias(alias);
        MainUtil.sendMessage(player, C.ALIAS_SET_TO.s().replaceAll("%alias%", alias));
        return true;
    }
    
    private boolean removeAlias(PlotPlayer player, Plot plot) {
    	plot.setAlias(null);
    	MainUtil.sendMessage(player, C.ALIAS_REMOVED.s());
    	return true;
    }
    
    private boolean canExecuteCommand(PlotPlayer player, C caption) {
    	if (!Permissions.hasPermission(player, caption)) {
    		 MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
             return false;
    	}
    	return true;
    }
}
