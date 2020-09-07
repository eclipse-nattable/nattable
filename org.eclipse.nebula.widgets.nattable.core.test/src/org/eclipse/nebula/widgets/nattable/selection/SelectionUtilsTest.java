/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST, Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
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

    @Test
    public void testGetEmptyListOnNoSelection() {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider =
                new ListDataProvider<RowDataFixture>(
                        listFixture,
                        new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                                RowDataListFixture.getPropertyNames()));

        DataLayer dataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        List<RowDataFixture> selected = SelectionUtils.getSelectedRowObjects(
                selectionLayer,
                bodyDataProvider,
                false);

        assertNotNull(selected);
        assertEquals(0, selected.size());
    }

    @Test
    public void testGetSingleItemOnCellSelection() {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider =
                new ListDataProvider<RowDataFixture>(
                        listFixture,
                        new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                                RowDataListFixture.getPropertyNames()));

        DataLayer dataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        selectionLayer.selectCell(1, 3, false, false);

        List<RowDataFixture> selected = SelectionUtils.getSelectedRowObjects(
                selectionLayer,
                bodyDataProvider,
                false);

        assertNotNull(selected);
        assertEquals(1, selected.size());
        assertEquals(listFixture.get(3), selected.get(0));
    }

    @Test
    public void testGetMultipleItemsOnCellSelection() {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider =
                new ListDataProvider<RowDataFixture>(
                        listFixture,
                        new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                                RowDataListFixture.getPropertyNames()));

        DataLayer dataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        selectionLayer.selectCell(1, 3, false, true);
        selectionLayer.selectCell(1, 5, false, true);

        List<RowDataFixture> selected = SelectionUtils.getSelectedRowObjects(
                selectionLayer,
                bodyDataProvider,
                false);

        assertNotNull(selected);
        assertEquals(2, selected.size());
        assertEquals(listFixture.get(3), selected.get(0));
        assertEquals(listFixture.get(5), selected.get(1));
    }

    @Test
    public void testGetSingleItemsOnFullRowSelection() {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider =
                new ListDataProvider<RowDataFixture>(
                        listFixture,
                        new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                                RowDataListFixture.getPropertyNames()));

        DataLayer dataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        selectionLayer.selectRow(0, 3, false, false);

        List<RowDataFixture> selected = SelectionUtils.getSelectedRowObjects(
                selectionLayer,
                bodyDataProvider,
                true);

        assertNotNull(selected);
        assertEquals(1, selected.size());
        assertEquals(listFixture.get(3), selected.get(0));
    }

    @Test
    public void testGetMultipleItemsOnFullRowSelection() {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider =
                new ListDataProvider<RowDataFixture>(
                        listFixture,
                        new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                                RowDataListFixture.getPropertyNames()));

        DataLayer dataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        selectionLayer.selectRow(0, 3, false, true);
        selectionLayer.selectRow(0, 5, false, true);

        List<RowDataFixture> selected = SelectionUtils.getSelectedRowObjects(
                selectionLayer,
                bodyDataProvider,
                true);

        assertNotNull(selected);
        assertEquals(2, selected.size());
        assertEquals(listFixture.get(3), selected.get(0));
        assertEquals(listFixture.get(5), selected.get(1));
    }

    @Test
    public void testGetEmptyListOnCellSelectionForFullRowSelection() {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider =
                new ListDataProvider<RowDataFixture>(
                        listFixture,
                        new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                                RowDataListFixture.getPropertyNames()));

        DataLayer dataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(dataLayer);

        selectionLayer.selectCell(1, 3, false, false);

        List<RowDataFixture> selected = SelectionUtils.getSelectedRowObjects(
                selectionLayer,
                bodyDataProvider,
                true);

        assertNotNull(selected);
        assertEquals(0, selected.size());
    }
}
