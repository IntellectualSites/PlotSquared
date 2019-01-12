/*
This script will reset all biomes in claimed plots
*/
var plots = PS.sortPlots(PS.getPlots());
for (var i = 0; i < plots.size(); i++) {
    var plot = plots.get(i);
    if (!plot.isMerged() || !plot.getMerged(0)) {
        plot.setBiome("%s0", null);
        PS.class.static.log('&cSetting biome for: ' + plot);
    }
    java.lang.Thread.sleep(1000);
}
