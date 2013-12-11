/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.ActiveCellEditorRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.command.EditCellCommand;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.event.InlineCellEditEvent;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.data.DummySpanningBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.widget.NatCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EditIntegrationTest {

	private static final String TEST_LABEL = "testLabel";
	private static final String TEST_LABEL_2 = "testLabel2";

	private static final int COLUMN_HEADER_ROW_COUNT = 1;
	private static final int ROW_HEADER_COLUMN_COUNT = 1;

	private NatTableFixture natTable;
	private DummyGridLayerStack gridLayerStack;

	@Before
	public void setup() {
		gridLayerStack = new DummyGridLayerStack(5, 5);
		natTable = new NatTableFixture(gridLayerStack);

		// Ensure no active editor (static) is present
		assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
	}

	@Test
	public void testNotEditableByDefault() {
		ILayerCell cell = natTable.getCellByPosition(4, 4);
		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), cell));

		assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
	}

	@Test
	public void testEditorActivatedDuringInlineCellEdit() {
		natTable.enableEditingOnAllCells();

		ILayerCell cell = natTable.getCellByPosition(4, 4);
		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), cell));

		ICellEditor cellEditor = ActiveCellEditorRegistry.getActiveCellEditor();
		assertNotNull(cellEditor);
		assertTrue(cellEditor instanceof TextCellEditor);
		TextCellEditor textCellEditor = (TextCellEditor) cellEditor;
		assertEquals("Col: 4, Row: 4", textCellEditor.getCanonicalValue());

		Control control = cellEditor.getEditorControl();
		assertNotNull(control);
		assertTrue(control instanceof Text);
	}

	@Test
	public void testEditorClosesWhenANewEditCommandIsFired() {
		// Even rows are editable, Odd rows are not
		natTable.getConfigRegistry().registerConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE,
				DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
		natTable.getConfigRegistry().registerConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE,
				DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), 
				natTable.getCellByPosition(4, COLUMN_HEADER_ROW_COUNT + 2)));
		assertNotNull(ActiveCellEditorRegistry.getActiveCellEditor());

		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), 
				natTable.getCellByPosition(4, COLUMN_HEADER_ROW_COUNT + 3)));
		assertNotNull(ActiveCellEditorRegistry.getActiveCellEditor());
		assertFalse(ActiveCellEditorRegistry.getActiveCellEditor().isClosed());
	}

	@Test
	public void testEditorResize() {
		natTable.enableEditingOnAllCells();

		ILayerCell cell = natTable.getCellByPosition(4, 4);
		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), cell));
		assertEquals(new Rectangle(340, 80, 99, 19), 
				ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl().getBounds());

		natTable.doCommand(new ColumnResizeCommand(natTable, 2, 110));
		assertEquals(new Rectangle(340, 80, 99, 19), 
				ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl().getBounds());
		
		ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl().notifyListeners(
				SWT.FocusOut, null);
		//ActiveCellEditor should be closed if a ColumnResizeCommand is executed and the editor loses focus
		assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
	}

	@Test
	public void testDataValidation() {
		DummyGridLayerStack gridLayerStack = new DummyGridLayerStack(5, 5);
		natTable = new NatTableFixture(gridLayerStack);

		// Register custom validation
		DataLayer bodyDataLayer = (DataLayer) gridLayerStack.getBodyDataLayer();
		natTable.registerLabelOnColumn(bodyDataLayer, 0, TEST_LABEL);
		natTable.registerLabelOnColumn(bodyDataLayer, 1, TEST_LABEL_2);

		natTable.enableEditingOnAllCells();
		natTable.getConfigRegistry().registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, IDataValidator.NEVER_VALID,
																DisplayMode.EDIT, TEST_LABEL);
		natTable.getConfigRegistry().registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, IDataValidator.ALWAYS_VALID,
																DisplayMode.EDIT, TEST_LABEL_2);

		ILayerCell cell = natTable.getCellByPosition(1, 1);
		assertEquals("Col: 1, Row: 1", cell.getDataValue());

		// Column index 0 never valid
		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), cell));
		assertFalse(ActiveCellEditorRegistry.getActiveCellEditor().validateCanonicalValue(cell.getDataValue()));

		cell = natTable.getCellByPosition(2, 1);
		assertEquals("Col: 2, Row: 1", cell.getDataValue());

		// Column index 1 always valid
		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), cell));
		assertTrue(ActiveCellEditorRegistry.getActiveCellEditor().validateCanonicalValue(cell.getDataValue()));
	}

	@Test
	public void navigationWithTab() throws Exception {
		natTable.enableEditingOnAllCells();
		natTable.doCommand(new SelectCellCommand(natTable, 1, 1, false, false));

		// Edit cell
		ILayerCell cell = natTable.getCellByPosition(1, 1);
		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), cell));

		// Press tab - 3 times
		Text textControl = ((Text) ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl());
		textControl.notifyListeners(SWT.Traverse, SWTUtils.keyEvent(SWT.TAB));

		natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.TAB));
		natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.TAB));
		natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.TAB));

		// Verify new cell selection
		PositionCoordinate lastSelectedCellPosition = gridLayerStack.getBodyLayer().getSelectionLayer().getSelectionAnchor();
		assertEquals(4, lastSelectedCellPosition.columnPosition);
		assertEquals(0, lastSelectedCellPosition.rowPosition);

		// Verify that no cell is being edited
		assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
	}
	@Test
	public void testNavigationUsingTabButtonWhenAnInvalidValueIsEntered() throws InterruptedException {
		natTable.enableEditingOnAllCells();

		DataLayer bodyDataLayer = (DataLayer) gridLayerStack.getBodyDataLayer();
		natTable.registerLabelOnColumn(bodyDataLayer, 0, TEST_LABEL);
		natTable.getConfigRegistry().registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getStartingWithCValidator(), DisplayMode.EDIT, TEST_LABEL);

		// Start editing 1,1
		ILayerCell cell = natTable.getCellByPosition(1, 1);
		assertEquals("Col: 1, Row: 1", cell.getDataValue());
		natTable.doCommand(new SelectCellCommand(natTable, 1, 1, false, false));

		// Column position 1 - originally valid
		natTable.doCommand(new EditCellCommand(natTable, natTable.getConfigRegistry(), cell));
		assertTrue(ActiveCellEditorRegistry.getActiveCellEditor().validateCanonicalValue(cell.getDataValue()));

		// Set an invalid value in cell - AA
		Text textControl = ((Text) ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl());
		textControl.setText("AA");
		assertEquals("AA", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue());
		assertFalse(ActiveCellEditorRegistry.getActiveCellEditor().validateCanonicalValue(
				ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue()));

		// Press tab
		textControl.notifyListeners(SWT.Traverse, SWTUtils.keyEvent(SWT.TAB));
		assertEquals(textControl, ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl());
		assertEquals("AA", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue());
	}

	@Test
	public void directlyTypingInACellShoudlStartEditing() throws Exception {
		// Press 'A'
		natTable.enableEditingOnAllCells();
		natTable.doCommand(new SelectCellCommand(natTable, 1, 1, false, false));
		natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEventWithChar('A'));

		// Verify edit mode
		assertNotNull(ActiveCellEditorRegistry.getActiveCellEditor());
		assertEquals("A", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue());
	}

	@Test
	public void mustCommitValidValuesOnPressingEnter() throws Exception {
		natTable.enableEditingOnAllCells();

		// Cell value is valid if starting with 'C'
		DataLayer bodyDataLayer = (DataLayer) gridLayerStack.getBodyDataLayer();
		natTable.registerLabelOnColumn(bodyDataLayer, 0, TEST_LABEL);
		natTable.getConfigRegistry().registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, getStartingWithCValidator(), DisplayMode.EDIT, TEST_LABEL);

		// Enter 'A' in the cell
		natTable.doCommand(new SelectCellCommand(natTable, 1, 1, false, false));
		natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEventWithChar('A'));
		assertEquals("A", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue());
		assertFalse(ActiveCellEditorRegistry.getActiveCellEditor().validateCanonicalValue(
				ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue()));

		// Press 'Enter'
		ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl().notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.CR));

		// Value not committed
		assertNotNull(ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl());
		assertEquals("A", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue());

		// Enter a valid value - 'C'
		natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEventWithChar('C'));
		assertNotNull(ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl());
		assertEquals("C", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue());

		// Press 'Enter' again
		ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl().notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.CR));

		// Value committed and editor closed
		assertEquals("C", natTable.getCellByPosition(1, 1).getDataValue());
		assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
	}

	@Test
	public void clickingOnTheCheckBoxMustToggleItsValue() throws Exception {
		DefaultGridLayer layerStack = new DefaultGridLayer(RowDataListFixture.getList(), RowDataListFixture.getPropertyNames(), RowDataListFixture.getPropertyToLabelMap());
		natTable = new NatTableFixture(layerStack, 1200, 300, false);

		// Enable editing
		natTable.enableEditingOnAllCells();

		// Calculate pixel value to click on
		int columnIndex = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.PUBLISH_FLAG_PROP_NAME);
		int columnPosition = columnIndex + ROW_HEADER_COLUMN_COUNT ;
		int startX = natTable.getStartXOfColumnPosition(columnPosition);
		int columnWidth = natTable.getColumnWidthByPosition(columnPosition);
		int startY = natTable.getStartYOfRowPosition(1);
		int rowHeight = natTable.getRowHeightByPosition(1);

		// Register check box for the publish flag column
		DataLayer bodyDataLayer = (DataLayer) layerStack.getBodyDataLayer();
		natTable.registerLabelOnColumn(bodyDataLayer, columnIndex, TEST_LABEL);
		registerCheckBoxEditor(natTable.getConfigRegistry(), new CheckBoxPainter(), new CheckBoxCellEditor());

		natTable.configure();

		// Value before click
		assertEquals(true, natTable.getDataValueByPosition(columnPosition, 1));

		// Click on the check box
		SWTUtils.leftClick(startX + (columnWidth / 2), startY + (rowHeight / 2), SWT.NONE, natTable);

		// Value After click
		assertEquals(false, natTable.getDataValueByPosition(columnPosition, 1));
	}

	@Test
	public void pressingESCMustDiscardTheValueEnteredAndCloseControl() throws Exception {
		natTable.enableEditingOnAllCells();

		assertEquals("Col: 1, Row: 1", natTable.getDataValueByPosition(1, 1));

		// Select cell, press A
		natTable.doCommand(new SelectCellCommand(natTable, 1, 1, false, false));
		SWTUtils.pressCharKey('A', natTable);

		// Verify edit mode
		assertNotNull(ActiveCellEditorRegistry.getActiveCellEditor());
		assertEquals("A", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue());

		// Press ESC
		SWTUtils.pressKeyOnControl(SWT.ESC, ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl());

		// Verify state
		assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
		assertEquals("Col: 1, Row: 1", natTable.getDataValueByPosition(1, 1));
	}

	@Test
	public void comboBoxShouldCommitWhenAValueIsSelectedByClickingOnIt() throws Exception {
		if(SWTUtils.isRunningOnUnix()){
			return;
		}
		DefaultGridLayer layerStack = new DefaultGridLayer(RowDataListFixture.getList(), RowDataListFixture.getPropertyNames(), RowDataListFixture.getPropertyToLabelMap());
		natTable = new NatTableFixture(layerStack, 1200, 300, false);

		// Enable editing
		natTable.enableEditingOnAllCells();

		// Calculate pixel value to click on
		int columnIndex = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.PRICING_TYPE_PROP_NAME);
		int columnPosition = columnIndex + ROW_HEADER_COLUMN_COUNT ;
		int rowPosition = 0 + COLUMN_HEADER_ROW_COUNT;
		int startX = natTable.getStartXOfColumnPosition(columnPosition);
		int startY = natTable.getStartYOfRowPosition(1);

		// Register combo box for the publish flag column
		DataLayer bodyDataLayer = (DataLayer) layerStack.getBodyDataLayer();
		natTable.registerLabelOnColumn(bodyDataLayer, columnIndex, TEST_LABEL);
		registerComboBox(natTable.getConfigRegistry(),
				new ComboBoxPainter(),
				new ComboBoxCellEditor(Arrays.asList(new PricingTypeBean("MN"), new PricingTypeBean("AT"))));

		natTable.configure();

		//Original value
		assertTrue(natTable.getDataValueByPosition(columnPosition, rowPosition) instanceof PricingTypeBean);
		assertEquals("MN", natTable.getDataValueByPosition(columnPosition, rowPosition).toString());

		// Click - expand combo
		SWTUtils.leftClick(startX + 10, startY + 10, SWT.NONE, natTable);

		NatCombo combo = (NatCombo) ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl();
		assertNotNull(combo);
		assertTrue(ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue() instanceof PricingTypeBean);
		assertEquals("MN", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue().toString());

		// Click - expand select value 'Automatic'
		combo.select(1);
		SWTUtils.leftClickOnCombo(startX + 10, startY + 35, SWT.NONE, combo);

		assertTrue(natTable.getDataValueByPosition(columnPosition, rowPosition) instanceof PricingTypeBean);
		assertEquals("AT", natTable.getDataValueByPosition(columnPosition, rowPosition).toString());
		assertNull(ActiveCellEditorRegistry.getActiveCellEditor());
	}

	@Test
	public void openEditorForSpannedCellsShouldOpenInline() throws Exception {
		CompositeLayer layer = new CompositeLayer(1, 1);
		SelectionLayer selectionLayer = new SelectionLayer(new SpanningDataLayer(new DummySpanningBodyDataProvider(100, 100)));
		layer.setChildLayer(GridRegion.BODY, new ViewportLayer(selectionLayer), 0, 0);
		natTable = new NatTableFixture(layer, 1200, 300, false);

		layer.addConfiguration(new DefaultEditBindings());
		layer.addConfiguration(new DefaultEditConfiguration());
		
		natTable.enableEditingOnAllCells();

		final boolean[] inlineFired = new boolean[1];
		inlineFired[0] = false;
		selectionLayer.addLayerListener(new ILayerListener() {
			
			@Override
			public void handleLayerEvent(ILayerEvent event) {
				if (event instanceof InlineCellEditEvent) {
					inlineFired[0] = true;
				}
			}
		});
		
		natTable.configure();
		
		natTable.doCommand(new SelectCellCommand(natTable, 1, 1, false, false));
		natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.F2));

		// Verify edit mode
		assertNotNull(ActiveCellEditorRegistry.getActiveCellEditor());
		assertEquals("Col: 1, Row: 1", ActiveCellEditorRegistry.getActiveCellEditor().getCanonicalValue());

		//verify that inline editing is used and not dialog
		assertTrue("No InlineCellEditEvent fired", inlineFired[0]);
	}

	@Test
	public void updateAllUnderlyingCellsIfSpanned() throws Exception {
		CompositeLayer layer = new CompositeLayer(1, 1);
		DummySpanningBodyDataProvider dataProvider = new DummySpanningBodyDataProvider(100, 100);
		SelectionLayer selectionLayer = new SelectionLayer(new SpanningDataLayer(dataProvider));
		layer.setChildLayer(GridRegion.BODY, new ViewportLayer(selectionLayer), 0, 0);
		natTable = new NatTableFixture(layer, 1200, 300, false);

		layer.addConfiguration(new DefaultEditBindings());
		layer.addConfiguration(new DefaultEditConfiguration());
		
		natTable.enableEditingOnAllCells();

		natTable.configure();
		
		assertEquals("Col: 1, Row: 1", dataProvider.getDataValue(0, 0));
		assertEquals("Col: 1, Row: 2", dataProvider.getDataValue(0, 1));
		assertEquals("Col: 2, Row: 1", dataProvider.getDataValue(1, 0));
		assertEquals("Col: 2, Row: 2", dataProvider.getDataValue(1, 1));
		
		natTable.doCommand(new SelectCellCommand(natTable, 1, 1, false, false));
		natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEventWithChar('C'));

		assertNotNull(ActiveCellEditorRegistry.getActiveCellEditor());

		ActiveCellEditorRegistry.getActiveCellEditor().getEditorControl().notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.CR));

		assertEquals("C", dataProvider.getDataValue(0, 0));
		assertEquals("C", dataProvider.getDataValue(0, 1));
		assertEquals("C", dataProvider.getDataValue(1, 0));
		assertEquals("C", dataProvider.getDataValue(1, 1));
	}

	// *** Convenience methods ***.
	// Mostly code from the EditableGridExample.
	// The sane fixtures are used to ensure that the example keeps working without fail

	private static void registerComboBox(ConfigRegistry configRegistry, ICellPainter comboBoxCellPainter, ICellEditor comboBoxCellEditor) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, comboBoxCellPainter, DisplayMode.NORMAL, TEST_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor, DisplayMode.NORMAL, TEST_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor, DisplayMode.EDIT, TEST_LABEL);

		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, getPricingTypeDisplayConverter(), DisplayMode.NORMAL, TEST_LABEL);
	}

	private static void registerCheckBoxEditor(ConfigRegistry configRegistry, ICellPainter checkBoxCellPainter, ICellEditor checkBoxCellEditor) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, checkBoxCellPainter, DisplayMode.NORMAL, TEST_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL, TEST_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, checkBoxCellEditor, DisplayMode.NORMAL, TEST_LABEL);
	}


	private static IDisplayConverter getPricingTypeDisplayConverter() {
		return new DisplayConverter() {
			@Override
			public Object canonicalToDisplayValue(Object canonicalValue) {
				if (canonicalValue == null) {
					return null;
				} else {
					return canonicalValue.toString().equals("MN") ? "Manual" : "Automatic";
				}
			}

			@Override
			public Object displayToCanonicalValue(Object displayValue) {
				return displayValue.toString().equals("Manual") ? new PricingTypeBean("MN") : new PricingTypeBean("AT");
			}
		};
	}

	private IDataValidator getStartingWithCValidator() {
		return new DataValidator() {
			@Override
			public boolean validate(int columnIndex, int rowIndex, Object newValue) {
				String asString = newValue.toString();
				return asString.startsWith("C");
			}
		};
	}

	@After
	public void clearStaticEditor() {
		ActiveCellEditorRegistry.unregisterActiveCellEditor();
	}

}

