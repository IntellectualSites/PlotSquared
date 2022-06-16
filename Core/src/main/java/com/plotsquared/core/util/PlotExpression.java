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

import com.sk89q.worldedit.internal.expression.Expression;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An expression that can be evaluated.
 * Evaluation is thread-safe.
 * This is a wrapper for {@link Expression}.
 */
public class PlotExpression {

    private final Expression expression;
    private final Object lock = new Object();

    private PlotExpression(final @NonNull String rawExpression, final @NonNull String @NonNull [] variableNames) {
        this.expression = Expression.compile(rawExpression, variableNames);
    }

    /**
     * Compiles an expression from a string.
     *
     * @param expression    the expression to compile.
     * @param variableNames the variables that can be set in {@link #evaluate(double...)}.
     * @return the compiled expression.
     */
    public static @NonNull PlotExpression compile(
            final @NonNull String expression,
            final @NonNull String @NonNull ... variableNames
    ) {
        return new PlotExpression(expression, variableNames);
    }

    /**
     * Evaluates the expression with the given variable values.
     *
     * @param values the values to set the variables to.
     * @return the result of the evaluation.
     */
    public double evaluate(double... values) {
        double evaluate;
        // synchronization is likely the best option in terms of memory and cpu consumption
        synchronized (this.lock) {
            evaluate = this.expression.evaluate(values);
        }
        return evaluate;
    }

}
