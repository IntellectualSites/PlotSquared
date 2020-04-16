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
package com.plotsquared.core.queue;

public abstract class QueueProvider {
    public static QueueProvider of(final Class<? extends LocalBlockQueue> primary,
        final Class<? extends LocalBlockQueue> fallback) {
        return new QueueProvider() {

            private boolean failed = false;

            @Override public LocalBlockQueue getNewQueue(String world) {
                if (!failed) {
                    try {
                        return (LocalBlockQueue) primary.getConstructors()[0].newInstance(world);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        failed = true;
                    }
                }
                try {
                    return (LocalBlockQueue) fallback.getConstructors()[0].newInstance(world);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public abstract LocalBlockQueue getNewQueue(String world);
}
