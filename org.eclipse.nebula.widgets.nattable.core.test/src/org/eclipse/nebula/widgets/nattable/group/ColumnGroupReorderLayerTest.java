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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;


import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnsAndGroupsCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.group.ColumnGroupModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupReorderLayerTest {

	private ColumnGroupReorderLayer layer;
	private ColumnGroupModelFixture modelFixture;
	public ColumnReorderLayer reorderLayer;

	@Before
	public void setUp() {
		modelFixture = new ColumnGroupModelFixture();
		reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(24, 20));
		layer = new ColumnGroupReorderLayer(reorderLayer, modelFixture);
	}

	@Test
	public void shouldDragLeftAndMoveColumnIntoGroup() {
		// Drag left into group containing (0,1)
		ColumnReorderCommand command = new ColumnReorderCommand(layer, 5, 1);
		layer.doCommand(command);

		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(5).getName());
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(1).getName());
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(0).getName());

		assertEquals(3, getColumnIndexesInGroup(5).size());

		assertEquals(0, reorderLayer.getColumnIndexByPosition(0));
		assertEquals(5, reorderLayer.getColumnIndexByPosition(1));
		assertEquals(1, reorderLayer.getColumnIndexByPosition(2));
	}

	@Test
	public void shouldDragLeftAndRemoveColumnFromGroup() {
		// Drag left out of group
		ColumnReorderCommand command = new ColumnReorderCommand(layer, 3, 2);
		layer.doCommand(command);

		assertNull(modelFixture.getColumnGroupByIndex(3));
		assertNull(modelFixture.getColumnGroupByIndex(2));

		assertEquals(1, getColumnIndexesInGroup(4).size());
	}

	@Test
	public void shouldDragRightAndAddColumnToGroup() {
		final int fromColumnIndex = 2;
		final int toColumnIndex = 4;

		// Drag right into group
		ColumnReorderCommand command = new ColumnReorderCommand(layer, fromColumnIndex, toColumnIndex);
		layer.doCommand(command);

		assertEquals(ColumnGroupModelFixture.TEST_GROUP_2, modelFixture.getColumnGroupByIndex(fromColumnIndex).getName());
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_2, modelFixture.getColumnGroupByIndex(toColumnIndex).getName());
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_2, modelFixture.getColumnGroupByIndex(4).getName());
		assertEquals(3, getColumnIndexesInGroup(fromColumnIndex).size());
	}

	@Test
	public void shouldDragFirstCellRightAndAddColumnToGroup() {
		ColumnGroup columnGroup = modelFixture.getColumnGroupByIndex(0);
		columnGroup.removeColumn(0);
		assertEquals(1, getColumnIndexesInGroup(1).size());

		modelFixture.insertColumnIndexes(ColumnGroupModelFixture.TEST_GROUP_1, 2);
		assertEquals(2, getColumnIndexesInGroup(1).size());

		// Drag right into group
		ColumnReorderCommand command = new ColumnReorderCommand(layer, 0, 2);
		layer.doCommand(command);

		final int fromColumnIndex = 0;
		final int toColumnIndex = 2;

		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(fromColumnIndex).getName());
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(toColumnIndex).getName());
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(2).getName());
		assertEquals(3, getColumnIndexesInGroup(fromColumnIndex).size());
	}

	@Test
	public void shouldDragRightAndRemoveColumnFromGroup() {
		// Index in CG: 0,1
		// Drag 0 -> 1

		assertEquals(2, getColumnIndexesInGroup(0).size());

		// Drag right out of group
		ColumnReorderCommand command = new ColumnReorderCommand(layer, 0, 2);
		layer.doCommand(command);

		assertEquals(1, getColumnIndexesInGroup(1).size());
		assertNull(modelFixture.getColumnGroupByIndex(0));
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(1).getName());
	}

	@Test
	public void shouldDragRightAndNotRemoveFromAnUnbreakableGroup() {
		assertEquals(2, getColumnIndexesInGroup(0).size());

		setGroupUnBreakable(0);
		layer.doCommand(new ColumnReorderCommand(layer, 0, 1));

		assertEquals(2, getColumnIndexesInGroup(0).size());
	}

	@Test
	public void shouldRemoveFromOneGroupAndAddToAnother() {
		final int fromColumnIndex = 3;
		final int toColumnIndex = 1;

		assertEquals(ColumnGroupModelFixture.TEST_GROUP_2, modelFixture.getColumnGroupByIndex(3).getName());
		assertEquals(2, getColumnIndexesInGroup(3).size());
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(1).getName());
		assertEquals(2, getColumnIndexesInGroup(1).size());

		// Swap members of column groups
		ColumnReorderCommand command = new ColumnReorderCommand(layer, fromColumnIndex, toColumnIndex);
		layer.doCommand(command);

		assertEquals(ColumnGroupModelFixture.TEST_GROUP_1, modelFixture.getColumnGroupByIndex(3).getName());
		assertEquals(3, getColumnIndexesInGroup(3).size());
		assertEquals(ColumnGroupModelFixture.TEST_GROUP_2, modelFixture.getColumnGroupByIndex(4).getName());
		assertEquals(1, getColumnIndexesInGroup(4).size());
	}

	@Test
	public void shouldNotMoveColumnBetweenGroupsIfEitherGroupIsUnbreakable() {
		setGroupUnBreakable(3);

		ColumnReorderCommand command = new ColumnReorderCommand(layer, 3, 1);
		layer.doCommand(command);

		modelFixture.assertUnchanged();
	}

	@Test
	public void shouldLeaveModelUnchangedOnDragLeftWithinSameGroup() {
		modelFixture.assertTestGroup3IsUnchanged();

		// Drag right and swap positions in group
		ColumnReorderCommand command = new ColumnReorderCommand(layer, 11, 10);
		layer.doCommand(command);

		//The group remains unchanged - order not tracked
		modelFixture.assertTestGroup3IsUnchanged();
	}

	@Test
	public void shouldLeaveModelUnchangedOnDragRightWithinSameGroup() {
		modelFixture.assertTestGroup3IsUnchanged();

		// Drag right and swap positions in group
		ColumnReorderCommand command = new ColumnReorderCommand(layer, 10, 11);
		layer.doCommand(command);

		modelFixture.assertTestGroup3IsUnchanged();
	}

	@Test
	public void shouldReorderEntireColumnGroup() {
		assertEquals(3, reorderLayer.getColumnIndexByPosition(3));
		assertEquals(4, reorderLayer.getColumnIndexByPosition(4));

		ReorderColumnGroupCommand reorderGroupCommand = new ReorderColumnGroupCommand(layer, 3, 13);
		layer.doCommand(reorderGroupCommand);

		assertEquals(3, reorderLayer.getColumnIndexByPosition(11));
		assertEquals(4, reorderLayer.getColumnIndexByPosition(12));
	}

	@Test
	public void shouldReorderEntireColumnGroupAndAddAColumnToCollapsedGroup() {
		collapse(3);
		layer.reorderColumnGroup(3, 13);

		assertEquals(3, reorderLayer.getColumnIndexByPosition(11));
		assertEquals(4, reorderLayer.getColumnIndexByPosition(12));

		layer.doCommand(new ColumnReorderCommand(layer, 16, 12));
		assertEquals(3, getColumnIndexesInGroup(3).size());
	}

	/*
	 *   ..  3     4    5    6    7   ...
	 * -------------------------------------
	 *      |<- G2 ->||<- G4 ->|
	 */
	@Test
	public void adjacentColumnGroups() throws Exception {
		modelFixture.addColumnsIndexesToGroup("G4", 5,6);

		// Drag between the two groups
		ColumnReorderCommand command = new ColumnReorderCommand(layer, 2, 4);
		layer.doCommand(command);

		assertEquals(3, getColumnIndexesInGroup(3).size());
		assertTrue(getColumnIndexesInGroup(3).contains(Integer.valueOf(2)));
		assertTrue(getColumnIndexesInGroup(3).contains(Integer.valueOf(4)));
		assertTrue(getColumnIndexesInGroup(3).contains(Integer.valueOf(4)));

		assertEquals(2, getColumnIndexesInGroup(5).size());
		assertTrue(getColumnIndexesInGroup(5).contains(Integer.valueOf(5)));
		assertTrue(getColumnIndexesInGroup(5).contains(Integer.valueOf(6)));
	}

	/*
	 *        0   1    2    3     4    5    6    7   ...  10  11  12
	 * ------------------------------------------------------------------
	 *     |<- G1 ->|     |<-- G2 -->|                   |<--- G3 --->|
	 */
	@SuppressWarnings("boxing")
	@Test
	public void handleReorderColumnsAndGroupsCommand() throws Exception {
		ReorderColumnsAndGroupsCommand command = new ReorderColumnsAndGroupsCommand(layer, Arrays.asList(3, 5, 6), 8);
		layer.doCommand(command);

		modelFixture.assertUnchanged();

		/* See output for a better idea
		System.out.println("Index\tPosition");
		for (int i = 0; i < reorderLayer.getColumnCount(); i++)
			System.out.println(i  + "\t" + reorderLayer.getColumnIndexByPosition(i)); */

		// Check new positions
		assertEquals(3, reorderLayer.getColumnIndexByPosition(4));
		assertEquals(4, reorderLayer.getColumnIndexByPosition(5));
		assertEquals(5, reorderLayer.getColumnIndexByPosition(6));
		assertEquals(6, reorderLayer.getColumnIndexByPosition(7));
	}

	@Test
	public void shouldAddToAGroupWhileReorderingMultipleColumns() throws Exception {
		MultiColumnReorderCommand reorderCommand = new MultiColumnReorderCommand(layer, Arrays.asList(5, 6), 11);
		layer.doCommand(reorderCommand);

		assertEquals(5, getColumnIndexesInGroup(11).size());
		assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(10)));
		assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(11)));
		assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(12)));
		assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(5)));
		assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(6)));

		assertEquals(10, layer.getColumnIndexByPosition(8));
		assertEquals(5, layer.getColumnIndexByPosition(9));
		assertEquals(6, layer.getColumnIndexByPosition(10));
		assertEquals(11, layer.getColumnIndexByPosition(11));
		assertEquals(12, layer.getColumnIndexByPosition(12));
	}

	@Test
	public void shouldRemoveFromAGroupWhileReorderingMultipleColumns() throws Exception {
		MultiColumnReorderCommand reorderCommand = new MultiColumnReorderCommand(layer, Arrays.asList(10, 11), 5);
		layer.doCommand(reorderCommand);

		assertEquals(1, getColumnIndexesInGroup(12).size());
		assertTrue(getColumnIndexesInGroup(12).contains(Integer.valueOf(12)));
	}

	@Test
	public void reorderMultipleColums() throws Exception {
		// One of the 'from' column is already in the destination group
		MultiColumnReorderCommand reorderCommand = new MultiColumnReorderCommand(layer, Arrays.asList(9, 10), 11);
		layer.doCommand(reorderCommand);

		assertEquals(4, getColumnIndexesInGroup(12).size());
	}

	@Test
	public void shouldNotAllowMultiReorderIntoAnUnbreakableGroup() throws Exception {
		setGroupUnBreakable(11);

		MultiColumnReorderCommand reorderCommand = new MultiColumnReorderCommand(layer, Arrays.asList(5, 6), 11);
		layer.doCommand(reorderCommand);

		modelFixture.assertUnchanged();
	}

	@Test
	public void shouldNotAllowRemovalFromAColumnGroupDuringMultiColumnReorder() throws Exception {
		setGroupUnBreakable(4);

		MultiColumnReorderCommand reorderCommand = new MultiColumnReorderCommand(layer, Arrays.asList(4, 5), 6);
		layer.doCommand(reorderCommand);

		modelFixture.assertUnchanged();
	}
	
	private List<Integer> getColumnIndexesInGroup(int columnIndex) {
		return modelFixture.getColumnGroupByIndex(columnIndex).getMembers();
	}
	
	private void collapse(int columnIndex) {
		modelFixture.getColumnGroupByIndex(columnIndex).setCollapsed(true);
	}
	
	private void setGroupUnBreakable(int columnIndex) {
		modelFixture.getColumnGroupByIndex(columnIndex).setUnbreakable(true);
	}
	
}
