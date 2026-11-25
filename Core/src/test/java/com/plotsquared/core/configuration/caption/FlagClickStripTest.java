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
package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.plot.flag.implementations.GreetingFlag;
import com.plotsquared.core.plot.flag.implementations.UseFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlagClickStripTest {

    @Test
    @DisplayName("A stripped greeting fragment carries no click event")
    void strippedGreetingFragmentCarriesNoClickEvent() {
        String stripped = CaptionUtility.stripClickEvents(
                GreetingFlag.GREETING_FLAG_EMPTY,
                "<click:run_command:'/x'>text</click>"
        );
        Assertions.assertFalse(
                hasClickEvent(stripped),
                () -> "stripped greeting fragment must not contain a click event: " + stripped
        );
    }

    @Test
    @DisplayName("Merging two separately stripped greeting fragments carries no click event")
    void mergedGreetingOfStrippedFragmentsCarriesNoClickEvent() {
        String first = CaptionUtility.stripClickEvents(
                GreetingFlag.GREETING_FLAG_EMPTY,
                "<click:run_command:'/x'>one</click>"
        );
        String second = CaptionUtility.stripClickEvents(
                GreetingFlag.GREETING_FLAG_EMPTY,
                "<click:suggest_command:'/y'>two</click>"
        );
        String merged = GreetingFlag.GREETING_FLAG_EMPTY.parse(first).merge(second).toString();
        Assertions.assertFalse(
                hasClickEvent(merged),
                () -> "merged greeting must not contain a click event: " + merged
        );
    }

    @Test
    @DisplayName("A click tag split across two stripped fragments carries no click event after merge")
    void clickTagSplitAcrossTwoFragmentsCarriesNoClickEvent() {
        String first = CaptionUtility.stripClickEvents(
                GreetingFlag.GREETING_FLAG_EMPTY,
                "<click:run_command:'/x"
        );
        String second = CaptionUtility.stripClickEvents(
                GreetingFlag.GREETING_FLAG_EMPTY,
                "'>text</click>"
        );
        String merged = GreetingFlag.GREETING_FLAG_EMPTY.parse(first).merge(second).toString();
        Assertions.assertFalse(
                hasClickEvent(merged),
                () -> "click tag split across two fragments must not yield a click event: " + merged
        );
    }

    @Test
    @DisplayName("Stripping leaves the value of a non MiniMessage flag untouched")
    void strippingLeavesNonMiniMessageFlagValueUntouched() {
        String input = "<click:run_command:'/x'>a</click>";
        Assertions.assertEquals(
                input,
                CaptionUtility.stripClickEvents(UseFlag.USE_NONE, input),
                "non MiniMessage flags must not be modified by click event stripping"
        );
    }

    private static boolean hasClickEvent(String miniMessage) {
        return hasClickEvent(MiniMessage.miniMessage().deserialize(miniMessage));
    }

    private static boolean hasClickEvent(Component component) {
        if (component.clickEvent() != null) {
            return true;
        }
        for (Component child : component.children()) {
            if (hasClickEvent(child)) {
                return true;
            }
        }
        return false;
    }

}
