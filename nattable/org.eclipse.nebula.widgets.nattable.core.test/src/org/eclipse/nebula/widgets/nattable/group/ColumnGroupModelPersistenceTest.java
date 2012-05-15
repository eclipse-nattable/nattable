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

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
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
		model.collapse(7);
		model.setGroupUnBreakable(7);
		
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
		
		Assert.assertNull(model.getColumnGroupNameForIndex(0));
		Assert.assertEquals("groupA", model.getColumnGroupNameForIndex(1));
		Assert.assertEquals("groupA", model.getColumnGroupNameForIndex(2));
		Assert.assertEquals("groupA", model.getColumnGroupNameForIndex(3));
		Assert.assertNull(model.getColumnGroupNameForIndex(4));
		Assert.assertEquals("groupB", model.getColumnGroupNameForIndex(5));
		Assert.assertNull(model.getColumnGroupNameForIndex(6));
		Assert.assertEquals("groupB", model.getColumnGroupNameForIndex(7));
		Assert.assertNull(model.getColumnGroupNameForIndex(8));
		Assert.assertEquals("groupB", model.getColumnGroupNameForIndex(9));
		Assert.assertNull(model.getColumnGroupNameForIndex(10));
		
		Assert.assertFalse(model.isCollapsed("groupA"));
		Assert.assertTrue(model.isCollapsed("groupB"));
		
		Assert.assertTrue(model.isCollapseable("groupA"));
		Assert.assertTrue(model.isCollapseable("groupB"));

		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(1));
		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(2));
		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(3));

		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(4));

		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(5));
		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(7));
		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(9));
	}
	
}
