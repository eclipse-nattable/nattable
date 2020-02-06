/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class HideShowRowEventTest {

    private LayerListenerFixture layerListener;
    private RowReorderLayer reorderLayer;
    private RowHideShowLayer hideShowLayer;

    @Before
    public void setUp() {
        this.reorderLayer = new RowReorderLayer(new DataLayerFixture(100, 40));
        this.hideShowLayer = new RowHideShowLayer(this.reorderLayer);
        this.layerListener = new LayerListenerFixture();
        this.hideShowLayer.addLayerListener(this.layerListener);

        // this triggers building the cache
        assertEquals(5, this.hideShowLayer.getColumnCount());
    }

    @Test
    public void shouldFireHideColumnPositionsEventOnHide() {
        this.hideShowLayer.hideRowPositions(1, 4);

        assertTrue(this.hideShowLayer.isRowIndexHidden(1));
        assertTrue(this.hideShowLayer.isRowIndexHidden(4));

        assertEquals(1, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(HideRowPositionsEvent.class);
        assertNotNull(receivedEvent);

        HideRowPositionsEvent event = (HideRowPositionsEvent) receivedEvent;
        assertEquals(2, event.getRowPositionRanges().size());
        Iterator<Range> iterator = event.getRowPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireHideRowPositionsEventOnHideWithReorder() {
        // reorder first two rows to the end
        this.reorderLayer.reorderMultipleRowPositions(Arrays.asList(0, 1), 4, false);

        this.hideShowLayer.hideRowPositions(1, 4);

        assertTrue(this.hideShowLayer.isRowIndexHidden(3));
        assertTrue(this.hideShowLayer.isRowIndexHidden(1));

        assertEquals(2, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(HideRowPositionsEvent.class);
        assertNotNull(receivedEvent);

        HideRowPositionsEvent event = (HideRowPositionsEvent) receivedEvent;
        assertEquals(2, event.getRowPositionRanges().size());
        Iterator<Range> iterator = event.getRowPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireShowColumnPositionsEventOnShowIndexes() {
        this.hideShowLayer.hideRowPositions(1, 4);

        // hide is tested in another test, so clear the test initialization
        this.layerListener.clearReceivedEvents();

        assertTrue(this.hideShowLayer.isRowIndexHidden(1));
        assertTrue(this.hideShowLayer.isRowIndexHidden(4));

        this.hideShowLayer.showRowIndexes(1, 4);
        assertFalse(this.hideShowLayer.isRowIndexHidden(1));
        assertFalse(this.hideShowLayer.isRowIndexHidden(4));

        assertEquals(1, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(ShowRowPositionsEvent.class);
        assertNotNull(receivedEvent);

        ShowRowPositionsEvent event = (ShowRowPositionsEvent) receivedEvent;
        assertEquals(2, event.getRowPositionRanges().size());
        Iterator<Range> iterator = event.getRowPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireShowColumnPositionsEventOnShowIndexesWithReorder() {
        // reorder first two columns to the end
        this.reorderLayer.reorderMultipleRowPositions(Arrays.asList(0, 1), 4, false);

        this.hideShowLayer.hideRowPositions(1, 4);

        assertTrue(this.hideShowLayer.isRowIndexHidden(3));
        assertTrue(this.hideShowLayer.isRowIndexHidden(1));

        this.hideShowLayer.showRowIndexes(3, 1);
        assertFalse(this.hideShowLayer.isRowIndexHidden(3));
        assertFalse(this.hideShowLayer.isRowIndexHidden(1));

        assertEquals(3, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(ShowRowPositionsEvent.class);
        assertNotNull(receivedEvent);

        ShowRowPositionsEvent event = (ShowRowPositionsEvent) receivedEvent;
        assertEquals(2, event.getRowPositionRanges().size());
        Iterator<Range> iterator = event.getRowPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireShowColumnPositionsEventOnShowAll() {
        this.hideShowLayer.hideRowPositions(1, 4);

        // hide is tested in another test, so clear the test initialization
        this.layerListener.clearReceivedEvents();

        assertTrue(this.hideShowLayer.isRowIndexHidden(1));
        assertTrue(this.hideShowLayer.isRowIndexHidden(4));

        this.hideShowLayer.showAllRows();
        assertFalse(this.hideShowLayer.isRowIndexHidden(1));
        assertFalse(this.hideShowLayer.isRowIndexHidden(4));

        assertEquals(1, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(ShowRowPositionsEvent.class);
        assertNotNull(receivedEvent);

        ShowRowPositionsEvent event = (ShowRowPositionsEvent) receivedEvent;
        assertEquals(2, event.getRowPositionRanges().size());
        Iterator<Range> iterator = event.getRowPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireShowColumnPositionsEventOnShowAllWithReorder() {
        // reorder first two columns to the end
        this.reorderLayer.reorderMultipleRowPositions(Arrays.asList(0, 1), 4, false);

        this.hideShowLayer.hideRowPositions(1, 4);

        assertTrue(this.hideShowLayer.isRowIndexHidden(3));
        assertTrue(this.hideShowLayer.isRowIndexHidden(1));

        this.hideShowLayer.showAllRows();
        assertFalse(this.hideShowLayer.isRowIndexHidden(3));
        assertFalse(this.hideShowLayer.isRowIndexHidden(1));

        assertEquals(3, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(ShowRowPositionsEvent.class);
        assertNotNull(receivedEvent);

        ShowRowPositionsEvent event = (ShowRowPositionsEvent) receivedEvent;
        assertEquals(2, event.getRowPositionRanges().size());
        Iterator<Range> iterator = event.getRowPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }
}
