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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupModelTest {

	public static final String TEST_GROUP_NAME_3 = "testGroupName3";
	public static final String TEST_GROUP_NAME_2 = "testGroupName2";
	public static final String TEST_GROUP_NAME_1 = "testGroupName";
	private ColumnGroupModel model;
	
	@Before
	public void setup() {
		model = new ColumnGroupModel();
		model.addColumnsIndexesToGroup(TEST_GROUP_NAME_1, 0, 1);
		model.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 7, 8);
		model.addColumnsIndexesToGroup(TEST_GROUP_NAME_3, 12, 13);
	}
	
	@Test
	public void getColumnGroupForIndex() throws Exception {
		Assert.assertEquals(TEST_GROUP_NAME_1, model.getColumnGroupNameForIndex(1));
		Assert.assertEquals(TEST_GROUP_NAME_2, model.getColumnGroupNameForIndex(7));
		Assert.assertEquals(TEST_GROUP_NAME_3, model.getColumnGroupNameForIndex(13));
		Assert.assertEquals(null, model.getColumnGroupNameForIndex(15));

		Assert.assertTrue(model.isPartOfAGroup(1));
		Assert.assertTrue(model.isPartOfAGroup(7));
		Assert.assertTrue(model.isPartOfAGroup(13));
		Assert.assertFalse(model.isPartOfAGroup(130));
	}

	@Test
	public void getColumnIndexesInGroup() throws Exception {
		List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(0);
		Assert.assertNotNull(columnIndexesInGroup);
		Assert.assertEquals(2, columnIndexesInGroup.size());
		Assert.assertEquals(0, columnIndexesInGroup.get(0).intValue());
		Assert.assertEquals(1, columnIndexesInGroup.get(1).intValue());
	}

	@Test
	public void getColumnIndexesInGroupForAColumnNotInAGroup() throws Exception {
		List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(100);

		Assert.assertNotNull(columnIndexesInGroup);
		Assert.assertEquals(0, columnIndexesInGroup.size());
	}
	
	@Test
	public void isPartOfAGroup() throws Exception {
		Assert.assertTrue(model.isPartOfAGroup(7));
		Assert.assertFalse(model.isPartOfAGroup(70));
	}
	
	@Test
	public void collapse() throws Exception {
		model.collapse(0);
		
		Assert.assertTrue(model.isCollapsed(0));
		Assert.assertTrue(model.isCollapsed(1));

		Assert.assertFalse(model.isCollapsed(7));
	}
	
	@Test
	public void expand() throws Exception {
		model.collapse(7);
		Assert.assertTrue(model.isCollapsed(7));
		Assert.assertTrue(model.isCollapsed(8));
		
		model.expand(7);
		Assert.assertFalse(model.isCollapsed(7));
		Assert.assertFalse(model.isCollapsed(8));
	}

	@Test
	public void getCollapsedColumnCount() throws Exception {
		Assert.assertEquals(0, model.getCollapsedColumnCount());
		
		model.collapse(0);
		Assert.assertEquals(1, model.getCollapsedColumnCount());

		model.collapse(8);
		Assert.assertEquals(2, model.getCollapsedColumnCount());

		model.expand(8);
		Assert.assertEquals(1, model.getCollapsedColumnCount());
	}
	
	@Test
	public void removeColumnFromGroup() {
		List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(12);
		
		Assert.assertTrue(columnIndexesInGroup.contains(12));
		Assert.assertTrue(model.removeColumnFromGroup(12));
		
		columnIndexesInGroup = model.getColumnIndexesInGroup(13);
		Assert.assertEquals(1, columnIndexesInGroup.size());
		Assert.assertTrue(model.removeColumnFromGroup(13));

		Assert.assertFalse(model.isPartOfAGroup(12));
	}
	
	@Test
	public void removeanIndexNotInAGroup() throws Exception {
		Assert.assertFalse(model.removeColumnFromGroup(100));
	}
	
	@Test
	public void shouldInsertAColumnIndexToAGroup() {
		List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(0);
		
		Assert.assertTrue(2 == columnIndexesInGroup.size());
		Assert.assertTrue(columnIndexesInGroup.contains(new Integer(0)));
		Assert.assertTrue(columnIndexesInGroup.contains(new Integer(1)));
		
		Assert.assertTrue(model.insertColumnIndexes(model.getColumnGroupNameForIndex(0), 4));
		columnIndexesInGroup = model.getColumnIndexesInGroup(0);

		Assert.assertEquals(3, columnIndexesInGroup.size());
		Assert.assertTrue(columnIndexesInGroup.contains(new Integer(0)));
		Assert.assertTrue(columnIndexesInGroup.contains(new Integer(1)));
		Assert.assertTrue(columnIndexesInGroup.contains(new Integer(4)));
		
		Assert.assertTrue(model.isPartOfAGroup(4));
	}
	
	@Test
	public void shouldNotInsertIntoAnUnbreakableGroup() throws Exception {
		model.setGroupUnBreakable(0);

		Assert.assertFalse(model.insertColumnIndexes(model.getColumnGroupNameForIndex(0), 4));
		
		List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(0);
		Assert.assertEquals(2, columnIndexesInGroup.size());
		Assert.assertTrue(columnIndexesInGroup.contains(new Integer(0)));
		Assert.assertTrue(columnIndexesInGroup.contains(new Integer(1)));
	}
	
	@Test
	public void shouldFailWhenTryingToInsertSameColumnTwice() {
		model.insertColumnIndexes(model.getColumnGroupNameForIndex(0), 4);
		Assert.assertFalse(model.insertColumnIndexes(model.getColumnGroupNameForIndex(0), 4, 1, 0));
	}
	
	@Test
	public void shouldFindColumnGroupPositionForColumnIndex() {
		Assert.assertEquals(1, model.getColumnGroupPositionFromIndex(8));
		Assert.assertEquals(-1, model.getColumnGroupPositionFromIndex(11));
	}
	
	@Test
	public void toggleColumnGroup() throws Exception {
		Assert.assertFalse(model.isCollapsed(0));
		
		model.toggleColumnGroupExpandCollapse(0);
		Assert.assertTrue(model.isCollapsed(0));

		model.toggleColumnGroupExpandCollapse(0);
		Assert.assertFalse(model.isCollapsed(0));
	}
	
	@Test
	public void isCollapsedByName() throws Exception {
		Assert.assertFalse(model.isCollapsed(TEST_GROUP_NAME_1));
		
		model.collapse(0);
		
		Assert.assertTrue(model.isCollapsed(TEST_GROUP_NAME_1));
		Assert.assertFalse(model.isCollapsed(TEST_GROUP_NAME_2));
		Assert.assertFalse(model.isCollapsed("XYZ"));
	}
	
	@Test
	public void sizeOfGroup() throws Exception {
		assertEquals(2, model.sizeOfGroup(0));
		assertEquals(0, model.sizeOfGroup(100)); //non existent
	}
	
	@Test
	public void markAsUnbreakable() throws Exception {
		Assert.assertFalse(model.isPartOfAnUnbreakableGroup(0));

		model.setGroupUnBreakable(0);
		Assert.assertTrue(model.isPartOfAnUnbreakableGroup(0));
	}
	
	@Test
	public void shouldNotRemoveFromAnUnbreakableGroup() throws Exception {
		model.setGroupUnBreakable(7);
		Assert.assertFalse(model.removeColumnFromGroup(7));
	}
	
	@Test
	public void getAllIndexesInGroups() throws Exception {
		List<Integer> indexes = model.getAllIndexesInGroups();
		Assert.assertEquals(6, indexes.size());
		Assert.assertTrue(indexes.contains(0));
		Assert.assertTrue(indexes.contains(1));
		Assert.assertTrue(indexes.contains(7));
		Assert.assertTrue(indexes.contains(8));
		Assert.assertTrue(indexes.contains(12));
		Assert.assertTrue(indexes.contains(13));
	}
	
	@Test
	public void sizeOfStaticColumns() throws Exception {
		model.addColumnsIndexesToGroup("TEST_GROUP_NAME_4", 14, 15, 16, 17);
		model.insertStaticColumnIndexes("TEST_GROUP_NAME_4", 15, 16);
		
		Assert.assertEquals(2, model.sizeOfStaticColumns(14));
		Assert.assertEquals(2, model.sizeOfStaticColumns(15));
		Assert.assertEquals(2, model.sizeOfStaticColumns(16));
		Assert.assertEquals(2, model.sizeOfStaticColumns(17));
	}
	
	@Test
	public void getStaticColumnIndexesInGroup() throws Exception {
		model.addColumnsIndexesToGroup("TEST_GROUP_NAME_4", 14, 15, 16, 17);
		model.insertStaticColumnIndexes("TEST_GROUP_NAME_4", 15, 16);
		
		Assert.assertEquals(2, model.getStaticColumnIndexesInGroup(14).size());
		Assert.assertEquals(0, model.getStaticColumnIndexesInGroup(33).size());
		
		Assert.assertEquals(15, model.getStaticColumnIndexesInGroup(14).get(0).intValue());
		Assert.assertEquals(16, model.getStaticColumnIndexesInGroup(14).get(1).intValue());
	}
	
	
}
