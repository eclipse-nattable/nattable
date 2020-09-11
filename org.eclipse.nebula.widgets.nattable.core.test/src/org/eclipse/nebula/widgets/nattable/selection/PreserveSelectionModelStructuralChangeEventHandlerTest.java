/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.preserve.PreserveSelectionModel;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class PreserveSelectionModelStructuralChangeEventHandlerTest {

    private NatTable nattable;
    private List<RowDataFixture> listFixture;
    private IRowDataProvider<RowDataFixture> bodyDataProvider;
    private DataLayer bodyDataLayer;
    private SelectionLayer selectionLayer;

    @Before
    public void setup() {
        this.listFixture = RowDataListFixture.getList(10);
        this.bodyDataProvider = new ListDataProvider<RowDataFixture>(this.listFixture,
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()));

        GridLayerFixture gridLayer = new GridLayerFixture(this.bodyDataProvider);
        this.nattable = new NatTableFixture(gridLayer, false);

        this.bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
        this.selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();

        this.selectionLayer.setSelectionModel(new PreserveSelectionModel<RowDataFixture>(
                this.selectionLayer, this.bodyDataProvider,
                new IRowIdAccessor<RowDataFixture>() {

                    @Override
                    public Serializable getRowId(
                            RowDataFixture rowObject) {
                        return rowObject.getSecurity_id();
                    }

                }));
    }

    @Test
    public void shouldRetainRowSelectionOnUpdates() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        this.listFixture.add(0, RowDataFixture.getInstance("Tata motors", "A"));

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(this.bodyDataLayer));

        // Tata motors at top but Ford motors still selected
        assertEquals("Tata motors", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());
    }

    @Test
    public void shouldRetainRowSelectionOnMove() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        RowDataFixture ford = getSelected();

        // move selected to the bottom
        this.listFixture.remove(ford);
        this.listFixture.add(ford);

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(this.bodyDataLayer));

        // Tata motors at top but Ford motors still selected
        assertEquals("A Alphabet Co.", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());
        assertEquals(this.listFixture.size() - 1l, this.selectionLayer.getFullySelectedRowPositions()[0]);
    }

    @Test
    public void shouldRemoveSelectionOnDelete() {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        this.listFixture.remove(0);

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new RowDeleteEvent(this.bodyDataLayer, 0));

        // another value on top now
        assertEquals("A Alphabet Co.", this.nattable.getDataValueByPosition(2, 1).toString());
        // selection should be empty since the selected row was deleted
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());
        assertEquals(0, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void shouldOnlyRemoveSelectionForDeleted() {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, new int[] { 1, 2 }, true, false, 1));
        assertEquals(2, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(2, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());

        boolean fordFound = false;
        boolean alphabetFound = false;
        for (Range selection : this.selectionLayer.getSelectedRowPositions()) {
            for (int i = selection.start; i < selection.end; i++) {
                if ("B Ford Motor".equals(this.listFixture.get(i).getSecurity_description())) {
                    fordFound = true;
                }
                if ("A Alphabet Co.".equals(this.listFixture.get(i).getSecurity_description())) {
                    alphabetFound = true;
                }
            }
        }
        assertTrue("B Ford Motor not found", fordFound);
        assertTrue("A Alphabet Co. not found", alphabetFound);

        this.listFixture.remove(0);

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new RowDeleteEvent(this.bodyDataLayer, 0));

        // another value on top now
        assertEquals("A Alphabet Co.", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("A Alphabet Co.", getSelected().getSecurity_description());
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void shouldClearSelectionOnClearingTableWithStructuralRefresh() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        // clear the table
        this.listFixture.clear();

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(this.bodyDataLayer));

        // row count of 1 for NatTable because of header
        assertEquals(1, this.nattable.getRowCount());
        assertEquals(0, this.selectionLayer.getSelectedRowCount());
        assertTrue("selection model is not empty", this.selectionLayer.getSelectionModel().getSelections().isEmpty());
    }

    @Test
    public void shouldClearSelectionOnClearingTableWithRowStructuralRefresh() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(0, this.selectionLayer.getSelectedRowCount());

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        // clear the table
        this.listFixture.clear();

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new RowStructuralRefreshEvent(this.bodyDataLayer));

        // row count of 1 for NatTable because of header
        assertEquals(1, this.nattable.getRowCount());
        assertEquals(0, this.selectionLayer.getSelectedRowCount());
        assertTrue("selection model is not empty", this.selectionLayer.getSelectionModel().getSelections().isEmpty());
    }

    @Test
    public void shouldRemoveCellSelectionOnRowDelete() {
        assertTrue("selection is not empty", this.selectionLayer.getSelectedCells().isEmpty());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getSelectedCells().size());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 2, 1, true, false));
        assertEquals(2, this.selectionLayer.getSelectedCells().size());

        this.listFixture.remove(0);

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new RowDeleteEvent(this.bodyDataLayer, 0));

        Collection<ILayerCell> selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(0, selectedCells.size());
    }

    @Test
    public void shouldRemovePartialCellSelectionOnRowDelete() {
        assertTrue("selection is not empty", this.selectionLayer.getSelectedCells().isEmpty());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getSelectedCells().size());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 2, 1, true, false));
        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 2, true, false));
        this.nattable.doCommand(new SelectCellCommand(this.nattable, 2, 2, true, false));
        assertEquals(4, this.selectionLayer.getSelectedCells().size());

        this.listFixture.remove(0);

        // fire event to trigger structural refresh
        this.bodyDataLayer.fireLayerEvent(new RowDeleteEvent(this.bodyDataLayer, 0));

        Collection<ILayerCell> selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(2, selectedCells.size());

        // the row position of the still selected cells need to be 0 as we
        // deleted row 0 and therefore the selection from row 1 should have
        // moved up
        for (ILayerCell cell : selectedCells) {
            assertEquals(0, cell.getRowPosition());
        }
    }

    @Test
    public void shouldRemoveCellSelectionOnColumnDelete() {
        assertTrue("selection is not empty", this.selectionLayer.getSelectedCells().isEmpty());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getSelectedCells().size());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 2, false, true));
        assertEquals(2, this.selectionLayer.getSelectedCells().size());

        this.nattable.doCommand(new ColumnHideCommand(this.nattable, 1));

        Collection<ILayerCell> selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(0, selectedCells.size());
    }

    @Test
    public void shouldRemovePartialCellSelectionOnColumnDelete() {
        assertTrue("selection is not empty", this.selectionLayer.getSelectedCells().isEmpty());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getSelectedCells().size());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 2, false, true));
        this.nattable.doCommand(new SelectCellCommand(this.nattable, 2, 1, false, true));
        this.nattable.doCommand(new SelectCellCommand(this.nattable, 2, 2, false, true));
        assertEquals(4, this.selectionLayer.getSelectedCells().size());

        this.nattable.doCommand(new ColumnHideCommand(this.nattable, 1));

        Collection<ILayerCell> selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(2, selectedCells.size());

        // the column position of the still selected cells need to be 0 as we
        // hide column 0 and therefore the selection from column 1 should have
        // moved to the left
        for (ILayerCell cell : selectedCells) {
            assertEquals(0, cell.getColumnPosition());
        }

        // insert again to verify the column position shift
        this.nattable.doCommand(new ShowAllColumnsCommand());

        // the deselected cells shouldn't get automatically selected again
        selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(2, selectedCells.size());

        // the column position of the selected cells need to be 1 as we
        // showed column 0 and therefore the selection from column 0 should have
        // moved to the right
        for (ILayerCell cell : selectedCells) {
            assertEquals(1, cell.getColumnPosition());
        }
    }

    @Test
    public void shouldRemovePartialSplitCellSelectionOnColumnDelete() {
        assertTrue("selection is not empty", this.selectionLayer.getSelectedCells().isEmpty());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getSelectedCells().size());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 5, 2, true, false));
        Collection<ILayerCell> selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(10, selectedCells.size());

        boolean[] found = new boolean[5];
        for (ILayerCell cell : selectedCells) {
            if (cell.getColumnPosition() >= 5) {
                fail("wrong column position selected");
            } else {
                found[cell.getColumnPosition()] = true;
            }
        }

        for (int i = 0; i < found.length; i++) {
            assertTrue("columnPosition " + i + " not selected", found[i]);
        }

        this.nattable.doCommand(new MultiColumnHideCommand(this.nattable, new int[] { 2, 4 }));
        selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(6, selectedCells.size());
        found = new boolean[3];
        for (ILayerCell cell : selectedCells) {
            if (cell.getColumnPosition() >= 3) {
                fail("wrong column position selected");
            } else {
                found[cell.getColumnPosition()] = true;
            }
        }

        for (int i = 0; i < found.length; i++) {
            assertTrue("columnPosition " + i + " not selected", found[i]);
        }

        // insert again to verify the column position shift
        this.nattable.doCommand(new ShowAllColumnsCommand());

        selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(6, selectedCells.size());

        found = new boolean[5];
        for (ILayerCell cell : selectedCells) {
            if (cell.getColumnPosition() >= 5) {
                fail("wrong column position selected");
            } else {
                found[cell.getColumnPosition()] = true;
            }
        }

        for (int i = 0; i < found.length; i++) {
            if (i == 0 || i == 2 || i == 4) {
                assertTrue("columnPosition " + i + " not selected", found[i]);
            }
        }
    }

    @Test
    public void shouldUpdateSelectionOnShiftSelect() {
        // integration test case for drag/shift selection
        // not really a test case for structural changes but important for
        // selection interactions
        assertTrue("selection is not empty", this.selectionLayer.getSelectedCells().isEmpty());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 3, 3, false, false));
        assertEquals(1, this.selectionLayer.getSelectedCells().size());

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 5, 5, true, false));
        Collection<ILayerCell> selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(9, selectedCells.size());

        boolean[] found = new boolean[3];
        for (ILayerCell cell : selectedCells) {
            if (cell.getColumnPosition() >= 5
                    || cell.getColumnPosition() < 2) {
                fail("wrong column position selected: " + cell.getColumnPosition());
            } else {
                found[cell.getColumnPosition() - 2] = true;
            }
        }

        for (int i = 0; i < found.length; i++) {
            assertTrue("columnPosition " + (i + 3) + " not selected", found[i]);
        }

        this.nattable.doCommand(new SelectCellCommand(this.nattable, 1, 1, true, false));
        selectedCells = this.selectionLayer.getSelectedCells();
        assertEquals(9, selectedCells.size());

        found = new boolean[3];
        for (ILayerCell cell : selectedCells) {
            if (cell.getColumnPosition() >= 3) {
                fail("wrong column position selected: " + cell.getColumnPosition());
            } else {
                found[cell.getColumnPosition()] = true;
            }
        }

        for (int i = 0; i < found.length; i++) {
            assertTrue("columnPosition " + i + " not selected", found[i]);
        }
    }

    private RowDataFixture getSelected() {
        Range selection = this.selectionLayer.getSelectedRowPositions().iterator().next();
        return this.listFixture.get(selection.start);
    }

}
