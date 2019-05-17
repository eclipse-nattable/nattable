/*******************************************************************************
 * Copyright (c) 2013, 2019 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseRowReorderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.junit.Test;

public class ReorderRowEventTest {

    @Test
    public void shouldThrowAReorderRowEvent() {
        BaseRowReorderLayerFixture rowReorderLayer =
                new BaseRowReorderLayerFixture(new DataLayerFixture());
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        rowReorderLayer.addLayerListener(listenerFixture);
        rowReorderLayer.reorderRowPosition(3, 1);

        assertEquals(1, listenerFixture.getEventsCount());
        assertNotNull(listenerFixture.getReceivedEvent(RowReorderEvent.class));
    }

    @Test
    public void reorderEventMustPropagateToTheTop() throws Exception {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(20, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        assertEquals(10, natTableFixture.getRowCount());
        assertEquals(1, natTableFixture.getRowIndexByPosition(1));

        // Move to outside the visible range
        List<Integer> rowToMove = Arrays.asList(1, 2, 3);
        int destinationPosition = 10;
        natTableFixture.doCommand(
                new MultiRowReorderCommand(natTableFixture, rowToMove, destinationPosition));

        // Ensure that the event propagates to the top
        assertEquals(1, listenerFixture.getEventsCount());
        assertNotNull(listenerFixture.getReceivedEvent(RowReorderEvent.class));
        assertEquals(4, natTableFixture.getRowIndexByPosition(1));
    }

    @Test
    public void reorderMultipleNotConsecutiveRowsInHiddenState() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        assertEquals(10, natTableFixture.getRowCount());

        // hide some columns
        natTableFixture.doCommand(
                new MultiRowHideCommand(natTableFixture, new int[] { 2, 5, 8 }));

        assertEquals(7, natTableFixture.getRowCount());

        List<Integer> rowToMove = Arrays.asList(3, 4, 6, 7);
        int destinationPosition = 0;
        natTableFixture.doCommand(
                new MultiRowReorderCommand(underlyingLayer.getRowReorderLayer(), rowToMove, destinationPosition));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertTrue(StructuralChangeEventHelper.isReorder(event.getRowDiffs()));

        assertEquals(7, natTableFixture.getRowCount());

        assertEquals(0, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(3));
        assertEquals(1, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(4));
        assertEquals(2, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(6));
        assertEquals(3, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(7));
        assertEquals(4, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(0));
        assertEquals(5, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(1));
        assertEquals(6, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(2));
        assertEquals(7, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(5));
        assertEquals(8, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(8));
        assertEquals(9, underlyingLayer.getRowReorderLayer().getRowPositionByIndex(9));
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderNone() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new RowReorderCommand(natTableFixture, 3, 3));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(3, range.start);
        assertEquals(4, range.end);

        assertEquals(3, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderUp() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new RowReorderCommand(natTableFixture, 3, 0));

        // verify reorder
        RowReorderLayer columnReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(3, columnReorderLayer.getRowIndexByPosition(0));
        assertEquals(0, columnReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, columnReorderLayer.getRowIndexByPosition(2));
        assertEquals(2, columnReorderLayer.getRowIndexByPosition(3));
        assertEquals(4, columnReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(3, range.start);
        assertEquals(4, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderDown() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new RowReorderCommand(natTableFixture, 1, 4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(1, range.start);
        assertEquals(2, range.end);

        assertEquals(4, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderUp() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(3, 4), 0));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(3, range.start);
        assertEquals(5, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderDown() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(1, 2), 4));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(1, range.start);
        assertEquals(3, range.end);

        assertEquals(4, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiNonConsecutiveReorderUp() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(3, 5), 0));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(5, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(4));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(5));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(2, event.getBeforeFromRowPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromRowPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(3, range.start);
        assertEquals(4, range.end);
        range = iterator.next();
        assertEquals(5, range.start);
        assertEquals(6, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiNonConsecutiveReorderDown() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(1, 3), 4));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(2, event.getBeforeFromRowPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromRowPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(1, range.start);
        assertEquals(2, range.end);
        range = iterator.next();
        assertEquals(3, range.start);
        assertEquals(4, range.end);

        assertEquals(4, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderNoneWithHidden() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new RowReorderCommand(natTableFixture, 2, 2));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(2, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderUpWithHidden() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new RowReorderCommand(natTableFixture, 2, 0));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderUpWithMultiHidden() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 2, 3));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new RowReorderCommand(natTableFixture, 2, 0));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnReorderDownWithHidden() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new RowReorderCommand(natTableFixture, 2, 3));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(3, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderUpWithHidden() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 2, 5));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(2, 3), 0));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(4));
        assertEquals(5, rowReorderLayer.getRowIndexByPosition(5));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromRowPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(2, range.start);
        assertEquals(4, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderUpWithHiddenBetween() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(2, 3), 0));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(5, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(4));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(5));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromRowPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(2, range.start);
        assertEquals(4, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderDownWithHidden() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 3));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(0, 1), 3));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(0, range.start);
        assertEquals(2, range.end);

        assertEquals(3, event.getBeforeToRowPosition());

    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderDownWithHiddenBetween() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 0, 2, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(0, 1), 3));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(5, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(4));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(5));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(0, range.start);
        assertEquals(2, range.end);

        assertEquals(3, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderDownWithHiddenAround() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 0, 3));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(0, 1), 3));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(0, range.start);
        assertEquals(2, range.end);

        assertEquals(3, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiReorderUpWithHiddenAround() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 1, 4));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(1, 2), 0));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(4));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(1, event.getBeforeFromRowPositionRanges().size());
        Range range = event.getBeforeFromRowPositionRanges().iterator().next();
        assertEquals(1, range.start);
        assertEquals(3, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiNonConsecutiveReorderUpWithHidden() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 2, 5));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(2, 4), 0));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(6, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(4));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(5));
        assertEquals(5, rowReorderLayer.getRowIndexByPosition(6));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(2, event.getBeforeFromRowPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromRowPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);
        range = iterator.next();
        assertEquals(4, range.start);
        assertEquals(5, range.end);

        assertEquals(0, event.getBeforeToRowPosition());
    }

    @Test
    public void shouldConvertBeforePositionsOnMultiNonConsecutiveReorderDownWithHidden() {
        RowReorderBodyLayerStack underlyingLayer =
                new RowReorderBodyLayerStack(new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // hide columns
        natTableFixture.doCommand(new MultiRowHideCommand(natTableFixture, 2, 5));

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        // trigger reorder
        natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, Arrays.asList(0, 2), 4));

        // verify reorder
        RowReorderLayer rowReorderLayer = underlyingLayer.getRowReorderLayer();
        assertEquals(1, rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(2, rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(4, rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(5, rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(0, rowReorderLayer.getRowIndexByPosition(4));
        assertEquals(3, rowReorderLayer.getRowIndexByPosition(5));
        assertEquals(6, rowReorderLayer.getRowIndexByPosition(6));

        // verify the event
        RowReorderEvent event = (RowReorderEvent) listenerFixture.getReceivedEvent(RowReorderEvent.class);
        assertNotNull(event);

        assertEquals(2, event.getBeforeFromRowPositionRanges().size());
        Iterator<Range> iterator = event.getBeforeFromRowPositionRanges().iterator();
        Range range = iterator.next();
        assertEquals(0, range.start);
        assertEquals(1, range.end);
        range = iterator.next();
        assertEquals(2, range.start);
        assertEquals(3, range.end);

        assertEquals(4, event.getBeforeToRowPosition());
    }

    class RowReorderBodyLayerStack extends AbstractIndexLayerTransform {

        private final RowReorderLayer rowReorderLayer;
        private final RowHideShowLayer rowHideShowLayer;
        private final SelectionLayer selectionLayer;
        private final ViewportLayer viewportLayer;

        public RowReorderBodyLayerStack(IUniqueIndexLayer underlyingLayer) {
            this.rowReorderLayer = new RowReorderLayer(underlyingLayer);
            this.rowHideShowLayer = new RowHideShowLayer(this.rowReorderLayer);
            this.selectionLayer = new SelectionLayer(this.rowHideShowLayer);
            this.viewportLayer = new ViewportLayer(this.selectionLayer);
            setUnderlyingLayer(this.viewportLayer);

            registerCommandHandler(new CopyDataCommandHandler(this.selectionLayer));
        }

        @Override
        public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
            super.setClientAreaProvider(clientAreaProvider);
        }

        public RowReorderLayer getRowReorderLayer() {
            return this.rowReorderLayer;
        }

        public RowHideShowLayer getRowHideShowLayer() {
            return this.rowHideShowLayer;
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public ViewportLayer getViewportLayer() {
            return this.viewportLayer;
        }

    }
}
