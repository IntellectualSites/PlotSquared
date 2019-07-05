/*
Need to script something quick with PlotSquared?
/plot debugexec runasync fixborder.js <id>


The following utility classes are usable:
 - PS
 - TaskManager
 - TitleManager
 - ConsolePlayer
 - SchematicHandler
 - ChunkManager
 - BlockManager
 - SetupUtils
 - EventUtil
 - UUIDHandler
 - DBFunc
 - HybridUtils
 - IMP ( BukkitMain or SpongeMain)
 - MainCommand
 - MainUtil
 - Settings
 - StringMan
 - MathMan
 - C ( use C_ )
 - Permissions ( use Permissions_ )
 
 For more information see: https://github.com/IntellectualSites/PlotSquared/wiki/Scripting
*/
var plots = PS.sortPlotsByTemp(PS.getPlots());
PS.class.static.log("Attempting to fix border for " + plots.size() + " plots");
for (var i = 0; i < plots.size(); i++) {
    var plot = plots.get(i);
    plot.setComponent("border", "%s0");
}
