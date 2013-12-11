/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.integration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Dirk Fauth
 *
 */
public class RowStructuralChangeEventIntegrationTest {

	List<String> contents;	
	private IUniqueIndexLayer underlyingLayer;
	private RowReorderLayer rowReorderLayer;
	private RowHideShowLayer rowHideShowLayer;
	private SelectionLayer selectionLayer;
	private ViewportLayer viewportLayer;
	
	private NatTable natTable;

	@Before
	public void setUp() {
		contents = new ArrayList<String>(Arrays.asList("one", "two", "three", "four", "five"));
		IDataProvider bodyDataProvider = new ListDataProvider<String>(contents, new IColumnAccessor<String>() {

			@Override
			public Object getDataValue(String rowObject, int columnIndex) {
				return rowObject;
			}

			@Override
			public void setDataValue(String rowObject, int columnIndex, Object newValue) {
				// ignore
			}

			@Override
			public int getColumnCount() {
				return 1;
			}
		});
		underlyingLayer = new DataLayer(bodyDataProvider);
		rowReorderLayer = new RowReorderLayer(underlyingLayer);
		rowHideShowLayer = new RowHideShowLayer(rowReorderLayer);
		
		selectionLayer = new SelectionLayer(rowHideShowLayer);
		viewportLayer = new ViewportLayer(selectionLayer);
		
		IDataProvider colDataProvider = new DummyColumnHeaderDataProvider(bodyDataProvider);
		ColumnHeaderLayer colHeader = new ColumnHeaderLayer(
				new DataLayer(colDataProvider), viewportLayer, selectionLayer);
		
		IDataProvider rowDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(
				new DataLayer(rowDataProvider), viewportLayer, selectionLayer);
		
		CornerLayer cornerLayer = new CornerLayer(
				new DataLayer(new DefaultCornerDataProvider(colDataProvider, rowDataProvider)), rowHeaderLayer, colHeader);

		GridLayer grid = new GridLayer(viewportLayer, colHeader, rowHeaderLayer, cornerLayer);
		natTable = new NatTable(new Shell(), grid);
		natTable.setSize(600, 400);
	}

	@Test
	public void testInit() {
		//test start order: 0 1 2 3 4
		assertEquals(0, viewportLayer.getRowIndexByPosition(0));
		assertEquals(1, viewportLayer.getRowIndexByPosition(1));
		assertEquals(2, viewportLayer.getRowIndexByPosition(2));
		assertEquals(3, viewportLayer.getRowIndexByPosition(3));
		assertEquals(4, viewportLayer.getRowIndexByPosition(4));
		
		assertEquals("one", viewportLayer.getDataValueByPosition(0, 0));
		assertEquals("two", viewportLayer.getDataValueByPosition(0, 1));
		assertEquals("three", viewportLayer.getDataValueByPosition(0, 2));
		assertEquals("four", viewportLayer.getDataValueByPosition(0, 3));
		assertEquals("five", viewportLayer.getDataValueByPosition(0, 4));
	}
	
	@Test
	public void testReorder() {
		testInit();
		
		//reorder to inverse order: 4 3 2 1 0
		natTable.doCommand(new RowReorderCommand(viewportLayer, 4, 0));
		natTable.doCommand(new RowReorderCommand(viewportLayer, 4, 1));
		natTable.doCommand(new RowReorderCommand(viewportLayer, 4, 2));
		natTable.doCommand(new RowReorderCommand(viewportLayer, 4, 3));

		assertEquals(4, viewportLayer.getRowIndexByPosition(0));
		assertEquals(3, viewportLayer.getRowIndexByPosition(1));
		assertEquals(2, viewportLayer.getRowIndexByPosition(2));
		assertEquals(1, viewportLayer.getRowIndexByPosition(3));
		assertEquals(0, viewportLayer.getRowIndexByPosition(4));
		
		assertEquals("five", viewportLayer.getDataValueByPosition(0, 0));
		assertEquals("four", viewportLayer.getDataValueByPosition(0, 1));
		assertEquals("three", viewportLayer.getDataValueByPosition(0, 2));
		assertEquals("two", viewportLayer.getDataValueByPosition(0, 3));
		assertEquals("one", viewportLayer.getDataValueByPosition(0, 4));
	}
	
