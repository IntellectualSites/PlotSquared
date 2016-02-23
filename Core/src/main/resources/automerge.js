/*
Need to script something quick with PlotSquared?
/plot debugexec runasync automerge.js <removeroads>

This is an example script that will auto merge all plots

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
var plots = PS.sortPlots(PS.getPlots()); 
PS.class.static.log("Attempting to auto merge " + plots.size() + " plots");
if ("%s0" === "true") {
    for (var i = 0; i < plots.size(); i++) {
        var plot = plots.get(i);
        plot.autoMerge(false);
    }
}
else if ("%s0" === "false") {
    for (var i = 0; i < plots.size(); i++) {
        var plot = plots.get(i);
        plot.autoMerge(false);
    }
}
else {
    C_COMMAND_SYNTAX.send(PlotPlayer, "/plot debugexec automerge.js <removeroads>");
    MainUtil.sendMessage(PlotPlayer, "$1<removeroads> is true or false if you want to remove roads when auto merging");
}