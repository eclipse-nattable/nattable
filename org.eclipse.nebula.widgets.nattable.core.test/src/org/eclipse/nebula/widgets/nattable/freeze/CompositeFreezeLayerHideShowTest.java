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
package org.eclipse.nebula.widgets.nattable.freeze;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeRowCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.UnFreezeGridCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class CompositeFreezeLayerHideShowTest {

    private ColumnReorderLayer reorderLayer;
    private RowHideShowLayer rowHideShowLayer;
    private ColumnHideShowLayer columnHideShowLayer;
    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;
    private FreezeLayer freezeLayer;
    private CompositeFreezeLayer compositeFreezeLayer;

    @Before
    public void setup() {
        reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(5, 5));
        rowHideShowLayer = new RowHideShowLayer(reorderLayer);
        columnHideShowLayer = new ColumnHideShowLayer(rowHideShowLayer);
        selectionLayer = new SelectionLayer(columnHideShowLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        freezeLayer = new FreezeLayer(selectionLayer);

        compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer,
                viewportLayer, selectionLayer);
        compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 600, 150);
            }

        });
    }

    @Test
    public void testNotFrozen() {
        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());
    }

    // Freeze

    @Test
    public void testFreezeAllColumns() {
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 4));

        assertEquals(5, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(4, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(-1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(500, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeAllRows() {
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 4));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(5, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(4, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(0, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(100, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeColumns() {
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeRows() {
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    // Column hide/show

    @Test
    public void testFreezeHideShowColumnFrozenRegion() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new ColumnHideCommand(
                compositeFreezeLayer, 0));

        assertEquals(1, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(0, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnFrozenRegionEdge() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new ColumnHideCommand(
                compositeFreezeLayer, 1));

        assertEquals(1, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(0, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again - since edge is shown again the frozen region is not
        // extended
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(0, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(4, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnAllFrozenRegion() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiColumnHideCommand(
                compositeFreezeLayer, new int[] { 0, 1 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnViewportRegion() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new ColumnHideCommand(
                compositeFreezeLayer, 3));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnViewportRegionEdge() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new ColumnHideCommand(
                compositeFreezeLayer, 2));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnAllViewportRegion() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiColumnHideCommand(
                compositeFreezeLayer, new int[] { 2, 3, 4 }));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegions() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiColumnHideCommand(
                compositeFreezeLayer, new int[] { 0, 3 }));

        assertEquals(1, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(0, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegionsEdge() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiColumnHideCommand(
                compositeFreezeLayer, new int[] { 1, 2 }));

        assertEquals(1, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(0, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(0, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(4, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnFrozenRegionMiddle() {
        // freeze the first 4 columns
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 3));

        assertEquals(4, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(3, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(1, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(4, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(400, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide column 1 and 2
        compositeFreezeLayer.doCommand(new MultiColumnHideCommand(
                compositeFreezeLayer, new int[] { 1, 2 }));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(1, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(4, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(3, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(1, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(4, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(400, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegionsViewportAll() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiColumnHideCommand(
                compositeFreezeLayer, new int[] { 1, 2, 3, 4 }));

        assertEquals(1, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(0, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(-1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(0, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(4, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(-1, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegionsFreezeAll() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiColumnHideCommand(
                compositeFreezeLayer, new int[] { 0, 1, 2 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegionsAll() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                compositeFreezeLayer, 1));

        assertEquals(2, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiColumnHideCommand(
                compositeFreezeLayer, new int[] { 0, 1, 2, 3, 4 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    // Row hide/show

    @Test
    public void testFreezeHideShowRowFrozenRegion() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new RowHideCommand(compositeFreezeLayer,
                0));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(1, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowFrozenRegionEdge() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new RowHideCommand(compositeFreezeLayer,
                1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(1, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, viewportLayer.getMinimumOrigin().getY());

        // show again - since edge is shown again the frozen region is not
        // extended
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(1, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(4, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowAllFrozenRegion() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiRowHideCommand(
                compositeFreezeLayer, new int[] { 0, 1 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowViewportRegion() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new RowHideCommand(compositeFreezeLayer,
                3));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(2, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowViewportRegionEdge() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new RowHideCommand(compositeFreezeLayer,
                2));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(2, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowAllViewportRegion() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiRowHideCommand(
                compositeFreezeLayer, new int[] { 2, 3, 4 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(0, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegions() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiRowHideCommand(
                compositeFreezeLayer, new int[] { 0, 3 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(1, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(2, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegionsEdge() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiRowHideCommand(
                compositeFreezeLayer, new int[] { 1, 2 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(1, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(2, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(1, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(4, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowFrozenRegionMiddle() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 3));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(4, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(1, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(4, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(80, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiRowHideCommand(
                compositeFreezeLayer, new int[] { 1, 2 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(1, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(4, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(1, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(4, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(80, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegionsViewportAll() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiRowHideCommand(
                compositeFreezeLayer, new int[] { 1, 2, 3, 4 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(1, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(0, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(1, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(4, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegionsFreezeAll() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiRowHideCommand(
                compositeFreezeLayer, new int[] { 0, 1, 2 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(2, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegionsAll() {
        // freeze
        compositeFreezeLayer.doCommand(new FreezeRowCommand(
                compositeFreezeLayer, 1));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(2, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(3, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, viewportLayer.getMinimumOrigin().getY());

        // hide
        compositeFreezeLayer.doCommand(new MultiRowHideCommand(
                compositeFreezeLayer, new int[] { 0, 1, 2, 3, 4 }));

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(0, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        // show again
        compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    private void reset() {
        compositeFreezeLayer.doCommand(new UnFreezeGridCommand());

        assertEquals(0, freezeLayer.getColumnCount());
        assertEquals(0, freezeLayer.getRowCount());
        assertEquals(-1, freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, viewportLayer.getColumnCount());
        assertEquals(5, viewportLayer.getRowCount());
        assertEquals(0, viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, viewportLayer.getMinimumOrigin().getY());
    }
}
