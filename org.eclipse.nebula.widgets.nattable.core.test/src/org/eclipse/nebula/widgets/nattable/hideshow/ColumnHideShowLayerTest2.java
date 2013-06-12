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
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.test.LayerAssert;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnHideShowLayerTest2 {

	private ColumnHideShowLayer hideShowLayer;

	@Before
	public void setup(){
		TestLayer dataLayer =
			new TestLayer(
		              4, 4,
		              "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
		              "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
		              "A0 | B0 | C0 | D0 \n" +
		              "A1 | B1 | C1 | D1 \n" +
		              "A2 | B2 | C2 | D2 \n" +
		              "A3 | B3 | C3 | D3 \n"
			);

		hideShowLayer = new ColumnHideShowLayer(dataLayer);
	}

	@Test
	public void hideColumn() {
		hideShowLayer.hideColumnPositions(Arrays.asList(new Integer[] { 0, 2 }));

		TestLayer expectedLayer =
			new TestLayer(
					2, 4,
					"1:1;100 | 3:3;100",
					"0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
					"B0 | D0 \n" +
					"B1 | D1 \n" +
					"B2 | D2 \n" +
					"B3 | D3 \n"
			);

		LayerAssert.assertLayerEquals(expectedLayer, hideShowLayer);
	}

	@Test
	public void hideLastColumn() throws Exception {
		hideShowLayer.hideColumnPositions(Arrays.asList(new Integer[] { 3 }));

		TestLayer expectedLayer =
			new TestLayer(
					3, 4,
					"0:0;100 | 1:1;100 | 2:2;100",
					"0:0;40  | 1:1;40  | 2:2;40 | 3:3;40",
					"A0 | B0 | C0 \n" +
					"A1 | B1 | C1 \n" +
					"A2 | B2 | C2 \n" +
					"A3 | B3 | C3 \n"
			);

		LayerAssert.assertLayerEquals(expectedLayer, hideShowLayer);
	}

	@Test
	public void shouldFireTheCorrectEventOnColumnHide() throws Exception {
		NatTable natTable = new NatTableFixture();
		LayerListenerFixture listener = new LayerListenerFixture();

		natTable.addLayerListener(listener);

		// Grid coordinates
		natTable.doCommand(new ColumnHideCommand(natTable, 5));

		assertEquals(1, listener.getReceivedEvents().size());
		HideColumnPositionsEvent hideEvent = (HideColumnPositionsEvent) listener.getReceivedEvents().get(0);

		Range range = hideEvent.getColumnPositionRanges().iterator().next();
		assertEquals(5, range.start);
		assertEquals(6, range.end);

		// The range Before hide: 5 -> 6
		// The range After hide: 5 -> 5 (column is not there anymore)
		StructuralDiff columnDiff = hideEvent.getColumnDiffs().iterator().next();
		assertEquals(5, columnDiff.getBeforePositionRange().start);
		assertEquals(6, columnDiff.getBeforePositionRange().end);
		assertEquals(5, columnDiff.getAfterPositionRange().start);
		assertEquals(5, columnDiff.getAfterPositionRange().end);
	}

	/**
	 * Integration test
	 */
	@Test
	public void scrollAndHideTheLastColumn() throws Exception {
		// Total columns in fixture - 20 (index 0 - 19)
		NatTableFixture natTable = new NatTableFixture();
		LayerListenerFixture natTableListener = new LayerListenerFixture();
		natTable.addLayerListener(natTableListener);

		// Scroll to position 14 in grid/14 in body
		natTable.scrollToColumn(14);
		assertEquals(14, natTable.getColumnIndexByPosition(1));

		// Hide last column - position 6/index 19
		assertEquals(19, natTable.getColumnIndexByPosition(6));
		natTable.doCommand(new ColumnHideCommand(natTable, 6));

		// Assert event received
		assertNotNull(natTableListener.getReceivedEvent(HideColumnPositionsEvent.class));
		HideColumnPositionsEvent hideEvent = (HideColumnPositionsEvent) natTableListener.getReceivedEvent(HideColumnPositionsEvent.class);

		// When last column is hidden it is not carrying the following info
		assertEquals(1, hideEvent.getColumnPositionRanges().size());

		// View port adjusted origin to move an extra column in
		Range hiddenRange = hideEvent.getColumnPositionRanges().iterator().next();
		assertEquals(7, hiddenRange.start);
		assertEquals(8, hiddenRange.end);
	}
}
