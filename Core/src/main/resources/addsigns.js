/*
This script will fix all signs in the world.
*/
var plots = PS.sortPlotsByTemp(PS.getPlots());
for (var i = 0; i < plots.size(); i++) {
    var plot = plots.get(i);
    if (plot.isBasePlot()) {
        plot.setSign();
        PS.class.static.log('&cSetting sign for: ' + plot);
    }
    java.lang.Thread.sleep(10);
}
