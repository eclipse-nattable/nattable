/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class FreezeHandlerTest {

    private FreezeCommandHandler commandHandler;
    private CompositeFreezeLayer compositeFreezeLayer;
    private FreezeLayer freezeLayer;
    private ViewportLayer viewportLayer;
    private SelectionLayer selectionLayer;

    @Before
    public void setUp() {
        final DataLayer bodyDataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
        final DefaultBodyLayerStack bodyLayer = new DefaultBodyLayerStack(bodyDataLayer);
        this.selectionLayer = bodyLayer.getSelectionLayer();

        this.freezeLayer = new FreezeLayer(this.selectionLayer);
        this.compositeFreezeLayer = new CompositeFreezeLayer(this.freezeLayer, bodyLayer.getViewportLayer(), bodyLayer.getSelectionLayer());
        this.viewportLayer = bodyLayer.getViewportLayer();
        this.commandHandler = new FreezeCommandHandler(this.freezeLayer, this.viewportLayer, this.selectionLayer);
        this.compositeFreezeLayer.registerCommandHandler(this.commandHandler);

        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 600, 400);
            }
        });

        // Shoot this command so that the viewport can be initialized
        this.compositeFreezeLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.H_SCROLL | SWT.V_SCROLL)));
    }

    @Test
    public void shouldFreezeFirstColumn() {
        // This is what would happen if we selected to freeze a column from some
        // sort of menu action
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(this.compositeFreezeLayer, 1));
        assertEquals(0, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Check viewport origin
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
    }

    @Test
    public void shouldFreezeRowsAndColumnsBasedOnSelection() {
        this.compositeFreezeLayer.doCommand(new SelectCellCommand(this.compositeFreezeLayer, 2, 2, false, false));

        // Make sure selection layer processed command
        final PositionCoordinate lastSelectedCell = this.selectionLayer.getLastSelectedCellPosition();
        assertEquals(2, lastSelectedCell.columnPosition);
        assertEquals(2, lastSelectedCell.rowPosition);

        // This is what would happen if we selected to freeze from a selected
        // cell
        this.compositeFreezeLayer.doCommand(new FreezeSelectionCommand());
        assertEquals(0, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertNotNull(this.freezeLayer.getBottomRightPosition());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Check viewport origin
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
    }

    @Test
    public void shouldFreezeRowsAndColumnsBasedOnSelectionInclude() {
        this.compositeFreezeLayer.doCommand(new SelectCellCommand(this.compositeFreezeLayer, 2, 2, false, false));

        // Make sure selection layer processed command
        final PositionCoordinate lastSelectedCell = this.selectionLayer.getLastSelectedCellPosition();
        assertEquals(2, lastSelectedCell.columnPosition);
        assertEquals(2, lastSelectedCell.rowPosition);

        // This is what would happen if we selected to freeze from a selected
        // cell
        this.compositeFreezeLayer.doCommand(new FreezeSelectionCommand(false, false, true));
        assertEquals(0, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertNotNull(this.freezeLayer.getBottomRightPosition());
        assertEquals(2, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(2, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Check viewport origin
        assertEquals(3, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(3, this.viewportLayer.getMinimumOriginRowPosition());
    }

    @Test
    public void shouldFreezeRowsAndColumnsBasedOnMultiSelection() {
        this.compositeFreezeLayer.doCommand(new SelectCellCommand(this.compositeFreezeLayer, 2, 2, false, false));
        this.compositeFreezeLayer.doCommand(new SelectCellCommand(this.compositeFreezeLayer, 3, 3, true, false));

        // Make sure selection layer processed command
        assertEquals(4, this.selectionLayer.getSelectedCellPositions().length);
        PositionCoordinate lastSelectedCell = this.selectionLayer.getLastSelectedCellPosition();
        assertEquals(3, lastSelectedCell.columnPosition);
        assertEquals(3, lastSelectedCell.rowPosition);

        // This is what would happen if we selected to freeze from a selected
        // cell
        this.compositeFreezeLayer.doCommand(new FreezeSelectionCommand());
        assertEquals(0, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertNotNull(this.freezeLayer.getBottomRightPosition());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Check viewport origin
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
    }

    @Test
    public void shouldFreezeRowsAndColumnsBasedOnMultiSelectionInclude() {
        this.compositeFreezeLayer.doCommand(new SelectCellCommand(this.compositeFreezeLayer, 2, 2, false, false));
        this.compositeFreezeLayer.doCommand(new SelectCellCommand(this.compositeFreezeLayer, 3, 3, true, false));

        // Make sure selection layer processed command
        assertEquals(4, this.selectionLayer.getSelectedCellPositions().length);
        PositionCoordinate lastSelectedCell = this.selectionLayer.getLastSelectedCellPosition();
        assertEquals(3, lastSelectedCell.columnPosition);
        assertEquals(3, lastSelectedCell.rowPosition);

        // This is what would happen if we selected to freeze from a selected
        // cell
        this.compositeFreezeLayer.doCommand(new FreezeSelectionCommand(false, false, true));
        assertEquals(0, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertNotNull(this.freezeLayer.getBottomRightPosition());
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Check viewport origin
        assertEquals(4, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(4, this.viewportLayer.getMinimumOriginRowPosition());
    }

    @Test
    public void shouldFreezeAfterScrolling() {
        // Scroll the viewport to the first column
        this.viewportLayer.resetOrigin(this.viewportLayer.getStartXOfColumnPosition(0), this.viewportLayer.getStartYOfRowPosition(0));
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(1));
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(0));

        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 3, false, false));
        this.compositeFreezeLayer.doCommand(new FreezeSelectionCommand());

        // Freezelayer
        assertEquals(2, this.freezeLayer.getColumnCount());

        // Test Positions
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(2, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(2, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Test indexes
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);

        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(this.freezeLayer.getColumnCount() - 1));
        assertEquals(2, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(2, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Test viewport
        assertEquals(3, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(3, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(3, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldRestructureFrozenArea() {
        final ReorderListener reorderListener = new ReorderListener();
        this.viewportLayer.addLayerListener(reorderListener);

        // Scroll the viewport to the first column
        this.viewportLayer.resetOrigin(this.viewportLayer.getStartXOfColumnPosition(0), this.viewportLayer.getStartYOfRowPosition(0));
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(1));
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(0));

        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 3, false, false));
        this.compositeFreezeLayer.doCommand(new FreezeSelectionCommand());

        // Move right edge out of frozen area
        assertEquals(2, this.freezeLayer.getColumnCount());
        this.compositeFreezeLayer.doCommand(new ColumnReorderCommand(this.compositeFreezeLayer, 1, 3));

        assertEquals(1, this.freezeLayer.getColumnCount());
    }

    @Test
    public void testFreezeColumnResize() {
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 400);
            }
        });

        // Fire this command so that the viewport can be initialized
        this.compositeFreezeLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.H_SCROLL | SWT.V_SCROLL)));

        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(this.compositeFreezeLayer, 2));

        assertEquals(3, this.freezeLayer.getColumnCount());
        assertEquals(7, this.viewportLayer.getColumnCount());
        assertEquals(300, this.viewportLayer.getOrigin().getX());

        this.compositeFreezeLayer.doCommand(new ColumnResizeCommand(this.freezeLayer, 2, 200));

        assertEquals(3, this.freezeLayer.getColumnCount());
        assertEquals(7, this.viewportLayer.getColumnCount());
        assertEquals(400, this.viewportLayer.getOrigin().getX());
    }

    @Test
    public void testFreezeAllColumnsResize() {
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 400);
            }
        });

        // Fire this command so that the viewport can be initialized
        this.compositeFreezeLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.H_SCROLL | SWT.V_SCROLL)));

        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(this.compositeFreezeLayer, 9));

        assertEquals(10, this.freezeLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(1000, this.viewportLayer.getOrigin().getX());

        this.compositeFreezeLayer.doCommand(new ColumnResizeCommand(this.freezeLayer, 2, 200));

        assertEquals(10, this.freezeLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(1100, this.viewportLayer.getOrigin().getX());
    }

    @Test
    public void testFreezeReorderToAllColumns() {
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 400);
            }
        });

        // Fire this command so that the viewport can be initialized
        this.compositeFreezeLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.H_SCROLL | SWT.V_SCROLL)));

        // freeze all-1 columns
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(this.compositeFreezeLayer, 8));

        assertEquals(9, this.freezeLayer.getColumnCount());
        assertEquals(1, this.viewportLayer.getColumnCount());
        assertEquals(900, this.viewportLayer.getOrigin().getX());

        this.compositeFreezeLayer.doCommand(new ColumnReorderCommand(this.compositeFreezeLayer, 9, 7));

        assertEquals(10, this.freezeLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(1000, this.viewportLayer.getOrigin().getX());
    }

    @Test
    public void testFreezeRowResize() {
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 400);
            }
        });

        // Fire this command so that the viewport can be initialized
        this.compositeFreezeLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.H_SCROLL | SWT.V_SCROLL)));

        this.compositeFreezeLayer.doCommand(new FreezeRowCommand(this.compositeFreezeLayer, 2));

        assertEquals(3, this.freezeLayer.getRowCount());
        assertEquals(7, this.viewportLayer.getRowCount());
        assertEquals(60, this.viewportLayer.getOrigin().getY());

        this.compositeFreezeLayer.doCommand(new RowResizeCommand(this.freezeLayer, 2, 120));

        assertEquals(3, this.freezeLayer.getRowCount());
        assertEquals(7, this.viewportLayer.getRowCount());
        assertEquals(160, this.viewportLayer.getOrigin().getY());
    }

    @Test
    public void testFreezeAllRowsResize() {
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 400);
            }
        });

        // Shoot this command so that the viewport can be initialized
        this.compositeFreezeLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.H_SCROLL | SWT.V_SCROLL)));

        this.compositeFreezeLayer.doCommand(new FreezeRowCommand(this.compositeFreezeLayer, 9));

        assertEquals(10, this.freezeLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getRowCount());
        assertEquals(200, this.viewportLayer.getOrigin().getY());

        this.compositeFreezeLayer.doCommand(new RowResizeCommand(this.freezeLayer, 2, 120));

        assertEquals(10, this.freezeLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getRowCount());
        assertEquals(300, this.viewportLayer.getOrigin().getY());
    }

    class ReorderListener implements ILayerListener {

        private ColumnReorderEvent reorderEvent;

        @Override
        public void handleLayerEvent(ILayerEvent event) {
            if (event instanceof ColumnReorderEvent) {
                this.reorderEvent = (ColumnReorderEvent) event;
            }
        }

        public int getReorderToColumnPosition() {
            return this.reorderEvent.getBeforeToColumnPosition();
        }
    };
}
