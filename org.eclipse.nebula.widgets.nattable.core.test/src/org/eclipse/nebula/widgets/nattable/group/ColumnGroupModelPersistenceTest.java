/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnGroupModelPersistenceTest {

    private ColumnGroupModel model;

    @BeforeEach
    public void setup() {
        this.model = new ColumnGroupModel();
    }

    @Test
    public void testSaveState() {
        this.model.addColumnsIndexesToGroup("groupA", 1, 2, 3);
        this.model.addColumnsIndexesToGroup("groupB", 5, 7, 9);
        this.model.getColumnGroupByIndex(7).setCollapsed(true);
        this.model.getColumnGroupByIndex(7).setUnbreakable(true);

        Properties properties = new Properties();
        this.model.saveState("prefix", properties);

        assertEquals(1, properties.size());
        assertEquals(
                "groupA=expanded:collapseable:breakable:1,2,3,|groupB=collapsed:collapseable:unbreakable:5,7,9,|",
                properties.getProperty("prefix.columnGroups"));
    }

    @Test
    public void testSaveStateWithStatics() {
        this.model.addColumnsIndexesToGroup("groupA", 1, 2, 3);
        this.model.insertStaticColumnIndexes("groupA", 1, 2);
        this.model.addColumnsIndexesToGroup("groupB", 5, 7, 9);
        this.model.getColumnGroupByIndex(7).setCollapsed(true);
        this.model.getColumnGroupByIndex(7).setUnbreakable(true);

        Properties properties = new Properties();
        this.model.saveState("prefix", properties);

        assertEquals(1, properties.size());
        assertEquals(
                "groupA=expanded:collapseable:breakable:1,2,3,:1,2,|groupB=collapsed:collapseable:unbreakable:5,7,9,|",
                properties.getProperty("prefix.columnGroups"));
    }

    @Test
    public void testLoadState() {
        Properties properties = new Properties();
        properties
                .setProperty(
                        "prefix.columnGroups",
                        "groupA=expanded:collapseable:breakable:1,2,3,|groupB=collapsed:collapseable:unbreakable:5,7,9,|");

        this.model.loadState("prefix", properties);

        assertTrue(this.model.isAGroup("groupA"));
        assertTrue(this.model.isAGroup("groupB"));

        assertNull(this.model.getColumnGroupByIndex(0));
        assertEquals("groupA", this.model.getColumnGroupByIndex(1).getName());
        assertEquals("groupA", this.model.getColumnGroupByIndex(2).getName());
        assertEquals("groupA", this.model.getColumnGroupByIndex(3).getName());
        assertNull(this.model.getColumnGroupByIndex(4));
        assertEquals("groupB", this.model.getColumnGroupByIndex(5).getName());
        assertNull(this.model.getColumnGroupByIndex(6));
        assertEquals("groupB", this.model.getColumnGroupByIndex(7).getName());
        assertNull(this.model.getColumnGroupByIndex(8));
        assertEquals("groupB", this.model.getColumnGroupByIndex(9).getName());
        assertNull(this.model.getColumnGroupByIndex(10));

        assertFalse(this.model.getColumnGroupByName("groupA").isCollapsed());
        assertTrue(this.model.getColumnGroupByName("groupB").isCollapsed());

        assertTrue(this.model.getColumnGroupByName("groupA").isCollapseable());
        assertTrue(this.model.getColumnGroupByName("groupB").isCollapseable());

        assertFalse(this.model.isPartOfAnUnbreakableGroup(1));
        assertFalse(this.model.isPartOfAnUnbreakableGroup(2));
        assertFalse(this.model.isPartOfAnUnbreakableGroup(3));

        assertFalse(this.model.isPartOfAnUnbreakableGroup(4));

        assertTrue(this.model.isPartOfAnUnbreakableGroup(5));
        assertTrue(this.model.isPartOfAnUnbreakableGroup(7));
        assertTrue(this.model.isPartOfAnUnbreakableGroup(9));
    }

    @Test
    public void testLoadStateWithStatics() {
        Properties properties = new Properties();
        properties
                .setProperty(
                        "prefix.columnGroups",
                        "groupA=expanded:collapseable:breakable:1,2,3,:1,2,|groupB=collapsed:collapseable:unbreakable:5,7,9,|");

        this.model.loadState("prefix", properties);

        assertTrue(this.model.isAGroup("groupA"));
        assertTrue(this.model.isAGroup("groupB"));

        assertNull(this.model.getColumnGroupByIndex(0));
        assertEquals("groupA", this.model.getColumnGroupByIndex(1).getName());
        assertEquals("groupA", this.model.getColumnGroupByIndex(2).getName());
        assertEquals("groupA", this.model.getColumnGroupByIndex(3).getName());
        assertNull(this.model.getColumnGroupByIndex(4));
        assertEquals("groupB", this.model.getColumnGroupByIndex(5).getName());
        assertNull(this.model.getColumnGroupByIndex(6));
        assertEquals("groupB", this.model.getColumnGroupByIndex(7).getName());
        assertNull(this.model.getColumnGroupByIndex(8));
        assertEquals("groupB", this.model.getColumnGroupByIndex(9).getName());
        assertNull(this.model.getColumnGroupByIndex(10));

        assertFalse(this.model.getColumnGroupByName("groupA").isCollapsed());
        assertTrue(this.model.getColumnGroupByName("groupB").isCollapsed());

        assertTrue(this.model.getColumnGroupByName("groupA").isCollapseable());
        assertTrue(this.model.getColumnGroupByName("groupB").isCollapseable());

        assertFalse(this.model.isPartOfAnUnbreakableGroup(1));
        assertFalse(this.model.isPartOfAnUnbreakableGroup(2));
        assertFalse(this.model.isPartOfAnUnbreakableGroup(3));

        assertFalse(this.model.isPartOfAnUnbreakableGroup(4));

        assertTrue(this.model.isPartOfAnUnbreakableGroup(5));
        assertTrue(this.model.isPartOfAnUnbreakableGroup(7));
        assertTrue(this.model.isPartOfAnUnbreakableGroup(9));

        assertEquals(2, this.model.getColumnGroupByName("groupA")
                .getStaticColumnIndexes().size());
        assertEquals(1, this.model.getColumnGroupByName("groupA")
                .getStaticColumnIndexes().get(0).intValue());
        assertEquals(2, this.model.getColumnGroupByName("groupA")
                .getStaticColumnIndexes().get(1).intValue());
    }

    @Test
    public void testSaveAndLoadStateWithStatics() {
        this.model.addColumnsIndexesToGroup("groupA", 1, 2, 3);
        this.model.addColumnsIndexesToGroup("groupB", 5, 7, 9);
        this.model.insertStaticColumnIndexes("groupB", 7);
        this.model.getColumnGroupByIndex(7).setCollapsed(true);
        this.model.getColumnGroupByIndex(7).setUnbreakable(true);

        Properties properties = new Properties();
        this.model.saveState("prefix", properties);

        assertEquals(1, properties.size());
        assertEquals(
                "groupA=expanded:collapseable:breakable:1,2,3,|groupB=collapsed:collapseable:unbreakable:5,7,9,:7,|",
                properties.getProperty("prefix.columnGroups"));

        ColumnGroupModel loadedModel = new ColumnGroupModel();
        loadedModel.loadState("prefix", properties);

        assertTrue(loadedModel.isAGroup("groupA"));
        assertTrue(loadedModel.isAGroup("groupB"));

        assertNull(loadedModel.getColumnGroupByIndex(0));
        assertEquals("groupA", loadedModel.getColumnGroupByIndex(1)
                .getName());
        assertEquals("groupA", loadedModel.getColumnGroupByIndex(2)
                .getName());
        assertEquals("groupA", loadedModel.getColumnGroupByIndex(3)
                .getName());
        assertNull(loadedModel.getColumnGroupByIndex(4));
        assertEquals("groupB", loadedModel.getColumnGroupByIndex(5)
                .getName());
        assertNull(loadedModel.getColumnGroupByIndex(6));
        assertEquals("groupB", loadedModel.getColumnGroupByIndex(7)
                .getName());
        assertNull(loadedModel.getColumnGroupByIndex(8));
        assertEquals("groupB", loadedModel.getColumnGroupByIndex(9)
                .getName());
        assertNull(loadedModel.getColumnGroupByIndex(10));

        assertFalse(loadedModel.getColumnGroupByName("groupA")
                .isCollapsed());
        assertTrue(loadedModel.getColumnGroupByName("groupB")
                .isCollapsed());

        assertTrue(loadedModel.getColumnGroupByName("groupA")
                .isCollapseable());
        assertTrue(loadedModel.getColumnGroupByName("groupB")
                .isCollapseable());

        assertFalse(loadedModel.isPartOfAnUnbreakableGroup(1));
        assertFalse(loadedModel.isPartOfAnUnbreakableGroup(2));
        assertFalse(loadedModel.isPartOfAnUnbreakableGroup(3));

        assertFalse(loadedModel.isPartOfAnUnbreakableGroup(4));

        assertTrue(loadedModel.isPartOfAnUnbreakableGroup(5));
        assertTrue(loadedModel.isPartOfAnUnbreakableGroup(7));
        assertTrue(loadedModel.isPartOfAnUnbreakableGroup(9));

        assertEquals(1, loadedModel.getColumnGroupByName("groupB")
                .getStaticColumnIndexes().size());
        assertEquals(7, loadedModel.getColumnGroupByName("groupB")
                .getStaticColumnIndexes().get(0).intValue());
    }
}
