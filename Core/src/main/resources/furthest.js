/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        if (plot.getX() > max) {
            max = plot.getX();
            maxplot = plot;
        }
        if (plot.getY() > max) {
            max = plot.getY();
            maxplot = plot;
        }
        if (-plot.getX() > max) {
            max = -plot.getX();
            maxplot = plot;
        }
        if (-plot.getY() > max) {
            max = -plot.getY();
            maxplot = plot;
        }
    }
    PS.class.static.log(plot);
} else {
    PlotPlayer.sendMessage("Usage: /plot debugexec runasync furthest.js <plotworld>");
}
