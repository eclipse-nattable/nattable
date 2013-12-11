/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		model = new ColumnGroupModel();
	}
	
	@Test
	public void testSaveState() {
		model.addColumnsIndexesToGroup("groupA", 1,2,3);
		model.addColumnsIndexesToGroup("groupB", 5,7,9);
		model.getColumnGroupByIndex(7).setCollapsed(true);
		model.getColumnGroupByIndex(7).setUnbreakable(true);
		
		Properties properties = new Properties();
		model.saveState("prefix", properties);
		
		Assert.assertEquals(1, properties.size());
		Assert.assertEquals("groupA=expanded:collapseable:breakable:1,2,3,|groupB=collapsed:collapseable:unbreakable:5,7,9,|", properties.getProperty("prefix.columnGroups"));
	}
	
	@Test
	public void testSaveStateWithStatics() {
		model.addColumnsIndexesToGroup("groupA", 1,2,3);
		model.insertStaticColumnIndexes("groupA", 1, 2);
		model.addColumnsIndexesToGroup("groupB", 5,7,9);
		model.getColumnGroupByIndex(7).setCollapsed(true);
		model.getColumnGroupByIndex(7).setUnbreakable(true);
		
		Properties properties = new Properties();
		model.saveState("prefix", properties);
		
		Assert.assertEquals(1, properties.size());
		Assert.assertEquals("groupA=expanded:collapseable:breakable:1,2,3,:1,2,|groupB=collapsed:collapseable:unbreakable:5,7,9,|", properties.getProperty("prefix.columnGroups"));
	}
	
	@Test
	public void testLoadState() {
		Properties properties = new Properties();
		properties.setProperty("prefix.columnGroups", "groupA=expanded:collapseable:breakable:1,2,3,|groupB=collapsed:collapseable:unbreakable:5,7,9,|");
		
		model.loadState("prefix", properties);
		
		Assert.assertTrue(model.isAGroup("groupA"));
		Assert.assertTrue(model.isAGroup("groupB"));
		
		Assert.assertNull(model.getColumnGroupByIndex(0));
		Assert.assertEquals("groupA", model.getColumnGroupByIndex(1).getName());
		Assert.assertEquals("groupA", model.getColumnGroupByIndex(2).getName());
		Assert.assertEquals("groupA", model.getColumnGroupByIndex(3).getName());
		Assert.assertNull(model.getColumnGroupByIndex(4));
		Assert.assertEquals("groupB", model.getColumnGroupByIndex(5).getName());
		Assert.assertNull(model.getColumnGroupByIndex(6));
		Assert.assertEquals("groupB", model.getColumnGroupByIndex(7).getName());
		Assert.assertNull(model.getColumnGroupByIndex(8));
		Assert.assertEquals("groupB", model.getColumnGroupByIndex(9).getName());
		Assert.assertNull(model.getColumnGroupByIndex(10));
		
		Assert.assertFalse(model.getColumnGroupByName("groupA").isCollapsed());
		Assert.assertTrue(model.getColumnGroupByName("groupB").isCollapsed());
		
		Assert.assertTrue(model.getColumnGroupByName("groupA").isCollapseable());
		Assert.assertTrue(model.getColumnGroupByName("groupB").isCollapseable());

		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(1));
		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(2));
		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(3));

		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(4));

		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(5));
		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(7));
		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(9));
	}
	
	@Test
	public void testLoadStateWithStatics() {
		Properties properties = new Properties();
		properties.setProperty("prefix.columnGroups", "groupA=expanded:collapseable:breakable:1,2,3,:1,2,|groupB=collapsed:collapseable:unbreakable:5,7,9,|");
		
		model.loadState("prefix", properties);
		
		Assert.assertTrue(model.isAGroup("groupA"));
		Assert.assertTrue(model.isAGroup("groupB"));
		
		Assert.assertNull(model.getColumnGroupByIndex(0));
		Assert.assertEquals("groupA", model.getColumnGroupByIndex(1).getName());
		Assert.assertEquals("groupA", model.getColumnGroupByIndex(2).getName());
		Assert.assertEquals("groupA", model.getColumnGroupByIndex(3).getName());
		Assert.assertNull(model.getColumnGroupByIndex(4));
		Assert.assertEquals("groupB", model.getColumnGroupByIndex(5).getName());
		Assert.assertNull(model.getColumnGroupByIndex(6));
		Assert.assertEquals("groupB", model.getColumnGroupByIndex(7).getName());
		Assert.assertNull(model.getColumnGroupByIndex(8));
		Assert.assertEquals("groupB", model.getColumnGroupByIndex(9).getName());
		Assert.assertNull(model.getColumnGroupByIndex(10));
		
		Assert.assertFalse(model.getColumnGroupByName("groupA").isCollapsed());
		Assert.assertTrue(model.getColumnGroupByName("groupB").isCollapsed());
		
		Assert.assertTrue(model.getColumnGroupByName("groupA").isCollapseable());
		Assert.assertTrue(model.getColumnGroupByName("groupB").isCollapseable());

		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(1));
		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(2));
		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(3));

		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(4));

		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(5));
		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(7));
		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(9));
		
		Assert.assertEquals(2, model.getColumnGroupByName("groupA").getStaticColumnIndexes().size());
		Assert.assertEquals(1, model.getColumnGroupByName("groupA").getStaticColumnIndexes().get(0).intValue());
		Assert.assertEquals(2, model.getColumnGroupByName("groupA").getStaticColumnIndexes().get(1).intValue());
	}
	
	@Test
	public void testSaveAndLoadStateWithStatics() {
		model.addColumnsIndexesToGroup("groupA", 1,2,3);
		model.addColumnsIndexesToGroup("groupB", 5,7,9);
		model.insertStaticColumnIndexes("groupB", 7);
		model.getColumnGroupByIndex(7).setCollapsed(true);
		model.getColumnGroupByIndex(7).setUnbreakable(true);
		
		Properties properties = new Properties();
		model.saveState("prefix", properties);
		
		Assert.assertEquals(1, properties.size());
		Assert.assertEquals("groupA=expanded:collapseable:breakable:1,2,3,|groupB=collapsed:collapseable:unbreakable:5,7,9,:7,|", properties.getProperty("prefix.columnGroups"));
		
		ColumnGroupModel loadedModel = new ColumnGroupModel();
		loadedModel.loadState("prefix", properties);
		
		Assert.assertTrue(loadedModel.isAGroup("groupA"));
		Assert.assertTrue(loadedModel.isAGroup("groupB"));
		
		Assert.assertNull(loadedModel.getColumnGroupByIndex(0));
		Assert.assertEquals("groupA", loadedModel.getColumnGroupByIndex(1).getName());
		Assert.assertEquals("groupA", loadedModel.getColumnGroupByIndex(2).getName());
		Assert.assertEquals("groupA", loadedModel.getColumnGroupByIndex(3).getName());
		Assert.assertNull(loadedModel.getColumnGroupByIndex(4));
		Assert.assertEquals("groupB", loadedModel.getColumnGroupByIndex(5).getName());
		Assert.assertNull(loadedModel.getColumnGroupByIndex(6));
		Assert.assertEquals("groupB", loadedModel.getColumnGroupByIndex(7).getName());
		Assert.assertNull(loadedModel.getColumnGroupByIndex(8));
		Assert.assertEquals("groupB", loadedModel.getColumnGroupByIndex(9).getName());
		Assert.assertNull(loadedModel.getColumnGroupByIndex(10));
		
		Assert.assertFalse(loadedModel.getColumnGroupByName("groupA").isCollapsed());
		Assert.assertTrue(loadedModel.getColumnGroupByName("groupB").isCollapsed());
		
		Assert.assertTrue(loadedModel.getColumnGroupByName("groupA").isCollapseable());
		Assert.assertTrue(loadedModel.getColumnGroupByName("groupB").isCollapseable());

		Assert.assertFalse(loadedModel.isPartOfAnUnbreakableGroup(1));
		Assert.assertFalse(loadedModel.isPartOfAnUnbreakableGroup(2));
		Assert.assertFalse(loadedModel.isPartOfAnUnbreakableGroup(3));

		Assert.assertFalse(loadedModel.isPartOfAnUnbreakableGroup(4));

		Assert.assertTrue(loadedModel.isPartOfAnUnbreakableGroup(5));
		Assert.assertTrue(loadedModel.isPartOfAnUnbreakableGroup(7));
		Assert.assertTrue(loadedModel.isPartOfAnUnbreakableGroup(9));
		
		Assert.assertEquals(1, loadedModel.getColumnGroupByName("groupB").getStaticColumnIndexes().size());
		Assert.assertEquals(7, loadedModel.getColumnGroupByName("groupB").getStaticColumnIndexes().get(0).intValue());
	}
}
