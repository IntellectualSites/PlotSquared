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
package com.plotsquared.core.plot;

import com.plotsquared.core.database.AbstractDBTest;
import com.plotsquared.core.database.DBFunc;
import com.sk89q.worldedit.world.item.ItemType;
import org.junit.Before;

public class FlagTest {

    private ItemType testBlock;

    @Before public void setUp() throws Exception {
        //EventUtil.manager = new EventUtilTest();
        DBFunc.dbManager = new AbstractDBTest();
    }

//    @Test public void flagTest() throws Exception {
//        Plot plot = new Plot(null, PlotId.of(0, 0));
//        plot.owner = UUID.fromString("84499644-ad72-454b-a19d-f28c28df382b");
//        //plot.setFlag(use, use.parseValue("33,33:1,6:4")); //TODO fix this so FlagTest will run during compile
//        Optional<? extends Collection> flag = plot.getFlag(use);
//        if (flag.isPresent()) {
//            System.out.println(Flags.USE.valueToString(flag.get()));
//            testBlock = ItemTypes.BONE_BLOCK;
//            flag.get().add(testBlock);
//        }
//        flag.ifPresent(collection -> System.out.println(Flags.USE.valueToString(collection)));
//        Optional<Set<BlockType>> flag2 = plot.getFlag(Flags.USE);
//        if (flag2.isPresent()) {
//            //   assertThat(flag2.get(), (Matcher<? super Set<BlockType>>) IsCollectionContaining.hasItem(testBlock));
//        }
//        if (flag.isPresent() && flag2.isPresent()) {
//            assertEquals(flag.get(), flag2.get());
//        }
//    }
}