	@Test
	public void testHideShow() {
		testInit();
		
		//hide row at position 2: 0 1 3 4 
		natTable.doCommand(new RowHideCommand(viewportLayer, 2));
		
		assertEquals(4, viewportLayer.getRowCount());
		
		assertEquals(0, viewportLayer.getRowIndexByPosition(0));
		assertEquals(1, viewportLayer.getRowIndexByPosition(1));
		assertEquals(3, viewportLayer.getRowIndexByPosition(2));
		assertEquals(4, viewportLayer.getRowIndexByPosition(3));
		assertEquals(-1, viewportLayer.getRowIndexByPosition(4));
		
		assertEquals("one", viewportLayer.getDataValueByPosition(0, 0));
		assertEquals("two", viewportLayer.getDataValueByPosition(0, 1));
		assertEquals("four", viewportLayer.getDataValueByPosition(0, 2));
		assertEquals("five", viewportLayer.getDataValueByPosition(0, 3));
	}
	
	@Test
	public void testReorderHide() {
		testInit();
		
		//reorder to inverse order: 4 3 2 1 0
		natTable.doCommand(new RowReorderCommand(viewportLayer, 4, 0));
		natTable.doCommand(new RowReorderCommand(viewportLayer, 4, 1));
		natTable.doCommand(new RowReorderCommand(viewportLayer, 4, 2));
		natTable.doCommand(new RowReorderCommand(viewportLayer, 4, 3));
		
		//hide row at position 2: 0 1 3 4 
		natTable.doCommand(new RowHideCommand(viewportLayer, 2));
		
		assertEquals(4, viewportLayer.getRowCount());
		
		assertEquals(4, viewportLayer.getRowIndexByPosition(0));
		assertEquals(3, viewportLayer.getRowIndexByPosition(1));
		assertEquals(1, viewportLayer.getRowIndexByPosition(2));
		assertEquals(0, viewportLayer.getRowIndexByPosition(3));
		assertEquals(-1, viewportLayer.getRowIndexByPosition(4));
		
		assertEquals("five", viewportLayer.getDataValueByPosition(0, 0));
		assertEquals("four", viewportLayer.getDataValueByPosition(0, 1));
		assertEquals("two", viewportLayer.getDataValueByPosition(0, 2));
		assertEquals("one", viewportLayer.getDataValueByPosition(0, 3));
	}
	
	@Test
	public void testHideReorder() {
		testInit();
		
		//hide row at position 2: 0 1 3 4 
		natTable.doCommand(new RowHideCommand(viewportLayer, 2));
		
		//reorder to inverse order: 4 3 1 0
		natTable.doCommand(new RowReorderCommand(viewportLayer, 3, 0));
		natTable.doCommand(new RowReorderCommand(viewportLayer, 3, 1));
		natTable.doCommand(new RowReorderCommand(viewportLayer, 3, 2));
		
		assertEquals(4, viewportLayer.getRowCount());
		
		assertEquals(4, viewportLayer.getRowIndexByPosition(0));
		assertEquals(3, viewportLayer.getRowIndexByPosition(1));
		assertEquals(1, viewportLayer.getRowIndexByPosition(2));
		assertEquals(0, viewportLayer.getRowIndexByPosition(3));
		assertEquals(-1, viewportLayer.getRowIndexByPosition(4));
		
		assertEquals("five", viewportLayer.getDataValueByPosition(0, 0));
		assertEquals("four", viewportLayer.getDataValueByPosition(0, 1));
		assertEquals("two", viewportLayer.getDataValueByPosition(0, 2));
		assertEquals("one", viewportLayer.getDataValueByPosition(0, 3));
	}
	
	@Test
	public void testDeleteLastRow() {
		testInit();
		
		//delete last row
		int index = contents.size()-1;
		contents.remove(index);
		underlyingLayer.fireLayerEvent(new RowDeleteEvent(underlyingLayer, index));

		assertEquals(4, viewportLayer.getRowCount());
	}
}
