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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;


import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowHideShowLayerFixtureTest {

	private ILayer rowHideShowLayerFixture;
	
	@Before
	public void setup() {
		rowHideShowLayerFixture = new RowHideShowLayerFixture();
	}
	
	@Test
	public void testColumnIndexes() {
		Assert.assertEquals(0, rowHideShowLayerFixture.getColumnIndexByPosition(0));
		Assert.assertEquals(1, rowHideShowLayerFixture.getColumnIndexByPosition(1));
		Assert.assertEquals(2, rowHideShowLayerFixture.getColumnIndexByPosition(2));
		Assert.assertEquals(3, rowHideShowLayerFixture.getColumnIndexByPosition(3));
		Assert.assertEquals(4, rowHideShowLayerFixture.getColumnIndexByPosition(4));
	}
	
	@Test
	public void testColumnWidths() {
		Assert.assertEquals(150, rowHideShowLayerFixture.getColumnWidthByPosition(0));
		Assert.assertEquals(100, rowHideShowLayerFixture.getColumnWidthByPosition(1));
		Assert.assertEquals(35, rowHideShowLayerFixture.getColumnWidthByPosition(2));
		Assert.assertEquals(100, rowHideShowLayerFixture.getColumnWidthByPosition(3));
		Assert.assertEquals(80, rowHideShowLayerFixture.getColumnWidthByPosition(4));
	}
	
	@Test
	public void testRowIndexes() {
		Assert.assertEquals(4, rowHideShowLayerFixture.getRowIndexByPosition(0));
		Assert.assertEquals(1, rowHideShowLayerFixture.getRowIndexByPosition(1));
		Assert.assertEquals(2, rowHideShowLayerFixture.getRowIndexByPosition(2));
		Assert.assertEquals(5, rowHideShowLayerFixture.getRowIndexByPosition(3));
		Assert.assertEquals(6, rowHideShowLayerFixture.getRowIndexByPosition(4));
		Assert.assertEquals(-1, rowHideShowLayerFixture.getRowIndexByPosition(5));
	}
	
	@Test
	public void testRowHeights() {
		Assert.assertEquals(50, rowHideShowLayerFixture.getRowHeightByPosition(0));
		Assert.assertEquals(70, rowHideShowLayerFixture.getRowHeightByPosition(1));
		Assert.assertEquals(25, rowHideShowLayerFixture.getRowHeightByPosition(2));
		Assert.assertEquals(40, rowHideShowLayerFixture.getRowHeightByPosition(3));
		Assert.assertEquals(100, rowHideShowLayerFixture.getRowHeightByPosition(4));
	}
	
}
