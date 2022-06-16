/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.plotsquared.core.util.task.RunnableVal;

import java.util.concurrent.atomic.AtomicBoolean;

public class RuntimeExceptionRunnableVal<T> extends RunnableVal<RuntimeException> {

    private final RunnableVal<T> function;
    private final AtomicBoolean running;

    public RuntimeExceptionRunnableVal(final RunnableVal<T> function, final AtomicBoolean running) {
        this.function = function;
        this.running = running;
    }

    @Override
    public void run(RuntimeException value) {
        try {
            function.run();
        } catch (RuntimeException e) {
            this.value = e;
        } catch (Throwable neverHappens) {
            neverHappens.printStackTrace();
        } finally {
            running.set(false);
        }
        synchronized (function) {
            function.notifyAll();
        }
    }

}
