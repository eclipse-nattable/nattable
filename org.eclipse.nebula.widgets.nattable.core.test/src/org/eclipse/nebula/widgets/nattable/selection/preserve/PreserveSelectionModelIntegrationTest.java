/*******************************************************************************
 * Copyright (c) 2018, 2022 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial test
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.preserve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ResizeColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PreserveSelectionModelIntegrationTest {

    private List<Person> dataModel;
    private IRowDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private ColumnHideShowLayer columnHideShowLayer;
    private RowHideShowLayer rowHideShowLayer;
    private SelectionLayer selectionLayer;

    private SelectionLayer selectionLayerWithResizeHideShow;

    @BeforeEach
    public void setup() {
        this.dataModel = PersonService.getFixedPersons();
        this.dataProvider = new ListDataProvider<>(
                this.dataModel,
                new ReflectiveColumnPropertyAccessor<>(new String[] {
                        "firstName",
                        "lastName",
                        "gender",
                        "married",
                        "birthday" }));

        this.dataLayer = new DataLayer(this.dataProvider);

        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(this.dataLayer);
        this.columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
        this.rowHideShowLayer = new RowHideShowLayer(this.columnHideShowLayer);

        this.selectionLayer = new SelectionLayer(this.rowHideShowLayer);

        this.selectionLayer.setSelectionModel(
                new PreserveSelectionModel<>(
                        this.selectionLayer,
                        this.dataProvider,
                        new IRowIdAccessor<Person>() {

                            @Override
                            public Serializable getRowId(Person rowObject) {
                                return rowObject.getId();
                            }
                        }));

        ResizeColumnHideShowLayer resizeColumnHideShowLayer = new ResizeColumnHideShowLayer(columnReorderLayer, this.dataLayer);
        this.selectionLayerWithResizeHideShow = new SelectionLayer(resizeColumnHideShowLayer);
    }

    @Test
    public void shouldShowFullySelectedColumnOnColumnSelection() {
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedColumnOnClear() {
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");
        this.selectionLayer.doCommand(new ClearAllSelectionsCommand());
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedColumnOnDeselectCell() {
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");
        assertTrue(this.selectionLayer.isCellPositionSelected(1, 3), "cell 1/3 is not selected");
        // deselect a cell that was selected before
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 3, false, true));
        assertFalse(this.selectionLayer.isCellPositionSelected(1, 3), "cell 1/3 is selected");
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
    }

    @Test
    public void shouldShowFullySelectedColumnOnColumnSelectionBeforeHideRow() {
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");

        // hide a row
        assertEquals(18, this.selectionLayer.getRowCount());
        this.selectionLayer.doCommand(new RowHideCommand(this.selectionLayer, 5));
        assertEquals(17, this.selectionLayer.getRowCount());

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");
    }

    @Test
    public void shouldShowFullySelectedColumnOnColumnSelectionAfterHideRow() {
        // hide a row
        assertEquals(18, this.selectionLayer.getRowCount());
        this.selectionLayer.doCommand(new RowHideCommand(this.selectionLayer, 5));
        assertEquals(17, this.selectionLayer.getRowCount());

        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedColumnOnColumnSelectionBeforeHideRowOnDeselect() {
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");

        // hide a row
        assertEquals(18, this.selectionLayer.getRowCount());
        this.selectionLayer.doCommand(new RowHideCommand(this.selectionLayer, 5));
        assertEquals(17, this.selectionLayer.getRowCount());

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");

        assertTrue(this.selectionLayer.isCellPositionSelected(1, 3), "cell 1/3 is not selected");
        // deselect a cell that was selected before
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 3, false, true));
        assertFalse(this.selectionLayer.isCellPositionSelected(1, 3), "cell 1/3 is selected");
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedColumnOnColumnSelectionAfterHideAndShowAll() {
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");

        // hide a row
        assertEquals(18, this.selectionLayer.getRowCount());
        this.selectionLayer.doCommand(new RowHideCommand(this.selectionLayer, 5));
        assertEquals(17, this.selectionLayer.getRowCount());

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");

        // show all again
        this.selectionLayer.doCommand(new ShowAllRowsCommand());
        assertEquals(18, this.selectionLayer.getRowCount());

        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedColumnOnColumnSelectionAfterHideRowOnDeselect() {
        // hide a row
        assertEquals(18, this.selectionLayer.getRowCount());
        this.selectionLayer.doCommand(new RowHideCommand(this.selectionLayer, 5));
        assertEquals(17, this.selectionLayer.getRowCount());

        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not fully selected");

        assertTrue(this.selectionLayer.isCellPositionSelected(1, 3), "cell 1/3 is not selected");
        // deselect a cell that was selected before
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 3, false, true));
        assertFalse(this.selectionLayer.isCellPositionSelected(1, 3), "cell 1/3 is selected");
        assertFalse(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is fully selected");
    }

    @Test
    public void shouldShowFullySelectedRowOnRowSelection() {
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 1, false, false));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedRowOnClear() {
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 1, false, false));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");
        this.selectionLayer.doCommand(new ClearAllSelectionsCommand());
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedRowOnDeselectCell() {
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 1, false, false));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");
        assertTrue(this.selectionLayer.isCellPositionSelected(3, 1), "cell 3/1 is not selected");
        // deselect a cell that was selected before
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 1, false, true));
        assertFalse(this.selectionLayer.isCellPositionSelected(3, 1), "cell 3/1 is selected");
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
    }

    @Test
    public void shouldShowFullySelectedRowOnRowSelectionBeforeHideColumn() {
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 1, false, false));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");

        // hide a column
        assertEquals(5, this.selectionLayer.getColumnCount());
        this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 3));
        assertEquals(4, this.selectionLayer.getColumnCount());

        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");
    }

    @Test
    public void shouldShowFullySelectedRowOnRowSelectionAfterHideColumn() {
        // hide a column
        assertEquals(5, this.selectionLayer.getColumnCount());
        this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 3));
        assertEquals(4, this.selectionLayer.getColumnCount());

        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 1, false, false));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedRowOnRowSelectionBeforeHideColumnOnDeselect() {
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 1, false, false));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");

        // hide a column
        assertEquals(5, this.selectionLayer.getColumnCount());
        this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 3));
        assertEquals(4, this.selectionLayer.getColumnCount());

        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");

        assertTrue(this.selectionLayer.isCellPositionSelected(3, 1), "cell 3/1 is not selected");
        // deselect a cell that was selected before
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 1, false, true));
        assertFalse(this.selectionLayer.isCellPositionSelected(3, 1), "cell 3/1 is selected");
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedRowOnRowSelectionAfterHideColumnOnDeselect() {
        // hide a column
        assertEquals(5, this.selectionLayer.getColumnCount());
        this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 3));
        assertEquals(4, this.selectionLayer.getColumnCount());

        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 1, false, false));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");

        assertTrue(this.selectionLayer.isCellPositionSelected(3, 1), "cell 3/1 is not selected");
        // deselect a cell that was selected before
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 3, 1, false, true));
        assertFalse(this.selectionLayer.isCellPositionSelected(3, 1), "cell 3/1 is selected");
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
    }

    @Test
    public void shouldShowFullySelectedRowOnRowSelectionBeforeHideColumnResize() {
        assertFalse(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayerWithResizeHideShow.doCommand(new SelectRowsCommand(this.selectionLayerWithResizeHideShow, 0, 1, false, false));
        assertTrue(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is not fully selected");

        // hide a column
        assertEquals(5, this.selectionLayerWithResizeHideShow.getColumnCount());
        this.selectionLayerWithResizeHideShow.doCommand(new ColumnHideCommand(this.selectionLayerWithResizeHideShow, 3));
        assertEquals(5, this.selectionLayerWithResizeHideShow.getColumnCount());

        assertTrue(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is not fully selected");
    }

    @Test
    public void shouldShowFullySelectedRowOnRowSelectionAfterHideColumnResize() {
        // hide a column
        assertEquals(5, this.selectionLayerWithResizeHideShow.getColumnCount());
        this.selectionLayerWithResizeHideShow.doCommand(new ColumnHideCommand(this.selectionLayerWithResizeHideShow, 3));
        assertEquals(5, this.selectionLayerWithResizeHideShow.getColumnCount());

        assertFalse(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayerWithResizeHideShow.doCommand(new SelectRowsCommand(this.selectionLayerWithResizeHideShow, 0, 1, false, false));
        assertTrue(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is not fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedRowOnRowSelectionBeforeHideColumnOnDeselectResize() {
        assertFalse(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayerWithResizeHideShow.doCommand(new SelectRowsCommand(this.selectionLayerWithResizeHideShow, 0, 1, false, false));
        assertTrue(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is not fully selected");

        // hide a column
        assertEquals(5, this.selectionLayerWithResizeHideShow.getColumnCount());
        this.selectionLayerWithResizeHideShow.doCommand(new ColumnHideCommand(this.selectionLayerWithResizeHideShow, 3));
        assertEquals(5, this.selectionLayerWithResizeHideShow.getColumnCount());

        assertTrue(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is not fully selected");

        assertTrue(this.selectionLayerWithResizeHideShow.isCellPositionSelected(3, 1), "cell 3/1 is not selected");
        // deselect a cell that was selected before
        this.selectionLayerWithResizeHideShow.doCommand(new SelectCellCommand(this.selectionLayerWithResizeHideShow, 3, 1, false, true));
        assertFalse(this.selectionLayerWithResizeHideShow.isCellPositionSelected(3, 1), "cell 3/1 is selected");
        assertFalse(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is fully selected");
    }

    @Test
    public void shouldNotShowFullySelectedRowOnRowSelectionAfterHideColumnOnDeselectResize() {
        // hide a column
        assertEquals(5, this.selectionLayerWithResizeHideShow.getColumnCount());
        this.selectionLayerWithResizeHideShow.doCommand(new ColumnHideCommand(this.selectionLayerWithResizeHideShow, 3));
        assertEquals(5, this.selectionLayerWithResizeHideShow.getColumnCount());

        assertFalse(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayerWithResizeHideShow.doCommand(new SelectRowsCommand(this.selectionLayerWithResizeHideShow, 0, 1, false, false));
        assertTrue(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is not fully selected");

        assertTrue(this.selectionLayerWithResizeHideShow.isCellPositionSelected(3, 1), "cell 3/1 is not selected");
        // deselect a cell that was selected before
        this.selectionLayerWithResizeHideShow.doCommand(new SelectCellCommand(this.selectionLayerWithResizeHideShow, 3, 1, false, true));
        assertFalse(this.selectionLayerWithResizeHideShow.isCellPositionSelected(3, 1), "cell 3/1 is selected");
        assertFalse(this.selectionLayerWithResizeHideShow.isRowPositionFullySelected(1), "row 1 is fully selected");
    }

    @Test
    public void shouldShowFullySelectedRowOnReorder() {
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is fully selected");
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 1, false, false));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");

        this.selectionLayer.doCommand(new ColumnReorderCommand(this.selectionLayer, 4, 0));

        assertTrue(this.selectionLayer.isRowPositionFullySelected(1), "row 1 is not fully selected");
    }

    @Test
    public void shouldClearColumnSelectionOnMultiHideColumnCommand() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, true));

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(2), "column 2 is not selected");
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(3), "column 3 is not selected");

        this.columnHideShowLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2, 3));

        assertEquals(3, this.selectionLayer.getColumnCount());
        assertEquals(0, this.selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedColumnPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowPositions().size());
    }

    @Test
    public void shouldClearAndUpdateConsecutiveColumnSelectionOnMultiHideColumnCommand() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(2), "column 2 is not selected");
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(3), "column 3 is not selected");
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(4), "column 4 is not selected");

        this.columnHideShowLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2, 3));

        assertEquals(3, this.selectionLayer.getColumnCount());
        assertEquals(1, this.selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertEquals(18, this.selectionLayer.getSelectedRowPositions().size());
    }

    @Test
    public void shouldClearAndUpdateColumnSelectionOnMultiHideColumnCommand() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(1), "column 1 is not selected");
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(3), "column 3 is not selected");
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(4), "column 4 is not selected");

        this.columnHideShowLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 1, 3));

        assertEquals(3, this.selectionLayer.getColumnCount());
        assertEquals(1, this.selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertEquals(18, this.selectionLayer.getSelectedRowPositions().size());
    }

    @Test
    public void shouldUpdateConsecutiveColumnSelectionOnShowColumns() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(4), "column 4 is not selected");

        this.columnHideShowLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2, 3));

        assertEquals(3, this.selectionLayer.getColumnCount());
        assertEquals(1, this.selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(2), "column 2 is not selected");
        assertEquals(18, this.selectionLayer.getSelectedRowPositions().size());

        this.columnHideShowLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(5, this.selectionLayer.getColumnCount());
        assertEquals(1, this.selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(4), "column 4 is not selected");
        assertEquals(18, this.selectionLayer.getSelectedRowPositions().size());
    }

    @Test
    public void shouldUpdateColumnSelectionOnShowColumns() {
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));

        assertTrue(this.selectionLayer.isColumnPositionFullySelected(4), "column 4 is not selected");

        this.columnHideShowLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 1, 3));

        assertEquals(3, this.selectionLayer.getColumnCount());
        assertEquals(1, this.selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(2), "column 2 is not selected");
        assertEquals(18, this.selectionLayer.getSelectedRowPositions().size());

        this.columnHideShowLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(5, this.selectionLayer.getColumnCount());
        assertEquals(1, this.selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);
        assertTrue(this.selectionLayer.isColumnPositionFullySelected(4), "column 4 is not selected");
        assertEquals(18, this.selectionLayer.getSelectedRowPositions().size());
    }

    @Test
    public void shouldClearRowSelectionOnMultiHideRowCommand() {
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 2, false, false));
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 3, false, true));

        assertTrue(this.selectionLayer.isRowPositionFullySelected(2), "row 2 is not selected");
        assertTrue(this.selectionLayer.isRowPositionFullySelected(3), "row 3 is not selected");

        this.rowHideShowLayer.doCommand(new MultiRowHideCommand(this.selectionLayer, 2, 3));

        assertEquals(16, this.selectionLayer.getRowCount());
        assertEquals(0, this.selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedColumnPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowPositions().size());
    }

}
