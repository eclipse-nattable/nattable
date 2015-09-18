/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class RowSelectionModelStructuralChangeEventHandlerTest {

    private NatTable nattable;
    private List<RowDataFixture> listFixture;
    private IRowDataProvider<RowDataFixture> bodyDataProvider;
    private DataLayer bodyDataLayer;
    private SelectionLayer selectionLayer;

    private LayerListenerFixture listener;

    @Before
    public void setup() {
        this.listFixture = RowDataListFixture.getList(10);
        this.bodyDataProvider = new ListDataProvider<RowDataFixture>(this.listFixture,
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()));

        GridLayerFixture gridLayer = new GridLayerFixture(this.bodyDataProvider);
        this.nattable = new NatTableFixture(gridLayer, false);

        this.bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
        this.selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();

        this.selectionLayer.setSelectionModel(new RowSelectionModel<RowDataFixture>(
                this.selectionLayer,
                this.bodyDataProvider,
                new IRowIdAccessor<RowDataFixture>() {

                    @Override
                    public Serializable getRowId(RowDataFixture rowObject) {
                        return rowObject.getSecurity_id();
                    }

                }));

        this.listener = new LayerListenerFixture();
        // we register the listener to the SelectionLayer because for some cases
        // like clearing a collection, the selection change is not propagated
        // the layer stack upwards as it gets stopped on layer conversion
        this.selectionLayer.addLayerListener(this.listener);
    }

    @Test
    public void shouldRetainRowSelectionOnUpdates() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        this.listFixture.add(0, RowDataFixture.getInstance("Tata motors", "A"));

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(this.bodyDataLayer));

        // Tata motors at top but Ford motors still selected
        assertEquals("Tata motors", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());
    }

    @Test
    public void shouldRetainRowSelectionOnMove() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        RowDataFixture ford = getSelected();

        // move selected to the bottom
        this.listFixture.remove(ford);
        this.listFixture.add(ford);

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(this.bodyDataLayer));

        // Tata motors at top but Ford motors still selected
        assertEquals("A Alphabet Co.", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(this.listFixture.size() - 1, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void shouldRemoveSelectionOnDelete() {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        this.listFixture.remove(0);

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new RowDeleteEvent(this.bodyDataLayer, 0));

        // another value on top now
        assertEquals("A Alphabet Co.", this.nattable.getDataValueByPosition(2, 1).toString());
        // selection should be empty since the selected row was deleted
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void shouldOnlyRemoveSelectionForDeleted() {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, new int[] { 1, 2 }, true, false, 1));
        assertEquals(2, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(2, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());

        boolean fordFound = false;
        boolean alphabetFound = false;
        for (Range selection : this.selectionLayer.getSelectedRowPositions()) {
            for (int i = selection.start; i < selection.end; i++) {
                if ("B Ford Motor".equals(this.listFixture.get(i).getSecurity_description())) {
                    fordFound = true;
                }
                if ("A Alphabet Co.".equals(this.listFixture.get(i).getSecurity_description())) {
                    alphabetFound = true;
                }
            }
        }
        assertTrue("B Ford Motor not found", fordFound);
        assertTrue("A Alphabet Co. not found", alphabetFound);

        this.listFixture.remove(0);

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new RowDeleteEvent(this.bodyDataLayer, 0));

        // another value on top now
        assertEquals("A Alphabet Co.", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("A Alphabet Co.", getSelected().getSecurity_description());
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void shouldClearSelectionOnClearingTableWithStructuralRefresh() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        // clear the table
        this.listFixture.clear();

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(this.bodyDataLayer));

        // row count of 1 for NatTable because of header
        assertEquals(1, this.nattable.getRowCount());
        assertEquals(0, this.selectionLayer.getSelectedRowCount());
        assertTrue("selection model is not empty", this.selectionLayer.getSelectionModel().getSelections().isEmpty());
    }

    @Test
    public void shouldClearSelectionOnClearingTableWithRowStructuralRefresh() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        // clear the table
        this.listFixture.clear();

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new RowStructuralRefreshEvent(this.bodyDataLayer));

        // row count of 1 for NatTable because of header
        assertEquals(1, this.nattable.getRowCount());
        assertEquals(0, this.selectionLayer.getSelectedRowCount());
        assertTrue("selection model is not empty", this.selectionLayer.getSelectionModel().getSelections().isEmpty());
    }

    @Test
    public void shouldFireRowSelectionEvent() {
        // Select single row
        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 5, 5, false, false));

        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        RowSelectionEvent event = (RowSelectionEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(4, event.getRowPositionRanges().iterator().next().start);
        assertEquals(5, event.getRowPositionRanges().iterator().next().end);

        // Select additional rows with shift
        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 5, 7, true, false));

        assertEquals(3, this.selectionLayer.getSelectedRowCount());

        assertEquals(2, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        event = (RowSelectionEvent) this.listener.getReceivedEvents().get(1);
        assertEquals(1, event.getRowPositionRanges().size());

        assertEquals(4, event.getRowPositionRanges().iterator().next().start);
        assertEquals(7, event.getRowPositionRanges().iterator().next().end);
    }

    @Test
    public void shouldFireRowSelectionEventOnDeselect() {
        // Select single row
        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 5, 5, false, false));

        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        RowSelectionEvent event = (RowSelectionEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(4, event.getRowPositionRanges().iterator().next().start);
        assertEquals(5, event.getRowPositionRanges().iterator().next().end);

        // Deselect single row again
        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 5, 5, false, true));

        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        assertEquals(2, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        event = (RowSelectionEvent) this.listener.getReceivedEvents().get(1);
        assertEquals(1, event.getRowPositionRanges().size());

        assertEquals(4, event.getRowPositionRanges().iterator().next().start);
        assertEquals(5, event.getRowPositionRanges().iterator().next().end);
    }

    @Test
    public void shouldFireRowSelectionEventOnDelete() {
        // Select single row
        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 5, 5, false, false));

        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        RowSelectionEvent event = (RowSelectionEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(4, event.getRowPositionRanges().iterator().next().start);
        assertEquals(5, event.getRowPositionRanges().iterator().next().end);

        // Delete the selected row
        this.listFixture.remove(4);
        this.bodyDataLayer.fireLayerEvent(new RowDeleteEvent(this.bodyDataLayer, 4));

        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        assertEquals(3, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        event = (RowSelectionEvent) this.listener.getReceivedEvents().get(1);
        assertEquals(1, event.getRowPositionRanges().size());

        assertEquals(4, event.getRowPositionRanges().iterator().next().start);
        assertEquals(5, event.getRowPositionRanges().iterator().next().end);
    }

    @Test
    public void shouldFireRowSelectionEventOnClear() {
        // Select single row
        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 5, 5, false, false));

        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        RowSelectionEvent event = (RowSelectionEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(4, event.getRowPositionRanges().iterator().next().start);
        assertEquals(5, event.getRowPositionRanges().iterator().next().end);

        // clear
        this.listFixture.clear();
        this.bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(this.bodyDataLayer));

        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        assertEquals(3, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowSelectionEvent.class));

        event = (RowSelectionEvent) this.listener.getReceivedEvents().get(1);
        // since the underlying collection is cleared the ranges should be empty
        assertEquals(0, event.getRowPositionRanges().size());
    }

    private RowDataFixture getSelected() {
        Range selection = this.selectionLayer.getSelectedRowPositions().iterator().next();
        return this.listFixture.get(selection.start);
    }

}
