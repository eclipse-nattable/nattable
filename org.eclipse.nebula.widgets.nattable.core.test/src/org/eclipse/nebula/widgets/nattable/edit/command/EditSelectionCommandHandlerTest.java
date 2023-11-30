/*******************************************************************************
 * Copyright (c) 2023 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EditSelectionCommandHandlerTest {

    private static final String TEST_LABEL = "testLabel";

    private NatTableFixture natTable;
    private DummyGridLayerStack gridLayerStack;
    private SelectionLayer selectionLayer;
    private EditSelectionCommandHandler handler;

    @BeforeEach
    public void setup() {
        this.gridLayerStack = new DummyGridLayerStack(5, 5);
        this.selectionLayer = this.gridLayerStack.getBodyLayer().getSelectionLayer();
        this.natTable = new NatTableFixture(this.gridLayerStack);

        this.handler = new EditSelectionCommandHandler(this.selectionLayer);

        // Ensure no active editor (static) is present
        assertNull(this.natTable.getActiveCellEditor());
    }

    @Test
    public void shouldHandleOnlyAllEditable() {
        // by default editing should only be possible if all cells are editable
        assertTrue(this.handler.handleOnlyAllSelectedEditable(this.natTable.getConfigRegistry()));
    }

    @Test
    public void shouldHandleOnlyAllEditableViaConfiguration() {
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.MULTI_EDIT_ALL_SELECTED_EDITABLE,
                true);
        assertTrue(this.handler.handleOnlyAllSelectedEditable(this.natTable.getConfigRegistry()));
    }

    @Test
    public void shouldHandleAllEditableInSelection() {
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.MULTI_EDIT_ALL_SELECTED_EDITABLE,
                false);
        assertFalse(this.handler.handleOnlyAllSelectedEditable(this.natTable.getConfigRegistry()));
    }

    @Test
    public void testGetSelectedCellsForEditing() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        Collection<ILayerCell> cellsForEditing = this.handler.getCellsForEditing(this.selectionLayer, null, this.natTable.getConfigRegistry(), false);

        // by default check if all selected cells are editable, as editing is
        // disabled, no cells are returned
        assertEquals(0, cellsForEditing.size());
    }

    @Test
    public void testGetSelectedCellsForEditingEditingEnabled() {
        this.natTable.enableEditingOnAllCells();

        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        Collection<ILayerCell> cellsForEditing = this.handler.getCellsForEditing(this.selectionLayer, null, this.natTable.getConfigRegistry(), false);

        assertEquals(3, cellsForEditing.size());
    }

    @Test
    public void testGetSelectedCellsForEditingEditingEnabledForSingleCell() {
        DataLayer bodyDataLayer = (DataLayer) this.gridLayerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, 2, TEST_LABEL);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.ALWAYS_EDITABLE,
                DisplayMode.EDIT,
                TEST_LABEL);

        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        Collection<ILayerCell> cellsForEditing = this.handler.getCellsForEditing(this.selectionLayer, null, this.natTable.getConfigRegistry(), false);

        // not all selected cells are editable, so no cells
        assertEquals(0, cellsForEditing.size());
    }

    @Test
    public void testGetSelectedCellsForEditingHandleSelectedEditable() {
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.MULTI_EDIT_ALL_SELECTED_EDITABLE,
                false);

        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        Collection<ILayerCell> cellsForEditing = this.handler.getCellsForEditing(this.selectionLayer, null, this.natTable.getConfigRegistry(), false);

        // as editing is disabled, no cells are returned
        assertEquals(0, cellsForEditing.size());
    }

    @Test
    public void testGetSelectedCellsForEditingHandleSelectedEditableEditingEnabled() {
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.MULTI_EDIT_ALL_SELECTED_EDITABLE,
                false);

        this.natTable.enableEditingOnAllCells();

        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        Collection<ILayerCell> cellsForEditing = this.handler.getCellsForEditing(this.selectionLayer, null, this.natTable.getConfigRegistry(), false);

        assertEquals(3, cellsForEditing.size());
    }

    @Test
    public void testGetSelectedCellsForEditingHandleSelectedEditableEditingEnabledForSingleCell() {
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.MULTI_EDIT_ALL_SELECTED_EDITABLE,
                false);

        DataLayer bodyDataLayer = (DataLayer) this.gridLayerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, 2, TEST_LABEL);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.ALWAYS_EDITABLE,
                DisplayMode.EDIT,
                TEST_LABEL);

        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        Collection<ILayerCell> cellsForEditing = this.handler.getCellsForEditing(this.selectionLayer, null, this.natTable.getConfigRegistry(), false);

        // only one of the selected cells is editable, so size is 1
        assertEquals(1, cellsForEditing.size());
    }

}
