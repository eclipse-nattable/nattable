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
package org.eclipse.nebula.widgets.nattable.reorder;

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
 * Test class for testing handling of IStructuralChangeEvents in ColumnReorderLayer
 * 
 * @author Dirk Fauth
 *
 */
public class ColumnReorderLayerStructuralChangeEventTest {

	List<List<String>> contents;	
	private IUniqueIndexLayer underlyingLayer;
	private ColumnReorderLayer columnReorderLayer;

	@Before
	public void setUp() {
		contents = new ArrayList<List<String>>();
		contents.add(new ArrayList<String>(Arrays.asList("one", "two", "three", "four")));
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
		columnReorderLayer = new ColumnReorderLayer(underlyingLayer);
	}
	
	@Test
	public void testHandleColumnDeleteEvent() {
		//test start order: 0 1 2 3
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("one", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnReorderLayer.getDataValueByPosition(3, 0));

		//reorder to inverse order: 3 2 1 0
		columnReorderLayer.reorderColumnPosition(3, 0);
		columnReorderLayer.reorderColumnPosition(3, 1);
		columnReorderLayer.reorderColumnPosition(3, 2);
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("four", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("one", columnReorderLayer.getDataValueByPosition(3, 0));

		//delete column position 1 (index 2: value "three")
		contents.get(0).remove(2);
		underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(underlyingLayer, 2));
		
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(-1, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("four", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("one", columnReorderLayer.getDataValueByPosition(2, 0));
	}
	
	@Test
	public void testHandleLastColumnDeleteEvent() {
		//test start order: 0 1 2 3
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("one", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnReorderLayer.getDataValueByPosition(3, 0));

		//reorder to inverse order: 3 2 1 0
		columnReorderLayer.reorderColumnPosition(3, 0);
		columnReorderLayer.reorderColumnPosition(3, 1);
		columnReorderLayer.reorderColumnPosition(3, 2);
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("four", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("one", columnReorderLayer.getDataValueByPosition(3, 0));

		//delete last column
		int lastColumnIndex = contents.get(0).size()-1;
		contents.get(0).remove(lastColumnIndex);
		underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(underlyingLayer, lastColumnIndex));
		
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(-1, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("three", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("one", columnReorderLayer.getDataValueByPosition(2, 0));
	}
	
	@Test
	public void testHandleMultipleColumnDeleteEvent() {
		//test start order: 0 1 2 3
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("one", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnReorderLayer.getDataValueByPosition(3, 0));

		//reorder to inverse order: 3 2 1 0
		columnReorderLayer.reorderColumnPosition(3, 0);
		columnReorderLayer.reorderColumnPosition(3, 1);
		columnReorderLayer.reorderColumnPosition(3, 2);
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("four", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("one", columnReorderLayer.getDataValueByPosition(3, 0));

		//delete columns in the middle
		contents.get(0).remove(1);
		contents.get(0).remove(1);
		underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(underlyingLayer, new Range(1, 3)));
		
		assertEquals(2, columnReorderLayer.getColumnCount());
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(-1, columnReorderLayer.getColumnIndexByPosition(2));
		
		assertEquals("four", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("one", columnReorderLayer.getDataValueByPosition(1, 0));
	}
	
	@Test
	public void testHandleColumnAddEvent() {
		//test start order: 0 1 2 3
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("one", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("four", columnReorderLayer.getDataValueByPosition(3, 0));

		//reorder to inverse order: 3 2 1 0
		columnReorderLayer.reorderColumnPosition(3, 0);
		columnReorderLayer.reorderColumnPosition(3, 1);
		columnReorderLayer.reorderColumnPosition(3, 2);
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(3));
		
		assertEquals("four", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("one", columnReorderLayer.getDataValueByPosition(3, 0));

		//add row add index 2
		contents.get(0).add(2, "test");
		underlyingLayer.fireLayerEvent(new ColumnInsertEvent(underlyingLayer, 2));
		
		assertEquals(4, columnReorderLayer.getColumnIndexByPosition(0));
		assertEquals(3, columnReorderLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnReorderLayer.getColumnIndexByPosition(2));
		assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
		assertEquals(0, columnReorderLayer.getColumnIndexByPosition(4));
		
		assertEquals("four", columnReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", columnReorderLayer.getDataValueByPosition(1, 0));
		assertEquals("test", columnReorderLayer.getDataValueByPosition(2, 0));
		assertEquals("two", columnReorderLayer.getDataValueByPosition(3, 0));
		assertEquals("one", columnReorderLayer.getDataValueByPosition(4, 0));
	}
}
