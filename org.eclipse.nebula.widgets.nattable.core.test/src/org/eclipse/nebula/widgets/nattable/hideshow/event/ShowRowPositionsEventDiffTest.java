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
package org.eclipse.nebula.widgets.nattable.hideshow.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ShowRowPositionsEventDiffTest {

	private ShowRowPositionsEvent event;
	private DataLayerFixture dataLayer;
	private RowHideShowLayer hideShowLayer;
	private ViewportLayer viewportLayer;
	
	@Before
	public void before() {
		dataLayer = new DataLayerFixture(20, 20, 100, 40);
		hideShowLayer = new RowHideShowLayer(dataLayer);
		viewportLayer = new ViewportLayer(hideShowLayer);
		viewportLayer.setClientAreaProvider(new IClientAreaProvider() {
			
			@Override
			public Rectangle getClientArea() {
				return new Rectangle(0, 0, 800, 400);
			}
			
		});
		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(2));
		viewportLayer.setOriginY(viewportLayer.getStartYOfRowPosition(2));
		
		event = new ShowRowPositionsEvent(dataLayer, Arrays.asList(new Integer[] { 2, 4, 7, 8, 9 }));
	}
	
	@After
	public void after() {
		Assert.assertFalse(event.isHorizontalStructureChanged());
		
		Assert.assertTrue(event.isVerticalStructureChanged());
		Assert.assertNull(event.getColumnDiffs());
	}
	
	/**
	 *             + +   +
	 * before: 0 1 3 5 6 8 9 10 11 12
	 * after:  0 1 2 3 4 5 6 7  8  9
	 *             +   +     +  +  +
	 */
	@Test
	public void testColumnDiffs() {
		Collection<StructuralDiff> rowDiffs = event.getRowDiffs();
		Assert.assertNotNull(rowDiffs);
		Assert.assertEquals(3, rowDiffs.size());
		Iterator<StructuralDiff> iterator = rowDiffs.iterator();
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(2, 2), new Range(2, 3)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(3, 3), new Range(4, 5)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(5, 5), new Range(7, 10)), iterator.next());
	}
	
	/**
	 *             + +   +
	 * before: 0 1 3 5 6 8 9 10 11 12
	 * after:  0 1 2 3 4 5 6 7  8  9
	 *             +   +     +  +  +
	 */
	@Test
	public void testConvertToLocal() {
		event.convertToLocal(hideShowLayer);
		
		Collection<StructuralDiff> rowDiffs = event.getRowDiffs();
		Assert.assertNotNull(rowDiffs);
		Assert.assertEquals(3, rowDiffs.size());
		Iterator<StructuralDiff> iterator = rowDiffs.iterator();
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(2, 2), new Range(2, 3)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(3, 3), new Range(4, 5)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(5, 5), new Range(7, 10)), iterator.next());
		
		event.convertToLocal(viewportLayer);
		
		rowDiffs = event.getRowDiffs();
		Assert.assertNotNull(rowDiffs);
		Assert.assertEquals(3, rowDiffs.size());
		iterator = rowDiffs.iterator();
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(0, 0), new Range(0, 1)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(1, 1), new Range(2, 3)), iterator.next());
		Assert.assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(3, 3), new Range(5, 8)), iterator.next());
	}
	
}
