/*
This is an example script that will auto merge all plots
/plot debugexec runasync automerge.js <removeroads>
*/
var plots = PS.sortPlotsByTemp(PS.getPlots());
PS.class.static.log("Attempting to auto merge " + plots.size() + " plots");
if ("%s0" === "true") {
    for (var i = 0; i < plots.size(); i++) {
        var plot = plots.get(i);
        plot.autoMerge(-1, 250000, plot.owner, true);
    }
} else if ("%s0" === "false") {
    for (var i = 0; i < plots.size(); i++) {
        var plot = plots.get(i);
        plot.autoMerge(-1, 250000, plot.owner, false);
    }
} else {
    C_COMMAND_SYNTAX.send(PlotPlayer, "/plot debugexec automerge.js <removeroads>");
    MainUtil.class.static.sendMessage(PlotPlayer, "$1<removeroads> is true or false if you want to remove roads when auto merging");
}
