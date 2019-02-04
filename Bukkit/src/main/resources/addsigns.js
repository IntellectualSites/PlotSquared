/*
This script will fix all signs in the world.
*/
var plots = PS.sortPlots(PS.getPlots());
for (var i = 0; i < plots.size(); i++) {
    var plot = plots.get(i);
    if (!plot.isMerged() || !plot.getMerged(0)) {
        plot.setSign();
        PS.class.static.log('&cSetting sign for: ' + plot);
    }
    java.lang.Thread.sleep(10);
}
