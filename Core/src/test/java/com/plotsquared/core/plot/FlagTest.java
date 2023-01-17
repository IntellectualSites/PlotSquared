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
package com.plotsquared.core.plot;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.AbstractDBTest;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.PlotTitleFlag;
import com.plotsquared.core.plot.flag.implementations.UseFlag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FlagTest {

    @BeforeEach
    public void setUp() {
        DBFunc.dbManager = new AbstractDBTest();
    }

    @Test
    public void testFlagName() {
        String flagName = PlotFlag.getFlagName(UseFlag.class);
        Assertions.assertEquals("use", flagName);
    }

    @Test
    public void shouldSuccessfullyParseTitleFlagWithTitleSingularAndSubTitleEmpty() {
        Assertions.assertDoesNotThrow(() -> {
            var title = PlotTitleFlag.TITLE_FLAG_DEFAULT.parse("\"test\" \"\"").getValue();
            Assertions.assertEquals("test", title.title());
            Assertions.assertEquals("", title.subtitle());
        }, "Should not throw a FlagParseException");
    }

    @Test
    public void shouldSuccessfullyParseTitleFlagWithTitleMultipleWordsAndSubTitleEmpty() {
        Assertions.assertDoesNotThrow(() -> {
            var title = PlotTitleFlag.TITLE_FLAG_DEFAULT.parse("\"test hello test\" \"\"").getValue();
            Assertions.assertEquals("test hello test", title.title());
            Assertions.assertEquals("", title.subtitle());
        }, "Should not throw a FlagParseException");
    }

    @Test
    public void shouldSuccessfullyParseTitleFlagWithTitleMultipleWordsAndSubTitleMultipleWords() {
        Assertions.assertDoesNotThrow(() -> {
            var title = PlotTitleFlag.TITLE_FLAG_DEFAULT.parse("\"test hello test\" \"a very long subtitle\"").getValue();
            Assertions.assertEquals("test hello test", title.title());
            Assertions.assertEquals("a very long subtitle", title.subtitle());
        }, "Should not throw a FlagParseException");
    }

    @Test
    public void shouldSuccessfullyParseTitleFlagWithTitleEmptyAndSubTitleSingleWord() {
        Assertions.assertDoesNotThrow(() -> {
            var title = PlotTitleFlag.TITLE_FLAG_DEFAULT.parse("\"\" \"single\"").getValue();
            Assertions.assertEquals("", title.title());
            Assertions.assertEquals("single", title.subtitle());
        }, "Should not throw a FlagParseException");
    }

    @Test
    public void shouldExtractTitleWhenASingleDoubleQuoteAtEndOfTitle() {
        Assertions.assertDoesNotThrow(() -> {
            var plotTitle = PlotTitleFlag.TITLE_FLAG_DEFAULT.parse("title\"").getValue();
            Assertions.assertEquals("title", plotTitle.title());
            Assertions.assertEquals("", plotTitle.subtitle());
        }, "Should not throw a FlagParseException");
    }

    @Test
    public void shouldThrowFlagParseExceptionWithQuotesGreater4() {
        var exception = Assertions.assertThrows(
                FlagParseException.class,
                () -> PlotTitleFlag.TITLE_FLAG_DEFAULT.parse("\"title\" \"subtitle\" \"more\""),
                "Needs to throw a FlagParseException"
        );
        Assertions.assertTrue(exception.getErrorMessage() instanceof TranslatableCaption);
        Assertions.assertEquals(
                "flags.flag_error_title",
                ((TranslatableCaption) exception.getErrorMessage()).getKey()
        );
    }

}
