/*******************************************************************************
 * Copyright (c) 2022 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MoveRowSelectionCommandHandlerTest {

    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;

    @BeforeEach
    public void setUp() {
        // only use 10 columns to make the test cases easier
        String[] propertyNames = Arrays.copyOfRange(RowDataListFixture.getPropertyNames(), 0, 10);

        IRowDataProvider<RowDataFixture> bodyDataProvider = new ListDataProvider<RowDataFixture>(
                RowDataListFixture.getList(10),
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(propertyNames));

        this.selectionLayer = new SelectionLayer(new DataLayer(bodyDataProvider));
        this.selectionLayer.registerCommandHandler(new MoveRowSelectionCommandHandler(this.selectionLayer));

        this.selectionLayer.setSelectionModel(new RowSelectionModel<RowDataFixture>(
                this.selectionLayer, bodyDataProvider,
                new IRowIdAccessor<RowDataFixture>() {

                    @Override
                    public Serializable getRowId(RowDataFixture rowObject) {
                        return rowObject.getSecurity_id();
                    }

                }));

        this.viewportLayer = new ViewportLayer(this.selectionLayer);
        // only show 6 columns
        this.viewportLayer.setClientAreaProvider(() -> new Rectangle(0, 0, 600, 200));
    }

    @AfterEach
    public void cleanUp() {
        this.selectionLayer.clear();
    }

    @Test
    public void shouldUpdateSelectionOnMoveDown() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 1, 4, false, false));
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));

        // move down
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, false, false));

        // anchor moved one row down
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(5, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // row selection has changed
        assertFalse(this.selectionLayer.isRowPositionFullySelected(4));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(5));

        // viewport has not scrolled
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldUpdateSelectionOnMoveUp() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 1, 4, false, false));
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));

        // move down
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, false, false));

        // anchor moved one row up
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // row selection has changed
        assertFalse(this.selectionLayer.isRowPositionFullySelected(4));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(3));

        // viewport has not scrolled
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldExtendSelectionOnMoveDownShift() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 1, 4, false, false));
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));

        // move down with shift
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, true, false));

        // selection anchor stays
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // both rows are selected
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(5));

        // viewport has not scrolled
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldExtendSelectionOnMoveUpShift() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 1, 4, false, false));
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));

        // move up
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, true, false));

        // selection anchor stays
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // both rows are selected
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(3));

        // viewport has not scrolled
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldExtendAndUpdateSelectionOnMoveDownUpShift() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 1, 4, false, false));
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));

        // move down
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, true, false));

        // selection anchor stays
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // both rows are selected
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(5));

        // viewport has not scrolled
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));

        // move up again which should result in deselection
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, true, false));

        // selection anchor stays
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // only one row selected
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(5));

        // viewport has not scrolled
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldExtendAndUpdateSelectionOnMoveUpDownShift() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 1, 4, false, false));
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));

        // move up
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, true, false));

        // selection anchor stays
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // both rows are selected
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(3));

        // viewport has not scrolled
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));

        // move down again which should result in deselection
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, true, false));

        // selection anchor stays
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // only one row selected
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(3));

        // viewport has not scrolled
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldUpdateSelectionAnchorOnMoveLeft() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 1, 4, false, false));
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));

        // move left
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, false, false));

        // selection anchor moves
        assertEquals(0, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldUpdateSelectionAnchorOnMoveRight() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 1, 4, false, false));
        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));

        // move right
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, false, false));

        // selection anchor moves
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldUpdateAndScrollSelectionAnchorOnMoveLeft() {
        // scroll to last column
        this.viewportLayer.moveColumnPositionIntoViewport(9);

        // select a row, use last visible column in viewport
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 5, 4, false, false));
        assertEquals(9, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));

        // viewport shows last column
        assertEquals(4, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(9, this.viewportLayer.getColumnIndexByPosition(5));

        // move left to first column
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, 9, false, false));

        // selection anchor moves
        assertEquals(0, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));

        // viewport scrolled to first column
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(5, this.viewportLayer.getColumnIndexByPosition(5));
    }

    @Test
    public void shouldUpdateAndScrollSelectionAnchorOnMoveRight() {
        // select a row
        this.viewportLayer.doCommand(new SelectRowsCommand(this.viewportLayer, 0, 4, false, false));
        assertEquals(0, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));

        // viewport shows first column
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(5, this.viewportLayer.getColumnIndexByPosition(5));

        // move right to last column
        this.viewportLayer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, 9, false, false));

        // selection anchor moves
        assertEquals(9, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));

        // viewport scrolled to last column
        assertEquals(4, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(9, this.viewportLayer.getColumnIndexByPosition(5));
    }
}
