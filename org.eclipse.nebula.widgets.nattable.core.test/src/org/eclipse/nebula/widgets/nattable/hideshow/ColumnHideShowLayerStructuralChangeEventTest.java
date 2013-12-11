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
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnInsertEvent;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for testing handling of IStructuralChangeEvents in RowHideShowLayer
 * 
 * @author Dirk Fauth
 *
 */
public class ColumnHideShowLayerStructuralChangeEventTest {

	List<List<String>> contents;	
	private IUniqueIndexLayer underlyingLayer;
	private ColumnHideShowLayer columnHideShowLayer;
	
	@Before
	public void setUp() {
		contents = new ArrayList<List<String>>();
		contents.add(new ArrayList<String>(Arrays.asList("one", "two", "three", "four", "five")));
		underlyingLayer = new DataLayer(new ListDataProvider<List<String>>(
				contents, new IColumnAccessor<List<String>>() {

			@Override
			public Object getDataValue(List<String> rowObject, int columnIndex) {
				return rowObject.get(columnIndex);
			}

			@Override
			public void setDataValue(List<String> rowObject, int columnIndex, Object newValue) {
				// ignore
			}

			@Override
			public int getColumnCount() {
				return contents.get(0).size();
			}
		}));
		columnHideShowLayer = new ColumnHideShowLayer(underlyingLayer);
	}
	
	@Test
	public void testHandleColumnDeleteEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(3, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(4, 0));
		
		//hide column at position 2: 0 1 3 4 
		List<Integer> columnsToHide = new ArrayList<Integer>();
		columnsToHide.add(2);
		columnHideShowLayer.hideColumnPositions(columnsToHide);
		
		assertEquals(4, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(3, 0));

		//delete column index 1: value "two")
		contents.get(0).remove(1);
		underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(underlyingLayer, 1));
		
		assertEquals(3, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(3));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(2, 0));
	}
	
	@Test
	public void testHandleHiddenColumnDeleteEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(3, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(4, 0));
		
		//hide column at position 2: 0 1 3 4 
		List<Integer> columnsToHide = new ArrayList<Integer>();
		columnsToHide.add(2);
		columnHideShowLayer.hideColumnPositions(columnsToHide);
		
		assertEquals(4, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(3, 0));

		//delete column index 2: value "three"
		contents.get(0).remove(2);
		underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(underlyingLayer, 2));
		
		assertEquals(4, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(3, 0));
	}
	
	@Test
	public void testHandleLastColumnDeleteEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(3, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(4, 0));
		
		//hide column at position 2: 0 1 3 4 
		List<Integer> columnsToHide = new ArrayList<Integer>();
		columnsToHide.add(2);
		columnHideShowLayer.hideColumnPositions(columnsToHide);
		
		assertEquals(4, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(3, 0));

		//delete last column in list
		int lastColumnIndex = contents.get(0).size()-1;
		contents.get(0).remove(lastColumnIndex);
		underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(underlyingLayer, lastColumnIndex));
		
		assertEquals(3, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(3));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(2, 0));
	}
	
	@Test
	public void testHandleMultipleColumnDeleteEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(3, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(4, 0));
		
		//hide column at position 2: 0 1 3 4 
		List<Integer> columnsToHide = new ArrayList<Integer>();
		columnsToHide.add(2);
		columnHideShowLayer.hideColumnPositions(columnsToHide);
		
		assertEquals(4, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(3, 0));
		
		//delete columns in the middle
		contents.get(0).remove(1);
		contents.get(0).remove(1);
		contents.get(0).remove(1);
		underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(underlyingLayer, new Range(1, 4)));
		
		assertEquals(2, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(2));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(1, 0));
	}	

	@Test
	public void testHandleColumnAddEvent() {
		//test start order: 0 1 2 3 4
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(3, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(4, 0));
		
		//hide column at position 2: 0 1 3 4 
		List<Integer> columnsToHide = new ArrayList<Integer>();
		columnsToHide.add(2);
		columnHideShowLayer.hideColumnPositions(columnsToHide);
		
		assertEquals(4, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(4));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(3, 0));

		//add column add index 2
		contents.get(0).add(2, "test");
		underlyingLayer.fireLayerEvent(new ColumnInsertEvent(underlyingLayer, 2));
	
		assertEquals(5, columnHideShowLayer.getColumnCount());
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(5, columnHideShowLayer.getColumnIndexByPosition(4));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(5));
		
		assertEquals("one", columnHideShowLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnHideShowLayer.getDataValueByPosition(1, 0));
		assertEquals("test", columnHideShowLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnHideShowLayer.getDataValueByPosition(3, 0));
		assertEquals("five", columnHideShowLayer.getDataValueByPosition(4, 0));
	}
}
