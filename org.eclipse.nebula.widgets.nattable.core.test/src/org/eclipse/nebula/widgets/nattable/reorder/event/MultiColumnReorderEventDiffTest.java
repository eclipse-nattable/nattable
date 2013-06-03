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
package org.eclipse.nebula.widgets.nattable.reorder.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultiColumnReorderEventDiffTest {

	private ColumnReorderEvent event;
	private DataLayerFixture dataLayer;
	private ViewportLayer viewportLayer;
	
	@Before
	public void before() {
		dataLayer = new DataLayerFixture(20, 20, 100, 40);
		viewportLayer = new ViewportLayer(dataLayer);
		viewportLayer.setClientAreaProvider(new IClientAreaProvider() {
			
			public Rectangle getClientArea() {
				return new Rectangle(0, 0, 800, 400);
			}
			
		});
		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(2));
		viewportLayer.setOriginY(viewportLayer.getStartYOfRowPosition(2));
	}
	
	@After
	public void after() {
		Assert.assertTrue(event.isHorizontalStructureChanged());
		
		Assert.assertFalse(event.isVerticalStructureChanged());
		Assert.assertNull(event.getRowDiffs());
	}
	
	/**
	 *             - - -     +
	 * before: 0 1 2 3 4 5 6 7 8
	 * after:  0 1 5 6 2 3 4 7 8
	 *             -   + + +
	 */
	@Test
	public void testReorderRightColumnDiffs() {
		event = new ColumnReorderEvent(dataLayer, Arrays.asList(new Integer[] { 2, 3, 4 }), 7, true);
		
		Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
		Assert.assertNotNull(columnDiffs);
		Assert.assertEquals(2, columnDiffs.size());
		Iterator<StructuralDiff> iterator = columnDiffs.iterator();
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(2, 5), new Range(2, 2)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(7, 7), new Range(4, 7)), iterator.next());
	}
	
	/**
	 *             - - -     +
	 * before: 0 1 2 3 4 5 6 7 8
	 * after:  0 1 5 6 2 3 4 7 8
	 *             -   + + +
	 */
	@Test
	public void testReorderRightConvertToLocal() {
		event = new ColumnReorderEvent(dataLayer, Arrays.asList(new Integer[] { 2, 3, 4 }), 7, true);
		event.convertToLocal(viewportLayer);
		
		Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
		Assert.assertNotNull(columnDiffs);
		Assert.assertEquals(2, columnDiffs.size());
		Iterator<StructuralDiff> iterator = columnDiffs.iterator();
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(0, 3), new Range(0, 0)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(5, 5), new Range(2, 5)), iterator.next());
	}
	
	/**
	 *             +         - - -
	 * before: 0 1 2 3 4 5 6 7 8 9 10
	 * after:  0 1 7 8 9 2 3 4 5 6 10
	 *             + + +           -
	 */
	@Test
	public void testReorderLeftColumnDiffs() {
		event = new ColumnReorderEvent(dataLayer, Arrays.asList(new Integer[] { 7, 8, 9 }), 2, true);
		
		Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
		Assert.assertNotNull(columnDiffs);
		Assert.assertEquals(2, columnDiffs.size());
		Iterator<StructuralDiff> iterator = columnDiffs.iterator();
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(7, 10), new Range(10, 10)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(2, 2), new Range(2, 5)), iterator.next());
	}
	
	/**
	 *             +         - - -
	 * before: 0 1 2 3 4 5 6 7 8 9 10
	 * after:  0 1 7 8 9 2 3 4 5 6 10
	 *             + + +           -
	 */
	@Test
	public void testReorderLeftConvertToLocal() {
		event = new ColumnReorderEvent(dataLayer, Arrays.asList(new Integer[] { 7, 8, 9 }), 2, true);
		event.convertToLocal(viewportLayer);
		
		Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
		Assert.assertNotNull(columnDiffs);
		Assert.assertEquals(2, columnDiffs.size());
		Iterator<StructuralDiff> iterator = columnDiffs.iterator();
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(5, 8), new Range(8, 8)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(0, 0), new Range(0, 3)), iterator.next());
	}
	
}
