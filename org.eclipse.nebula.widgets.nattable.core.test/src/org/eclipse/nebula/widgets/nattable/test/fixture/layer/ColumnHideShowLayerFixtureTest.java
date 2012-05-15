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

public class ColumnHideShowLayerFixtureTest {

	private ILayer columnHideShowLayerFixture;
	
	@Before
	public void setup() {
		columnHideShowLayerFixture = new ColumnHideShowLayerFixture();
	}
	
	@Test
	public void testColumnIndexes() {
		Assert.assertEquals(4, columnHideShowLayerFixture.getColumnIndexByPosition(0));
		Assert.assertEquals(1, columnHideShowLayerFixture.getColumnIndexByPosition(1));
		Assert.assertEquals(2, columnHideShowLayerFixture.getColumnIndexByPosition(2));
		Assert.assertEquals(-1, columnHideShowLayerFixture.getColumnIndexByPosition(3));
	}
	
	@Test
	public void testColumnWidths() {
		Assert.assertEquals(80, columnHideShowLayerFixture.getColumnWidthByPosition(0));
		Assert.assertEquals(100, columnHideShowLayerFixture.getColumnWidthByPosition(1));
		Assert.assertEquals(35, columnHideShowLayerFixture.getColumnWidthByPosition(2));
	}
	
	@Test
	public void testRowIndexes() {
		Assert.assertEquals(0, columnHideShowLayerFixture.getRowIndexByPosition(0));
		Assert.assertEquals(1, columnHideShowLayerFixture.getRowIndexByPosition(1));
		Assert.assertEquals(2, columnHideShowLayerFixture.getRowIndexByPosition(2));
		Assert.assertEquals(3, columnHideShowLayerFixture.getRowIndexByPosition(3));
		Assert.assertEquals(4, columnHideShowLayerFixture.getRowIndexByPosition(4));
		Assert.assertEquals(5, columnHideShowLayerFixture.getRowIndexByPosition(5));
		Assert.assertEquals(6, columnHideShowLayerFixture.getRowIndexByPosition(6));
	}
	
	@Test
	public void testRowHeights() {
		Assert.assertEquals(40, columnHideShowLayerFixture.getRowHeightByPosition(0));
		Assert.assertEquals(70, columnHideShowLayerFixture.getRowHeightByPosition(1));
		Assert.assertEquals(25, columnHideShowLayerFixture.getRowHeightByPosition(2));
		Assert.assertEquals(40, columnHideShowLayerFixture.getRowHeightByPosition(3));
		Assert.assertEquals(50, columnHideShowLayerFixture.getRowHeightByPosition(4));
		Assert.assertEquals(40, columnHideShowLayerFixture.getRowHeightByPosition(5));
		Assert.assertEquals(100, columnHideShowLayerFixture.getRowHeightByPosition(6));
	}
	
}
