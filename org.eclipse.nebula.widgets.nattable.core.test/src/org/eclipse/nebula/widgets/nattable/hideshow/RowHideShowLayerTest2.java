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
import static org.junit.Assert.assertNotNull;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class RowHideShowLayerTest2 {

	@Test
	public void shouldFireTheCorrectEventOnRowHide() throws Exception {
		NatTable natTable = new NatTableFixture(new Shell(), 
				new DummyGridLayerStack() {
					
					@Override
					protected void init(IUniqueIndexLayer bodyDataLayer, IUniqueIndexLayer columnHeaderDataLayer, IUniqueIndexLayer rowHeaderDataLayer, IUniqueIndexLayer cornerDataLayer) {
						RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(bodyDataLayer);
						super.init(rowHideShowLayer, columnHeaderDataLayer, rowHeaderDataLayer, cornerDataLayer);
					}
		
			});
		LayerListenerFixture listener = new LayerListenerFixture();

		natTable.addLayerListener(listener);

		// Grid coordinates
		natTable.doCommand(new RowHideCommand(natTable, 5));

		assertEquals(1, listener.getReceivedEvents().size());
		HideRowPositionsEvent hideEvent = (HideRowPositionsEvent) listener.getReceivedEvents().get(0);

		Range range = hideEvent.getRowPositionRanges().iterator().next();
		assertEquals(5, range.start);
		assertEquals(6, range.end);

		// The range Before hide: 5 -> 6
		// The range After hide: 5 -> 5 (row is not there anymore)
		StructuralDiff rowDiff = hideEvent.getRowDiffs().iterator().next();
		assertEquals(5, rowDiff.getBeforePositionRange().start);
		assertEquals(6, rowDiff.getBeforePositionRange().end);
		assertEquals(5, rowDiff.getAfterPositionRange().start);
		assertEquals(5, rowDiff.getAfterPositionRange().end);
	}

	/**
	 * Integration test
	 */
	@Test
	public void scrollAndHideTheLastRow() throws Exception {
		// Total rows in fixture - 20 (index 0 - 19)
		NatTableFixture natTable = new NatTableFixture(new Shell(), 
				new DummyGridLayerStack() {
					
					@Override
					protected void init(IUniqueIndexLayer bodyDataLayer, IUniqueIndexLayer columnHeaderDataLayer, IUniqueIndexLayer rowHeaderDataLayer, IUniqueIndexLayer cornerDataLayer) {
						RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(bodyDataLayer);
						super.init(rowHideShowLayer, columnHeaderDataLayer, rowHeaderDataLayer, cornerDataLayer);
					}
		
			}, 600, 120);
		LayerListenerFixture natTableListener = new LayerListenerFixture();
		natTable.addLayerListener(natTableListener);

		// Scroll to position 15 in grid/15 in body
		natTable.scrollToRow(15);
		assertEquals(15, natTable.getRowIndexByPosition(1));

		// Hide last row - position 5/index 19
		assertEquals(19, natTable.getRowIndexByPosition(5));
		natTable.doCommand(new RowHideCommand(natTable, 5));

		// Assert event received
		assertNotNull(natTableListener.getReceivedEvent(HideRowPositionsEvent.class));
		HideRowPositionsEvent hideEvent = (HideRowPositionsEvent) natTableListener.getReceivedEvent(HideRowPositionsEvent.class);

		// When last row is hidden it is not carrying the following info
		assertEquals(1, hideEvent.getRowPositionRanges().size());

		// View port adjusted origin to move an extra row in
		Range hiddenRange = hideEvent.getRowPositionRanges().iterator().next();
		assertEquals(6, hiddenRange.start);
		assertEquals(7, hiddenRange.end);
	}
}
