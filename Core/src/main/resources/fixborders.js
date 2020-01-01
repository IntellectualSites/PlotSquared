/*
Fixes border around plots
/plot debugexec runasync fixborder.js <Plot ID>
*/
var plots = PS.sortPlotsByTemp(PS.getPlots());
PS.class.static.log("Attempting to fix border for " + plots.size() + " plots");
for (var i = 0; i < plots.size(); i++) {
    var plot = plots.get(i);
    plot.setComponent("border", "%s0");
}
