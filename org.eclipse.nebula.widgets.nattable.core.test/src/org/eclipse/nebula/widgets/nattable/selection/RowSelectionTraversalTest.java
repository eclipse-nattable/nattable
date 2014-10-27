/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RowSelectionTraversalTest {

    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;

    @Before
    public void setUp() {
        // only use 10 columns to make the test cases easier
        String[] propertyNames = Arrays.copyOfRange(RowDataListFixture.getPropertyNames(), 0, 10);

        IRowDataProvider<RowDataFixture> bodyDataProvider = new ListDataProvider<RowDataFixture>(
                RowDataListFixture.getList(10),
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(propertyNames));

        this.selectionLayer = new SelectionLayer(new DataLayer(bodyDataProvider));

        this.selectionLayer.setSelectionModel(new RowSelectionModel<RowDataFixture>(
                this.selectionLayer, bodyDataProvider,
                new IRowIdAccessor<RowDataFixture>() {

                    @Override
                    public Serializable getRowId(RowDataFixture rowObject) {
                        return rowObject.getSecurity_id();
                    }

                }));

        this.viewportLayer = new ViewportLayer(this.selectionLayer);
    }

    @After
    public void cleanUp() {
        this.selectionLayer.clear();
    }

    // move right

    @Test
    public void testMoveRightAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(5, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightAtEndAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(9, 4);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move on to right at end -> stay
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(5, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightAtEndAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(9, 4);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right at end -> relocate at beginning, same row
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(5, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightAtEndTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(9, 4);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right at end -> relocate at beginning, new row
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(5, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(5, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightAtEndBottomTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(9, 9);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right at end -> stay
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(9, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(5, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightAtEndTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(9, 4);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right at end -> relocate at beginning, new row
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(5, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(5, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveRightAtEndBottomTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(9, 9);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right at end -> relocate at beginning, top
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    // move left

    @Test
    public void testMoveLeftAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftAtBeginningAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(0, 4);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move on to left at beginning -> stay
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftAtBeginningAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(0, 4);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left at beginning -> relocate at end, same row
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftAtBeginningTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(0, 4);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left at beginning -> relocate at end, new row
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(3, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftAtBeginningTopTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left at beginning -> stay
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftAtBeginningTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(0, 4);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left at beginning -> relocate at end, new row
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(3, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveLeftAtBeginningTopTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to left at end -> relocate at end, bottom
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(9, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    // move down

    @Test
    public void testMoveDownAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one down
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(5, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(5, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownAtEndAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 9);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move down at end -> stay
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(9, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one down
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(5, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(5, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownAtEndAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 9);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move down at end -> relocate at beginning, same column
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one down
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(5, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(5, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownAtEndTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 9);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one down at end -> relocate at beginning, new column
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(5, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownAtEndBottomTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(9, 9);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right at end -> stay
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(9, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one down
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(5, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(5, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownAtEndTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 9);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one down at end -> relocate at beginning, new column
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(5, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveDownAtEndBottomTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(9, 9);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one to right at end -> relocate at beginning, top
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    // move up

    @Test
    public void testMoveUpAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(3, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpAtBeginningAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 0);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move up at beginning -> stay
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(3, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpAtBeginningAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 0);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up at beginning -> relocate at end, same column
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(9, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(3, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpAtBeginningTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 0);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up at beginning -> relocate at end, new column
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(9, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpAtBeginningTopTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up at beginning -> stay
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 4);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(3, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpAtBeginningTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(4, 0);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up at beginning -> relocate at end, new column
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(9, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void testMoveUpAtBeginningTopTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveRowSelectionCommandHandler(this.selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));
        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // move one up at end -> relocate at end, bottom
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(9, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

}
