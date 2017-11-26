package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(
        command = "visit",
        permission = "plots.visit",
        description = "Visit someones plot",
        usage = "/plot visit [<player>|<alias>|<world>|<id>] [#]",
        aliases = {"v", "tp", "teleport", "goto", "home", "h"},
        requiredType = RequiredType.NONE,
        category = CommandCategory.TELEPORT)
public class Visit extends Command {

	private static final int PAGE_OUT_OF_RANGE = -998899; // this is to flag Page argument is to long. Can occur if someone enters a large number (some player uses numeric names)
	private final int MaxPageRange = 100;
	
    public Visit() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        return tabOf(player, args, space, getUsage());
    }

    @Override
    public void execute(final PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, final RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 1 && args[0].contains(":")) {
            args = args[0].split(":");
        }
        int page = Integer.MIN_VALUE;
        Collection<Plot> unsorted = null;
        PlotArea sortByArea = player.getApplicablePlotArea();
        boolean shouldSortByArea = Settings.Teleport.PER_WORLD_VISIT;
        switch (args.length) {
        	case 2:
        		if (MathMan.isInteger(args[1])) {
        			page = tryReadPageIdFromArg(player, args[1]);
        		} else {        			
        			C.COMMAND_SYNTAX.send(player, getUsage());
        			return;
        		}
        		// don't add break here. we handle the first argument in case 1
        	case 1:
        		boolean isCorrectSyntaxWithoutResults = false;
        		Collection<Plot> plots = new HashSet<Plot>();
        		if (args[0] != null) {
        			plots = getPlotsFromSingleArgument(args[0], sortByArea); 
            		if (!plots.isEmpty()) {
            			unsorted = plots;        			
            		} else {
            			if (MathMan.isInteger(args[0])) {
            				page = tryReadPageIdFromArg(player, args[0]);
            				
            				if (page != PAGE_OUT_OF_RANGE && page != Integer.MIN_VALUE) {
            					unsorted = PS.get().getPlots(player);
            				}
            			} else {
            				// we know now syntax is correct but no results.
            				isCorrectSyntaxWithoutResults = true;
            			}            				
            		}
        		}
        		
        		if (!isCorrectSyntaxWithoutResults && plots.isEmpty() && page == Integer.MIN_VALUE) {        			
        			C.COMMAND_SYNTAX.send(player, getUsage());
        			return;
        		}
        		break;
        	case 0:
        		unsorted = PS.get().getPlots(player);
        		break;
        	default:
        		C.COMMAND_SYNTAX.send(player, getUsage());
    			return;        		
        }

        if (page == Integer.MIN_VALUE) {
            page = 1;
        }
        if (unsorted == null || unsorted.isEmpty()) {
            C.FOUND_NO_PLOTS.send(player);
            return;
        }
        Iterator<Plot> iterator = unsorted.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isBasePlot()) {
                iterator.remove();
            }
        }
        if (page < 1 || page > unsorted.size()) {
            C.NOT_VALID_NUMBER.send(player, "(1 - " + unsorted.size() + ")");
            return;
        }
        List<Plot> plots;
        if (shouldSortByArea) {
            plots = PS.get().sortPlots(unsorted, PS.SortType.CREATION_DATE, sortByArea);
        }  else {
            plots = PS.get().sortPlotsByTemp(unsorted);
        }
        final Plot plot = plots.get(page - 1);
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(player, C.PERMISSION_VISIT_UNOWNED)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_VISIT_UNOWNED);
                return;
            }
        } else if (plot.isOwner(player.getUUID())) {
            if (!Permissions.hasPermission(player, C.PERMISSION_VISIT_OWNED) && !Permissions.hasPermission(player, C.PERMISSION_HOME)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_VISIT_OWNED);
                return;
            }
        } else if (plot.isAdded(player.getUUID())) {
            if (!Permissions.hasPermission(player, C.PERMISSION_SHARED)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_SHARED);
                return;
            }
        } else {
            if (!Permissions.hasPermission(player, C.PERMISSION_VISIT_OTHER)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_VISIT_OTHER);
                return;
            }
        }
        confirm.run(this, new Runnable() {
            @Override
            public void run() {
                if (plot.teleportPlayer(player)) {
                    whenDone.run(Visit.this, CommandResult.SUCCESS);
                } else {
                    whenDone.run(Visit.this, CommandResult.FAILURE);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                whenDone.run(Visit.this, CommandResult.FAILURE);
            }
        });
    }

    /**
     * Get Plots based on an unknown argument type
     * Check order: Player, Alias, World, PlotId
     * @param argument to search plots
     * <pre>
     * Samples:
     * p h xPlotPlayerGuy	= Player [numeric] [alphanumeric] [UUID]
     * p h MyPlantage		= Alias  [numeric] [alphanumeric] [Set<Plot>]
     * p h PlotworldLarge	= World  [numeric] [alphanumeric] [PlotArea]
     * p h 1:1		= plotId [numeric] (1,1|1;2)      [PlotId]
     * </pre>
     * @param applicablePlotArea the area from the player invoked the command
     * @return Collection<{@link Plot}> empty if nothing found
     */
    private Collection<Plot> getPlotsFromSingleArgument(String argument, PlotArea applicablePlotArea) {
    	
    	Collection<Plot> result = new HashSet<Plot>();
		UUID user = UUIDHandler.getUUIDFromString(argument);
		if (user != null) {
			result = PS.get().getBasePlots(user);
		} else {
			result = PS.get().getPlotsByAlias(argument, applicablePlotArea.worldname);
		}
		
		if (result.isEmpty()) {
			PlotArea plotArea = PS.get().getPlotArea(argument, "0,0");
			if (plotArea != null) {
				result = plotArea.getBasePlots();
				if(result.isEmpty()) {
					result = Collections.singletonList(plotArea.getPlot(new PlotId(0, 0)));
				}
			}
		}
		
		if (result.isEmpty()) {
			PlotId plotId = PlotId.fromString(argument);
			if (plotId != null) {
				result = Collections.singletonList(applicablePlotArea.getPlot(plotId));
			}			
		}
				
		return result;
    }
    
    private int tryReadPageIdFromArg(PlotPlayer player, String arg) {
    	int page = Integer.MIN_VALUE;    	
    	
    	try {
    		if (MathMan.isInteger(arg)) {
    			page = Integer.parseInt(arg);
    			if (page > MaxPageRange) {
    				page = PAGE_OUT_OF_RANGE;
    			}
    		}
    	} catch (Exception ignored) {
    		page = PAGE_OUT_OF_RANGE;
    		C.NOT_VALID_NUMBER.send(player, "(1, ∞)");
    	}
    	
    	return page;
    }
}
