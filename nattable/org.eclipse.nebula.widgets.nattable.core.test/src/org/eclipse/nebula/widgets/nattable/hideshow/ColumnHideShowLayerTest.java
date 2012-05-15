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
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;


import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHideShowLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("boxing")
public class ColumnHideShowLayerTest {

	private ColumnHideShowLayer columnHideShowLayer;

	@Before
	public void setup(){
		columnHideShowLayer = new ColumnHideShowLayerFixture();
	}

	@Test
	public void getColumnIndexByPosition() throws Exception {
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));

		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(3));
	}

	@Test
	public void getColumnIndexHideAdditionalColumn() throws Exception {
		getColumnIndexByPosition();

		columnHideShowLayer.hideColumnPositions(Arrays.asList(1));

		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(1));

		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(2));
	}

	@Test
	public void getColumnPositionForASingleHiddenColumn() throws Exception {
		assertEquals(-1, columnHideShowLayer.getColumnPositionByIndex(0));
		assertEquals(1, columnHideShowLayer.getColumnPositionByIndex(1));
		assertEquals(2, columnHideShowLayer.getColumnPositionByIndex(2));
		assertEquals(-1, columnHideShowLayer.getColumnPositionByIndex(3));
		assertEquals(0, columnHideShowLayer.getColumnPositionByIndex(4));
	}

	@Test
	public void hideAllColumns() throws Exception {
		columnHideShowLayer.hideColumnPositions(Arrays.asList(0, 1, 2));

		assertEquals(0, columnHideShowLayer.getColumnCount());
	}

	@Test
	public void hideAllColumns2() throws Exception {
		List<Integer> columnPositions = Arrays.asList(0);
		columnHideShowLayer.hideColumnPositions(columnPositions);
		columnHideShowLayer.hideColumnPositions(columnPositions);
		columnHideShowLayer.hideColumnPositions(columnPositions);
		assertEquals(0, columnHideShowLayer.getColumnCount());
	}

	@Test
	public void showAColumn() throws Exception {
		assertEquals(3, columnHideShowLayer.getColumnCount());

		List<Integer> columnPositions = Arrays.asList(2);
		columnHideShowLayer.hideColumnPositions(columnPositions); // index = 2
		columnPositions = Arrays.asList(0);
		columnHideShowLayer.hideColumnPositions(columnPositions);  // index = 4
		assertEquals(1, columnHideShowLayer.getColumnCount());
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(1));

		columnHideShowLayer.showColumnIndexes(Arrays.asList(0));
		assertEquals(2, columnHideShowLayer.getColumnCount());
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(2));

		columnHideShowLayer.showColumnIndexes(Arrays.asList(2));
		assertEquals(3, columnHideShowLayer.getColumnCount());
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(3));
	}

	@Test
	public void showAllColumns() throws Exception {
		assertEquals(3, columnHideShowLayer.getColumnCount());

		columnHideShowLayer.hideColumnPositions(Arrays.asList(0));
		assertEquals(2, columnHideShowLayer.getColumnCount());
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(2));

		columnHideShowLayer.showAllColumns();
		assertEquals(5, columnHideShowLayer.getColumnCount());
		assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(0));
		assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(2));
		assertEquals(2, columnHideShowLayer.getColumnIndexByPosition(3));
		assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(4));
		assertEquals(-1, columnHideShowLayer.getColumnIndexByPosition(5));
	}

	@Test
	public void showColumnPositions() throws Exception {
		columnHideShowLayer = new ColumnHideShowLayerFixture(new DataLayerFixture(10,2,100,20));

		assertEquals(10, columnHideShowLayer.getColumnCount());

		columnHideShowLayer.hideColumnPositions(Arrays.asList(3,4,5));
		assertEquals(7, columnHideShowLayer.getColumnCount());
		assertEquals(-1, columnHideShowLayer.getColumnPositionByIndex(3));
		assertEquals(-1, columnHideShowLayer.getColumnPositionByIndex(4));

		columnHideShowLayer.showColumnIndexes(Arrays.asList(3,4));
		assertEquals(9, columnHideShowLayer.getColumnCount());
		assertEquals(3, columnHideShowLayer.getColumnPositionByIndex(3));
		assertEquals(4, columnHideShowLayer.getColumnPositionByIndex(4));

	}
}
