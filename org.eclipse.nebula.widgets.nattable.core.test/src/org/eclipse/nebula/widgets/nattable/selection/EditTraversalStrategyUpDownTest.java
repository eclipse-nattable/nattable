/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DefaultDataValidator;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.command.EditSelectionCommand;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.RowOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.integration.SWTUtils;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EditTraversalStrategyUpDownTest {

    private static final String NOT_EDITABLE = "NOT_EDITABLE";

    private DataLayer dataLayer;
    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;
    private NatTableFixture natTable;

    private RowOverrideLabelAccumulator<RowDataFixture> overrider;

    @Before
    public void setUp() {
        // only use 10 columns to make the test cases easier
        String[] propertyNames = Arrays.copyOfRange(RowDataListFixture.getPropertyNames(), 0, 10);

        IRowDataProvider<RowDataFixture> bodyDataProvider = new ListDataProvider<>(
                RowDataListFixture.getList(10),
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(propertyNames));

        this.dataLayer = new DataLayer(bodyDataProvider, 20, 20);
        this.selectionLayer = new SelectionLayer(this.dataLayer);
        this.viewportLayer = new ViewportLayer(this.selectionLayer);
        this.viewportLayer.setRegionName(GridRegion.BODY);

        this.viewportLayer.addConfiguration(new DefaultEditBindings());
        this.viewportLayer.addConfiguration(new DefaultEditConfiguration() {
            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        new TextCellEditor(true, true));
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.DATA_VALIDATOR,
                        new DefaultDataValidator());
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        new DefaultBooleanDisplayConverter(),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 9);
            }

        });

        this.natTable = new NatTableFixture(this.viewportLayer);

        this.natTable.enableEditingOnAllCells();
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.NEVER_EDITABLE,
                DisplayMode.EDIT,
                NOT_EDITABLE);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.OPEN_ADJACENT_EDITOR,
                Boolean.TRUE);

        // register non editable rows
        this.overrider = new RowOverrideLabelAccumulator<>(bodyDataProvider, new IRowIdAccessor<RowDataFixture>() {

            @Override
            public Serializable getRowId(RowDataFixture rowObject) {
                return rowObject.getSecurity_id();
            }

        });
        this.overrider.registerRowOverrides(2, NOT_EDITABLE);
        this.overrider.registerRowOverrides(5, NOT_EDITABLE);
        this.overrider.registerRowOverrides(6, NOT_EDITABLE);
        this.overrider.registerRowOverrides(7, NOT_EDITABLE);
        this.overrider.registerRowOverrides(8, NOT_EDITABLE);
        this.overrider.registerRowOverrides(9, NOT_EDITABLE);

        AggregateConfigLabelAccumulator accumulator = new AggregateConfigLabelAccumulator();
        accumulator.add(this.overrider);
        accumulator.add(new ColumnLabelAccumulator());
        this.dataLayer.setConfigLabelAccumulator(accumulator);
    }

    @After
    public void cleanUp() {
        this.selectionLayer.clear();
        // since we are not interested in commit operations it is sufficient to
        // close the editor
        if (this.natTable.getActiveCellEditor() != null) {
            this.natTable.getActiveCellEditor().close();
        }
    }

    // move down

    @Test
    public void testOpenAdjacentDownAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down
        processEnter();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // row 3 since row 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverEndAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 4 times
        processEnter();
        processEnter();
        processEnter();
        processEnter();

        // we expect the traversal stopped at the last editable cell in the
        // current column
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownOneEditableColumnAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one row is editable
        this.overrider.registerRowOverrides(0, NOT_EDITABLE);
        this.overrider.registerRowOverrides(1, NOT_EDITABLE);
        this.overrider.registerRowOverrides(3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(0, 4);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // we expect to stay at the same editable cell
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down
        processEnter();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // row 3 since row 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverEndAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 4 times
        processEnter();
        processEnter();
        processEnter();
        processEnter();

        // we expect the traversal cycled to the beginning of the current column
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownOneEditableColumnAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one row is editable
        this.overrider.registerRowOverrides(0, NOT_EDITABLE);
        this.overrider.registerRowOverrides(1, NOT_EDITABLE);
        this.overrider.registerRowOverrides(3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(0, 4);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // we expect to stay at the same cell
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down
        processEnter();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // row 3 since row 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverEndTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 4 times
        processEnter();
        processEnter();
        processEnter();
        processEnter();

        // we expect the traversal cycled to the beginning of the next column
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownOneEditableColumnTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one row is editable
        this.overrider.registerRowOverrides(0, NOT_EDITABLE);
        this.overrider.registerRowOverrides(1, NOT_EDITABLE);
        this.overrider.registerRowOverrides(3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(0, 4);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // we expect to traverse 2 columns right
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverEndBottomTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(9, 4);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(9, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // we expect the traversal stops at last editable cell in table
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(9, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down
        processEnter();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // row 3 since row 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverEndTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 4 times
        processEnter();
        processEnter();
        processEnter();
        processEnter();

        // we expect the traversal cycled to the beginning of the next row
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownOneEditableColumnTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one row is editable
        this.overrider.registerRowOverrides(0, NOT_EDITABLE);
        this.overrider.registerRowOverrides(1, NOT_EDITABLE);
        this.overrider.registerRowOverrides(3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(0, 4);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // we expect to traverse 2 columns right
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentDownStepOverEndBottomTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(9, 4);
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(9, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse down 2 times
        processEnter();
        processEnter();

        // we expect the traversal started over at table beginning
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    // move up

    @Test
    public void testOpenAdjacentUpAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 1);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(1, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left
        processShiftEnter();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 3);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // row 0 since row 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverEndAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(2, 4);
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 4 times
        processShiftEnter();
        processShiftEnter();
        processShiftEnter();
        processShiftEnter();

        // we expect the traversal stopped at the first editable cell in the
        // current column
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpOneEditableColumnAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one row is editable
        this.overrider.registerRowOverrides(0, NOT_EDITABLE);
        this.overrider.registerRowOverrides(1, NOT_EDITABLE);
        this.overrider.registerRowOverrides(3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(2, 4);
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // we expect to stay at the same editable cell
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 1);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(1, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up
        processShiftEnter();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 3);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // row 0 since row 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverEndAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(2, 3);
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 4 times
        processShiftEnter();
        processShiftEnter();
        processShiftEnter();
        processShiftEnter();

        // we expect the traversal cycle so we will be at row 1
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpOneEditableColumnAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one row is editable
        this.overrider.registerRowOverrides(0, NOT_EDITABLE);
        this.overrider.registerRowOverrides(1, NOT_EDITABLE);
        this.overrider.registerRowOverrides(3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(2, 4);
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // we expect to stay at the same cell
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 1);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(1, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up
        processShiftEnter();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 3);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // row 0 since row 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverEndTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(2, 4);
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 4 times
        processShiftEnter();
        processShiftEnter();
        processShiftEnter();
        processShiftEnter();

        // we expect the traversal stopped at the first editable cell in the
        // current column
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpOneEditableColumnTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one row is editable
        this.overrider.registerRowOverrides(0, NOT_EDITABLE);
        this.overrider.registerRowOverrides(1, NOT_EDITABLE);
        this.overrider.registerRowOverrides(3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(2, 4);
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // we expect to traverse 2 rows up
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverStartTopTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // we expect the traversal stops at first editable cell in table
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 1);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(1, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up
        processShiftEnter();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 3);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // row 0 since row 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverEndTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(2, 4);
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 4 times
        processShiftEnter();
        processShiftEnter();
        processShiftEnter();
        processShiftEnter();

        // we expect the traversal stopped at the first editable cell in the
        // previous column
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpOneEditableColumnTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one row is editable
        this.overrider.registerRowOverrides(0, NOT_EDITABLE);
        this.overrider.registerRowOverrides(1, NOT_EDITABLE);
        this.overrider.registerRowOverrides(3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(2, 4);
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(2, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // we expect to traverse 2 columns left
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(4, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentUpStepOverStartTopTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(0, 0);
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse up 2 times
        processShiftEnter();
        processShiftEnter();

        // we expect the traversal started over at table beginning
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(9, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(3, this.natTable.getActiveCellEditor().getRowIndex());
    }

    private void processEnter() {
        Text textControl = ((Text) this.natTable.getActiveCellEditor().getEditorControl());
        SWTUtils.pressKeyOnControl(SWT.CR, textControl);
    }

    private void processShiftEnter() {
        Text textControl = ((Text) this.natTable.getActiveCellEditor().getEditorControl());
        SWTUtils.pressKeyOnControl(SWT.CR, SWT.SHIFT, textControl);
        // wait shortly to ensure that the event can be processed
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
