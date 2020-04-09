/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.command.EditCellCommand;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
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
import org.eclipse.nebula.widgets.nattable.layer.NoScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
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
        this.gridLayerStack = new DummyGridLayerStack(5, 5);
        this.natTable = new NatTableFixture(this.gridLayerStack);
        this.natTable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));

        // Ensure no active editor (static) is present
        assertNull(this.natTable.getActiveCellEditor());
    }

    @After
    public void tearDown() {
        // ensure the editor is closed
        this.natTable.commitAndCloseActiveCellEditor();
    }

    @Test
    public void testNotEditableByDefault() {
        ILayerCell cell = this.natTable.getCellByPosition(4, 4);
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));

        assertNull(this.natTable.getActiveCellEditor());
    }

    @Test
    public void testEditorActivatedDuringInlineCellEdit() {
        this.natTable.enableEditingOnAllCells();

        ILayerCell cell = this.natTable.getCellByPosition(4, 4);
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));

        ICellEditor cellEditor = this.natTable.getActiveCellEditor();
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
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.ALWAYS_EDITABLE, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.NEVER_EDITABLE, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(),
                this.natTable.getCellByPosition(4,
                        COLUMN_HEADER_ROW_COUNT + 2)));
        assertNotNull(this.natTable.getActiveCellEditor());

        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(),
                this.natTable.getCellByPosition(4,
                        COLUMN_HEADER_ROW_COUNT + 3)));
        assertNotNull(this.natTable.getActiveCellEditor());
        assertFalse(this.natTable.getActiveCellEditor().isClosed());
    }

    @Test
    public void testEditorResize() {
        this.natTable.enableEditingOnAllCells();

        ILayerCell cell = this.natTable.getCellByPosition(4, 4);
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));
        assertEquals(new Rectangle(340, 80, 99, 19), this.natTable
                .getActiveCellEditor().getEditorControl().getBounds());

        this.natTable.doCommand(new ColumnResizeCommand(this.natTable, 2, 110));
        assertEquals(new Rectangle(340, 80, 99, 19), this.natTable
                .getActiveCellEditor().getEditorControl().getBounds());

        this.natTable.getActiveCellEditor().getEditorControl()
                .notifyListeners(SWT.FocusOut, null);
        // ActiveCellEditor should be closed if a ColumnResizeCommand is
        // executed and the editor loses focus
        assertNull(this.natTable.getActiveCellEditor());
    }

    @Test
    public void testDataValidation() {
        DummyGridLayerStack gridLayerStack = new DummyGridLayerStack(5, 5);
        this.natTable = new NatTableFixture(gridLayerStack);

        // Register custom validation
        DataLayer bodyDataLayer = (DataLayer) gridLayerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, 0, TEST_LABEL);
        this.natTable.registerLabelOnColumn(bodyDataLayer, 1, TEST_LABEL_2);

        this.natTable.enableEditingOnAllCells();
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.DATA_VALIDATOR,
                IDataValidator.NEVER_VALID, DisplayMode.EDIT, TEST_LABEL);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.DATA_VALIDATOR,
                IDataValidator.ALWAYS_VALID, DisplayMode.EDIT, TEST_LABEL_2);

        ILayerCell cell = this.natTable.getCellByPosition(1, 1);
        assertEquals("Col: 1, Row: 1", cell.getDataValue());

        // Column index 0 never valid
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));
        assertFalse(this.natTable.getActiveCellEditor().validateCanonicalValue(
                cell.getDataValue()));

        cell = this.natTable.getCellByPosition(2, 1);
        assertEquals("Col: 2, Row: 1", cell.getDataValue());

        // Column index 1 always valid
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));
        assertTrue(this.natTable.getActiveCellEditor().validateCanonicalValue(
                cell.getDataValue()));
    }

    @Test
    public void navigationWithTab() throws Exception {
        this.natTable.enableEditingOnAllCells();
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 1, false, false));

        // Edit cell
        ILayerCell cell = this.natTable.getCellByPosition(1, 1);
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));

        // Press tab - 3 times
        Text textControl = ((Text) this.natTable.getActiveCellEditor()
                .getEditorControl());
        textControl.notifyListeners(SWT.Traverse, SWTUtils.keyEvent(SWT.TAB));

        this.natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.TAB));
        this.natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.TAB));
        this.natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.TAB));

        // Verify new cell selection
        PositionCoordinate lastSelectedCellPosition = this.gridLayerStack
                .getBodyLayer().getSelectionLayer().getSelectionAnchor();
        assertEquals(4, lastSelectedCellPosition.columnPosition);
        assertEquals(0, lastSelectedCellPosition.rowPosition);

        // Verify that no cell is being edited
        assertNull(this.natTable.getActiveCellEditor());
    }

    @Test
    public void testNavigationUsingTabButtonWhenAnInvalidValueIsEntered()
            throws InterruptedException {
        this.natTable.enableEditingOnAllCells();

        DataLayer bodyDataLayer = (DataLayer) this.gridLayerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, 0, TEST_LABEL);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.DATA_VALIDATOR,
                getStartingWithCValidator(), DisplayMode.EDIT, TEST_LABEL);

        // Start editing 1,1
        ILayerCell cell = this.natTable.getCellByPosition(1, 1);
        assertEquals("Col: 1, Row: 1", cell.getDataValue());
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 1, false, false));

        // Column position 1 - originally valid
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));
        assertTrue(this.natTable.getActiveCellEditor().validateCanonicalValue(
                cell.getDataValue()));

        // Set an invalid value in cell - AA
        Text textControl = ((Text) this.natTable.getActiveCellEditor()
                .getEditorControl());
        textControl.setText("AA");
        assertEquals("AA", this.natTable.getActiveCellEditor().getCanonicalValue());
        assertFalse(this.natTable.getActiveCellEditor().validateCanonicalValue(
                this.natTable.getActiveCellEditor().getCanonicalValue()));

        // Press tab
        textControl.notifyListeners(SWT.Traverse, SWTUtils.keyEvent(SWT.TAB));
        assertEquals(textControl, this.natTable.getActiveCellEditor()
                .getEditorControl());
        assertEquals("AA", this.natTable.getActiveCellEditor().getCanonicalValue());
    }

    @Test
    public void directlyTypingInACellShoudlStartEditing() throws Exception {
        // Press 'A'
        this.natTable.enableEditingOnAllCells();
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 1, false, false));
        this.natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEventWithChar('A'));

        // Verify edit mode
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals("A", this.natTable.getActiveCellEditor().getCanonicalValue());
    }

    @Test
    public void mustCommitValidValuesOnPressingEnter() throws Exception {
        this.natTable.enableEditingOnAllCells();

        // Cell value is valid if starting with 'C'
        DataLayer bodyDataLayer = (DataLayer) this.gridLayerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, 0, TEST_LABEL);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.DATA_VALIDATOR,
                getStartingWithCValidator(), DisplayMode.EDIT, TEST_LABEL);

        // Enter 'A' in the cell
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 1, false, false));
        this.natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEventWithChar('A'));
        assertEquals("A", this.natTable.getActiveCellEditor().getCanonicalValue());
        assertFalse(this.natTable.getActiveCellEditor().validateCanonicalValue(
                this.natTable.getActiveCellEditor().getCanonicalValue()));

        // Press 'Enter'
        this.natTable.getActiveCellEditor().getEditorControl()
                .notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.CR));

        // Value not committed
        assertNotNull(this.natTable.getActiveCellEditor().getEditorControl());
        assertEquals("A", this.natTable.getActiveCellEditor().getCanonicalValue());

        // Enter a valid value - 'C'
        this.natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEventWithChar('C'));
        assertNotNull(this.natTable.getActiveCellEditor().getEditorControl());
        assertEquals("C", this.natTable.getActiveCellEditor().getCanonicalValue());

        // Press 'Enter' again
        this.natTable.getActiveCellEditor().getEditorControl()
                .notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.CR));

        // Value committed and editor closed
        assertEquals("C", this.natTable.getCellByPosition(1, 1).getDataValue());
        assertNull(this.natTable.getActiveCellEditor());
    }

    @Test
    public void clickingOnTheCheckBoxMustToggleItsValue() throws Exception {
        DefaultGridLayer layerStack = new DefaultGridLayer(
                RowDataListFixture.getList(),
                RowDataListFixture.getPropertyNames(),
                RowDataListFixture.getPropertyToLabelMap());
        this.natTable = new NatTableFixture(layerStack, 1200, 300, false);
        this.natTable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));

        // Enable editing
        this.natTable.enableEditingOnAllCells();

        // Calculate pixel value to click on
        int columnIndex = RowDataListFixture
                .getColumnIndexOfProperty(RowDataListFixture.PUBLISH_FLAG_PROP_NAME);
        int columnPosition = columnIndex + ROW_HEADER_COLUMN_COUNT;
        int startX = this.natTable.getStartXOfColumnPosition(columnPosition);
        int columnWidth = this.natTable.getColumnWidthByPosition(columnPosition);
        int startY = this.natTable.getStartYOfRowPosition(1);
        int rowHeight = this.natTable.getRowHeightByPosition(1);

        // Register check box for the publish flag column
        DataLayer bodyDataLayer = (DataLayer) layerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, columnIndex, TEST_LABEL);
        registerCheckBoxEditor(this.natTable.getConfigRegistry(),
                new CheckBoxPainter(), new CheckBoxCellEditor());

        this.natTable.configure();

        // Value before click
        assertEquals(true, this.natTable.getDataValueByPosition(columnPosition, 1));

        // Click on the check box
        SWTUtils.leftClick(startX + (columnWidth / 2),
                startY + (rowHeight / 2), SWT.NONE, this.natTable);

        // Value After click
        assertEquals(false, this.natTable.getDataValueByPosition(columnPosition, 1));
    }

    @Test
    public void pressingESCMustDiscardTheValueEnteredAndCloseControl()
            throws Exception {
        this.natTable.enableEditingOnAllCells();

        assertEquals("Col: 1, Row: 1", this.natTable.getDataValueByPosition(1, 1));

        // Select cell, press A
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 1, false, false));
        SWTUtils.pressCharKey('A', this.natTable);

        // Verify edit mode
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals("A", this.natTable.getActiveCellEditor().getCanonicalValue());

        // Press ESC
        SWTUtils.pressKeyOnControl(SWT.ESC, this.natTable.getActiveCellEditor()
                .getEditorControl());

        // Verify state
        assertNull(this.natTable.getActiveCellEditor());
        assertEquals("Col: 1, Row: 1", this.natTable.getDataValueByPosition(1, 1));
    }

    @Test
    public void comboBoxShouldCommitWhenAValueIsSelectedByClickingOnIt()
            throws Exception {
        if (SWTUtils.isRunningOnUnix()) {
            return;
        }
        DefaultGridLayer layerStack = new DefaultGridLayer(
                RowDataListFixture.getList(),
                RowDataListFixture.getPropertyNames(),
                RowDataListFixture.getPropertyToLabelMap());
        this.natTable = new NatTableFixture(layerStack, 1200, 300, false);
        this.natTable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));

        // Enable editing
        this.natTable.enableEditingOnAllCells();

        // Calculate pixel value to click on
        int columnIndex = RowDataListFixture
                .getColumnIndexOfProperty(RowDataListFixture.PRICING_TYPE_PROP_NAME);
        int columnPosition = columnIndex + ROW_HEADER_COLUMN_COUNT;
        int rowPosition = 0 + COLUMN_HEADER_ROW_COUNT;
        int startX = this.natTable.getStartXOfColumnPosition(columnPosition);
        int startY = this.natTable.getStartYOfRowPosition(1);

        // Register combo box for the publish flag column
        DataLayer bodyDataLayer = (DataLayer) layerStack.getBodyDataLayer();
        this.natTable.registerLabelOnColumn(bodyDataLayer, columnIndex, TEST_LABEL);
        registerComboBox(
                this.natTable.getConfigRegistry(),
                new ComboBoxPainter(),
                new ComboBoxCellEditor(Arrays.asList(new PricingTypeBean("MN"),
                        new PricingTypeBean("AT"))));

        this.natTable.configure();

        // Original value
        assertTrue(this.natTable.getDataValueByPosition(columnPosition, rowPosition) instanceof PricingTypeBean);
        assertEquals("MN",
                this.natTable.getDataValueByPosition(columnPosition, rowPosition)
                        .toString());

        // Click - expand combo
        SWTUtils.leftClick(startX + 10, startY + 10, SWT.NONE, this.natTable);

        NatCombo combo = (NatCombo) this.natTable.getActiveCellEditor()
                .getEditorControl();
        assertNotNull(combo);
        assertTrue(this.natTable.getActiveCellEditor().getCanonicalValue() instanceof PricingTypeBean);
        assertEquals("MN", this.natTable.getActiveCellEditor().getCanonicalValue()
                .toString());

        // Click - expand select value 'Automatic'
        combo.select(1);
        SWTUtils.leftClickOnCombo(startX + 10, startY + 35, SWT.NONE, combo);

        assertTrue(this.natTable.getDataValueByPosition(columnPosition, rowPosition) instanceof PricingTypeBean);
        assertEquals("AT",
                this.natTable.getDataValueByPosition(columnPosition, rowPosition)
                        .toString());
        assertNull(this.natTable.getActiveCellEditor());
    }

    @Test
    public void openEditorForSpannedCellsShouldOpenInline() throws Exception {
        CompositeLayer layer = new CompositeLayer(1, 1);
        SelectionLayer selectionLayer = new SelectionLayer(
                new SpanningDataLayer(new DummySpanningBodyDataProvider(100,
                        100)));
        layer.setChildLayer(GridRegion.BODY, new ViewportLayer(selectionLayer),
                0, 0);
        this.natTable = new NatTableFixture(layer, 1200, 300, false);

        layer.addConfiguration(new DefaultEditBindings());
        layer.addConfiguration(new DefaultEditConfiguration());

        this.natTable.enableEditingOnAllCells();

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

        this.natTable.configure();

        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 1, false, false));
        this.natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.F2));

        // Verify edit mode
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals("Col: 1, Row: 1", this.natTable.getActiveCellEditor()
                .getCanonicalValue());

        // verify that inline editing is used and not dialog
        assertTrue("No InlineCellEditEvent fired", inlineFired[0]);
    }

    @Test
    public void updateAllUnderlyingCellsIfSpanned() throws Exception {
        CompositeLayer layer = new CompositeLayer(1, 1);
        DummySpanningBodyDataProvider dataProvider = new DummySpanningBodyDataProvider(
                100, 100);
        SelectionLayer selectionLayer = new SelectionLayer(
                new SpanningDataLayer(dataProvider));
        layer.setChildLayer(GridRegion.BODY, new ViewportLayer(selectionLayer),
                0, 0);
        this.natTable = new NatTableFixture(layer, 1200, 300, false);

        layer.addConfiguration(new DefaultEditBindings());
        layer.addConfiguration(new DefaultEditConfiguration());

        this.natTable.enableEditingOnAllCells();

        this.natTable.configure();

        assertEquals("Col: 1, Row: 1", dataProvider.getDataValue(0, 0));
        assertEquals("Col: 1, Row: 2", dataProvider.getDataValue(0, 1));
        assertEquals("Col: 2, Row: 1", dataProvider.getDataValue(1, 0));
        assertEquals("Col: 2, Row: 2", dataProvider.getDataValue(1, 1));

        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 1, false, false));
        this.natTable.notifyListeners(SWT.KeyDown, SWTUtils.keyEventWithChar('C'));

        assertNotNull(this.natTable.getActiveCellEditor());

        this.natTable.getActiveCellEditor().getEditorControl()
                .notifyListeners(SWT.KeyDown, SWTUtils.keyEvent(SWT.CR));

        assertEquals("C", dataProvider.getDataValue(0, 0));
        assertEquals("C", dataProvider.getDataValue(0, 1));
        assertEquals("C", dataProvider.getDataValue(1, 0));
        assertEquals("C", dataProvider.getDataValue(1, 1));
    }

    @Test
    public void moveOriginIntoViewportOnOpenSpannedCellInScrolledState() throws Exception {
        CompositeLayer layer = new CompositeLayer(1, 2);
        SelectionLayer selectionLayer =
                new SelectionLayer(
                        new SpanningDataLayer(
                                new DummySpanningBodyDataProvider(100, 100)));
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        layer.setChildLayer(GridRegion.COLUMN_HEADER, new DataLayer(new IDataProvider() {

            @Override
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            }

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return "header_" + columnIndex;
            }

            @Override
            public int getColumnCount() {
                return selectionLayer.getColumnCount();
            }
        }), 0, 0);
        layer.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);
        this.natTable = new NatTableFixture(layer, 1200, 300, false);
        this.natTable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));

        layer.addConfiguration(new DefaultEditBindings());
        layer.addConfiguration(new DefaultEditConfiguration());
        this.natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        this.natTable.enableEditingOnAllCells();
        this.natTable.configure();

        // scroll down one row
        viewportLayer.setOriginY(20);

        // verify that the first row is not visible in the viewport
        assertEquals(1, viewportLayer.getRowIndexByPosition(0));

        // trigger cell selection via command
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 1, 1, false, false));

        // verify that the first row is visible in the viewport
        assertEquals(0, viewportLayer.getRowIndexByPosition(0));
        assertEquals(0, viewportLayer.getOrigin().getY());

        // try the same with a simulated click
        viewportLayer.setOriginY(20);
        assertEquals(1, viewportLayer.getRowIndexByPosition(0));

        // trigger cell selection via command
        SWTUtils.leftClick(120, 25, 0, this.natTable);

        assertEquals(0, viewportLayer.getRowIndexByPosition(0));
        assertEquals(0, viewportLayer.getOrigin().getY());

        // test the editor borders to be inside the body
        assertNotNull(this.natTable.getActiveCellEditor());
        Control editorControl = this.natTable.getActiveCellEditor().getEditorControl();
        assertEquals(new Rectangle(1, 20, 198, 39), editorControl.getBounds());
    }

    /**
     * Test case that ensures that an editor, which is committed through the
     * {@linkplain EditUtils}, is also removed from the table.
     * <p>
     * Ensures that the backward compatibility is not broken.
     * </p>
     */
    @Test
    public void testEditorRemovedWhenCommitted() {
        this.natTable.enableEditingOnAllCells();

        ILayerCell cell = this.natTable.getCellByPosition(4, 4);
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));

        Text editor = (Text) this.natTable.getActiveCellEditor().getEditorControl();
        editor.setText("A");

        // Close the again
        this.natTable.commitAndCloseActiveCellEditor();

        // check if value is saved and editor is gone
        assertEquals("A", this.natTable.getCellByPosition(4, 4).getDataValue());
        assertNull(this.natTable.getActiveCellEditor());
    }

    /**
     * Test case that ensures that an editor, which is closed via API, is also
     * removed from the table.
     * <p>
     * Ensures that the backward compatibility is not broken.
     * </p>
     */
    @Test
    public void testEditorRemovedWhenClosed() {
        this.natTable.enableEditingOnAllCells();

        ILayerCell cell = this.natTable.getCellByPosition(4, 4);
        this.natTable.doCommand(new EditCellCommand(this.natTable, this.natTable
                .getConfigRegistry(), cell));

        // close the editor
        this.natTable.getActiveCellEditor().close();

        // check if editor is gone
        assertNull(this.natTable.getActiveCellEditor());
    }

    // *** Convenience methods ***.
    // Mostly code from the EditableGridExample.
    // The sane fixtures are used to ensure that the example keeps working
    // without fail

    private static void registerComboBox(IConfigRegistry configRegistry,
            ICellPainter comboBoxCellPainter, ICellEditor comboBoxCellEditor) {
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER, comboBoxCellPainter,
                DisplayMode.NORMAL, TEST_LABEL);
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor,
                DisplayMode.NORMAL, TEST_LABEL);
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor,
                DisplayMode.EDIT, TEST_LABEL);

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                getPricingTypeDisplayConverter(), DisplayMode.NORMAL,
                TEST_LABEL);
    }

    private static void registerCheckBoxEditor(IConfigRegistry configRegistry,
            ICellPainter checkBoxCellPainter, ICellEditor checkBoxCellEditor) {
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER, checkBoxCellPainter,
                DisplayMode.NORMAL, TEST_LABEL);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL,
                TEST_LABEL);
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITOR, checkBoxCellEditor,
                DisplayMode.NORMAL, TEST_LABEL);
    }

    private static IDisplayConverter getPricingTypeDisplayConverter() {
        return new DisplayConverter() {
            @Override
            public Object canonicalToDisplayValue(Object canonicalValue) {
                if (canonicalValue == null) {
                    return null;
                } else {
                    return canonicalValue.toString().equals("MN") ? "Manual"
                            : "Automatic";
                }
            }

            @Override
            public Object displayToCanonicalValue(Object displayValue) {
                return displayValue.toString().equals("Manual") ? new PricingTypeBean(
                        "MN") : new PricingTypeBean("AT");
            }
        };
    }

    private IDataValidator getStartingWithCValidator() {
        return new DataValidator() {
            @Override
            public boolean validate(int columnIndex, int rowIndex,
                    Object newValue) {
                String asString = newValue.toString();
                return asString.startsWith("C");
            }
        };
    }

}
