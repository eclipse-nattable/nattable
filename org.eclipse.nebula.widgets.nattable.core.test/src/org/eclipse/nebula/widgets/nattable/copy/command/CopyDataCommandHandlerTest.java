/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.DataProviderFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CopyDataCommandHandlerTest {

    private DefaultGridLayer gridLayer;
    private RowHeaderLayer rowHeaderLayer;
    private SelectionLayer selectionLayer;
    private ColumnHeaderLayer columnHeaderLayer;
    private CopyDataCommandHandler commandHandler;

    @Before
    public void setUp() {
        final IDataProvider bodyDataProvider = new DataProviderFixture(10, 10);
        this.gridLayer = new DefaultGridLayer(bodyDataProvider,
                getColumnHeaderDataProvider(bodyDataProvider),
                getRowHeaderDataProvider(bodyDataProvider));
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 1050);
            }
        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display
                .getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
        this.rowHeaderLayer = this.gridLayer.getRowHeaderLayer();
        this.columnHeaderLayer = this.gridLayer.getColumnHeaderLayer();
        this.selectionLayer = this.gridLayer.getBodyLayer().getSelectionLayer();

        this.commandHandler = new CopyDataCommandHandler(this.selectionLayer,
                this.columnHeaderLayer, this.rowHeaderLayer);
    }

    private IDataProvider getRowHeaderDataProvider(
            final IDataProvider bodyDataProvider) {
        return new IDataProvider() {

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return Integer.valueOf(rowIndex + 1);
            }

            @Override
            public int getRowCount() {
                return bodyDataProvider.getRowCount();
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex,
                    Object newValue) {}

        };
    }

    private IDataProvider getColumnHeaderDataProvider(
            final IDataProvider dependent) {
        return new IDataProvider() {

            @Override
            public int getColumnCount() {
                return dependent.getColumnCount();
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return "Column " + (columnIndex + 1);
            }

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex,
                    Object newValue) {}
        };
    }

    @Test
    public void shouldReturnArrayOfCellsForColumnsInSelectionModel() {
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 3,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 4, 1,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 9, 9,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 0,
                false, true));

        ILayerCell[][] columns = this.commandHandler.assembleColumnHeaders();

        assertEquals(5, columns.length);
        assertEquals("Column 2", columns[0][1].getDataValue());
        assertEquals("Column 3", columns[0][2].getDataValue());
        assertEquals("Column 5", columns[0][3].getDataValue());
        assertEquals("Column 10", columns[0][4].getDataValue());
    }

    @Test
    public void shouldReturnOnlySelectedBodyCells() {
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 2,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 7,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 4, 8,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 7, 9,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 8, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 9, 9,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 5, 0,
                false, true));

        ILayerCell[] bodyCells = this.commandHandler.assembleBody(0);
        assertEquals(8, bodyCells.length);
        assertEquals("[5,0]", bodyCells[4].getDataValue());
        assertEquals("[8,0]", bodyCells[6].getDataValue());

        bodyCells = this.commandHandler.assembleBody(9);
        assertEquals("[9,9]", bodyCells[7].getDataValue());
    }

    /**
     * Returns a collection representing a 11 x 11 grid. Only selected cells
     * will have data, those are (col,row): (2,3),(4,1),(1,0),(9,9)
     *
     * TODO: Test is ignored since it passes locally and fails on the build.
     * Can't figure out why.
     */
    @Ignore
    @Test
    public void shouldReturnGridWithSelectedCellsAndHeaders() {
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 2,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 7,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 4, 8,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 7, 9,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 8, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 9, 9,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 5, 0,
                false, true));

        ILayerCell[][] copiedGrid = this.commandHandler
                .assembleCopiedDataStructure();

        // Assert structure of assembled copy grid with headers
        assertEquals(11, copiedGrid.length);
        assertEquals(8, copiedGrid[0].length);
        checkColumnHeaderCells(copiedGrid[0]);
        checkBodyCells(copiedGrid);
    }

    @Test
    public void shouldReturnGridWithSelectedCellsNoHeaders() {
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 2,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 7,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 4, 8,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 7, 9,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 8, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 9, 9,
                false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 5, 0,
                false, true));

        this.commandHandler = new CopyDataCommandHandler(this.selectionLayer);
        ILayerCell[][] copiedGrid = this.commandHandler
                .assembleCopiedDataStructure();

        // Assert structure of assembled copy grid with headers
        assertEquals(10, copiedGrid.length);
        assertEquals(7, copiedGrid[0].length);
        assertNotNull(copiedGrid[0][5]);
    }

    @Test
    public void shouldCopySingleCell() {
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 7,
                false, false));

        CopyDataCommandHandler commandHandler = new CopyDataCommandHandler(
                this.selectionLayer);
        commandHandler.assembleCopiedDataStructure();
        ILayerCell[][] copiedGrid = commandHandler
                .assembleCopiedDataStructure();
        assertNotNull(copiedGrid[0][0]);
    }

    private void checkColumnHeaderCells(ILayerCell[] cells) {
        // First cell should be blank, this is the corner
        assertNull(cells[0]);
        // Should only have Column headers
        int[] selectedColumns = this.gridLayer.getBodyLayer().getSelectionLayer()
                .getSelectedColumnPositions();
        for (int columnPosition = 1; columnPosition < cells.length; columnPosition++) {
            ILayerCell cell = cells[columnPosition];
            // Remember to substract offset from columnPosition
            assertEquals(this.columnHeaderLayer.getDataValueByPosition(
                    selectedColumns[columnPosition - 1], 0),
                    cell.getDataValue());
        }
    }

    private void checkBodyCells(ILayerCell[][] copiedGrid) {
        int cellWithDataCounter = 0;
        int[] selectedColumns = this.selectionLayer.getSelectedColumnPositions();
        Set<Range> selectedRowRanges = this.selectionLayer.getSelectedRowPositions();

        Set<Integer> selectedRows = new HashSet<Integer>();
        for (Range range : selectedRowRanges) {
            selectedRows.addAll(range.getMembers());
        }
        Iterator<Integer> rowsIterator = selectedRows.iterator();

        // Row zero is for column headers
        for (int rowPosition = 1; rowPosition < copiedGrid.length; rowPosition++) {
            ILayerCell[] cells = copiedGrid[rowPosition];

            assertEquals(
                    this.rowHeaderLayer.getDataValueByPosition(0, rowPosition - 1),
                    cells[0].getDataValue());

            // Check body data
            int selectedRowPosition = rowsIterator.next().intValue();

            for (int columnPosition = 1; columnPosition < cells.length; columnPosition++) {
                final ILayerCell cell = cells[columnPosition];
                if (cell != null) {
                    cellWithDataCounter++;
                    assertEquals(this.selectionLayer.getDataValueByPosition(
                            selectedColumns[columnPosition - 1],
                            selectedRowPosition), cell.getDataValue());
                }
            }
        }
        assertEquals(this.selectionLayer.getSelectedCellPositions().length,
                cellWithDataCounter);
    }
}
