/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class SelectionEventsTest {

    public static final boolean NO_SHIFT = false;
    public static final boolean NO_CTRL = false;

    public static final boolean WITH_SHIFT = true;
    public static final boolean WITH_CTRL = true;

    NatTableFixture natTable;
    private LayerListenerFixture listener;

    @Before
    public void setup() {
        this.natTable = new NatTableFixture();
        this.listener = new LayerListenerFixture();
        this.natTable.addLayerListener(this.listener);
    }

    @Test
    public void shouldFireCellSelectionEvent() {
        // Grid coordinates
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 5, NO_SHIFT, NO_CTRL));

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(CellSelectionEvent.class));

        CellSelectionEvent event = (CellSelectionEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(1, event.getColumnPosition());
        assertEquals(5, event.getRowPosition());
    }

    @Test
    public void shouldFireRowSelectionEvent() {
        // Select single row
        this.natTable.doCommand(new SelectRowsCommand(this.natTable, 5, 5, NO_SHIFT, NO_CTRL));

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        RowSelectionEvent event = (RowSelectionEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(5, event.getRowPositionRanges().iterator().next().start);
        assertEquals(6, event.getRowPositionRanges().iterator().next().end);

        // Select additional rows with shift
        this.natTable.doCommand(new SelectRowsCommand(this.natTable, 5, 7, WITH_SHIFT, NO_CTRL));
        assertEquals(2, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        event = (RowSelectionEvent) this.listener.getReceivedEvents().get(1);
        assertEquals(1, event.getRowPositionRanges().size());

        assertEquals(5, event.getRowPositionRanges().iterator().next().start);
        assertEquals(8, event.getRowPositionRanges().iterator().next().end);
    }

    @Test
    public void shouldFireColumnSelectionEvent() {
        this.natTable.doCommand(new SelectColumnCommand(this.natTable, 5, 5, NO_SHIFT, NO_CTRL));

        assertEquals(2, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(ColumnSelectionEvent.class));
        assertTrue(this.listener.containsInstanceOf(ColumnHeaderSelectionEvent.class));

        ColumnSelectionEvent event = (ColumnSelectionEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(5, event.getColumnPositionRanges().iterator().next().start);
        assertEquals(6, event.getColumnPositionRanges().iterator().next().end);
    }
}
