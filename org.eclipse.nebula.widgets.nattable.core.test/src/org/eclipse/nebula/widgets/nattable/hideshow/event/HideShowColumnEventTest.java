/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class HideShowColumnEventTest {

    private LayerListenerFixture layerListener;
    private ColumnReorderLayer reorderLayer;
    private ColumnHideShowLayer hideShowLayer;

    @Before
    public void setUp() {
        this.reorderLayer = new ColumnReorderLayer(new DataLayerFixture(100, 40));
        this.hideShowLayer = new ColumnHideShowLayer(this.reorderLayer);
        this.layerListener = new LayerListenerFixture();
        this.hideShowLayer.addLayerListener(this.layerListener);

        // this triggers building the cache
        assertEquals(5, this.hideShowLayer.getColumnCount());
    }

    @Test
    public void shouldFireHideColumnPositionsEventOnHide() {
        this.hideShowLayer.hideColumnPositions(1, 4);

        assertTrue(this.hideShowLayer.isColumnIndexHidden(1));
        assertTrue(this.hideShowLayer.isColumnIndexHidden(4));

        assertEquals(1, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(HideColumnPositionsEvent.class);
        assertNotNull(receivedEvent);

        HideColumnPositionsEvent event = (HideColumnPositionsEvent) receivedEvent;
        assertEquals(2, event.getColumnPositionRanges().size());
        Iterator<Range> iterator = event.getColumnPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireHideColumnPositionsEventOnHideWithReorder() {
        // reorder first two columns to the end
        this.reorderLayer.reorderMultipleColumnPositions(Arrays.asList(0, 1), 4, false);

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertTrue(this.hideShowLayer.isColumnIndexHidden(3));
        assertTrue(this.hideShowLayer.isColumnIndexHidden(1));

        assertEquals(2, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(HideColumnPositionsEvent.class);
        assertNotNull(receivedEvent);

        HideColumnPositionsEvent event = (HideColumnPositionsEvent) receivedEvent;
        assertEquals(2, event.getColumnPositionRanges().size());
        Iterator<Range> iterator = event.getColumnPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireShowColumnPositionsEventOnShowIndexes() {
        this.hideShowLayer.hideColumnPositions(1, 4);

        // hide is tested in another test, so clear the test initialization
        this.layerListener.clearReceivedEvents();

        assertTrue(this.hideShowLayer.isColumnIndexHidden(1));
        assertTrue(this.hideShowLayer.isColumnIndexHidden(4));

        this.hideShowLayer.showColumnIndexes(1, 4);
        assertFalse(this.hideShowLayer.isColumnIndexHidden(1));
        assertFalse(this.hideShowLayer.isColumnIndexHidden(4));

        assertEquals(1, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(ShowColumnPositionsEvent.class);
        assertNotNull(receivedEvent);

        ShowColumnPositionsEvent event = (ShowColumnPositionsEvent) receivedEvent;
        assertEquals(2, event.getColumnPositionRanges().size());
        Iterator<Range> iterator = event.getColumnPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireShowColumnPositionsEventOnShowIndexesWithReorder() {
        // reorder first two columns to the end
        this.reorderLayer.reorderMultipleColumnPositions(Arrays.asList(0, 1), 4, false);

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertTrue(this.hideShowLayer.isColumnIndexHidden(3));
        assertTrue(this.hideShowLayer.isColumnIndexHidden(1));

        this.hideShowLayer.showColumnIndexes(3, 1);
        assertFalse(this.hideShowLayer.isColumnIndexHidden(3));
        assertFalse(this.hideShowLayer.isColumnIndexHidden(1));

        assertEquals(3, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(ShowColumnPositionsEvent.class);
        assertNotNull(receivedEvent);

        ShowColumnPositionsEvent event = (ShowColumnPositionsEvent) receivedEvent;
        assertEquals(2, event.getColumnPositionRanges().size());
        Iterator<Range> iterator = event.getColumnPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireShowColumnPositionsEventOnShowAll() {
        this.hideShowLayer.hideColumnPositions(1, 4);

        // hide is tested in another test, so clear the test initialization
        this.layerListener.clearReceivedEvents();

        assertTrue(this.hideShowLayer.isColumnIndexHidden(1));
        assertTrue(this.hideShowLayer.isColumnIndexHidden(4));

        this.hideShowLayer.showAllColumns();
        assertFalse(this.hideShowLayer.isColumnIndexHidden(1));
        assertFalse(this.hideShowLayer.isColumnIndexHidden(4));

        assertEquals(1, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(ShowColumnPositionsEvent.class);
        assertNotNull(receivedEvent);

        ShowColumnPositionsEvent event = (ShowColumnPositionsEvent) receivedEvent;
        assertEquals(2, event.getColumnPositionRanges().size());
        Iterator<Range> iterator = event.getColumnPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }

    @Test
    public void shouldFireShowColumnPositionsEventOnShowAllWithReorder() {
        // reorder first two columns to the end
        this.reorderLayer.reorderMultipleColumnPositions(Arrays.asList(0, 1), 4, false);

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertTrue(this.hideShowLayer.isColumnIndexHidden(3));
        assertTrue(this.hideShowLayer.isColumnIndexHidden(1));

        this.hideShowLayer.showAllColumns();
        assertFalse(this.hideShowLayer.isColumnIndexHidden(3));
        assertFalse(this.hideShowLayer.isColumnIndexHidden(1));

        assertEquals(3, this.layerListener.getEventsCount());
        ILayerEvent receivedEvent = this.layerListener.getReceivedEvent(ShowColumnPositionsEvent.class);
        assertNotNull(receivedEvent);

        ShowColumnPositionsEvent event = (ShowColumnPositionsEvent) receivedEvent;
        assertEquals(2, event.getColumnPositionRanges().size());
        Iterator<Range> iterator = event.getColumnPositionRanges().iterator();
        assertEquals(new Range(1, 2), iterator.next());
        assertEquals(new Range(4, 5), iterator.next());
    }
}
