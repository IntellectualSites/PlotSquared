package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class Schematic extends SubCommand {

	public Schematic() {
		super("schematic", "plots.admin", "Schematic Command", "schematic {arg}", "sch", CommandCategory.ACTIONS, false);
		
		// TODO command to fetch schematic from worldedit directory
	}
	
	private int counter = 0;
	private boolean running = false;
	private Plot[] plots;
	private int task;
	
	
	
	
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
		    if (plr==null) {
                PlotMain.sendConsoleSenderMessage(C.IS_CONSOLE);
                return false;
            }
		    if (!PlotMain.hasPermission(plr, "plots.schematic.save")) {
                PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.paste");
                return false;
            }
			if (args.length < 2) {
				sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
				break;
			}
			if (!PlayerFunctions.isInPlot(plr)) {
				sendMessage(plr, C.NOT_IN_PLOT);
				break;
			}
			file = args[1];
			schematic = SchematicHandler.getSchematic(file);
			boolean s = SchematicHandler.paste(plr.getLocation(), schematic, PlayerFunctions.getCurrentPlot(plr));
			if (s) {
				sendMessage(plr, C.SCHEMATIC_PASTE_SUCCESS);
			}
			else {
				sendMessage(plr, C.SCHEMATIC_PASTE_FAILED);
			}
			break;
		case "test":
		    if (plr==null) {
                PlotMain.sendConsoleSenderMessage(C.IS_CONSOLE);
                return false;
            }
		    if (!PlotMain.hasPermission(plr, "plots.schematic.save")) {
                PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.test");
                return false;
            }
			if (args.length < 2) {
				sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
				break;
			}
			file = args[1];
			schematic = SchematicHandler.getSchematic(file);
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
		case "saveall":
		case "exportall":
		    if (plr!=null) {
                PlayerFunctions.sendMessage(plr, C.NOT_CONSOLE);
                return false;
            }
		    if (args.length!=2) {
		        PlayerFunctions.sendMessage(plr, "&cNeed world arg. Use &7/plots sch exportall <world>");
                return false; 
		    }
		    HashMap<PlotId, Plot> plotmap = PlotMain.getPlots(args[1]);
		    if (plotmap==null || plotmap.size()==0) {
		        PlayerFunctions.sendMessage(plr, "&cInvalid world. Use &7/plots sch exportall <world>");
                return false; 
		    }
		    if (running) {
		        PlayerFunctions.sendMessage(plr, "&cTask is already running.");
                return false; 
		    }
		    
		    PlotMain.sendConsoleSenderMessage("&3PlotSquared&8->&3Schemaitc&8: &7Mass export has started. This may take a while.");
		    PlotMain.sendConsoleSenderMessage("&3PlotSquared&8->&3Schemaitc&8: &7Found &c"+plotmap.size()+"&7 plots...");
		    final World worldObj = Bukkit.getWorld(args[1]);
		    final String worldname = Bukkit.getWorld(args[1]).getName();
		    
		    final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
		    
		    
		    this.plots = plotmap.values().toArray(new Plot[0]);
		    this.running = true;
		    this.counter = 0;
		    
		    task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    if (counter>=plots.length) {
                        PlotMain.sendConsoleSenderMessage("&3PlotSquared&8->&3Schemaitc&8: &aFinished!");
                        running = false;
                        Bukkit.getScheduler().cancelTask(task);
                        return;
                    }
                    final Plot plot = plots[counter];
                    final CompoundTag sch = SchematicHandler.getCompoundTag(worldObj, plot.id);
                    String o = UUIDHandler.getName(plot.owner);
                    final String owner = o==null ? "unknown" : o ;
                    if (sch==null) {
                        PlayerFunctions.sendMessage(plr, "&7 - Skipped plot &c"+plot.id);
                    }
                    else {
                        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getServer().getPluginManager().getPlugin("PlotSquared"), new Runnable() {
                            @Override
                            public void run() {
                                counter++;
                                PlayerFunctions.sendMessage(plr, "&6ID: "+plot.id);
                                    boolean result = SchematicHandler.save(sch, Settings.SCHEMATIC_SAVE_PATH+"/"+plot.id.x+","+plot.id.y+","+worldname+","+owner+".schematic");
                                    
                                    if (!result) {
                                        PlayerFunctions.sendMessage(plr, "&7 - Failed to save &c"+plot.id);
                                    }
                                    else {
                                        PlayerFunctions.sendMessage(plr, "&7 - &aExport success: "+plot.id);
                                    }
                            }
                        });
                    }
                    counter++;
                }
		    }, 20, 20);
		    break;
		case "export":
		case "save":
		    if (!PlotMain.hasPermission(plr, "plots.schematic.save")) {
		        PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.save");
		        return false;
		    }
		    if (running) {
                PlayerFunctions.sendMessage(plr, "&cTask is already running.");
                return false; 
            }
		    final String world;
		    final Plot p2;
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
    	        p2 = myplot;
    	        world = plr.getWorld().getName();
		    }
		    else {
		        if (args.length==3) {
		            try {
		                world = args[0];
		                String[] split = args[2].split(";");
		                PlotId i = new PlotId(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
		                if (PlotMain.getPlots(world)==null || PlotMain.getPlots(world).get(i) == null) {
		                    PlayerFunctions.sendMessage(plr, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
	                        return false;
		                }
		                p2 = PlotMain.getPlots(world).get(i);
		            }
		            catch (Exception e) {
		                PlayerFunctions.sendMessage(plr, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
		                return false;
		            }
		        }
		        else {
		            PlayerFunctions.sendMessage(plr, "&cInvalid world or id. Use &7/plots sch save <world> <id>");
		            return false;
		        }
		    }
		    
            final Plugin plugin2 = Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
            
            
            this.plots = new Plot[] {p2} ;
            this.running = true;
            this.counter = 0;
            
            task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin2, new Runnable() {
                @Override
                public void run() {
                    if (counter>=plots.length) {
                        PlotMain.sendConsoleSenderMessage("&3PlotSquared&8->&3Schemaitc&8: &aFinished!");
                        running = false;
                        Bukkit.getScheduler().cancelTask(task);
                        return;
                    }
                    final Plot plot = plots[counter];
                    final CompoundTag sch = SchematicHandler.getCompoundTag(Bukkit.getWorld(world), plot.id);
                    String o = UUIDHandler.getName(plot.owner);
                    final String owner = o==null ? "unknown" : o ;
                    if (sch==null) {
                        PlayerFunctions.sendMessage(plr, "&7 - Skipped plot &c"+plot.id);
                    }
                    else {
                        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getServer().getPluginManager().getPlugin("PlotSquared"), new Runnable() {
                            @Override
                            public void run() {
                                counter++;
                                PlayerFunctions.sendMessage(plr, "&6ID: "+plot.id);
                                    boolean result = SchematicHandler.save(sch, Settings.SCHEMATIC_SAVE_PATH+"/"+plot.id.x+","+plot.id.y+","+world+","+owner+".schematic");
                                    
                                    if (!result) {
                                        PlayerFunctions.sendMessage(plr, "&7 - Failed to save &c"+plot.id);
                                    }
                                    else {
                                        PlayerFunctions.sendMessage(plr, "&7 - &aExport success: "+plot.id);
                                    }
                            }
                        });
                    }
                    counter++;
                }
            }, 20, 60);
	        break;
		default:
			sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
			break;
		}
		return true;
	}
}
