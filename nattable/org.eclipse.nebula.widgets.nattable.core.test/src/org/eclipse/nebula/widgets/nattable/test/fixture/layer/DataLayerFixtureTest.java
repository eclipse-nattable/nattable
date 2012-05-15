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

public class DataLayerFixtureTest {

	private ILayer dataLayerFixture;
	
	@Before
	public void setup() {
		dataLayerFixture = new DataLayerFixture();
	}
	
	@Test
	public void testColumnIndexes() {
		Assert.assertEquals(0, dataLayerFixture.getColumnIndexByPosition(0));
		Assert.assertEquals(1, dataLayerFixture.getColumnIndexByPosition(1));
		Assert.assertEquals(2, dataLayerFixture.getColumnIndexByPosition(2));
		Assert.assertEquals(3, dataLayerFixture.getColumnIndexByPosition(3));
		Assert.assertEquals(4, dataLayerFixture.getColumnIndexByPosition(4));
	}
	
	@Test
	public void testColumnWidths() {
		Assert.assertEquals(150, dataLayerFixture.getColumnWidthByPosition(0));
		Assert.assertEquals(100, dataLayerFixture.getColumnWidthByPosition(1));
		Assert.assertEquals(35, dataLayerFixture.getColumnWidthByPosition(2));
		Assert.assertEquals(100, dataLayerFixture.getColumnWidthByPosition(3));
		Assert.assertEquals(80, dataLayerFixture.getColumnWidthByPosition(4));
	}
	
	@Test
	public void testRowIndexes() {
		Assert.assertEquals(0, dataLayerFixture.getRowIndexByPosition(0));
		Assert.assertEquals(1, dataLayerFixture.getRowIndexByPosition(1));
		Assert.assertEquals(2, dataLayerFixture.getRowIndexByPosition(2));
		Assert.assertEquals(3, dataLayerFixture.getRowIndexByPosition(3));
		Assert.assertEquals(4, dataLayerFixture.getRowIndexByPosition(4));
		Assert.assertEquals(5, dataLayerFixture.getRowIndexByPosition(5));
		Assert.assertEquals(6, dataLayerFixture.getRowIndexByPosition(6));
	}
	
	@Test
	public void testRowHeights() {
		Assert.assertEquals(40, dataLayerFixture.getRowHeightByPosition(0));
		Assert.assertEquals(70, dataLayerFixture.getRowHeightByPosition(1));
		Assert.assertEquals(25, dataLayerFixture.getRowHeightByPosition(2));
		Assert.assertEquals(40, dataLayerFixture.getRowHeightByPosition(3));
		Assert.assertEquals(50, dataLayerFixture.getRowHeightByPosition(4));
		Assert.assertEquals(40, dataLayerFixture.getRowHeightByPosition(5));
		Assert.assertEquals(100, dataLayerFixture.getRowHeightByPosition(6));
	}
	
}
