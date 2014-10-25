package com.intellectualcrafters.plot.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.SchematicHandler;
import com.intellectualcrafters.plot.Settings;
import com.intellectualcrafters.plot.database.DBFunc;

public class Schematic extends SubCommand {

	public Schematic() {
		super("schematic", "plots.admin", "Schematic Command", "schematic {arg}", "sch", CommandCategory.ACTIONS, true);
		
		// TODO command to fetch schematic from worldedit directory
	}

	@Override
	public boolean execute(final Player plr, String... args) {
		if (args.length < 1) {
			sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
			return true;
		}
		String arg = args[0].toLowerCase();
		String file;
		SchematicHandler.Schematic schematic;
		switch (arg) {
		case "paste":
			if (args.length < 2) {
				sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
				break;
			}
			if (!PlayerFunctions.isInPlot(plr)) {
				sendMessage(plr, C.NOT_IN_PLOT);
				break;
			}
			file = args[1];
			schematic = new SchematicHandler().getSchematic(file);
			boolean s = new SchematicHandler().paste(plr.getLocation(), schematic, PlayerFunctions.getCurrentPlot(plr));
			if (s) {
				sendMessage(plr, C.SCHEMATIC_PASTE_SUCCESS);
			}
			else {
				sendMessage(plr, C.SCHEMATIC_PASTE_FAILED);
			}
			break;
		case "test":
			if (args.length < 2) {
				sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
				break;
			}
			file = args[1];
			schematic = new SchematicHandler().getSchematic(file);
			if (schematic == null) {
				sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent");
				break;
			}
            
			int l1 = schematic.getSchematicDimension().getX();
			int l2 = schematic.getSchematicDimension().getZ();

			Plot plot = PlayerFunctions.getCurrentPlot(plr);
			int length = PlotHelper.getPlotWidth(plr.getWorld(), plot.id);

			if ((l1 != length) || (l2 != length)) {
				sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", l1, l2, length));
				break;
			}
			sendMessage(plr, C.SCHEMATIC_VALID);
			break;
		case "save":
		    final PlotId i;
		    final String world;
		    if (plr!=null) {
    		    if(!PlayerFunctions.isInPlot(plr)) {
    	            sendMessage(plr, C.NOT_IN_PLOT);
    	            return false;
    	        }
    	        Plot myplot = PlayerFunctions.getCurrentPlot(plr);
    	        if(!myplot.hasRights(plr)) {
    	            sendMessage(plr, C.NO_PLOT_PERMS);
    	            return false;
    	        }
    	        i = myplot.id;
    	        world = plr.getWorld().getName();
		    }
		    else {
		        if (args.length==3) {
		            try {
		                world = args[0];
		                String[] split = args[2].split(";");
		                i = new PlotId(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
		                if (PlotMain.getPlots(world)==null || PlotMain.getPlots(world).get(i) == null) {
		                    PlayerFunctions.sendMessage(plr, "&cInvalid world or id. Use &7/plots schem save <world> <id>");
	                        return false;
		                }
		                
		            }
		            catch (Exception e) {
		                PlayerFunctions.sendMessage(plr, "&cInvalid world or id. Use &7/plots schem save <world> <id>");
		                return false;
		            }
		        }
		        else {
		            PlayerFunctions.sendMessage(plr, "&cInvalid world or id. Use &7/plots schem save <world> <id>");
		            return false;
		        }
		    }
		    
	        
	        
	        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getServer().getPluginManager().getPlugin("PlotSquared"), new Runnable() {
	            @Override
	            public void run() {
	                CompoundTag schematic = SchematicHandler.getCompoundTag(Bukkit.getWorld(world), i);
	                if (schematic==null) {
	                    PlayerFunctions.sendMessage(plr, "&cInvalid plot");
	                    return;
	                }
	                boolean result = SchematicHandler.save(schematic, Settings.Web.PATH+"/"+i.x+"-"+i.y+"-"+world+".schematic");
	                
	                if (!result) {
	                    PlayerFunctions.sendMessage(plr, "&cFailed to save schematic");
	                    return;
	                }
	                PlayerFunctions.sendMessage(plr, "&aSuccessfully saved schematic!");
	            }
	        });
	        
	        break;
		default:
			sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
			break;
		}
		return true;
	}
}
