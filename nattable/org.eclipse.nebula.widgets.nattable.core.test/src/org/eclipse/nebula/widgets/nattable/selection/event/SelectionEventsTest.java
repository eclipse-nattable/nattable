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
package org.eclipse.nebula.widgets.nattable.selection.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.grid.layer.event.ColumnHeaderSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class SelectionEventsTest {

	public static boolean NO_SHIFT = false;
	public static boolean NO_CTRL = false;

	public static boolean WITH_SHIFT = true;
	public static boolean WITH_CTRL = true;

	NatTableFixture nattable;
	private LayerListenerFixture listener;

	@Before
	public void setup(){
		nattable = new NatTableFixture();
		listener = new LayerListenerFixture();
		nattable.addLayerListener(listener);
	}

	@Test
	public void shouldFireCellSelectionEvent() throws Exception {
		// Grid coordinates
		nattable.doCommand(new SelectCellCommand(nattable, 1, 5, NO_SHIFT, NO_CTRL));

		assertEquals(1, listener.getEventsCount());
		assertTrue(listener.containsInstanceOf(CellSelectionEvent.class));

		CellSelectionEvent event = (CellSelectionEvent) listener.getReceivedEvents().get(0);
		assertEquals(1, event.getColumnPosition());
		assertEquals(5, event.getRowPosition());
	}

	@Test
	public void shouldFireRowSelectionEvent() throws Exception {
		// Select single row
		nattable.doCommand(new SelectRowsCommand(nattable, 5, 5, NO_SHIFT, NO_CTRL));

		assertEquals(1, listener.getEventsCount());
		assertTrue(listener.containsInstanceOf(RowSelectionEvent.class));

		RowSelectionEvent event = (RowSelectionEvent) listener.getReceivedEvents().get(0);
		assertEquals(5, event.getRowPositionRanges().iterator().next().start);
		assertEquals(6, event.getRowPositionRanges().iterator().next().end);

		// Select additional rows with shift
		nattable.doCommand(new SelectRowsCommand(nattable, 5, 7, WITH_SHIFT, NO_CTRL));
		assertEquals(2, listener.getEventsCount());
		assertTrue(listener.containsInstanceOf(RowSelectionEvent.class));

		event = (RowSelectionEvent) listener.getReceivedEvents().get(1);
		assertEquals(1, event.getRowPositionRanges().size());

		assertEquals(5, event.getRowPositionRanges().iterator().next().start);
		assertEquals(8, event.getRowPositionRanges().iterator().next().end);
	}

	@Test
	public void shouldFireColumnSelectionEvent() throws Exception {
		nattable.doCommand(new SelectColumnCommand(nattable, 5, 5, NO_SHIFT, NO_CTRL));

		assertEquals(2, listener.getEventsCount());
		assertTrue(listener.containsInstanceOf(ColumnSelectionEvent.class));
		assertTrue(listener.containsInstanceOf(ColumnHeaderSelectionEvent.class));

		ColumnSelectionEvent event = (ColumnSelectionEvent) listener.getReceivedEvents().get(0);
		assertEquals(5, event.getColumnPositionRanges().iterator().next().start);
		assertEquals(6, event.getColumnPositionRanges().iterator().next().end);
	}
}
