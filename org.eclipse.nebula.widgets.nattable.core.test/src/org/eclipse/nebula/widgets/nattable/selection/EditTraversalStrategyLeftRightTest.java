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
import static org.junit.Assert.assertNotNull;

import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.command.EditSelectionCommand;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.integration.SWTUtils;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EditTraversalStrategyLeftRightTest {

    private static final String NOT_EDITABLE = "NOT_EDITABLE";

    private DataLayer dataLayer;
    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;
    private NatTableFixture natTable;

    @Before
    public void setUp() {
        this.dataLayer = new DataLayerFixture(10, 10, 100, 20);
        this.selectionLayer = new SelectionLayer(this.dataLayer);
        this.viewportLayer = new ViewportLayer(this.selectionLayer);
        this.viewportLayer.setRegionName(GridRegion.BODY);

        this.viewportLayer.addConfiguration(new DefaultEditBindings());
        this.viewportLayer.addConfiguration(new DefaultEditConfiguration());

        this.natTable = new NatTableFixture(this.viewportLayer);

        this.natTable.enableEditingOnAllCells();
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.NEVER_EDITABLE, DisplayMode.EDIT, NOT_EDITABLE);
        this.natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.OPEN_ADJACENT_EDITOR,
                Boolean.TRUE);

        this.natTable.registerLabelOnColumn(this.dataLayer, 2, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 5, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 6, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 7, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 8, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 9, NOT_EDITABLE);
    }

    @After
    public void cleanUp() {
        this.selectionLayer.clear();
        // since we are not interested in commit operations it is sufficient to
        // close the editor
        this.natTable.getActiveCellEditor().close();
    }

    // move right

    @Test
    public void testOpenAdjacentToRightAxis() {
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

        // traverse to right
        processTab();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverAxis() {
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

        // traverse to right 2 times
        processTab();
        processTab();

        // column 3 since column 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverEndAxis() {
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

        // traverse to right 4 times
        processTab();
        processTab();
        processTab();
        processTab();

        // we expect the traversal stopped at the last editable cell in the
        // current row
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightOneEditableColumnAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one column is editable
        this.natTable.registerLabelOnColumn(this.dataLayer, 0, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 1, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(4, 0);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to right 2 times
        processTab();
        processTab();

        // we expect to stay at the same editable cell
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightAxisCycle() {
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

        // traverse to right
        processTab();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverAxisCycle() {
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

        // traverse to right 2 times
        processTab();
        processTab();

        // column 3 since column 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverEndAxisCycle() {
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

        // traverse to right 4 times
        processTab();
        processTab();
        processTab();
        processTab();

        // we expect the traversal cycled to the beginning of the current row
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightOneEditableColumnAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one column is editable
        this.natTable.registerLabelOnColumn(this.dataLayer, 0, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 1, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(4, 0);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to right 2 times
        processTab();
        processTab();

        // we expect to stay at the same cell
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightTable() {
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

        // traverse to right
        processTab();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverTable() {
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

        // traverse to right 2 times
        processTab();
        processTab();

        // column 3 since column 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverEndTable() {
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

        // traverse to right 4 times
        processTab();
        processTab();
        processTab();
        processTab();

        // we expect the traversal cycled to the beginning of the next row
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightOneEditableColumnTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one column is editable
        this.natTable.registerLabelOnColumn(this.dataLayer, 0, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 1, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(4, 0);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to right 2 times
        processTab();
        processTab();

        // we expect to traverse 2 rows down
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverEndBottomTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(4, 9);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(9, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to right 2 times
        processTab();
        processTab();

        // we expect the traversal stops at last editable cell in table
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(9, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightTableCycle() {
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

        // traverse to right
        processTab();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverTableCycle() {
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

        // traverse to right 2 times
        processTab();
        processTab();

        // column 3 since column 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverEndTableCycle() {
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

        // traverse to right 4 times
        processTab();
        processTab();
        processTab();
        processTab();

        // we expect the traversal cycled to the beginning of the next row
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightOneEditableColumnTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one column is editable
        this.natTable.registerLabelOnColumn(this.dataLayer, 0, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 1, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(4, 0);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to right 2 times
        processTab();
        processTab();

        // we expect to traverse 2 rows down
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToRightStepOverEndBottomTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(4, 9);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(9, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(9, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to right 2 times
        processTab();
        processTab();

        // we expect the traversal started over at table beginning
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    // move left

    @Test
    public void testOpenAdjacentToLeftAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(1, 0);
        assertEquals(1, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left
        processShiftTab();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(3, 0);
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // column 0 since column 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverEndAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(4, 2);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 4 times
        processShiftTab();
        processShiftTab();
        processShiftTab();
        processShiftTab();

        // we expect the traversal stopped at the first editable cell in the
        // current row
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftOneEditableColumnAxis() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one column is editable
        this.natTable.registerLabelOnColumn(this.dataLayer, 0, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 1, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(4, 2);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // we expect to stay at the same editable cell
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(1, 0);
        assertEquals(1, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left
        processShiftTab();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(3, 0);
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // column 0 since column 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverEndAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(3, 2);
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 4 times
        processShiftTab();
        processShiftTab();
        processShiftTab();
        processShiftTab();

        // we expect the traversal cycle so we will be at column 1
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftOneEditableColumnAxisCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one column is editable
        this.natTable.registerLabelOnColumn(this.dataLayer, 0, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 1, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(4, 2);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // we expect to stay at the same cell
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(1, 0);
        assertEquals(1, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left
        processShiftTab();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(3, 0);
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // column 0 since column 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverEndTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(4, 2);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 4 times
        processShiftTab();
        processShiftTab();
        processShiftTab();
        processShiftTab();

        // we expect the traversal stopped at the first editable cell in the
        // current row
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftOneEditableColumnTable() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one column is editable
        this.natTable.registerLabelOnColumn(this.dataLayer, 0, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 1, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(4, 2);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // we expect to traverse 2 rows up
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverStartTopTable() {
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

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // we expect the traversal stops at first editable cell in table
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(1, 0);
        assertEquals(1, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(1, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left
        processShiftTab();

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(3, 0);
        assertEquals(3, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(0, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // column 0 since column 2 is not editable
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(0, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverEndTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // select a cell
        this.selectionLayer.setSelectedCell(4, 2);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 4 times
        processShiftTab();
        processShiftTab();
        processShiftTab();
        processShiftTab();

        // we expect the traversal stopped at the first editable cell in the
        // current row
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(1, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftOneEditableColumnTableCycle() {
        // register axis traversal
        this.viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(this.selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, this.natTable)));

        // ensure only one column is editable
        this.natTable.registerLabelOnColumn(this.dataLayer, 0, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 1, NOT_EDITABLE);
        this.natTable.registerLabelOnColumn(this.dataLayer, 3, NOT_EDITABLE);

        // select a cell
        this.selectionLayer.setSelectedCell(4, 2);
        assertEquals(4, this.selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(2, this.selectionLayer.getLastSelectedCell().getRowPosition());

        // open editor
        this.natTable.doCommand(new EditSelectionCommand(this.natTable, this.natTable.getConfigRegistry()));

        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(2, this.natTable.getActiveCellEditor().getRowIndex());

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // we expect to traverse 2 rows up
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(4, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(0, this.natTable.getActiveCellEditor().getRowIndex());
    }

    @Test
    public void testOpenAdjacentToLeftStepOverStartTopTableCycle() {
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

        // traverse to left 2 times
        processShiftTab();
        processShiftTab();

        // we expect the traversal started over at table beginning
        assertNotNull(this.natTable.getActiveCellEditor());
        assertEquals(3, this.natTable.getActiveCellEditor().getColumnIndex());
        assertEquals(9, this.natTable.getActiveCellEditor().getRowIndex());
    }

    private void processTab() {
        Text textControl = ((Text) this.natTable.getActiveCellEditor().getEditorControl());
        textControl.notifyListeners(SWT.Traverse, SWTUtils.keyEvent(SWT.TAB));
    }

    private void processShiftTab() {
        Text textControl = ((Text) this.natTable.getActiveCellEditor().getEditorControl());
        textControl.notifyListeners(SWT.Traverse, SWTUtils.keyEventWithModifier(SWT.TAB, SWT.SHIFT));
    }
}
