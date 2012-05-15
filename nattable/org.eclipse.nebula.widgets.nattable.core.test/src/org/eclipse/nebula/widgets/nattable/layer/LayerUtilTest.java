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
package org.eclipse.nebula.widgets.nattable.layer;


import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LayerUtilTest {

	private ILayer layer;

	@Before
	public void setup() {
		layer = new DataLayerFixture();
	}
	
	// Column
	
	@Test
	public void testFindColumnPosition0() {
		Assert.assertEquals(0, LayerUtil.getColumnPositionByX(layer, 0));
	}
	
	@Test
	public void testFindColumnPositionAtEnd() {
		Assert.assertEquals(4, LayerUtil.getColumnPositionByX(layer, 464));
	}
	
	@Test
	public void testFindColumnPositionOffEnd() {
		Assert.assertEquals(-1, LayerUtil.getColumnPositionByX(layer, 465));
	}
	
	@Test
	public void testFindWeirdColumnPosition() {
		Assert.assertEquals(0, LayerUtil.getColumnPositionByX(layer, 145));
	}
	
	@Test
	public void testFindWeirdColumnPosition2() {
		Assert.assertEquals(2, LayerUtil.getColumnPositionByX(layer, 284));
		Assert.assertEquals(3, LayerUtil.getColumnPositionByX(layer, 285));
	}
	
	// Row
	
	@Test
	public void testFindRowPosition0() {
		Assert.assertEquals(0, LayerUtil.getRowPositionByY(layer, 0));
	}
	
	@Test
	public void testFindRowPositionAtEnd() {
		Assert.assertEquals(6, LayerUtil.getRowPositionByY(layer, 364));
	}
	
	@Test
	public void testFindRowPositionOffEnd() {
		Assert.assertEquals(-1, LayerUtil.getRowPositionByY(layer, 365));
	}
	
	@Test
	public void testFindWeirdRowPosition() {
		Assert.assertEquals(0, LayerUtil.getRowPositionByY(layer, 17));
	}
	
	@Test
	public void testFindWeirdRowPosition2() {
		Assert.assertEquals(1, LayerUtil.getRowPositionByY(layer, 42));
		Assert.assertEquals(5, LayerUtil.getRowPositionByY(layer, 241));
	}
	
}
