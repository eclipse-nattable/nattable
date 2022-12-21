/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.selection;

import static org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum.RIGHT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CellSelectionTest {

    private SelectionLayer selectionLayer;
    private MoveCellSelectionCommandHandler moveCommandHandler;

    private ITraversalStrategy AXIS_TRAVERSAL_ALL = new ITraversalStrategy() {

        @Override
        public TraversalScope getTraversalScope() {
            return TraversalScope.AXIS;
        }

        @Override
        public boolean isCycle() {
            return false;
        }

        @Override
        public int getStepCount() {
            return SelectionLayer.MOVE_ALL;
        }

        @Override
        public boolean isValidTarget(ILayerCell from, ILayerCell to) {
            return true;
        }

    };

    @BeforeEach
    public void setUp() {
        this.selectionLayer = new SelectionLayer(new DataLayerFixture(10, 10, 100, 40));
        // Selection grid origin as starting point
        this.selectionLayer.setSelectedCell(0, 0);
        this.moveCommandHandler = new MoveCellSelectionCommandHandler(this.selectionLayer);
    }

    @AfterEach
    public void cleanUp() {
        this.selectionLayer.clear();
    }

    @Test
    public void shouldHaveOriginSelected() {
        assertTrue(isLastCellInOrigin());
    }

    private boolean isLastCellInOrigin() {
        return (0 == this.selectionLayer.getLastSelectedCellPosition().columnPosition && 0 == this.selectionLayer
                .getLastSelectedCellPosition().rowPosition);
    }

    private boolean isSelectonAnchorInOrigin() {
        return (0 == this.selectionLayer.getSelectionAnchor().columnPosition && 0 == this.selectionLayer
                .getSelectionAnchor().rowPosition);
    }

    private boolean wasPreviousSelectionCleared() {
        // Make sure previous selection was cleared
        return (this.selectionLayer.getSelectedColumnPositions().length == 1 && this.selectionLayer
                .getSelectedRowCount() == 1);
    }

    private boolean wasPreviousColumnSelectionAppended() {
        // Make sure previous column selection was not cleared
        return this.selectionLayer.getSelectedColumnPositions().length > 1;
    }

    private boolean wasPreviousRowSelectionAppended() {
        // Make sure previous column selection was not cleared
        return this.selectionLayer.getSelectedRowCount() > 1;
    }

    // Tests for cell selection NTBL-224

    @Test
    public void shouldMoveTheSelectionAnchorLeftUsingLeftArrowKey() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, false, false);
        // Should not have moved
        assertTrue(isLastCellInOrigin());

        this.selectionLayer.setSelectedCell(1, 0);

        // Should move back to origin
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, false, false);
        assertTrue(isLastCellInOrigin());

        // Previous selection was cleared
        assertTrue(wasPreviousSelectionCleared());
    }

    @Test
    public void shouldExtendTheSelectionToTheLeftUsingLeftArrowAndShiftKeys() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, true, false);
        // Should not have moved
        assertTrue(isLastCellInOrigin());

        this.selectionLayer.setSelectedCell(2, 0);

        // Should move back to origin
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, true, false);

        // Last selected cell should now be the origin
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, true, false);
        assertTrue(isLastCellInOrigin());

        // Selection anchor should not have changed
        assertEquals(2, this.selectionLayer.getSelectionAnchor()
                .getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor()
                .getRowPosition());

        // Cells in between should have been appended
        assertTrue(wasPreviousColumnSelectionAppended());
    }

    @Test
    public void shouldMoveTheSelectionAnchorRightUsingRightArrowKey() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.RIGHT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, false, false);

        // Previous selection was cleared and origin should no longer be
        // selected
        assertFalse(isLastCellInOrigin());

        // The selection anchor moved right
        assertEquals(1, this.selectionLayer.getSelectionAnchor()
                .getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor()
                .getRowPosition());

        // Previous selection was cleared
        assertTrue(wasPreviousSelectionCleared());
    }

    @Test
    public void shouldExtendTheSelectionRightUsingRightArrowAndShiftKeys() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.RIGHT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, true, false);
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.RIGHT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, true, false);

        // Since selection started at origin, then origin should be part of the
        // selected range
        assertTrue(isSelectonAnchorInOrigin());

        // Selection should now end on the cell to the right of the selection
        // anchor
        assertEquals(2, this.selectionLayer.getLastSelectedCellPosition()
                .getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor()
                .getColumnPosition());

        // Cells in between should have been appended
        assertTrue(wasPreviousColumnSelectionAppended());
    }

    @Test
    public void shouldMoveTheSelectionAnchorUpUsingUpArrowKey() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.UP, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, false, false);

        // Should not have moved
        assertTrue(isLastCellInOrigin());

        this.selectionLayer.setSelectedCell(0, 2);

        // Should move back to origin
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.UP, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, false, false);
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.UP, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, false, false);
        assertTrue(isLastCellInOrigin());

        // Previous selection was cleared
        assertTrue(wasPreviousSelectionCleared());
    }

    @Test
    public void shouldExtendTheSelectionUpUsingUpArrowAndShiftKeys() {
        this.selectionLayer.setSelectedCell(0, 1);
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.UP, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, true, false);

        // Anchor should not have changed
        assertEquals(1, this.selectionLayer.getSelectionAnchor()
                .getRowPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor()
                .getColumnPosition());

        // Last selected cell should be the origin
        assertTrue(isLastCellInOrigin());

        // Cells in between should have been appended
        assertTrue(wasPreviousRowSelectionAppended());
    }

    @Test
    public void shouldMoveTheSelectionAnchorDownUsingDownArrowKey() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.DOWN, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, false, false);

        // Previous selection was cleared and origin should no longer be
        // selected
        assertFalse(isLastCellInOrigin());

        // Las selected cell is one step below origin
        assertEquals(0, this.selectionLayer.getLastSelectedCellPosition()
                .getColumnPosition());
        assertEquals(1, this.selectionLayer.getLastSelectedCellPosition()
                .getRowPosition());

        // Previous selection was cleared
        assertTrue(wasPreviousSelectionCleared());
    }

    @Test
    public void shouldExtendTheSelectionDownUsingDownArrowAndShiftKeys() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.DOWN, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, true, false);

        // Selection anchor remains at origing
        assertTrue(isSelectonAnchorInOrigin());

        // Las selected cell is one step below origin
        assertEquals(0, this.selectionLayer.getLastSelectedCellPosition()
                .getColumnPosition());
        assertEquals(1, this.selectionLayer.getLastSelectedCellPosition()
                .getRowPosition());

        // Cells in between should have been appended
        assertTrue(wasPreviousRowSelectionAppended());
    }

    @Test
    public void shouldMoveTheSelecitonAnchorToStartOfRowUsingHomeKey() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, this.AXIS_TRAVERSAL_ALL, false, false);

        // Should not have moved
        assertTrue(isLastCellInOrigin());

        // Move to middle of grid
        this.selectionLayer.setSelectedCell(2, 0);

        // Should move back to origin
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, this.AXIS_TRAVERSAL_ALL, false, false);
        assertTrue(isLastCellInOrigin());

        // Previous selection was cleared
        assertTrue(wasPreviousSelectionCleared());
    }

    @Test
    public void shouldExtendTheSelectionToStartOfRowUsingHomeAndShiftKeys() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, this.AXIS_TRAVERSAL_ALL, true, false);

        // Should not have moved
        assertTrue(isLastCellInOrigin());

        // Move to middle of grid
        this.selectionLayer.setSelectedCell(2, 0);

        // Should move back to origin
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.LEFT, this.AXIS_TRAVERSAL_ALL, true, false);
        assertTrue(isLastCellInOrigin());

        // Selection anchor should not have changed
        assertEquals(2, this.selectionLayer.getSelectionAnchor()
                .getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor()
                .getRowPosition());

        // Cells in between should have been appended
        assertTrue(wasPreviousColumnSelectionAppended());
    }

    @Test
    public void shouldMoveTheSelectionAnchorToEndOfRowUsingEndKey() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.RIGHT, this.AXIS_TRAVERSAL_ALL, false, false);

        // Selection anchor moved to end of grid
        assertEquals(9, this.selectionLayer.getSelectionAnchor()
                .getColumnPosition());
        assertEquals(0, this.selectionLayer.getSelectionAnchor()
                .getRowPosition());

        // Previous selection was cleared and origin should no longer be
        // selected
        assertFalse(isLastCellInOrigin());

        // Previous selection was cleared
        assertTrue(wasPreviousSelectionCleared());
    }

    @Test
    public void shouldExtendTheSelectionToEndOfRowUsingEndAndShiftKeys() {
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.RIGHT, this.AXIS_TRAVERSAL_ALL, true, false);

        // Selection anchor should stay at the origin
        assertTrue(isSelectonAnchorInOrigin());

        // Last selected cell is at end of grid
        assertEquals(9, this.selectionLayer.getLastSelectedCellPosition()
                .getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCellPosition()
                .getRowPosition());

        // Cells in between should have been appended
        assertTrue(wasPreviousColumnSelectionAppended());
    }

    @Test
    public void shouldMoveTheSelectionAnchorOnePageUpUsingPageUpKey() {
        ITraversalStrategy customTraversal = new ITraversalStrategy() {

            @Override
            public TraversalScope getTraversalScope() {
                return TraversalScope.AXIS;
            }

            @Override
            public boolean isCycle() {
                return false;
            }

            @Override
            public int getStepCount() {
                return 6;
            }

            @Override
            public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                return true;
            }

        };

        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.UP, customTraversal, false, false);

        // Should not have moved
        assertTrue(isLastCellInOrigin());

        // Move to middle of grid
        final int columnPosition = 2;
        final int rowPosition = 4;
        this.selectionLayer.setSelectedCell(columnPosition, rowPosition);

        customTraversal = new ITraversalStrategy() {

            @Override
            public TraversalScope getTraversalScope() {
                return TraversalScope.AXIS;
            }

            @Override
            public boolean isCycle() {
                return false;
            }

            @Override
            public int getStepCount() {
                return 10;
            }

            @Override
            public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                return true;
            }

        };

        // Should not have moved
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.UP, customTraversal, false, false);

        // Previous selection was cleared and origin should no longer be
        // selected
        assertFalse(isLastCellInOrigin());

        customTraversal = new ITraversalStrategy() {

            @Override
            public TraversalScope getTraversalScope() {
                return TraversalScope.AXIS;
            }

            @Override
            public boolean isCycle() {
                return false;
            }

            @Override
            public int getStepCount() {
                return 60;
            }

            @Override
            public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                return true;
            }

        };

        // Should move back to first row event if step size is greater than
        // available number of rows
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.UP, customTraversal, false, false);
        assertEquals(2, this.selectionLayer.getLastSelectedCellPosition()
                .getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCellPosition()
                .getRowPosition());

        // Previous selection was cleared
        assertTrue(wasPreviousSelectionCleared());
    }

    @Test
    public void shouldExtendSelectionOnePageUpUsingThePageUpAndShiftKeys() {
        ITraversalStrategy customTraversal = new ITraversalStrategy() {

            @Override
            public TraversalScope getTraversalScope() {
                return TraversalScope.AXIS;
            }

            @Override
            public boolean isCycle() {
                return false;
            }

            @Override
            public int getStepCount() {
                return 10;
            }

            @Override
            public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                return true;
            }

        };

        // Move to middle of grid
        final int columnPosition = 2;
        final int rowPosition = 4;
        this.selectionLayer.setSelectedCell(columnPosition, rowPosition);

        // Should move back to first row event if step size is greater than
        // available number of rows
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.UP, customTraversal, true, false);
        assertEquals(2, this.selectionLayer.getLastSelectedCellPosition()
                .getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCellPosition()
                .getRowPosition());

        // Selection anchor should not have changed
        assertEquals(2, this.selectionLayer.getSelectionAnchor()
                .getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor()
                .getRowPosition());

        // Cells in between should have been appended
        assertTrue(wasPreviousRowSelectionAppended());
    }

    @Test
    public void shouldMoveTheSelectionAnchorOnePageDownUsingPageDownKey() {
        ITraversalStrategy customTraversal = new ITraversalStrategy() {

            @Override
            public TraversalScope getTraversalScope() {
                return TraversalScope.AXIS;
            }

            @Override
            public boolean isCycle() {
                return false;
            }

            @Override
            public int getStepCount() {
                return 6;
            }

            @Override
            public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                return true;
            }

        };

        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.DOWN, customTraversal, false, false);

        customTraversal = new ITraversalStrategy() {

            @Override
            public TraversalScope getTraversalScope() {
                return TraversalScope.AXIS;
            }

            @Override
            public boolean isCycle() {
                return false;
            }

            @Override
            public int getStepCount() {
                return 60;
            }

            @Override
            public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                return true;
            }

        };

        // Should move to last row even if step size is greater than available
        // number of rows
        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.DOWN, customTraversal, false, false);
        assertEquals(0, this.selectionLayer.getLastSelectedCellPosition()
                .getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCellPosition()
                .getRowPosition());

        // Previous selection was cleared
        assertTrue(wasPreviousSelectionCleared());
    }

    @Test
    public void shouldExtendSelectionOnePageDownUsingPageDownAndShiftKeys() {
        ITraversalStrategy customTraversal = new ITraversalStrategy() {

            @Override
            public TraversalScope getTraversalScope() {
                return TraversalScope.AXIS;
            }

            @Override
            public boolean isCycle() {
                return false;
            }

            @Override
            public int getStepCount() {
                return 6;
            }

            @Override
            public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                return true;
            }

        };

        this.moveCommandHandler.moveSelection(
                MoveDirectionEnum.DOWN, customTraversal, true, false);

        // Selection anchor should not have changed
        assertTrue(isSelectonAnchorInOrigin());

        // Last selected cell should be in the last row
        assertEquals(0, this.selectionLayer.getLastSelectedCellPosition()
                .getColumnPosition());
        assertEquals(6, this.selectionLayer.getLastSelectedCellPosition()
                .getRowPosition());

        // Previous selection was cleared
        assertTrue(wasPreviousRowSelectionAppended());
    }

    /**
     * If a range of cells is selected - clear selection and move anchor to the
     * next cell in the direction moved
     */
    @Test
    public void moveCellWhenARangeOfCellsIsSelected() throws Exception {

        new SelectColumnCommandHandler(this.selectionLayer).selectColumn(2, 0,
                false, false);
        this.moveCommandHandler.moveSelection(
                RIGHT, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, false, false);

        assertEquals(3, this.selectionLayer.getSelectionAnchor().columnPosition);
        assertEquals(0, this.selectionLayer.getSelectionAnchor().rowPosition);
    }

    /**
     * Selected cells are (col,row): (2,3),(4,1),(1,0),(9,9)
     */
    @Test
    public void shouldReturnTheCorrectCountOfSelectedCells() {
        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 3,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 4, 1,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 9, 9,
                false, true));

        PositionCoordinate[] cells = this.selectionLayer.getSelectedCellPositions();
        assertEquals(4, cells.length);
        // (1, 0)
        assertEquals(1, cells[0].columnPosition);
        assertEquals(0, cells[0].rowPosition);
        // (2, 3)
        assertEquals(2, cells[1].columnPosition);
        assertEquals(3, cells[1].rowPosition);
        // (4, 1)
        assertEquals(4, cells[2].columnPosition);
        assertEquals(1, cells[2].rowPosition);
        // (9, 9)
        assertEquals(9, cells[3].columnPosition);
        assertEquals(9, cells[3].rowPosition);
    }

    @Test
    public void shouldReturnSixCells() {
        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 0,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 1,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 1,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 2,
                false, true));
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 2,
                false, true));

        Collection<PositionCoordinate> cells = ArrayUtil
                .asCollection(this.selectionLayer.getSelectedCellPositions());

        assertEquals(6, cells.size());
        // (1, 0)
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                1, 0)));
        // (1, 1)
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                1, 1)));
        // (1, 2)
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                1, 2)));
        // (2, 0)
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                2, 0)));
        // (2, 1)
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                2, 1)));
        // (2, 2)
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                2, 2)));
    }

    @Test
    public void onlyOneCellSelectedAtAnyTime() {
        this.selectionLayer.getSelectionModel().setMultipleSelectionAllowed(false);

        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 0,
                false, true));

        Collection<PositionCoordinate> cells = ArrayUtil
                .asCollection(this.selectionLayer.getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                1, 0)));

        // select another cell with control mask
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 0,
                false, true));

        cells = ArrayUtil.asCollection(this.selectionLayer
                .getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                2, 0)));

        // select additional cells with shift mask
        // only the first cell should be selected afterwards
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 10,
                true, false));

        cells = ArrayUtil.asCollection(this.selectionLayer
                .getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                2, 0)));

        // select additional cells with shift mask
        // only the first cell should be selected afterwards
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 10, 0,
                true, false));

        cells = ArrayUtil.asCollection(this.selectionLayer
                .getSelectedCellPositions());
        assertEquals(1, cells.size());
        assertTrue(cells.contains(new PositionCoordinate(this.selectionLayer,
                2, 0)));
    }
}
