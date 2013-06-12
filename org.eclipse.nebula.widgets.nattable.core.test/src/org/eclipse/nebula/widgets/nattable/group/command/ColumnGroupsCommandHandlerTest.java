/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;


import java.util.List;

import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupsCommandHandlerTest {
	
	private ColumnGroupsCommandHandler handler;
	private ColumnGroupModel model;
	private SelectionLayer selectionLayer;
	private DefaultGridLayer gridLayer;
	
	@Before
	public void setUp() {	
		gridLayer = new GridLayerFixture();
		selectionLayer = (SelectionLayer) gridLayer.getBodyLayer().getViewportLayer().getScrollableLayer();
		model = new ColumnGroupModel();
		handler = new ColumnGroupsCommandHandler(model, selectionLayer, new ColumnGroupHeaderLayer(gridLayer.getColumnHeaderLayer(), gridLayer.getBodyLayer().getSelectionLayer(), new ColumnGroupModel()));
		gridLayer.setClientAreaProvider(new IClientAreaProvider() {
			
			public Rectangle getClientArea() {
				return new Rectangle(0,0,1050,250);
			}
			
		});
		gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
	}
	
	@Test
	public void shouldCreateColumnGroupFromSelectedColumns() {
		
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, false));
		Assert.assertTrue(selectionLayer.isColumnPositionFullySelected(0));
		Assert.assertTrue(model.isEmpty());
		
		final String columnGroupName = "Test Group";
		handler.loadSelectedColumnsIndexesWithPositions();
		handler.handleGroupColumnsCommand(columnGroupName);
		
		Assert.assertEquals(columnGroupName, getColumnGroupNameForIndex(0));
		Assert.assertEquals(1, getColumnIndexesInGroup(0).size());
	}

	@Test
	public void shouldCreateColumnGroupAfterReordering() {
		// Reorder column to first position
		selectionLayer.doCommand(new ColumnReorderCommand(selectionLayer, 9, 0));
		// Select first column position
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, false));
		
		final String columnGroupName = "Test Group";
		handler.loadSelectedColumnsIndexesWithPositions();
		handler.handleGroupColumnsCommand(columnGroupName);
		
		Assert.assertEquals(columnGroupName, getColumnGroupNameForIndex(9));
		Assert.assertEquals(9, getColumnIndexesInGroup(9).get(0).intValue());
	}
	
	@Test
	public void shouldUngroupMiddleSelectedColumns() {
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 2, 0, false, true));
		
		final String columnGroupName = "Test Group 3";
		handler.loadSelectedColumnsIndexesWithPositions();
		handler.handleGroupColumnsCommand(columnGroupName);
		
		Assert.assertEquals(3, getColumnIndexesInGroup(0).size());
		Assert.assertEquals(0, getColumnIndexesInGroup(0).get(0).intValue());
		Assert.assertEquals(1, getColumnIndexesInGroup(0).get(1).intValue());
		Assert.assertEquals(2, getColumnIndexesInGroup(0).get(2).intValue());
		
		// Test ungrouping column in middle
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, false));
		handler.handleUngroupCommand();
		
		Assert.assertEquals(2, getColumnIndexesInGroup(0).size());
		Assert.assertEquals(0, getColumnIndexesInGroup(0).get(0).intValue());
		Assert.assertEquals(2, getColumnIndexesInGroup(0).get(1).intValue());
		
		Assert.assertEquals(0, selectionLayer.getColumnPositionByIndex(0));
		Assert.assertEquals(2, selectionLayer.getColumnPositionByIndex(2));
		Assert.assertEquals(1, selectionLayer.getColumnPositionByIndex(1));
	}
	
	@Test
	public void shouldNotUngroupColumnsInUnbreakableGroups() throws Exception {
		model.addColumnsIndexesToGroup("Test group 1", 0 , 1, 2);
		model.getColumnGroupByIndex(0).setUnbreakable(true);
		
		// Ungroup column in the middle
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, false));
		handler.handleUngroupCommand();
		
		Assert.assertEquals(3, getColumnIndexesInGroup(0).size());
		Assert.assertTrue(getColumnIndexesInGroup(0).contains(0));
		Assert.assertTrue(getColumnIndexesInGroup(0).contains(1));
		Assert.assertTrue(getColumnIndexesInGroup(0).contains(2));

		// Ungroup first column
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, false));
		handler.handleUngroupCommand();

		Assert.assertEquals(3, getColumnIndexesInGroup(0).size());
		Assert.assertTrue(getColumnIndexesInGroup(0).contains(0));
		Assert.assertTrue(getColumnIndexesInGroup(0).contains(1));
		Assert.assertTrue(getColumnIndexesInGroup(0).contains(2));
		
		// Assert the columns haven't moved
		Assert.assertEquals(0, selectionLayer.getColumnPositionByIndex(0));
		Assert.assertEquals(1, selectionLayer.getColumnPositionByIndex(1));
		Assert.assertEquals(2, selectionLayer.getColumnPositionByIndex(2));
	}
	
	@Test
	public void shouldUngroupFirstSelectedColumn() {
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 2, 0, false, true));
		
		final String columnGroupName = "Test Group 3";
		handler.loadSelectedColumnsIndexesWithPositions();
		handler.handleGroupColumnsCommand(columnGroupName);
		
		// Test ungrouping first column
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, false));
		handler.handleUngroupCommand();
		
		Assert.assertEquals(2, getColumnIndexesInGroup(2).size());
		Assert.assertEquals(1, getColumnIndexesInGroup(2).get(0).intValue());
		Assert.assertEquals(2, getColumnIndexesInGroup(2).get(1).intValue());
		
		Assert.assertEquals(0, selectionLayer.getColumnPositionByIndex(0));
		Assert.assertEquals(2, selectionLayer.getColumnPositionByIndex(2));
		Assert.assertEquals(1, selectionLayer.getColumnPositionByIndex(1));
	}
	
	@Test
	public void shouldUngroupFirstAndLastSelectedColumn() {
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 2, 0, false, true));
		
		final String columnGroupName = "Test Group 3";
		handler.loadSelectedColumnsIndexesWithPositions();
		handler.handleGroupColumnsCommand(columnGroupName);
		
		// Test ungrouping first column
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 2, 0, false, true));
		handler.handleUngroupCommand();
		
		Assert.assertEquals(1, getColumnIndexesInGroup(1).size());
		Assert.assertEquals(1, getColumnIndexesInGroup(1).get(0).intValue());
		
		Assert.assertEquals(0, selectionLayer.getColumnPositionByIndex(0));
		Assert.assertEquals(2, selectionLayer.getColumnPositionByIndex(2));
		Assert.assertEquals(1, selectionLayer.getColumnPositionByIndex(1));
	}
	
	@Test
	public void shouldRemoveAllColumnsInGroup() {
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 2, 0, false, true));
		
		final String columnGroupName = "Test Group 3";
		handler.loadSelectedColumnsIndexesWithPositions();
		handler.handleGroupColumnsCommand(columnGroupName);
		
		// Test ungrouping first column
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, true));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 2, 0, false, true));
		handler.handleUngroupCommand();
		
		Assert.assertFalse(model.isPartOfAGroup(0));
		Assert.assertFalse(model.isPartOfAGroup(1));
		Assert.assertFalse(model.isPartOfAGroup(2));
		
		Assert.assertEquals(0, selectionLayer.getColumnPositionByIndex(0));
		Assert.assertEquals(1, selectionLayer.getColumnPositionByIndex(1));
		Assert.assertEquals(2, selectionLayer.getColumnPositionByIndex(2));
	}
	
	private List<Integer> getColumnIndexesInGroup(int columnIndex) {
		return model.getColumnGroupByIndex(columnIndex).getMembers();
	}

	private String getColumnGroupNameForIndex(int columnIndex) {
		return model.getColumnGroupByIndex(columnIndex).getName();
	}
	
}
