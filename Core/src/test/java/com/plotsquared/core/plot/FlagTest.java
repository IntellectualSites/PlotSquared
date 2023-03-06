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
import com.sk89q.worldedit.world.item.ItemType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FlagTest {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + FlagTest.class.getSimpleName());

    private ItemType testBlock;

    @BeforeEach
    public void setUp() throws Exception {
        //EventUtil.manager = new EventUtilTest();
        DBFunc.dbManager = new AbstractDBTest();
    }

//    @Test public void flagTest() throws Exception {
//        Plot plot = new Plot(null, PlotId.of(0, 0));
//        plot.owner = UUID.fromString("84499644-ad72-454b-a19d-f28c28df382b");
//        //plot.setFlag(use, use.parseValue("33,33:1,6:4")); //TODO fix this so FlagTest will run during compile
//        Optional<? extends Collection> flag = plot.getFlag(use);
//        if (flag.isPresent()) {
//            LOGGER.info(Flags.USE.valueToString(flag.get()));
//            testBlock = ItemTypes.BONE_BLOCK;
//            flag.get().add(testBlock);
//        }
//        flag.ifPresent(collection -> LOGGER.info(Flags.USE.valueToString(collection)));
//        Optional<Set<BlockType>> flag2 = plot.getFlag(Flags.USE);
//        if (flag2.isPresent()) {
//            //   assertThat(flag2.get(), (Matcher<? super Set<BlockType>>) IsCollectionContaining.hasItem(testBlock));
//        }
//        if (flag.isPresent() && flag2.isPresent()) {
//            assertEquals(flag.get(), flag2.get());
//        }
//    }

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
