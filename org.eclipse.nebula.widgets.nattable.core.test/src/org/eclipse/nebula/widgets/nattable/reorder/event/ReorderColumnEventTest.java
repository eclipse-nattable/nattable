/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
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
import java.util.List;

import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseColumnReorderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class ReorderColumnEventTest {
    private BaseColumnReorderLayerFixture columnReorderLayer;

    @Before
    public void setUp() {
        this.columnReorderLayer = new BaseColumnReorderLayerFixture(
                new DataLayerFixture());
    }

    @Test
    public void shouldThrowAReorderColumnEvent() {
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        this.columnReorderLayer.addLayerListener(listenerFixture);
        this.columnReorderLayer.reorderColumnPosition(3, 1);

        assertEquals(1, listenerFixture.getEventsCount());
        assertNotNull(listenerFixture
                .getReceivedEvent(ColumnReorderEvent.class));
    }

    /**
     * Fix for http://nattable.org/jira/browse/NTBL-476
     */
    @Test
    public void reorderEventMustPropagateToTheTop() throws Exception {
        DefaultBodyLayerStack underlyingLayer = new DefaultBodyLayerStack(
                new DataLayerFixture(20, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        assertEquals(6, natTableFixture.getColumnCount());
        assertEquals(1, natTableFixture.getColumnIndexByPosition(1));

        // Move to outside the visible range
        List<Integer> columnToMove = Arrays.asList(1, 2, 3);
        int destinationPosition = 10;
        natTableFixture.doCommand(new MultiColumnReorderCommand(
                natTableFixture, columnToMove, destinationPosition));

        // Ensure that the event propagates to the top
        assertEquals(1, listenerFixture.getEventsCount());
        assertNotNull(listenerFixture
                .getReceivedEvent(ColumnReorderEvent.class));
        assertEquals(4, natTableFixture.getColumnIndexByPosition(1));
    }

    @Test
    public void reorderMultipleNotConsecutiveColumnsInHiddenState() {
        DefaultBodyLayerStack underlyingLayer = new DefaultBodyLayerStack(
                new DataLayerFixture(10, 10, 100, 20));
        NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer,
                1000, 400, true);

        // Add listener
        LayerListenerFixture listenerFixture = new LayerListenerFixture();
        natTableFixture.addLayerListener(listenerFixture);

        assertEquals(10, natTableFixture.getColumnCount());

        // hide some columns
        natTableFixture.doCommand(new MultiColumnHideCommand(natTableFixture,
                new int[] { 2, 5, 8 }));

        assertEquals(7, natTableFixture.getColumnCount());

        List<Integer> columnToMove = Arrays.asList(3, 4, 6, 7);
        int destinationPosition = 0;
        natTableFixture.doCommand(new MultiColumnReorderCommand(underlyingLayer
                .getColumnReorderLayer(), columnToMove, destinationPosition));

        // verify the event
        ColumnReorderEvent event = (ColumnReorderEvent) listenerFixture
                .getReceivedEvent(ColumnReorderEvent.class);
        assertNotNull(event);

        assertTrue(StructuralChangeEventHelper
                .isReorder(event.getColumnDiffs()));

        assertEquals(7, natTableFixture.getColumnCount());

        assertEquals(0, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(3));
        assertEquals(1, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(4));
        assertEquals(2, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(6));
        assertEquals(3, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(7));
        assertEquals(4, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(0));
        assertEquals(5, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(1));
        assertEquals(6, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(2));
        assertEquals(7, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(5));
        assertEquals(8, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(8));
        assertEquals(9, underlyingLayer.getColumnReorderLayer()
                .getColumnPositionByIndex(9));
    }
}
