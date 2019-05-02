/*******************************************************************************
 * Copyright (c) 2013, 2019 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.ActiveCellEditorRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class EditUtilsTest {

    private static final String TEST_LABEL = "testLabel";

    private NatTableFixture natTable;
    private DummyGridLayerStack gridLayerStack;
    private SelectionLayer selectionLayer;

    @Before
    public void setup() {
        this.gridLayerStack = new DummyGridLayerStack(5, 5);
        this.selectionLayer = this.gridLayerStack.getBodyLayer().getSelectionLayer();
        this.natTable = new NatTableFixture(this.gridLayerStack);

        // Ensure no active editor (static) is present
        // Although deprecated this needs to still work for backwards
        // compatibility
        assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
    }

    @Test
    public void testGetLastSelectedCellWithoutSelection() {
        assertNull(EditUtils.getLastSelectedCell(this.selectionLayer));
    }

    @Test
    public void testGetLastSelectedCellWithSingleSelection() {
        this.selectionLayer.selectCell(1, 1, false, false);
        ILayerCell cell = EditUtils.getLastSelectedCell(this.selectionLayer);
        assertNotNull(cell);
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getRowIndex());
    }

    @Test
    public void testGetLastSelectedCellWithMultiSelection() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);
        ILayerCell cell = EditUtils.getLastSelectedCell(this.selectionLayer);
        assertNotNull(cell);
        assertEquals(3, cell.getColumnIndex());
        assertEquals(3, cell.getRowIndex());
    }

    @Test
    public void testGetLastSelectedCellEditorWithoutSelection() {
        assertNull(EditUtils.getLastSelectedCellEditor(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testGetLastSelectedCellEditorWithSingleSelection() {
        this.selectionLayer.selectCell(1, 1, false, false);
        ICellEditor editor = EditUtils.getLastSelectedCellEditor(this.selectionLayer, this.natTable.getConfigRegistry());
        assertNotNull(editor);
        assertTrue(editor instanceof TextCellEditor);
    }

    @Test
    public void testGetLastSelectedCellEditorWithMultiSelection() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);
        ICellEditor editor = EditUtils.getLastSelectedCellEditor(this.selectionLayer, this.natTable.getConfigRegistry());
        assertNotNull(editor);
        assertTrue(editor instanceof TextCellEditor);
    }

    @Test
    public void testAllCellsEditableWithoutSelection() {
        assertTrue(EditUtils.allCellsEditable(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testAllCellsEditableWithoutSelectionEnableEditing() {
        this.natTable.enableEditingOnAllCells();
        assertTrue(EditUtils.allCellsEditable(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testAllCellsEditableWithSingleSelection() {
        this.selectionLayer.selectCell(1, 1, false, false);
        assertFalse(EditUtils.allCellsEditable(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testAllCellsEditableWithSingleSelectionEnableEditing() {
        this.natTable.enableEditingOnAllCells();
        this.selectionLayer.selectCell(1, 1, false, false);
        assertTrue(EditUtils.allCellsEditable(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testAllCellsEditableWithMultiSelection() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);
        assertFalse(EditUtils.allCellsEditable(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testAllCellsEditableWithMultiSelectionEnableEditing() {
        this.natTable.enableEditingOnAllCells();
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);
        assertTrue(EditUtils.allCellsEditable(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsCellEditableWithoutSelection() {
        PositionCoordinate coord = new PositionCoordinate(this.selectionLayer, 0, 0);
        assertFalse(EditUtils.isCellEditable(coord, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsCellEditableWithoutSelectionEnableEditing() {
        this.natTable.enableEditingOnAllCells();
        PositionCoordinate coord = new PositionCoordinate(this.selectionLayer, 0, 0);
        assertTrue(EditUtils.isCellEditable(coord, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsCellEditableWithSingleSelection() {
        this.selectionLayer.selectCell(1, 1, false, false);
        PositionCoordinate coord = new PositionCoordinate(this.selectionLayer, 1, 1);
        assertFalse(EditUtils.isCellEditable(coord, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsCellEditableWithSingleSelectionEnableEditing() {
        this.natTable.enableEditingOnAllCells();
        this.selectionLayer.selectCell(1, 1, false, false);
        PositionCoordinate coord = new PositionCoordinate(this.selectionLayer, 1, 1);
        assertTrue(EditUtils.isCellEditable(coord, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsEditorSameWithoutSelection() {
        assertTrue(EditUtils.isEditorSame(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsEditorSameWithSingleSelection() {
        this.selectionLayer.selectCell(1, 1, false, false);
        assertTrue(EditUtils.isEditorSame(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsEditorSameWithMultiSelection() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);
        assertTrue(EditUtils.isEditorSame(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsEditorSameWithMultiSelectionOneChangedEditor() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        DataLayer bodyDataLayer = (DataLayer) this.gridLayerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, 1, TEST_LABEL);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITOR,
                new CheckBoxCellEditor(),
                DisplayMode.EDIT, TEST_LABEL);

        assertFalse(EditUtils.isEditorSame(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsConverterSameWithoutSelection() {
        assertTrue(EditUtils.isConverterSame(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsConverterSameWithSingleSelection() {
        this.selectionLayer.selectCell(1, 1, false, false);
        assertTrue(EditUtils.isConverterSame(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsConverterSameWithMultiSelection() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);
        assertTrue(EditUtils.isConverterSame(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsConverterSameWithMultiSelectionOneChangedConverter() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        DataLayer bodyDataLayer = (DataLayer) this.gridLayerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, 1, TEST_LABEL);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new DefaultBooleanDisplayConverter(),
                DisplayMode.EDIT,
                TEST_LABEL);

        assertFalse(EditUtils.isConverterSame(this.selectionLayer, this.natTable.getConfigRegistry()));
    }

    @Test
    public void testIsValueSameWithoutSelection() {
        assertTrue(EditUtils.isValueSame(this.selectionLayer));
    }

    @Test
    public void testIsValueSameWithSingleSelection() {
        this.selectionLayer.selectCell(1, 1, false, false);
        assertTrue(EditUtils.isValueSame(this.selectionLayer));
    }

    @Test
    public void testIsValueSameWithMultiSelection() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);
        assertFalse(EditUtils.isValueSame(this.selectionLayer));
    }

    @Test
    public void testIsValueSameWithMultiSelectionEnsureSameValues() {
        this.selectionLayer.selectCell(1, 1, false, true);
        this.selectionLayer.selectCell(2, 2, false, true);
        this.selectionLayer.selectCell(3, 3, false, true);

        this.selectionLayer.doCommand(new UpdateDataCommand(this.selectionLayer, 1, 1, "Test"));
        this.selectionLayer.doCommand(new UpdateDataCommand(this.selectionLayer, 2, 2, "Test"));
        this.selectionLayer.doCommand(new UpdateDataCommand(this.selectionLayer, 3, 3, "Test"));

        assertTrue(EditUtils.isValueSame(this.selectionLayer));
    }

}
