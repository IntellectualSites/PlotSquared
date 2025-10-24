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

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReflectionHelperTest {

    @Test
    void findMethod() throws NoSuchMethodException {
        assertThrows(
                RuntimeException.class, () ->
                        ReflectionHelper.findMethod(MethodTesterClass.class, MethodType.methodType(String.class))
        );
        assertEquals(
                MethodTesterClass.class.getMethod("methodThree"),
                ReflectionHelper.findMethod(MethodTesterClass.class, MethodType.methodType(String.class), Modifier.PUBLIC)
                        .orElse(null)
        );
        assertEquals(
                MethodTesterClass.class.getDeclaredMethod("methodFour", String.class, Collection.class),
                ReflectionHelper.findMethod(MethodTesterClass.class, MethodType.methodType(
                        String.class, String.class, Collection.class
                )).orElse(null)
        );
        // check that helper allows super classes of parameters when searching
        assertEquals(
                MethodTesterClass.class.getDeclaredMethod("methodFour", String.class, Collection.class),
                ReflectionHelper.findMethod(MethodTesterClass.class, MethodType.methodType(
                        String.class, String.class, Object.class
                )).orElse(null)
        );
    }

    @SuppressWarnings("unused")
    private static class MethodTesterClass {

        private static String methodOne() {
            return "";
        }

        private static String methodTwo() {
            return "";
        }

        public static String methodThree() {
            return "";
        }

        protected static String methodFour(String param, Collection<String> paramList) {
            return "";
        }

    }

}
