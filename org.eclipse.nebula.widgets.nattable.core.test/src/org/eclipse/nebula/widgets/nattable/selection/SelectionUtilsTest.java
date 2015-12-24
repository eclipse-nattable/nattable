/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.DataProviderFixture;
import org.junit.Test;

public class SelectionUtilsTest {

    @Test
    public void testConsecutivePositions() {
        int[] test = new int[] { 1, 2, 3, 4, 5 };
        assertTrue(SelectionUtils.isConsecutive(test));
    }

    @Test
    public void testDuplicateConsecutivePositions() {
        int[] test = new int[] { 1, 1, 2, 3, 4, 5 };
        assertFalse(SelectionUtils.isConsecutive(test));
    }

    @Test
    public void testGapConsecutivePositions() {
        int[] test = new int[] { 1, 3, 4, 5 };
        assertFalse(SelectionUtils.isConsecutive(test));
    }

    @Test
    public void testEmptyArray() {
        int[] test = new int[] {};
        assertTrue(SelectionUtils.isConsecutive(test));
    }

    @Test
    public void testOneEntryArray() {
        int[] test = new int[] { 42 };
        assertTrue(SelectionUtils.isConsecutive(test));
    }

    @Test
    public void testGetBottomRightSelectAll() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        // select all cells
        selectionLayer.selectAll();

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertEquals(9, bottomRight.getColumnPosition());
        assertEquals(9, bottomRight.getRowPosition());
    }

    @Test
    public void testGetBottomRightSelectOne() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        // select one cell
        selectionLayer.selectCell(5, 5, false, false);

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertEquals(5, bottomRight.getColumnPosition());
        assertEquals(5, bottomRight.getRowPosition());
    }

    @Test
    public void testGetBottomRightSelectNothing() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertNull(bottomRight);
    }

    @Test
    public void testGetBottomRightSelectRegion() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        // select region
        selectionLayer.selectRegion(2, 2, 3, 3);

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertEquals(4, bottomRight.getColumnPosition());
        assertEquals(4, bottomRight.getRowPosition());
    }

    @Test
    public void testGetBottomRightSelectRegionDeselectMiddle() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        // select region
        selectionLayer.selectRegion(2, 2, 3, 3);

        // deselect a cell
        selectionLayer.clearSelection(3, 3);

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertNull(bottomRight);
    }

    @Test
    public void testGetBottomRightSelectRegionDeselectTopEdge() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        // select region
        selectionLayer.selectRegion(2, 2, 3, 3);

        // deselect a cell
        selectionLayer.clearSelection(2, 2);

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertNull(bottomRight);
    }

    @Test
    public void testGetBottomRightSelectRegionDeselectMiddleBottomEdge() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        // select region
        selectionLayer.selectRegion(2, 2, 3, 3);

        // deselect a cell
        selectionLayer.clearSelection(4, 4);

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertNull(bottomRight);
    }

    @Test
    public void testGetBottomRightSelectDifferentRows() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        // select cells for same columns in non consecutive rows
        selectionLayer.selectRegion(2, 2, 3, 1);
        selectionLayer.selectRegion(4, 4, 3, 1);

        assertEquals(6, selectionLayer.getSelectedCells().size());

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertNull(bottomRight);
    }

    @Test
    public void testGetBottomRightSelectDifferentColumns() {
        DataLayer dataLayer = new DataLayer(new DataProviderFixture(10, 10));
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        // select cells for same columns in non consecutive rows
        selectionLayer.selectRegion(2, 2, 1, 3);
        selectionLayer.selectRegion(4, 4, 1, 3);

        assertEquals(6, selectionLayer.getSelectedCells().size());

        ILayerCell bottomRight = SelectionUtils.getBottomRightCellInSelection(selectionLayer);
        assertNull(bottomRight);
    }

}
