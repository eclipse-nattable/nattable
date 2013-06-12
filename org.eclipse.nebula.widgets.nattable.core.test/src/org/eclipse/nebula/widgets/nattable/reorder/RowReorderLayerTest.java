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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.LayerCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowReorderLayerTest {

	private IUniqueIndexLayer underlyingLayer;
	private RowReorderLayer rowReorderLayer;

	@Before
	public void setUp() {
		underlyingLayer = new BaseDataLayerFixture(4,4);
		rowReorderLayer = new RowReorderLayer(underlyingLayer);
	}

	@Test
	public void reorderViewableRowsBottomToTop() throws Exception {
		// 0 1 2 3
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(3));

		// 3 0 1 2
		rowReorderLayer.reorderRowPosition(3, 0);
		assertEquals(1, rowReorderLayer.getRowPositionByIndex(0));
		assertEquals(0, rowReorderLayer.getRowPositionByIndex(3));

		assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(2, rowReorderLayer.getRowIndexByPosition(3));

		// 0 1 3 2
		rowReorderLayer.reorderRowPosition(0, 3);
		assertEquals(0, rowReorderLayer.getRowPositionByIndex(0));
		assertEquals(1, rowReorderLayer.getRowPositionByIndex(1));
		assertEquals(2, rowReorderLayer.getRowPositionByIndex(3));
		assertEquals(3, rowReorderLayer.getRowPositionByIndex(2));

		assertEquals(0, rowReorderLayer.getRowPositionByIndex(0));
		assertEquals(1, rowReorderLayer.getRowPositionByIndex(1));
		assertEquals(3, rowReorderLayer.getRowPositionByIndex(2));
		assertEquals(2, rowReorderLayer.getRowPositionByIndex(3));
	}

	@Test
	/**
	 * 	Index		1	2	3	0
	 *          --------------------
	 *  Position 	0 	1	2	3
	 */
	public void reorderViewableRowsTopToBottomByPosition() throws Exception {
		// Moving to the end
		rowReorderLayer.reorderRowPosition(0, 4);

		assertEquals(2, rowReorderLayer.getRowPositionByIndex(3));
		assertEquals(3, rowReorderLayer.getRowPositionByIndex(0));

		assertEquals(0, rowReorderLayer.getRowIndexByPosition(3));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(0));
	}

	@SuppressWarnings("boxing")
	@Test
	/**
	 * 	Index		2 	0	1	3
	 *          --------------------
	 *  Position 	0 	1	2	3
	 */
	public void reorderMultipleRowsTopToBottom() throws Exception {
		List<Integer> fromRowsPositions = Arrays.asList(new Integer[]{0, 1});

		rowReorderLayer.reorderMultipleRowPositions(fromRowsPositions, 3);

		assertEquals(2, rowReorderLayer.getRowIndexByPosition(0));
		assertEquals(0, rowReorderLayer.getRowIndexByPosition(1));
		assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
		assertEquals(3, rowReorderLayer.getRowIndexByPosition(3));
	}
	
	@SuppressWarnings("boxing")
	@Test
	/**
	 * 	Index		2 	3	0	1
	 *          --------------------
	 *  Position 	0 	1	2	3
	 */
	public void reorderMultipleRowsTopToBottomToTheEnd() throws Exception {
		List<Integer> fromRowPositions = Arrays.asList(new Integer[]{0, 1});

		rowReorderLayer.reorderMultipleRowPositions(fromRowPositions, 4);

		assertEquals(2, rowReorderLayer.getRowPositionByIndex(0));
		assertEquals(3, rowReorderLayer.getRowPositionByIndex(1));
		assertEquals(0, rowReorderLayer.getRowPositionByIndex(2));
		assertEquals(1, rowReorderLayer.getRowPositionByIndex(3));
	}

	@Test
	/**
	 * 	Index		0	1	3	2
	 *          --------------------
	 *  Position 	0 	1	2	3
	 */
	public void reorderViewableRowsBottomToTopByPosition() throws Exception {
		rowReorderLayer.reorderRowPosition(3, 2);

		assertEquals(2, rowReorderLayer.getRowPositionByIndex(3));
		assertEquals(0, rowReorderLayer.getRowPositionByIndex(0));

		assertEquals(2, rowReorderLayer.getRowPositionByIndex(3));
		assertEquals(0, rowReorderLayer.getRowPositionByIndex(0));
	}

	@SuppressWarnings("boxing")
	@Test
	/**
	 * 	Index		2	3	0	1
	 *          --------------------
	 *  Position 	0 	1	2	3
	 */
	public void reorderMultipleRowsBottomToTop() throws Exception {
		List<Integer> fromRowPositions = Arrays.asList(new Integer[]{2, 3});

		rowReorderLayer.reorderMultipleRowPositions(fromRowPositions, 0);

		assertEquals(2, rowReorderLayer.getRowPositionByIndex(0));
		assertEquals(3, rowReorderLayer.getRowPositionByIndex(1));
		assertEquals(0, rowReorderLayer.getRowPositionByIndex(2));
		assertEquals(1, rowReorderLayer.getRowPositionByIndex(3));
	}

	@SuppressWarnings("boxing")
	@Test
	/**
	 * 	Index		2	3	0	1 ... 20
	 *          --------------------
	 *  Position 	0 	1	2	3 ... 20
	 */
	public void reorderMultipleRowsLargeArrayToEdges() throws Exception {

		RowReorderLayer reorderLayer = new RowReorderLayer(new BaseDataLayerFixture(20, 20));

		List<Integer> fromRowPositions = Arrays.asList(new Integer[]{10, 11, 12, 13});

		reorderLayer.reorderMultipleRowPositions(fromRowPositions, 0);

		assertEquals(10, reorderLayer.getRowIndexByPosition(0));
		assertEquals(11, reorderLayer.getRowIndexByPosition(1));
		assertEquals(12, reorderLayer.getRowIndexByPosition(2));
		assertEquals(13, reorderLayer.getRowIndexByPosition(3));
		assertEquals(0, reorderLayer.getRowIndexByPosition(4));

		fromRowPositions = Arrays.asList(new Integer[]{8, 9, 10, 11});

		reorderLayer.reorderMultipleRowPositions(fromRowPositions, 8);

		assertEquals(4, reorderLayer.getRowIndexByPosition(8));
		assertEquals(5, reorderLayer.getRowIndexByPosition(9));
		assertEquals(6, reorderLayer.getRowIndexByPosition(10));
		assertEquals(7, reorderLayer.getRowIndexByPosition(11));

		fromRowPositions = Arrays.asList(new Integer[]{8, 9, 10, 11});

		reorderLayer.reorderMultipleRowPositions(fromRowPositions, reorderLayer.getColumnCount());

		assertEquals(7, reorderLayer.getRowIndexByPosition(19));
		assertEquals(6, reorderLayer.getRowIndexByPosition(18));
		assertEquals(5, reorderLayer.getRowIndexByPosition(17));
		assertEquals(4, reorderLayer.getRowIndexByPosition(16));
	}

	@Test
	public void commandPassedOnToParentIfCannotBeHandled() throws Exception {
		RowReorderLayer reorderLayer = new RowReorderLayer(new DataLayerFixture());
		assertFalse(reorderLayer.doCommand(new LayerCommandFixture()));
	}

	@Test
	public void canHandleRowReorderCommand() throws Exception {
		RowReorderLayer reorderLayer = new RowReorderLayer(new DataLayerFixture());
		RowReorderCommand reorderCommand = new RowReorderCommand(reorderLayer, 0, 2);
		assertTrue(reorderLayer.doCommand(reorderCommand));
	}

	@Test
	public void getHeightForReorderedRows() throws Exception {
		underlyingLayer = new DataLayerFixture();
		rowReorderLayer = new RowReorderLayer(underlyingLayer);

		// 0 1 2 3 4 - see DataLayerFixture
		rowReorderLayer.reorderRowPosition(0, 7);

		// 1 2 3 4 0
		Assert.assertEquals(70, rowReorderLayer.getRowHeightByPosition(0));
		Assert.assertEquals(25, rowReorderLayer.getRowHeightByPosition(1));
		Assert.assertEquals(40, rowReorderLayer.getRowHeightByPosition(2));
		Assert.assertEquals(50, rowReorderLayer.getRowHeightByPosition(3));
		Assert.assertEquals(40, rowReorderLayer.getRowHeightByPosition(4));
		Assert.assertEquals(100, rowReorderLayer.getRowHeightByPosition(5));
		Assert.assertEquals(40, rowReorderLayer.getRowHeightByPosition(6));
	}

	@SuppressWarnings("boxing")
	@Test
	public void getHeightForMultipleRowsReordering() throws Exception {
		underlyingLayer = new DataLayerFixture();
		rowReorderLayer = new RowReorderLayer(underlyingLayer);

		// 0 1 2 3 4 - see DataLayerFixture
		rowReorderLayer.reorderMultipleRowPositions(Arrays.asList(1, 2), 7);

		// 0 3 4 1 2
		assertEquals(40, rowReorderLayer.getRowHeightByPosition(0));
		assertEquals(40, rowReorderLayer.getRowHeightByPosition(1));
		assertEquals(50, rowReorderLayer.getRowHeightByPosition(2));
		assertEquals(40, rowReorderLayer.getRowHeightByPosition(3));
		assertEquals(100, rowReorderLayer.getRowHeightByPosition(4));
		assertEquals(70, rowReorderLayer.getRowHeightByPosition(5));
		assertEquals(25, rowReorderLayer.getRowHeightByPosition(6));
	}

	@Test
	public void getStartYForReorderedRow() throws Exception {
		underlyingLayer = new DataLayerFixture();
		rowReorderLayer = new RowReorderLayer(underlyingLayer);

		// 0 1 2 3 4 - see DataLayerFixture
		rowReorderLayer.reorderRowPosition(0, 5);

		// Index: 1 2 3 4 0 Height: 70 25 40 50 40 100 40
		assertEquals(0, rowReorderLayer.getStartYOfRowPosition(0));
		assertEquals(70, rowReorderLayer.getStartYOfRowPosition(1));
		assertEquals(95, rowReorderLayer.getStartYOfRowPosition(2));
		assertEquals(135, rowReorderLayer.getStartYOfRowPosition(3));
		assertEquals(185, rowReorderLayer.getStartYOfRowPosition(4));
		assertEquals(225, rowReorderLayer.getStartYOfRowPosition(5));
		assertEquals(265, rowReorderLayer.getStartYOfRowPosition(6));
	}

}








