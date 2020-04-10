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
