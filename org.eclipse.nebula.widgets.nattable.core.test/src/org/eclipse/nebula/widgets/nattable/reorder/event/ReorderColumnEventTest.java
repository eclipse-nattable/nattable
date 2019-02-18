/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseColumnReorderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Test;

public class ReorderColumnEventTest {

    @Test
    public void shouldThrowAReorderColumnEvent() {
        BaseColumnReorderLayerFixture columnReorderLayer =
                new BaseColumnReorderLayerFixture(new DataLayerFixture());
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        columnReorderLayer.addLayerListener(listenerFixture);
        columnReorderLayer.reorderColumnPosition(3, 1);

        assertEquals(1, listenerFixture.getEventsCount());
        assertNotNull(listenerFixture.getReceivedEvent(ColumnReorderEvent.class));
    }

    /**
     * Fix for http://nattable.org/jira/browse/NTBL-476
     */
    @Test
    public void reorderEventMustPropagateToTheTop() throws Exception {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(20, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        assertEquals(6, natTableFixture.getColumnCount());
        assertEquals(1, natTableFixture.getColumnIndexByPosition(1));

        // Move to outside the visible range
        List<Integer> columnToMove = Arrays.asList(1, 2, 3);
        int destinationPosition = 10;
        natTableFixture.doCommand(
                new MultiColumnReorderCommand(natTableFixture, columnToMove, destinationPosition));

        // Ensure that the event propagates to the top
        assertEquals(1, listenerFixture.getEventsCount());
        assertNotNull(listenerFixture.getReceivedEvent(ColumnReorderEvent.class));
        assertEquals(4, natTableFixture.getColumnIndexByPosition(1));
    }

    @Test
    public void reorderMultipleNotConsecutiveColumnsInHiddenState() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        assertEquals(10, natTableFixture.getColumnCount());

        // hide some columns
        natTableFixture.doCommand(
                new MultiColumnHideCommand(natTableFixture, new int[] { 2, 5, 8 }));

        assertEquals(7, natTableFixture.getColumnCount());

        List<Integer> columnToMove = Arrays.asList(3, 4, 6, 7);
        int destinationPosition = 0;
        natTableFixture.doCommand(
                new MultiColumnReorderCommand(underlyingLayer.getColumnReorderLayer(), columnToMove, destinationPosition));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertTrue(StructuralChangeEventHelper.isReorder(event.getColumnDiffs()));

        assertEquals(7, natTableFixture.getColumnCount());

        assertEquals(0, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(3));
        assertEquals(1, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(4));
        assertEquals(2, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(6));
        assertEquals(3, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(7));
        assertEquals(4, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(0));
        assertEquals(5, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(1));
        assertEquals(6, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(2));
        assertEquals(7, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(5));
        assertEquals(8, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(8));
        assertEquals(9, underlyingLayer.getColumnReorderLayer().getColumnPositionByIndex(9));
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderNone() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 3, 3));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(3, range.start);
        assertEquals(4, range.end);

        assertEquals(3, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderLeft() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 3, 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(3, range.start);
        assertEquals(4, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderRight() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 1, 4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(1, range.start);
        assertEquals(2, range.end);

        assertEquals(4, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderLeft() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(3, 4), 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(3, range.start);
        assertEquals(5, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderRight() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(1, 2), 4));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(1, range.start);
        assertEquals(3, range.end);

        assertEquals(4, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiNonConsecutiveReorderLeft() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(3, 5), 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(5, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(4));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(5));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(2, event.getBeforeFromColumnPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromColumnPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(3, range.start);
        assertEquals(4, range.end);
        range = iterator.next();
        assertEquals(5, range.start);
        assertEquals(6, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiNonConsecutiveReorderRight() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(1, 3), 4));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(2, event.getBeforeFromColumnPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromColumnPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(1, range.start);
        assertEquals(2, range.end);
        range = iterator.next();
        assertEquals(3, range.start);
        assertEquals(4, range.end);

        assertEquals(4, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderNoneWithHidden() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 2, 2));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(2, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderLeftWithHidden() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 2, 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderLeftWithMultiHidden() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 2, 3));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 2, 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderRightWithHidden() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 2, 3));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(3, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderLeftWithHidden() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 2, 5));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(2, 3), 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(4));
        assertEquals(5, columnReorderLayer.getColumnIndexByPosition(5));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromColumnPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(2, range.start);
        assertEquals(4, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderLeftWithHiddenBetween() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(2, 3), 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(5, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(4));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(5));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromColumnPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(2, range.start);
        assertEquals(4, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderRightWithHidden() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 3));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(0, 1), 3));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(0, range.start);
        assertEquals(2, range.end);

        assertEquals(3, event.getBeforeToColumnPosition());

    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderRightWithHiddenBetween() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 0, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(0, 1), 3));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(5, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(4));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(5));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(0, range.start);
        assertEquals(2, range.end);

        assertEquals(3, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderRightWithHiddenAround() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 0, 3));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(0, 1), 3));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(0, range.start);
        assertEquals(2, range.end);

        assertEquals(3, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderLeftWithHiddenAround() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 1, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(1, 2), 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(4));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromColumnPositionRanges().size());
        Range range = event.getBeforeFromColumnPositionRanges().iterator().next();
        assertEquals(1, range.start);
        assertEquals(3, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiNonConsecutiveReorderLeftWithHidden() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 2, 5));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(2, 4), 0));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(6, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(4));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(5));
        assertEquals(5, columnReorderLayer.getColumnIndexByPosition(6));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(2, event.getBeforeFromColumnPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromColumnPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);
        range = iterator.next();
        assertEquals(4, range.start);
        assertEquals(5, range.end);

        assertEquals(0, event.getBeforeToColumnPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiNonConsecutiveReorderRightWithHidden() {
        DefaultBodyLayerStack underlyingLayer =
                new DefaultBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer, 1000, 400, true);

        // hide columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture, 2, 5));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, Arrays.asList(0, 2), 4));

        // verify reorder
        ColumnReorderLayer columnReorderLayer = underlyingLayer.getColumnReorderLayer();
        assertEquals(1, columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(2, columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(4, columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(5, columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(0, columnReorderLayer.getColumnIndexByPosition(4));
        assertEquals(3, columnReorderLayer.getColumnIndexByPosition(5));
        assertEquals(6, columnReorderLayer.getColumnIndexByPosition(6));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture.getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertEquals(2, event.getBeforeFromColumnPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromColumnPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(0, range.start);
        assertEquals(1, range.end);
        range = iterator.next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(4, event.getBeforeToColumnPosition());
    }
}
