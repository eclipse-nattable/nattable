/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.CompositeLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ViewportLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

/**
 * @see org.eclipse.nebula.widgets.nattable.test.fixture.layer.CompositeLayerFixture
 *      for the layout of columns/rows.
 */
public class CompositeLayerTest {

    private CompositeLayerFixture layerFixture;

    @Before
    public void setup() {
        this.layerFixture = new CompositeLayerFixture();
        this.layerFixture.bodyLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 160, 100);
            }
        });
    }

    @Test
    public void testingChildLayerInfoForCornerByLayout() {
        ILayer childLayer = this.layerFixture.getChildLayerByLayoutCoordinate(0, 0);

        assertEquals(this.layerFixture.cornerLayer, childLayer);
        assertEquals(0, this.layerFixture.getColumnPositionOffset(0));
        assertEquals(0, this.layerFixture.getRowPositionOffset(0));
        assertEquals(0, this.layerFixture.getWidthOffset(0));
        assertEquals(0, this.layerFixture.getHeightOffset(0));

    }

    @Test
    public void testChildLayerInfoForViewportByLayout() {
        ILayer childLayer = this.layerFixture.getChildLayerByLayoutCoordinate(1, 1);

        assertEquals(this.layerFixture.bodyLayer, childLayer);
        assertEquals(5, this.layerFixture.getColumnPositionOffset(1));
        assertEquals(7, this.layerFixture.getRowPositionOffset(1));
        assertEquals(this.layerFixture.cornerLayer.getWidth(),
                this.layerFixture.getWidthOffset(1));
        assertEquals(this.layerFixture.cornerLayer.getHeight(),
                this.layerFixture.getHeightOffset(1));
    }

    @Test
    public void childLayerInfoByPixelPosition() throws Exception {
        Point layoutCoordinate = this.layerFixture.getLayoutXYByPixelXY(30, 40);
        ILayer layer = this.layerFixture.getChildLayerByLayoutCoordinate(
                layoutCoordinate.x, layoutCoordinate.y);
        assertEquals(this.layerFixture.bodyLayer, layer);
    }

    @Test
    public void getChildLayerByLayoutPosition() throws Exception {
        assertIsCorner(this.layerFixture.getChildLayerByLayoutCoordinate(0, 0));
        assertIsColHeader(this.layerFixture.getChildLayerByLayoutCoordinate(1, 0));
        assertIsRowHeader(this.layerFixture.getChildLayerByLayoutCoordinate(0, 1));
        assertIsBody(this.layerFixture.getChildLayerByLayoutCoordinate(1, 1));
    }

    /*
     * Column specific tests
     */

    @Test
    public void getColumnCount() throws Exception {
        assertEquals(10, this.layerFixture.getColumnCount());
    }

    @Test
    public void getColumnIndexByPosition() throws Exception {
        assertEquals(0, this.layerFixture.getColumnIndexByPosition(0));
        assertEquals(1, this.layerFixture.getColumnIndexByPosition(1));
        assertEquals(4, this.layerFixture.getColumnIndexByPosition(4));

        assertEquals(0, this.layerFixture.getColumnIndexByPosition(5));
        assertEquals(1, this.layerFixture.getColumnIndexByPosition(6));
        // Non existent col position
        assertEquals(-1, this.layerFixture.getColumnIndexByPosition(12));
    }

    @Test
    public void getWidth() throws Exception {
        assertEquals(150, this.layerFixture.getWidth());

        // 20 columns total - 100 wide each
        this.layerFixture.setChildLayer(GridRegion.CORNER, new DataLayerFixture(10,
                10, 100, 20), 0, 0);
        this.layerFixture.setChildLayer(GridRegion.COLUMN_HEADER,
                new DataLayerFixture(10, 10, 100, 20), 1, 0);

        assertEquals(2000, this.layerFixture.getWidth());
    }

    @Test
    public void getColumnWidthByPosition() throws Exception {
        assertEquals(5, this.layerFixture.getColumnWidthByPosition(0));
        assertEquals(5, this.layerFixture.getColumnWidthByPosition(4));
        assertEquals(25, this.layerFixture.getColumnWidthByPosition(5));
        assertEquals(25, this.layerFixture.getColumnWidthByPosition(8));
        // Non existent
        assertEquals(0, this.layerFixture.getColumnWidthByPosition(15));
    }

    @Test
    public void isColumnPositionResizable() throws Exception {
        assertTrue(this.layerFixture.isColumnPositionResizable(5));
        // Non existent
        assertFalse(this.layerFixture.isColumnPositionResizable(15));

        this.layerFixture.colHeaderLayer.setColumnPositionResizable(0, false);
        assertFalse(this.layerFixture.isColumnPositionResizable(5));
    }

    @Test
    public void getColumnPositionByX() throws Exception {
        assertEquals(0, this.layerFixture.getColumnPositionByX(0));
        assertEquals(0, this.layerFixture.getColumnPositionByX(4));
        assertEquals(1, this.layerFixture.getColumnPositionByX(5));
        assertEquals(1, this.layerFixture.getColumnPositionByX(9));
        assertEquals(2, this.layerFixture.getColumnPositionByX(10));
        // Non existent
        assertEquals(-1, this.layerFixture.getColumnPositionByX(200));
    }

    @Test
    public void getStartXOfColumnPosition() throws Exception {
        assertEquals(0, this.layerFixture.getStartXOfColumnPosition(0));
        assertEquals(5, this.layerFixture.getStartXOfColumnPosition(1));
        // Non existent
        assertEquals(-1, this.layerFixture.getStartXOfColumnPosition(12));
    }

    /*
     * Row specific tests
     */
    @Test
    public void getRowCount() throws Exception {
        assertEquals(14, this.layerFixture.getRowCount());
    }

    @Test
    public void getRowIndexByPosition() throws Exception {
        assertEquals(0, this.layerFixture.getRowIndexByPosition(0));
        assertEquals(1, this.layerFixture.getRowIndexByPosition(1));
        assertEquals(4, this.layerFixture.getRowIndexByPosition(4));

        assertEquals(0, this.layerFixture.getRowIndexByPosition(7));
        assertEquals(1, this.layerFixture.getRowIndexByPosition(8));
        // Non existent
        assertEquals(-1, this.layerFixture.getRowIndexByPosition(20));
    }

    @Test
    public void getHeight() throws Exception {
        assertEquals(70, this.layerFixture.getHeight());

        // 20 rows, each 20 high
        this.layerFixture.setChildLayer(GridRegion.CORNER, new DataLayerFixture(10,
                10, 100, 20), 0, 0);
        this.layerFixture.setChildLayer(GridRegion.ROW_HEADER, new DataLayerFixture(
                10, 10, 100, 20), 0, 1);

        assertEquals(400, this.layerFixture.getHeight());
    }

    @Test
    public void getRowHeightByPosition() throws Exception {
        assertEquals(5, this.layerFixture.getRowHeightByPosition(0));
        assertEquals(5, this.layerFixture.getRowHeightByPosition(4));
        assertEquals(5, this.layerFixture.getRowHeightByPosition(5));
        assertEquals(5, this.layerFixture.getRowHeightByPosition(8));
        // Non existent
        assertEquals(0, this.layerFixture.getRowHeightByPosition(20));
    }

    @Test
    public void isRowPositionResizable() throws Exception {
        assertTrue(this.layerFixture.isRowPositionResizable(7));
        // Non existent
        assertFalse(this.layerFixture.isRowPositionResizable(20));

        this.layerFixture.rowHeaderLayer.setRowPositionResizable(0, false);
        assertFalse(this.layerFixture.isRowPositionResizable(7));
    }

    @Test
    public void getRowPositionByY() throws Exception {
        assertEquals(0, this.layerFixture.getRowPositionByY(0));
        assertEquals(0, this.layerFixture.getRowPositionByY(4));
        assertEquals(1, this.layerFixture.getRowPositionByY(5));
        assertEquals(1, this.layerFixture.getRowPositionByY(9));
        assertEquals(2, this.layerFixture.getRowPositionByY(10));
        // Non existent
        assertEquals(-1, this.layerFixture.getRowPositionByY(200));
    }

    @Test
    public void getStartYOfRowPosition() throws Exception {
        assertEquals(0, this.layerFixture.getStartYOfRowPosition(0));
        assertEquals(5, this.layerFixture.getStartYOfRowPosition(1));
        assertEquals(50, this.layerFixture.getStartYOfRowPosition(10));
        // Non existent
        assertEquals(-1, this.layerFixture.getStartYOfRowPosition(20));
    }

    @Test
    public void getCellBounds() throws Exception {
        Rectangle cellBounds = this.layerFixture.getBoundsByPosition(0, 0);
        assertEquals(0, cellBounds.x);
        assertEquals(0, cellBounds.y);
        assertEquals(5, cellBounds.height);
        assertEquals(5, cellBounds.width);

        cellBounds = this.layerFixture.getBoundsByPosition(6, 6);
        assertEquals(50, cellBounds.x);
        assertEquals(30, cellBounds.y);
        assertEquals(5, cellBounds.height);
        assertEquals(25, cellBounds.width);
    }

    @Test
    public void cellBoundsForNonExistentCellPosition() throws Exception {
        Rectangle cellBounds = this.layerFixture.getBoundsByPosition(20, 20);
        assertNull(cellBounds);
    }

    @Test
    public void getDataValueByPosition() throws Exception {
        assertEquals("[0, 0]", this.layerFixture.getDataValueByPosition(0, 0)
                .toString());
        assertEquals("[0, 1]", this.layerFixture.getDataValueByPosition(0, 8)
                .toString());
        assertEquals("[3, 0]", this.layerFixture.getDataValueByPosition(8, 0)
                .toString());
    }

    @Test
    public void getDataValueForPositionNotInTheViewport() throws Exception {
        assertNull(this.layerFixture.getDataValueByPosition(12, 8));
    }

    @Test
    public void getUnderlyingLayersByColumnPosition() throws Exception {
        Collection<ILayer> underlyingLayers = this.layerFixture
                .getUnderlyingLayersByColumnPosition(5);

        assertEquals(2, underlyingLayers.size());

        List<String> classNames = new ArrayList<String>();
        for (Iterator<ILayer> iterator = underlyingLayers.iterator(); iterator
                .hasNext();) {
            ILayer iLayer = iterator.next();
            classNames.add(iLayer.getClass().getSimpleName());
        }
        assertTrue(classNames.contains("DataLayerFixture"));
        assertTrue(classNames.contains("ViewportLayerFixture"));
    }

    @Test
    public void getCellByPosition() throws Exception {
        ILayerCell cell = this.layerFixture.getCellByPosition(3, 2);
        assertIsCorner(cell.getLayer());
        assertEquals("[3, 2]", cell.getDataValue());
        assertEquals(new Rectangle(15, 10, 5, 5), cell.getBounds()); // pixel
                                                                     // values
        assertEquals(DisplayMode.NORMAL, cell.getDisplayMode());

        // Get a cell from the body
        cell = this.layerFixture.getCellByPosition(8, 8);
        assertTrue(cell.getLayer() instanceof CompositeLayer);
        assertEquals("[3, 1]", cell.getDataValue());
        assertEquals(new Rectangle(310, 75, 100, 70), cell.getBounds());
        assertEquals(DisplayMode.NORMAL, cell.getDisplayMode());
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(8, cell.getOriginRowPosition());
    }

    @Test
    public void getBoundsByPosition() throws Exception {
        Rectangle rowHeaderCellBounds = this.layerFixture.getBoundsByPosition(1, 10);
        assertEquals(new Rectangle(10, 50, 10, 5), rowHeaderCellBounds);
    }

    /*
     * The following methods probe the underlying DataLayerFixture to ensure
     * that we got the right one.
     */
    private void assertIsBody(ILayer bodyLayer) {
        assertTrue(bodyLayer instanceof ViewportLayerFixture);
    }

    private void assertIsRowHeader(ILayer rowHeaderLayer) {
        assertEquals(10, rowHeaderLayer.getColumnWidthByPosition(0));
    }

    private void assertIsColHeader(ILayer colHeaderLayer) {
        assertEquals(25, colHeaderLayer.getColumnWidthByPosition(0));
    }

    private void assertIsCorner(ILayer cornerLayer) {
        assertEquals(5, cornerLayer.getColumnWidthByPosition(0));
    }
}
