/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.copy.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CopyDataCommandHandlerTest {

    private DefaultGridLayer gridLayer;
    private SelectionLayer selectionLayer;
    private CopyDataCommandHandler commandHandler;

    @BeforeEach
    public void setUp() {
        final IDataProvider bodyDataProvider = new DataProviderFixture(10, 10);
        this.gridLayer = new DefaultGridLayer(
                bodyDataProvider,
                getColumnHeaderDataProvider(bodyDataProvider),
                getRowHeaderDataProvider(bodyDataProvider));
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 1050);
            }
        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(
                        new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.selectionLayer = this.gridLayer.getBodyLayer().getSelectionLayer();

        this.commandHandler =
                new CopyDataCommandHandler(
                        this.selectionLayer,
                        this.gridLayer.getColumnHeaderLayer(),
                        this.gridLayer.getRowHeaderLayer());
    }

    private IDataProvider getRowHeaderDataProvider(final IDataProvider bodyDataProvider) {
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
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            }

        };
    }

    private IDataProvider getColumnHeaderDataProvider(final IDataProvider dependent) {
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
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            }
        };
    }

    @Test
    public void shouldReturnArrayOfCellsForColumnsInSelectionModel() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 2, 3, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 4, 1, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 9, 9, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 1, 0, false, true));

        ILayerCell[][] columns = this.commandHandler.assembleColumnHeaders();

        assertEquals(5, columns.length);
        assertNull(columns[0][0]);
        assertEquals("Column 2", columns[0][1].getDataValue());
        assertEquals("Column 3", columns[0][2].getDataValue());
        assertEquals("Column 5", columns[0][3].getDataValue());
        assertEquals("Column 10", columns[0][4].getDataValue());
    }

    @Test
    public void shouldReturnOnlySelectedBodyCells() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 1, 2, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 3, 7, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 4, 8, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 5, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 7, 9, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 8, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 9, 9, false, true));

        ILayerCell[] bodyCells = this.commandHandler.assembleBody(0);
        assertEquals(8, bodyCells.length);
        assertEquals("[5,0]", bodyCells[4].getDataValue());
        assertEquals("[8,0]", bodyCells[6].getDataValue());

        bodyCells = this.commandHandler.assembleBody(9);
        assertEquals("[9,9]", bodyCells[7].getDataValue());
    }

    @Test
    public void shouldReturnGridWithSelectedCellsAndHeaders() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 1, 2, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 3, 7, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 4, 8, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 5, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 7, 9, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 8, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 9, 9, false, true));

        ILayerCell[][] copiedGrid = this.commandHandler.assembleCopiedDataStructure();

        // Assert structure of assembled copy grid with headers
        assertEquals(11, copiedGrid.length);
        assertEquals(8, copiedGrid[0].length);

        ILayerCell[] cells = copiedGrid[0];

        // First cell should be blank, this is the corner
        assertNull(cells[0]);
        // Should only have Column headers
        int[] selectedColumns = this.gridLayer.getBodyLayer().getSelectionLayer().getSelectedColumnPositions();
        for (int columnPosition = 1; columnPosition < cells.length; columnPosition++) {
            ILayerCell cell = cells[columnPosition];
            // Remember to substract offset from columnPosition
            assertEquals(
                    this.gridLayer.getColumnHeaderLayer().getDataValueByPosition(selectedColumns[columnPosition - 1], 0),
                    cell.getDataValue());
        }

        checkBodyCells(copiedGrid, 1, 1);
    }

    @Test
    public void shouldReturnGridWithSelectedCellsNoHeaders() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 1, 2, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 3, 7, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 4, 8, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 5, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 7, 9, false, true));
        this.selectionLayer.doCommand(
                new SelectColumnCommand(this.selectionLayer, 8, 0, false, true));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 9, 9, false, true));

        this.commandHandler = new CopyDataCommandHandler(this.selectionLayer);
        ILayerCell[][] copiedGrid = this.commandHandler.assembleCopiedDataStructure();

        // Assert structure of assembled copy grid with headers
        assertEquals(10, copiedGrid.length);
        assertEquals(7, copiedGrid[0].length);
        assertNotNull(copiedGrid[0][5]);

        checkBodyCells(copiedGrid, 0, 0);
    }

    @Test
    public void shouldCopySingleCell() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 3, 7, false, false));

        CopyDataCommandHandler commandHandler = new CopyDataCommandHandler(this.selectionLayer);
        ILayerCell[][] copiedGrid = commandHandler.assembleCopiedDataStructure();
        assertNotNull(copiedGrid[0][0]);

        assertEquals(
                this.selectionLayer.getDataValueByPosition(3, 7),
                copiedGrid[0][0].getDataValue());
    }

    @Test
    public void shouldNotCopySingleCellMarkedAsCopyNotAllowed() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 3, 3, false, false));

        CopyDataCommandHandler commandHandler = new CopyDataCommandHandler(this.selectionLayer) {
            @Override
            protected boolean isCopyAllowed(ILayerCell cellToCopy) {
                if (cellToCopy.getColumnPosition() == 3 && cellToCopy.getRowPosition() == 3) {
                    return false;
                }
                return super.isCopyAllowed(cellToCopy);
            }
        };
        ILayerCell[][] copiedGrid = commandHandler.assembleCopiedDataStructure();
        assertNull(copiedGrid);
    }

    @Test
    public void shouldNotCopyCellMarkedAsCopyNotAllowedMultiSelectionOneColumn() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 3, 0, false, false));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 3, 5, true, false));

        CopyDataCommandHandler commandHandler = new CopyDataCommandHandler(this.selectionLayer) {
            @Override
            protected boolean isCopyAllowed(ILayerCell cellToCopy) {
                if (cellToCopy.getColumnPosition() == 3 && cellToCopy.getRowPosition() == 3) {
                    return false;
                }
                return super.isCopyAllowed(cellToCopy);
            }
        };
        ILayerCell[][] copiedGrid = commandHandler.assembleCopiedDataStructure();
        assertNotNull(copiedGrid);

        assertEquals(6, copiedGrid.length);
        assertEquals(1, copiedGrid[0].length);
        assertNotNull(copiedGrid[0][0]);

        assertEquals(
                this.selectionLayer.getDataValueByPosition(3, 0),
                copiedGrid[0][0].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(3, 1),
                copiedGrid[1][0].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(3, 2),
                copiedGrid[2][0].getDataValue());
        assertNull(copiedGrid[3][0]);
        assertEquals(
                this.selectionLayer.getDataValueByPosition(3, 4),
                copiedGrid[4][0].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(3, 5),
                copiedGrid[5][0].getDataValue());
    }

    @Test
    public void shouldNotCopyCellMarkedAsCopyNotAllowedMultiSelectionOneRow() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 0, 3, false, false));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 5, 3, true, false));

        CopyDataCommandHandler commandHandler = new CopyDataCommandHandler(this.selectionLayer) {
            @Override
            protected boolean isCopyAllowed(ILayerCell cellToCopy) {
                if (cellToCopy.getColumnPosition() == 3 && cellToCopy.getRowPosition() == 3) {
                    return false;
                }
                return super.isCopyAllowed(cellToCopy);
            }
        };
        ILayerCell[][] copiedGrid = commandHandler.assembleCopiedDataStructure();
        assertNotNull(copiedGrid);

        assertEquals(1, copiedGrid.length);
        assertEquals(6, copiedGrid[0].length);
        assertNotNull(copiedGrid[0][0]);

        assertEquals(
                this.selectionLayer.getDataValueByPosition(0, 3),
                copiedGrid[0][0].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(1, 3),
                copiedGrid[0][1].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(2, 3),
                copiedGrid[0][2].getDataValue());
        assertNull(copiedGrid[0][3]);
        assertEquals(
                this.selectionLayer.getDataValueByPosition(4, 3),
                copiedGrid[0][4].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(5, 3),
                copiedGrid[0][5].getDataValue());
    }

    @Test
    public void shouldNotCopyCellMarkedAsCopyNotAllowedMultiSelectionRegion() {
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 2, 2, false, false));
        this.selectionLayer.doCommand(
                new SelectCellCommand(this.selectionLayer, 4, 4, true, false));

        CopyDataCommandHandler commandHandler = new CopyDataCommandHandler(this.selectionLayer) {
            @Override
            protected boolean isCopyAllowed(ILayerCell cellToCopy) {
                if (cellToCopy.getColumnPosition() == 3 && cellToCopy.getRowPosition() == 3) {
                    return false;
                }
                return super.isCopyAllowed(cellToCopy);
            }
        };
        ILayerCell[][] copiedGrid = commandHandler.assembleCopiedDataStructure();
        assertNotNull(copiedGrid);

        assertEquals(3, copiedGrid.length);
        assertEquals(3, copiedGrid[0].length);
        assertNotNull(copiedGrid[0][0]);

        assertEquals(
                this.selectionLayer.getDataValueByPosition(2, 2),
                copiedGrid[0][0].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(3, 2),
                copiedGrid[0][1].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(4, 2),
                copiedGrid[0][2].getDataValue());

        assertEquals(
                this.selectionLayer.getDataValueByPosition(2, 3),
                copiedGrid[1][0].getDataValue());
        assertNull(copiedGrid[1][1]);
        assertEquals(
                this.selectionLayer.getDataValueByPosition(4, 3),
                copiedGrid[1][2].getDataValue());

        assertEquals(
                this.selectionLayer.getDataValueByPosition(2, 4),
                copiedGrid[2][0].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(3, 4),
                copiedGrid[2][1].getDataValue());
        assertEquals(
                this.selectionLayer.getDataValueByPosition(4, 4),
                copiedGrid[2][2].getDataValue());
    }

    private void checkBodyCells(ILayerCell[][] copiedGrid, int columnOffset, int rowOffset) {
        int cellWithDataCounter = 0;
        int[] selectedColumns = this.selectionLayer.getSelectedColumnPositions();
        Set<Range> selectedRowRanges = this.selectionLayer.getSelectedRowPositions();

        Set<Integer> selectedRows = new HashSet<>();
        for (Range range : selectedRowRanges) {
            selectedRows.addAll(range.getMembers());
        }
        Iterator<Integer> rowsIterator = selectedRows.iterator();

        // Row zero is for column headers
        for (int rowPosition = rowOffset; rowPosition < copiedGrid.length; rowPosition++) {
            ILayerCell[] cells = copiedGrid[rowPosition];

            if (this.commandHandler.getRowHeaderLayer() != null) {
                assertEquals(
                        this.commandHandler.getRowHeaderLayer().getDataValueByPosition(0, rowPosition - rowOffset),
                        cells[0].getDataValue());
            }

            // Check body data
            int selectedRowPosition = rowsIterator.next().intValue();

            for (int columnPosition = columnOffset; columnPosition < cells.length; columnPosition++) {
                final ILayerCell cell = cells[columnPosition];
                if (cell != null) {
                    cellWithDataCounter++;
                    assertEquals(
                            this.selectionLayer.getDataValueByPosition(selectedColumns[columnPosition - columnOffset], selectedRowPosition),
                            cell.getDataValue());
                }
            }
        }
        assertEquals(this.selectionLayer.getSelectedCellPositions().length, cellWithDataCounter);
    }
}
