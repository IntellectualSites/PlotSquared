/*
* Script to find the furthest plot from origin in a world:
*  - /plot debugexec runasync furthest.js <plotworld>
*/

if (PS.hasPlotArea("%s0")) {
    var plots = PS.getAllPlotsRaw().get("%s0").values().toArray();
    var max = 0;
    var maxplot;
    for (var i in plots) {
        var plot = plots[i];
        if (plot.x > max) {
            max = plot.x;
            maxplot = plot;
        }
        if (plot.y > max) {
            max = plot.y;
            maxplot = plot;
        }
        if (-plot.x > max) {
            max = -plot.x;
            maxplot = plot;
        }
        if (-plot.y > max) {
            max = -plot.y;
            maxplot = plot;
        }
    }
    PS.class.static.log(plot);
}
else {
    PlotPlayer.sendMessage("Usage: /plot debugexec runasync furthest.js <plotworld>");
}
