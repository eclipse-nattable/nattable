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

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupModelPersistenceTest {

    private ColumnGroupModel model;

    @Before
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

        Assert.assertEquals(1, properties.size());
        Assert.assertEquals(
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

        Assert.assertEquals(1, properties.size());
        Assert.assertEquals(
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

        Assert.assertTrue(this.model.isAGroup("groupA"));
        Assert.assertTrue(this.model.isAGroup("groupB"));

        Assert.assertNull(this.model.getColumnGroupByIndex(0));
        Assert.assertEquals("groupA", this.model.getColumnGroupByIndex(1).getName());
        Assert.assertEquals("groupA", this.model.getColumnGroupByIndex(2).getName());
        Assert.assertEquals("groupA", this.model.getColumnGroupByIndex(3).getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(4));
        Assert.assertEquals("groupB", this.model.getColumnGroupByIndex(5).getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(6));
        Assert.assertEquals("groupB", this.model.getColumnGroupByIndex(7).getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(8));
        Assert.assertEquals("groupB", this.model.getColumnGroupByIndex(9).getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(10));

        Assert.assertFalse(this.model.getColumnGroupByName("groupA").isCollapsed());
        Assert.assertTrue(this.model.getColumnGroupByName("groupB").isCollapsed());

        Assert.assertTrue(this.model.getColumnGroupByName("groupA").isCollapseable());
        Assert.assertTrue(this.model.getColumnGroupByName("groupB").isCollapseable());

        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(1));
        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(2));
        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(3));

        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(4));

        Assert.assertTrue(this.model.isPartOfAnUnbreakableGroup(5));
        Assert.assertTrue(this.model.isPartOfAnUnbreakableGroup(7));
        Assert.assertTrue(this.model.isPartOfAnUnbreakableGroup(9));
    }

    @Test
    public void testLoadStateWithStatics() {
        Properties properties = new Properties();
        properties
                .setProperty(
                        "prefix.columnGroups",
                        "groupA=expanded:collapseable:breakable:1,2,3,:1,2,|groupB=collapsed:collapseable:unbreakable:5,7,9,|");

        this.model.loadState("prefix", properties);

        Assert.assertTrue(this.model.isAGroup("groupA"));
        Assert.assertTrue(this.model.isAGroup("groupB"));

        Assert.assertNull(this.model.getColumnGroupByIndex(0));
        Assert.assertEquals("groupA", this.model.getColumnGroupByIndex(1).getName());
        Assert.assertEquals("groupA", this.model.getColumnGroupByIndex(2).getName());
        Assert.assertEquals("groupA", this.model.getColumnGroupByIndex(3).getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(4));
        Assert.assertEquals("groupB", this.model.getColumnGroupByIndex(5).getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(6));
        Assert.assertEquals("groupB", this.model.getColumnGroupByIndex(7).getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(8));
        Assert.assertEquals("groupB", this.model.getColumnGroupByIndex(9).getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(10));

        Assert.assertFalse(this.model.getColumnGroupByName("groupA").isCollapsed());
        Assert.assertTrue(this.model.getColumnGroupByName("groupB").isCollapsed());

        Assert.assertTrue(this.model.getColumnGroupByName("groupA").isCollapseable());
        Assert.assertTrue(this.model.getColumnGroupByName("groupB").isCollapseable());

        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(1));
        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(2));
        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(3));

        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(4));

        Assert.assertTrue(this.model.isPartOfAnUnbreakableGroup(5));
        Assert.assertTrue(this.model.isPartOfAnUnbreakableGroup(7));
        Assert.assertTrue(this.model.isPartOfAnUnbreakableGroup(9));

        Assert.assertEquals(2, this.model.getColumnGroupByName("groupA")
                .getStaticColumnIndexes().size());
        Assert.assertEquals(1, this.model.getColumnGroupByName("groupA")
                .getStaticColumnIndexes().get(0).intValue());
        Assert.assertEquals(2, this.model.getColumnGroupByName("groupA")
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

        Assert.assertEquals(1, properties.size());
        Assert.assertEquals(
                "groupA=expanded:collapseable:breakable:1,2,3,|groupB=collapsed:collapseable:unbreakable:5,7,9,:7,|",
                properties.getProperty("prefix.columnGroups"));

        ColumnGroupModel loadedModel = new ColumnGroupModel();
        loadedModel.loadState("prefix", properties);

        Assert.assertTrue(loadedModel.isAGroup("groupA"));
        Assert.assertTrue(loadedModel.isAGroup("groupB"));

        Assert.assertNull(loadedModel.getColumnGroupByIndex(0));
        Assert.assertEquals("groupA", loadedModel.getColumnGroupByIndex(1)
                .getName());
        Assert.assertEquals("groupA", loadedModel.getColumnGroupByIndex(2)
                .getName());
        Assert.assertEquals("groupA", loadedModel.getColumnGroupByIndex(3)
                .getName());
        Assert.assertNull(loadedModel.getColumnGroupByIndex(4));
        Assert.assertEquals("groupB", loadedModel.getColumnGroupByIndex(5)
                .getName());
        Assert.assertNull(loadedModel.getColumnGroupByIndex(6));
        Assert.assertEquals("groupB", loadedModel.getColumnGroupByIndex(7)
                .getName());
        Assert.assertNull(loadedModel.getColumnGroupByIndex(8));
        Assert.assertEquals("groupB", loadedModel.getColumnGroupByIndex(9)
                .getName());
        Assert.assertNull(loadedModel.getColumnGroupByIndex(10));

        Assert.assertFalse(loadedModel.getColumnGroupByName("groupA")
                .isCollapsed());
        Assert.assertTrue(loadedModel.getColumnGroupByName("groupB")
                .isCollapsed());

        Assert.assertTrue(loadedModel.getColumnGroupByName("groupA")
                .isCollapseable());
        Assert.assertTrue(loadedModel.getColumnGroupByName("groupB")
                .isCollapseable());

        Assert.assertFalse(loadedModel.isPartOfAnUnbreakableGroup(1));
        Assert.assertFalse(loadedModel.isPartOfAnUnbreakableGroup(2));
        Assert.assertFalse(loadedModel.isPartOfAnUnbreakableGroup(3));

        Assert.assertFalse(loadedModel.isPartOfAnUnbreakableGroup(4));

        Assert.assertTrue(loadedModel.isPartOfAnUnbreakableGroup(5));
        Assert.assertTrue(loadedModel.isPartOfAnUnbreakableGroup(7));
        Assert.assertTrue(loadedModel.isPartOfAnUnbreakableGroup(9));

        Assert.assertEquals(1, loadedModel.getColumnGroupByName("groupB")
                .getStaticColumnIndexes().size());
        Assert.assertEquals(7, loadedModel.getColumnGroupByName("groupB")
                .getStaticColumnIndexes().get(0).intValue());
    }
}
