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
	
}
