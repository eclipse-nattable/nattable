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
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for testing handling of IStructuralChangeEvents in RowReorderLayer
 * 
 * @author Dirk Fauth
 *
 */
public class RowReorderLayerStructuralChangeEventTest {

	List<String> contents;	
	private IUniqueIndexLayer underlyingLayer;
	private RowReorderLayer rowReorderLayer;

	@Before
	public void setUp() {
		contents = new ArrayList<String>(Arrays.asList("one", "two", "three", "four"));
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
		rowReorderLayer = new RowReorderLayer(underlyingLayer);
	}
	
	@Test
	public void testHandleRowDeleteEvent() {
		//test start order: 0 1 2 3
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 3));

		//reorder to inverse order: 3 2 1 0
		rowReorderLayer.reorderRowPosition(3, 0);
		rowReorderLayer.reorderRowPosition(3, 1);
		rowReorderLayer.reorderRowPosition(3, 2);
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 3));

		//delete row position 1 (index 2: value "three")
		contents.remove(2);
		underlyingLayer.fireLayerEvent(new RowDeleteEvent(underlyingLayer, 2));
		
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(-1, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 2));
	}
	
	@Test
	public void testHandleLastRowDeleteEvent() {
		//test start order: 0 1 2 3
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 3));

		//reorder to inverse order: 3 2 1 0
		rowReorderLayer.reorderRowPosition(3, 0);
		rowReorderLayer.reorderRowPosition(3, 1);
		rowReorderLayer.reorderRowPosition(3, 2);
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 3));

		//delete last row in list
		int lastRowIndex = contents.size()-1;
		contents.remove(lastRowIndex);
		underlyingLayer.fireLayerEvent(new RowDeleteEvent(underlyingLayer, lastRowIndex));
		
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(-1, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 2));
	}
	
	@Test
	public void testHandleMultipleRowDeleteEvent() {
		//test start order: 0 1 2 3
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 3));

		//reorder to inverse order: 3 2 1 0
		rowReorderLayer.reorderRowPosition(3, 0);
		rowReorderLayer.reorderRowPosition(3, 1);
		rowReorderLayer.reorderRowPosition(3, 2);
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 3));

		//delete rows in the middle
		contents.remove(1);
		contents.remove(1);
		underlyingLayer.fireLayerEvent(new RowDeleteEvent(underlyingLayer, new Range(1, 3)));
		
		assertEquals(2, rowReorderLayer.getRowCount());
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(-1, rowReorderLayer.getRowIndexByPosition(2));
		
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 1));
	}
	
	@Test
	public void testHandleRowAddEvent() {
		//test start order: 0 1 2 3
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 3));

		//reorder to inverse order: 3 2 1 0
		rowReorderLayer.reorderRowPosition(3, 0);
		rowReorderLayer.reorderRowPosition(3, 1);
		rowReorderLayer.reorderRowPosition(3, 2);
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(3));
		
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 3));

		//add row add index 2
		contents.add(2, "test");
		underlyingLayer.fireLayerEvent(new RowInsertEvent(underlyingLayer, 2));
		
		assertEquals(4, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(4));
		
		assertEquals("four", rowReorderLayer.getDataValueByPosition(0, 0));
		assertEquals("three", rowReorderLayer.getDataValueByPosition(0, 1));
		assertEquals("test", rowReorderLayer.getDataValueByPosition(0, 2));
		assertEquals("two", rowReorderLayer.getDataValueByPosition(0, 3));
		assertEquals("one", rowReorderLayer.getDataValueByPosition(0, 4));
	}
}
