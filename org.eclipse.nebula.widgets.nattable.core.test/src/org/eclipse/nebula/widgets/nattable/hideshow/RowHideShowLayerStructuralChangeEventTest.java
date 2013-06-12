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
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for testing handling of IStructuralChangeEvents in RowHideShowLayer
 * 
 * @author Dirk Fauth
 *
 */
public class RowHideShowLayerStructuralChangeEventTest {

	List<String> contents;	
	private IUniqueIndexLayer underlyingLayer;
	private RowHideShowLayer rowHideShowLayer;
	
	@Before
	public void setUp() {
		contents = new ArrayList<String>(Arrays.asList("one", "two", "three", "four", "five"));
		underlyingLayer = new DataLayer(new ListDataProvider<String>(contents, new IColumnAccessor<String>() {

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
		}));
		rowHideShowLayer = new RowHideShowLayer(underlyingLayer);
	}
	
	@Test
	public void testHandleRowDeleteEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 3));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 4));
		
		//hide row at position 2: 0 1 3 4 
		List<Integer> rowsToHide = new ArrayList<Integer>();
		rowsToHide.add(2);
		rowHideShowLayer.hideRowPositions(rowsToHide);
		
		assertEquals(4, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 3));

		//delete row index 1: value "two")
		contents.remove(1);
		underlyingLayer.fireLayerEvent(new RowDeleteEvent(underlyingLayer, 1));
		
		assertEquals(3, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(3));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 2));
	}
	
	@Test
	public void testHandleHiddenRowDeleteEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 3));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 4));
		
		//hide row at position 2: 0 1 3 4 
		List<Integer> rowsToHide = new ArrayList<Integer>();
		rowsToHide.add(2);
		rowHideShowLayer.hideRowPositions(rowsToHide);
		
		assertEquals(4, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 3));

		//delete row index 2: value "three"
		contents.remove(2);
		underlyingLayer.fireLayerEvent(new RowDeleteEvent(underlyingLayer, 2));
		
		assertEquals(4, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 3));
	}
	
	@Test
	public void testHandleLastRowDeleteEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 3));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 4));
		
		//hide row at position 2: 0 1 3 4 
		List<Integer> rowsToHide = new ArrayList<Integer>();
		rowsToHide.add(2);
		rowHideShowLayer.hideRowPositions(rowsToHide);
		
		assertEquals(4, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 3));

		//delete last row in list
		int lastRowIndex = contents.size()-1;
		contents.remove(lastRowIndex);
		underlyingLayer.fireLayerEvent(new RowDeleteEvent(underlyingLayer, lastRowIndex));
		
		assertEquals(3, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(3));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 2));
	}
	
	@Test
	public void testHandleMultipleRowDeleteEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 3));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 4));
		
		//hide row at position 2: 0 1 3 4 
		List<Integer> rowsToHide = new ArrayList<Integer>();
		rowsToHide.add(2);
		rowHideShowLayer.hideRowPositions(rowsToHide);
		
		assertEquals(4, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 3));
		
		//delete rows in the middle
		contents.remove(1);
		contents.remove(1);
		contents.remove(1);
		underlyingLayer.fireLayerEvent(new RowDeleteEvent(underlyingLayer, new Range(1, 4)));
		
		assertEquals(2, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(2));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 1));
	}	

	@Test
	public void testHandleRowAddEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 3));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 4));
		
		//hide row at position 2: 0 1 3 4 
		List<Integer> rowsToHide = new ArrayList<Integer>();
		rowsToHide.add(2);
		rowHideShowLayer.hideRowPositions(rowsToHide);
		
		assertEquals(4, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(3, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 3));

		//add row add index 2
		contents.add(2, "test");
		underlyingLayer.fireLayerEvent(new RowInsertEvent(underlyingLayer, 2));
	
		assertEquals(5, rowHideShowLayer.getRowCount());
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(4, rowHideShowLayer.getRowIndexByPosition(3));
		assertEquals(5, rowHideShowLayer.getRowIndexByPosition(4));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(5));
		
		assertEquals("one", rowHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowHideShowLayer.getDataValueByPosition(0, 1));
		assertEquals("test", rowHideShowLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowHideShowLayer.getDataValueByPosition(0, 3));
		assertEquals("five", rowHideShowLayer.getDataValueByPosition(0, 4));
	}
}
