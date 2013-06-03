/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
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

public class EditUtilsTest {

	private static final String TEST_LABEL = "testLabel";

	private NatTableFixture natTable;
	private DummyGridLayerStack gridLayerStack;
	private SelectionLayer selectionLayer;

	@Before
	public void setup() {
		gridLayerStack = new DummyGridLayerStack(5, 5);
		selectionLayer = gridLayerStack.getBodyLayer().getSelectionLayer();
		natTable = new NatTableFixture(gridLayerStack);

		// Ensure no active editor (static) is present
		assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
	}

	@Test
	public void testGetLastSelectedCellWithoutSelection() {
		assertNull(EditUtils.getLastSelectedCell(selectionLayer));
	}

	@Test
	public void testGetLastSelectedCellWithSingleSelection() {
		selectionLayer.selectCell(1, 1, false, false);
		ILayerCell cell = EditUtils.getLastSelectedCell(selectionLayer);
		assertNotNull(cell);
		assertEquals(1, cell.getColumnIndex());
		assertEquals(1, cell.getRowIndex());
	}

	@Test
	public void testGetLastSelectedCellWithMultiSelection() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);
		ILayerCell cell = EditUtils.getLastSelectedCell(selectionLayer);
		assertNotNull(cell);
		assertEquals(3, cell.getColumnIndex());
		assertEquals(3, cell.getRowIndex());
	}

	@Test
	public void testGetLastSelectedCellEditorWithoutSelection() {
		assertNull(EditUtils.getLastSelectedCellEditor(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testGetLastSelectedCellEditorWithSingleSelection() {
		selectionLayer.selectCell(1, 1, false, false);
		ICellEditor editor = EditUtils.getLastSelectedCellEditor(selectionLayer, natTable.getConfigRegistry());
		assertNotNull(editor);
		assertTrue(editor instanceof TextCellEditor);
	}

	@Test
	public void testGetLastSelectedCellEditorWithMultiSelection() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);
		ICellEditor editor = EditUtils.getLastSelectedCellEditor(selectionLayer, natTable.getConfigRegistry());
		assertNotNull(editor);
		assertTrue(editor instanceof TextCellEditor);
	}

	@Test
	public void testAllCellsEditableWithoutSelection() {
		assertTrue(EditUtils.allCellsEditable(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testAllCellsEditableWithoutSelectionEnableEditing() {
		natTable.enableEditingOnAllCells();
		assertTrue(EditUtils.allCellsEditable(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testAllCellsEditableWithSingleSelection() {
		selectionLayer.selectCell(1, 1, false, false);
		assertFalse(EditUtils.allCellsEditable(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testAllCellsEditableWithSingleSelectionEnableEditing() {
		natTable.enableEditingOnAllCells();
		selectionLayer.selectCell(1, 1, false, false);
		assertTrue(EditUtils.allCellsEditable(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testAllCellsEditableWithMultiSelection() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);
		assertFalse(EditUtils.allCellsEditable(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testAllCellsEditableWithMultiSelectionEnableEditing() {
		natTable.enableEditingOnAllCells();
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);
		assertTrue(EditUtils.allCellsEditable(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsCellEditableWithoutSelection() {
		PositionCoordinate coord = new PositionCoordinate(selectionLayer, 0, 0);
		assertFalse(EditUtils.isCellEditable(selectionLayer, natTable.getConfigRegistry(), coord));
	}

	@Test
	public void testIsCellEditableWithoutSelectionEnableEditing() {
		natTable.enableEditingOnAllCells();
		PositionCoordinate coord = new PositionCoordinate(selectionLayer, 0, 0);
		assertTrue(EditUtils.isCellEditable(selectionLayer, natTable.getConfigRegistry(), coord));
	}

	@Test
	public void testIsCellEditableWithSingleSelection() {
		selectionLayer.selectCell(1, 1, false, false);
		PositionCoordinate coord = new PositionCoordinate(selectionLayer, 1, 1);
		assertFalse(EditUtils.isCellEditable(selectionLayer, natTable.getConfigRegistry(), coord));
	}

	@Test
	public void testIsCellEditableWithSingleSelectionEnableEditing() {
		natTable.enableEditingOnAllCells();
		selectionLayer.selectCell(1, 1, false, false);
		PositionCoordinate coord = new PositionCoordinate(selectionLayer, 1, 1);
		assertTrue(EditUtils.isCellEditable(selectionLayer, natTable.getConfigRegistry(), coord));
	}

	@Test
	public void testIsEditorSameWithoutSelection() {
		assertTrue(EditUtils.isEditorSame(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsEditorSameWithSingleSelection() {
		selectionLayer.selectCell(1, 1, false, false);
		assertTrue(EditUtils.isEditorSame(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsEditorSameWithMultiSelection() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);
		assertTrue(EditUtils.isEditorSame(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsEditorSameWithMultiSelectionOneChangedEditor() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);

		DataLayer bodyDataLayer = (DataLayer) gridLayerStack.getBodyDataLayer();
		natTable.registerLabelOnColumn(bodyDataLayer, 1, TEST_LABEL);
		natTable.getConfigRegistry().registerConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, new CheckBoxCellEditor(), DisplayMode.EDIT, TEST_LABEL);
		
		assertFalse(EditUtils.isEditorSame(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsConverterSameWithoutSelection() {
		assertTrue(EditUtils.isConverterSame(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsConverterSameWithSingleSelection() {
		selectionLayer.selectCell(1, 1, false, false);
		assertTrue(EditUtils.isConverterSame(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsConverterSameWithMultiSelection() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);
		assertTrue(EditUtils.isConverterSame(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsConverterSameWithMultiSelectionOneChangedConverter() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);

		DataLayer bodyDataLayer = (DataLayer) gridLayerStack.getBodyDataLayer();
		natTable.registerLabelOnColumn(bodyDataLayer, 1, TEST_LABEL);
		natTable.getConfigRegistry().registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, 
				new DefaultBooleanDisplayConverter(), 
				DisplayMode.EDIT, TEST_LABEL);
		
		assertFalse(EditUtils.isConverterSame(selectionLayer, natTable.getConfigRegistry()));
	}

	@Test
	public void testIsValueSameWithoutSelection() {
		assertTrue(EditUtils.isValueSame(selectionLayer));
	}

	@Test
	public void testIsValueSameWithSingleSelection() {
		selectionLayer.selectCell(1, 1, false, false);
		assertTrue(EditUtils.isValueSame(selectionLayer));
	}

	@Test
	public void testIsValueSameWithMultiSelection() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);
		assertFalse(EditUtils.isValueSame(selectionLayer));
	}

	@Test
	public void testIsValueSameWithMultiSelectionEnsureSameValues() {
		selectionLayer.selectCell(1, 1, false, true);
		selectionLayer.selectCell(2, 2, false, true);
		selectionLayer.selectCell(3, 3, false, true);

		selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, 1, 1, "Test"));
		selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, 2, 2, "Test"));
		selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, 3, 3, "Test"));

		assertTrue(EditUtils.isValueSame(selectionLayer));
	}

}
