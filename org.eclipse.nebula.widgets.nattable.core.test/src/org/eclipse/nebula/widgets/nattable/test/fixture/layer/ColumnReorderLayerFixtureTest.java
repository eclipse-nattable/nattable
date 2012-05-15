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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;


import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnReorderLayerFixtureTest {

	private ILayer columnReorderLayerFixture;

	@Before
	public void setup() {
		columnReorderLayerFixture = new ColumnReorderLayerFixture();
	}

	@Test
	public void testColumnIndexes() {
		Assert.assertEquals(4, columnReorderLayerFixture.getColumnIndexByPosition(0));
		Assert.assertEquals(1, columnReorderLayerFixture.getColumnIndexByPosition(1));
		Assert.assertEquals(0, columnReorderLayerFixture.getColumnIndexByPosition(2));
		Assert.assertEquals(2, columnReorderLayerFixture.getColumnIndexByPosition(3));
		Assert.assertEquals(3, columnReorderLayerFixture.getColumnIndexByPosition(4));
	}

	@Test
	public void testColumnWidths() {
		Assert.assertEquals(80, columnReorderLayerFixture.getColumnWidthByPosition(0));
		Assert.assertEquals(100, columnReorderLayerFixture.getColumnWidthByPosition(1));
		Assert.assertEquals(150, columnReorderLayerFixture.getColumnWidthByPosition(2));
		Assert.assertEquals(35, columnReorderLayerFixture.getColumnWidthByPosition(3));
		Assert.assertEquals(100, columnReorderLayerFixture.getColumnWidthByPosition(4));
	}

	@Test
	public void testRowIndexes() {
		Assert.assertEquals(0, columnReorderLayerFixture.getRowIndexByPosition(0));
		Assert.assertEquals(1, columnReorderLayerFixture.getRowIndexByPosition(1));
		Assert.assertEquals(2, columnReorderLayerFixture.getRowIndexByPosition(2));
		Assert.assertEquals(3, columnReorderLayerFixture.getRowIndexByPosition(3));
		Assert.assertEquals(4, columnReorderLayerFixture.getRowIndexByPosition(4));
		Assert.assertEquals(5, columnReorderLayerFixture.getRowIndexByPosition(5));
		Assert.assertEquals(6, columnReorderLayerFixture.getRowIndexByPosition(6));
	}

	@Test
	public void testRowHeights() {
		Assert.assertEquals(40, columnReorderLayerFixture.getRowHeightByPosition(0));
		Assert.assertEquals(70, columnReorderLayerFixture.getRowHeightByPosition(1));
		Assert.assertEquals(25, columnReorderLayerFixture.getRowHeightByPosition(2));
		Assert.assertEquals(40, columnReorderLayerFixture.getRowHeightByPosition(3));
		Assert.assertEquals(50, columnReorderLayerFixture.getRowHeightByPosition(4));
		Assert.assertEquals(40, columnReorderLayerFixture.getRowHeightByPosition(5));
		Assert.assertEquals(100, columnReorderLayerFixture.getRowHeightByPosition(6));
	}

}
