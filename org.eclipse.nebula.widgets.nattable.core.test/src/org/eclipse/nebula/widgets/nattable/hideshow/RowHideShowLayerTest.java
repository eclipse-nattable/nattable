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

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.RowHideShowLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("boxing")
public class RowHideShowLayerTest {

	private RowHideShowLayer rowHideShowLayer;

	@Before
	public void setup(){
		rowHideShowLayer = new RowHideShowLayerFixture();
	}

	@Test
	public void getRowIndexByPosition() throws Exception {
		Assert.assertEquals(4, rowHideShowLayer.getRowIndexByPosition(0));
		Assert.assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		Assert.assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		Assert.assertEquals(5, rowHideShowLayer.getRowIndexByPosition(3));
		Assert.assertEquals(6, rowHideShowLayer.getRowIndexByPosition(4));
		Assert.assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(5));
	}

	@Test
	public void getRowIndexHideAdditionalColumn() throws Exception {
		getRowIndexByPosition();

		rowHideShowLayer.hideRowPositions(Arrays.asList(1));

		Assert.assertEquals(4, rowHideShowLayer.getRowIndexByPosition(0));
		Assert.assertEquals(2, rowHideShowLayer.getRowIndexByPosition(1));
		Assert.assertEquals(5, rowHideShowLayer.getRowIndexByPosition(2));
		Assert.assertEquals(6, rowHideShowLayer.getRowIndexByPosition(3));
		Assert.assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));
	}

	@Test
	public void getRowPositionForASingleHiddenRow() throws Exception {
		assertEquals(-1, rowHideShowLayer.getRowPositionByIndex(0));
		assertEquals(1, rowHideShowLayer.getRowPositionByIndex(1));
		assertEquals(2, rowHideShowLayer.getRowPositionByIndex(2));
		assertEquals(-1, rowHideShowLayer.getRowPositionByIndex(3));
		assertEquals(0, rowHideShowLayer.getRowPositionByIndex(4));
		assertEquals(3, rowHideShowLayer.getRowPositionByIndex(5));
		assertEquals(4, rowHideShowLayer.getRowPositionByIndex(6));
	}

	@Test
	public void hideAllRows() throws Exception {
		rowHideShowLayer.hideRowPositions(Arrays.asList(0, 1, 2, 3, 4));

		assertEquals(0, rowHideShowLayer.getRowCount());
	}

	@Test
	public void hideAllRows2() throws Exception {
		List<Integer> rowPositions = Arrays.asList(0);
		rowHideShowLayer.hideRowPositions(rowPositions);
		rowHideShowLayer.hideRowPositions(rowPositions);
		rowHideShowLayer.hideRowPositions(rowPositions);
		rowHideShowLayer.hideRowPositions(rowPositions);
		rowHideShowLayer.hideRowPositions(rowPositions);
		assertEquals(0, rowHideShowLayer.getRowCount());
	}

	@Test
	public void showARow() throws Exception {
		assertEquals(5, rowHideShowLayer.getRowCount());

		List<Integer> rowPositions = Arrays.asList(2);
		rowHideShowLayer.hideRowPositions(rowPositions); // index = 2
		rowPositions = Arrays.asList(0);
		rowHideShowLayer.hideRowPositions(rowPositions);  // index = 4
		assertEquals(3, rowHideShowLayer.getRowCount());
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(3));

		rowHideShowLayer.showRowIndexes(Arrays.asList(0));
		assertEquals(4, rowHideShowLayer.getRowCount());
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));

		rowHideShowLayer.showRowIndexes(Arrays.asList(2));
		assertEquals(5, rowHideShowLayer.getRowCount());
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(0, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(2));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(5));
	}

	@Test
	public void showAllRows() throws Exception {
		assertEquals(5, rowHideShowLayer.getRowCount());

		rowHideShowLayer.hideRowPositions(Arrays.asList(0));
		assertEquals(4, rowHideShowLayer.getRowCount());
		assertEquals(1, rowHideShowLayer.getRowIndexByPosition(0));
		assertEquals(2, rowHideShowLayer.getRowIndexByPosition(1));
		assertEquals(-1, rowHideShowLayer.getRowIndexByPosition(4));

		rowHideShowLayer.showAllRows();
		assertEquals(7, rowHideShowLayer.getRowCount());
		Assert.assertEquals(4, rowHideShowLayer.getRowIndexByPosition(0));
		Assert.assertEquals(1, rowHideShowLayer.getRowIndexByPosition(1));
		Assert.assertEquals(0, rowHideShowLayer.getRowIndexByPosition(2));
		Assert.assertEquals(2, rowHideShowLayer.getRowIndexByPosition(3));
		Assert.assertEquals(3, rowHideShowLayer.getRowIndexByPosition(4));
		Assert.assertEquals(5, rowHideShowLayer.getRowIndexByPosition(5));
		Assert.assertEquals(6, rowHideShowLayer.getRowIndexByPosition(6));
	}

	@Test
	public void showRowPositions() throws Exception {
		rowHideShowLayer = new RowHideShowLayerFixture(new DataLayerFixture(2,10,100,20));

		assertEquals(10, rowHideShowLayer.getRowCount());

		rowHideShowLayer.hideRowPositions(Arrays.asList(3,4,5));
		assertEquals(7, rowHideShowLayer.getRowCount());
		assertEquals(-1, rowHideShowLayer.getRowPositionByIndex(3));
		assertEquals(-1, rowHideShowLayer.getRowPositionByIndex(4));

		rowHideShowLayer.showRowIndexes(Arrays.asList(3,4));
		assertEquals(9, rowHideShowLayer.getRowCount());
		assertEquals(3, rowHideShowLayer.getRowPositionByIndex(3));
		assertEquals(4, rowHideShowLayer.getRowPositionByIndex(4));

	}
}
