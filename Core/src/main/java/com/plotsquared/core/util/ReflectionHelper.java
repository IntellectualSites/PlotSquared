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

import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Optional;

@ApiStatus.Internal
public final class ReflectionHelper {

    /**
     * Find a (declared) method with an unknown or potentially obfuscated name by its signature and optional modifiers.
     * <br>
     * The method - if private - is not made accessible. Either call {@link Method#setAccessible(boolean)} or
     * use a {@link java.lang.invoke.MethodHandles.Lookup#privateLookupIn(Class, MethodHandles.Lookup) private lookup}.
     *
     * @param holder    The class providing the method.
     * @param signature The signature of the method, identified by parameter types and the return type.
     * @param modifiers All possible modifiers of the method that should be validated.
     * @return The method, if one has been found. Otherwise, an empty Optional.
     * @throws RuntimeException if multiple matching methods have been found.
     * @see java.lang.reflect.Modifier
     */
    public static Optional<Method> findMethod(Class<?> holder, MethodType signature, int... modifiers) {
        Method found = null;
        outer:
        for (final Method method : holder.getDeclaredMethods()) {
            if (method.getParameterCount() != signature.parameterCount()) {
                continue;
            }
            if (!signature.returnType().isAssignableFrom(method.getReturnType())) {
                continue;
            }

            for (final int modifier : modifiers) {
                if ((method.getModifiers() & modifier) == 0) {
                    continue outer;
                }
            }

            Class<?>[] parameterTypes = signature.parameterArray();
            for (int i = 0; i < parameterTypes.length; i++) {
                // validate expected parameter is either the same type or subtype of actual parameter
                if (!parameterTypes[i].isAssignableFrom(method.getParameterTypes()[i])) {
                    continue outer;
                }
            }
            if (found != null) {
                throw new RuntimeException("Found ambiguous method by selector: " + method + " vs " + found);
            }
            found = method;
        }
        return Optional.ofNullable(found);
    }

}
