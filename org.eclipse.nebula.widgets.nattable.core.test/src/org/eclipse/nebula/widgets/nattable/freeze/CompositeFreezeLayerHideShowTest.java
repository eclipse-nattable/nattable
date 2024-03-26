/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeRowCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.UnFreezeGridCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowShowCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.FixedScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CompositeFreezeLayerHideShowTest {

    private int columnCount = 5;
    private int rowCount = 5;

    private IDataProvider testDataProvider = new IDataProvider() {

        @Override
        public int getColumnCount() {
            return CompositeFreezeLayerHideShowTest.this.columnCount;
        }

        @Override
        public int getRowCount() {
            return CompositeFreezeLayerHideShowTest.this.rowCount;
        }

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return "[col:" + columnIndex + ", row:" + rowIndex + "]";
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            // Do nothing
        }

    };

    private DataLayer dataLayer;
    private ColumnReorderLayer columnReorderLayer;
    private RowReorderLayer rowReorderLayer;
    private RowHideShowLayer rowHideShowLayer;
    private ColumnHideShowLayer columnHideShowLayer;
    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;
    private FreezeLayer freezeLayer;
    private CompositeFreezeLayer compositeFreezeLayer;

    @BeforeEach
    public void setup() {
        this.dataLayer = new DataLayer(this.testDataProvider);
        this.columnReorderLayer = new ColumnReorderLayer(this.dataLayer);
        this.rowReorderLayer = new RowReorderLayer(this.columnReorderLayer);
        this.rowHideShowLayer = new RowHideShowLayer(this.rowReorderLayer);
        this.columnHideShowLayer = new ColumnHideShowLayer(this.rowHideShowLayer);
        this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
        this.viewportLayer = new ViewportLayer(this.selectionLayer);
        this.freezeLayer = new FreezeLayer(this.selectionLayer);

        this.compositeFreezeLayer = new CompositeFreezeLayer(
                this.freezeLayer, this.viewportLayer, this.selectionLayer);
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 600, 150);
            }

        });

        this.compositeFreezeLayer.doCommand(
                new ConfigureScalingCommand(new FixedScalingDpiConverter(96)));
    }

    @Test
    public void testNotFrozen() {
        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());
    }

    // Freeze

    @Test
    public void testFreezeAllColumns() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 4));

        assertEquals(5, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(4, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(-1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(500, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeAllRows() {
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 4));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(5, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(4, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeColumns() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeRows() {
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    // Column hide/show

    @Test
    public void testFreezeHideShowColumnFrozenRegion() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 0));

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnFrozenRegionEdge() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 1));

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again - since edge is shown again the frozen region is not
        // extended
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(4, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnAllFrozenRegion() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, new int[] { 0, 1 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnViewportRegion() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 3));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnViewportRegionEdge() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 2));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnAllViewportRegion() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, new int[] { 2, 3, 4 }));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegions() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, new int[] { 0, 3 }));

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegionsEdge() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, new int[] { 1, 2 }));

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(4, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnFrozenRegionMiddle() {
        // freeze the first 4 columns
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 3));

        assertEquals(4, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(1, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(4, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(400, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide column 1 and 2
        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, new int[] { 1, 2 }));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(1, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(4, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(1, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(4, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(400, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegionsViewportAll() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, new int[] { 1, 2, 3, 4 }));

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(-1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(4, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(-1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegionsFreezeAll() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, new int[] { 0, 1, 2 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnBothRegionsAll() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, new int[] { 0, 1, 2, 3, 4 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(-1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(-1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowColumnPositionViewportRegionRightEdge() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide last column
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 4));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(2, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new MultiColumnShowCommand(Arrays.asList(4)));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideMultiColumnShowPositionInvalid() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 4));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 3));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 2));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 0));

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(-1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new MultiColumnShowCommand(Arrays.asList(0, 2, 3, 4, 5)));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    // Row hide/show

    @Test
    public void testFreezeHideShowRowFrozenRegion() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(new RowHideCommand(this.compositeFreezeLayer,
                0));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowFrozenRegionEdge() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(new RowHideCommand(this.compositeFreezeLayer,
                1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        // show again - since edge is shown again the frozen region is not
        // extended
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(4, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowAllFrozenRegion() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiRowHideCommand(this.compositeFreezeLayer, new int[] { 0, 1 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowViewportRegion() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(new RowHideCommand(this.compositeFreezeLayer,
                3));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(2, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowViewportRegionEdge() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new RowHideCommand(this.compositeFreezeLayer, 2));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(2, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowAllViewportRegion() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiRowHideCommand(this.compositeFreezeLayer, new int[] { 2, 3, 4 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegions() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiRowHideCommand(this.compositeFreezeLayer, new int[] { 0, 3 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(2, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegionsEdge() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiRowHideCommand(this.compositeFreezeLayer, new int[] { 1, 2 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(2, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(4, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowFrozenRegionMiddle() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 3));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(4, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(1, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(4, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(80, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiRowHideCommand(this.compositeFreezeLayer, new int[] { 1, 2 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(1, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(4, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(1, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(4, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(80, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegionsViewportAll() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiRowHideCommand(this.compositeFreezeLayer, new int[] { 1, 2, 3, 4 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(4, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegionsFreezeAll() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiRowHideCommand(this.compositeFreezeLayer, new int[] { 0, 1, 2 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(2, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeHideShowRowBothRegionsAll() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new MultiRowHideCommand(this.compositeFreezeLayer, new int[] { 0, 1, 2, 3, 4 }));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeDeleteRows() {
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 3));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(4, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(1, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(4, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(80, this.viewportLayer.getMinimumOrigin().getY());

        // fake a multi row delete with no details on which rows are deleted
        // technically the same as collapsing or filtering a GlazedLists
        this.rowCount = 2;
        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer, new ArrayList<Range>()));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(-1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // since the RowHideShowLayer is involved in the current composition
        // the number of rows is cached and we need to update this before reset
        this.rowCount = 5;
        this.dataLayer.fireLayerEvent(new RowInsertEvent(this.dataLayer, new Range(0, 2)));
        reset();
    }

    @Test
    public void testFreezeHideShowRowPositionViewportRegionBottomEdge() {
        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // hide
        this.compositeFreezeLayer.doCommand(
                new RowHideCommand(this.compositeFreezeLayer, 4));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(2, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new MultiRowShowCommand(Arrays.asList(4)));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeColumnDynamicScale() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        this.compositeFreezeLayer.doCommand(
                new ConfigureScalingCommand(new FixedScalingDpiConverter(144)));

        assertEquals(300, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testFreezeRowDynamicScale() {
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 1));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(40, this.viewportLayer.getMinimumOrigin().getY());

        this.compositeFreezeLayer.doCommand(
                new ConfigureScalingCommand(new FixedScalingDpiConverter(144)));

        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(60, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testMultiColumnReorderBothRegions() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 0));

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(4, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // multi reorder so that column 0 and column 3 are next to each other
        // while 0 stays in frozen area and 3 stays in non-frozen area
        this.compositeFreezeLayer.doCommand(
                new MultiColumnReorderCommand(this.compositeFreezeLayer, new int[] { 0, 3 }, 0));

        assertEquals(1, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(4, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    @Test
    public void testMultiRowReorderBothRegions() {
        this.compositeFreezeLayer.doCommand(
                new FreezeRowCommand(this.compositeFreezeLayer, 0));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(4, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        // multi reorder so that row 0 and row 3 are next to each other
        // while 0 stays in frozen area and 3 stays in non-frozen area
        this.compositeFreezeLayer.doCommand(
                new MultiRowReorderCommand(this.compositeFreezeLayer, new int[] { 0, 3 }, 0));

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(4, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        reset();
    }

    private void reset() {
        this.compositeFreezeLayer.doCommand(
                new ConfigureScalingCommand(new FixedScalingDpiConverter(96)));

        this.compositeFreezeLayer.doCommand(new UnFreezeGridCommand());

        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(5, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());
    }
}
