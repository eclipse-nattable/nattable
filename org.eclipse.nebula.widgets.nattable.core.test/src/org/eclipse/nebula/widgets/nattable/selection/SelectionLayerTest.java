/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - Test delegation of markers to
 *         model iff model is an IMarkerSelectionModel. Test getters and setters
 *         for marker fields.
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 446275
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.test.LayerAssert;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class SelectionLayerTest {

    private TestLayer testLayer;
    private TestSelectionLayer selectionLayer;

    @Before
    public void setup() {
        String columnInfo = "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100";
        String rowInfo = "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40";

        String cellInfo = "A0 | <  | C0 | D0 \n" + "^  | <  | C1 | D1 \n"
                + "A2 | B2 | C2 | D2 \n" + "A3 | B3 | C3 | D3 \n";

        this.testLayer = new TestLayer(4, 4, columnInfo, rowInfo, cellInfo);

        this.selectionLayer = new TestSelectionLayer(this.testLayer);
    }

    @Test
    public void testIdentityLayerTransform() {
        LayerAssert.assertLayerEquals(this.testLayer, this.selectionLayer);
    }

    // Clear

    @Test
    public void testClearAllClearsAllMarkers() {
        this.selectionLayer.selectAll();

        this.selectionLayer.clear();

        assertNull(this.selectionLayer.getLastSelectedCellPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedRegion().width);
        assertEquals(0, this.selectionLayer.getLastSelectedRegion().height);

        assertEquals(SelectionLayer.NO_SELECTION,
                this.selectionLayer.getSelectionAnchor().columnPosition);
        assertEquals(SelectionLayer.NO_SELECTION,
                this.selectionLayer.getSelectionAnchor().rowPosition);
    }

    @Test
    public void testClearSingleCellClearsNoMarkers() {
        this.selectionLayer.selectAll();

        this.selectionLayer.clearSelection(1, 1);

        assertNotNull(this.selectionLayer.getLastSelectedCellPosition());
        assertTrue(this.selectionLayer.getLastSelectedRegion().width > 0);
        assertTrue(this.selectionLayer.getLastSelectedRegion().height > 0);

        assertFalse(this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION);
        assertFalse(this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION);
    }

    @Test
    public void testClearAnchorRectangleClearsOnlyAnchor() {
        this.selectionLayer.selectAll();

        this.selectionLayer.clearSelection(new Rectangle(0, 0, 1, 1));

        // if the cleared selection contains the last selected cell, it also
        // needs to be cleared
        assertNull(this.selectionLayer.getLastSelectedCellPosition());
        assertTrue(this.selectionLayer.getLastSelectedRegion().width > 0);
        assertTrue(this.selectionLayer.getLastSelectedRegion().height > 0);

        assertEquals(SelectionLayer.NO_SELECTION,
                this.selectionLayer.getSelectionAnchor().columnPosition);
        assertEquals(SelectionLayer.NO_SELECTION,
                this.selectionLayer.getSelectionAnchor().rowPosition);
    }

    @Test
    public void testClearOutsideAnchorRectangleClearsNoMarkers() {
        this.selectionLayer.selectAll();

        this.selectionLayer.clearSelection(new Rectangle(1, 1, 1, 1));

        assertFalse(this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION);
        assertFalse(this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION);
    }

    @Test
    public void testClearSingleSelectedCellClearsAllMarkers() {
        this.selectionLayer.selectCell(3, 3, false, false);

        assertNotNull(this.selectionLayer.getLastSelectedCellPosition());
        assertTrue(this.selectionLayer.getLastSelectedRegion().width > 0);
        assertTrue(this.selectionLayer.getLastSelectedRegion().height > 0);
        assertFalse(this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION);
        assertFalse(this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION);

        this.selectionLayer.clearSelection(new Rectangle(3, 3, 1, 1));

        assertNull(this.selectionLayer.getLastSelectedCellPosition());

        assertTrue(this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION);
        assertTrue(this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION);

        this.selectionLayer.selectCell(3, 3, false, false);

        assertNotNull(this.selectionLayer.getLastSelectedCellPosition());
        assertTrue(this.selectionLayer.getLastSelectedRegion().width > 0);
        assertTrue(this.selectionLayer.getLastSelectedRegion().height > 0);
        assertFalse(this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION);
        assertFalse(this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION);

        this.selectionLayer.clearSelection(3, 3);

        assertNull(this.selectionLayer.getLastSelectedCellPosition());

        assertTrue(this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION);
        assertTrue(this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION);
    }

    // Last Selected Region

    @Test
    public void testGetLastSelectedRegionDoesNotDelegateToModel() {
        Rectangle lastSelectedRegion = new Rectangle(22, 22, 22, 22);
        this.selectionLayer.lastSelectedRegion = lastSelectedRegion;

        assertSame(lastSelectedRegion, this.selectionLayer.getLastSelectedRegion());
    }

    @Test
    public void testSetLastSelectedRegionDoesNotDelegateToModel() {
        Rectangle region = new Rectangle(23454234, 123123, 12, 5);
        this.selectionLayer.setLastSelectedRegion(region);

        assertSame(region, this.selectionLayer.lastSelectedRegion);
    }

    @Test
    public void testSetLastSelectedRegionPreservesNULL() {
        this.selectionLayer.setLastSelectedRegion(null);

        assertNull(this.selectionLayer.lastSelectedRegion);
    }

    @Test
    public void testSetLastSelectedRegionFieldsDoesNotDelegateToModel() {
        this.selectionLayer.selectAll();

        Rectangle existingRegion = this.selectionLayer.lastSelectedRegion;

        Rectangle region = new Rectangle(23454234, 123123, 12, 5);
        this.selectionLayer.setLastSelectedRegion(region.x, region.y, region.width, region.height);

        assertEquals(region, this.selectionLayer.lastSelectedRegion);
        assertSame(existingRegion, this.selectionLayer.lastSelectedRegion);
    }

    // Selection Anchor

    @Test
    public void testGetAnchorDoesNotDelegateToModel() {
        PositionCoordinate existingAnchor = this.selectionLayer.selectionAnchor;

        assertSame(existingAnchor, this.selectionLayer.getSelectionAnchor());
    }

    @Test
    public void testSetSelectionAnchorDoesNotDelegateToModel() {
        this.selectionLayer.selectAll();

        this.selectionLayer.setSelectionAnchor(456, 8);

        assertEquals(456, this.selectionLayer.selectionAnchor.columnPosition);
        assertEquals(8, this.selectionLayer.selectionAnchor.rowPosition);
    }

    // Last Selected Cell

    @Test
    public void testSetLastSelectedCellDoesNotDelegateToModel() {
        this.selectionLayer.selectAll();

        this.selectionLayer.setLastSelectedCell(456, 8);

        assertEquals(456, this.selectionLayer.lastSelectedCell.columnPosition);
        assertEquals(8, this.selectionLayer.lastSelectedCell.rowPosition);
    }

    @Test
    public void testGetLastSelectedCellDoesNotDelegateToModel() {
        this.selectionLayer.selectAll();
        PositionCoordinate existingSelectedCell = this.selectionLayer.lastSelectedCell;

        assertSame(existingSelectedCell, this.selectionLayer.getLastSelectedCell());
    }

    @Test
    public void testGetLastSelectedCellPosition() {
        this.selectionLayer.selectAll();
        PositionCoordinate existingSelectedCell = this.selectionLayer.getLastSelectedCell();

        assertSame(existingSelectedCell, this.selectionLayer.getLastSelectedCellPosition());
        assertNotNull(existingSelectedCell);
    }

    @Test
    public void testGetLastSelectedCellPositionReturnsNullWhenUnselected() {
        assertNull(this.selectionLayer.getLastSelectedCellPosition());
    }

    // Register / unregister event handler

    @Test
    public void testIsModelEventHandlerRegistered() {
        assertEquals(1, this.selectionLayer.getEventHandler().size());

        ILayerEventHandler<? extends ILayerEvent> handler = this.selectionLayer.getEventHandler().get(IStructuralChangeEvent.class);

        assertSame(this.selectionLayer.getSelectionModel(), handler);
    }

    @Test
    public void testUnregisterModelEventHandler() {
        this.selectionLayer.unregisterEventHandler(this.selectionLayer.getSelectionModel());

        assertEquals(0, this.selectionLayer.getEventHandler().size());
    }

    /**
     * Simple SelectionLayer that makes the event handler accessible for
     * testing.
     */
    class TestSelectionLayer extends SelectionLayer {

        public TestSelectionLayer(IUniqueIndexLayer underlyingLayer) {
            super(underlyingLayer);
        }

        public Map<Class<? extends ILayerEvent>, ILayerEventHandler<? extends ILayerEvent>> getEventHandler() {
            return this.eventHandlers;
        }
    }
}
