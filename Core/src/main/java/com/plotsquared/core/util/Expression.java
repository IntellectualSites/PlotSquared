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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.plotsquared.core.command.DebugExec;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.configuration.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;

public abstract class Expression<T> {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + Expression.class.getSimpleName());

    public static <U> Expression<U> constant(final U value) {
        return new Expression<U>() {
            @Override public U evaluate(U arg) {
                return value;
            }
        };
    }

    public static Expression<Double> linearDouble(final Double value) {
        return new Expression<Double>() {
            @Override public Double evaluate(Double arg) {
                return (arg * value);
            }
        };
    }

    public static Expression<Double> doubleExpression(final String expression) {
        try {
            return constant(Double.parseDouble(expression));
        } catch (NumberFormatException ignore) {
        }
        if (expression.endsWith("*{arg}")) {
            try {
                return linearDouble(
                    Double.parseDouble(expression.substring(0, expression.length() - 6)));
            } catch (NumberFormatException ignore) {
            }
        }
        return new Expression<Double>() {
            @Override public Double evaluate(Double arg) {
                DebugExec exec = (DebugExec) MainCommand.getInstance().getCommand(DebugExec.class);
                try {
                    return (Double) exec.getEngine().eval(expression.replace("{arg}", "" + arg));
                } catch (ScriptException e) {
                    if (Settings.DEBUG) {
                        logger.info("Invalid expression: {}", expression);
                    }
                    e.printStackTrace();
                }
                return 0d;
            }
        };
    }

    public abstract T evaluate(T arg);
}
