/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezePositionCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeSelectionCommand;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class ResizeColumnHideShowLayerIntegrationTest {

    private IRowDataProvider<Person> bodyDataProvider =
            new ListDataProvider<>(
                    PersonService.getPersons(10),
                    new ReflectiveColumnPropertyAccessor<Person>(
                            new String[] { "firstName", "lastName", "gender", "married", "birthday" }));
    private DataLayer bodyDataLayer;
    private ColumnReorderLayer reorderLayer;
    private RowHideShowLayer rowHideShowLayer;
    private ResizeColumnHideShowLayer columnHideShowLayer;
    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;
    private FreezeLayer freezeLayer;
    private CompositeFreezeLayer compositeFreezeLayer;

    @Before
    public void setup() {
        this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
        this.bodyDataLayer.setColumnPercentageSizing(true);
        this.reorderLayer = new ColumnReorderLayer(this.bodyDataLayer);
        this.rowHideShowLayer = new RowHideShowLayer(this.reorderLayer);
        this.columnHideShowLayer = new ResizeColumnHideShowLayer(this.rowHideShowLayer, this.bodyDataLayer);
        this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
        this.viewportLayer = new ViewportLayer(this.selectionLayer);
        this.freezeLayer = new FreezeLayer(this.selectionLayer);

        this.compositeFreezeLayer = new CompositeFreezeLayer(
                this.freezeLayer, this.viewportLayer, this.selectionLayer);
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 500, 150);
            }

        });
        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 500, 150));
        this.rowHideShowLayer.doCommand(cmd);

    }

    @Test
    public void testNotFrozen() {
        assertEquals(0, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(5, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(0, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testFreezeColumns() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testFreezeHideFrozenFirst() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 0));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(125, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());

        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testFreezeHideFrozenOnEdge() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(125, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());

        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testFreezeHideNonFrozenAny() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 3));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(250, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());

        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testFreezeHideNonFrozenOnEdge() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 2));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(250, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());

        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testFreezeHideBothSidesOfFreezeBorder() {
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        this.compositeFreezeLayer.doCommand(
                new MultiColumnHideCommand(this.compositeFreezeLayer, 1, 2));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(167, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());

        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testHideLeftmostColumnsAndFreezeColumn() {
        // hide first column
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 0));

        // freeze first visible column
        // note FreezeColumnCommand is inclusive and
        // first column exists with size == 0
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(125, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());

        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testHideLeftmostColumnsAndFreezeSelection() {
        // hide first column
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 0));

        // select the first visible cell
        // note first column exists with size == 0
        this.compositeFreezeLayer.doCommand(
                new SelectCellCommand(this.compositeFreezeLayer, 1, 0, false, false));

        // freeze selection
        this.compositeFreezeLayer.doCommand(
                new FreezeSelectionCommand(false, false, true));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(7, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(125, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());

        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(7, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testHideLeftmostColumnsAndFreezePosition() {
        // hide first column
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 0));

        // freeze position
        // note FreezeColumnCommand is exclusive and
        // first column exists with size == 0
        this.compositeFreezeLayer.doCommand(
                new FreezePositionCommand(this.compositeFreezeLayer, 2, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(7, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(125, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());

        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(7, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(20, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testHideAllNonFrozen() {
        this.bodyDataLayer.setColumnPercentageSizing(false);

        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide all non frozen columns
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 4));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 3));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 2));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(200, this.compositeFreezeLayer.getWidth());

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());
    }

    @Test
    public void testHideAndShowAllNonFrozenAndOneFrozenConsecutive() {
        this.bodyDataLayer.setColumnPercentageSizing(false);

        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide all non frozen and one frozen column
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 4));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 3));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 2));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(100, this.compositeFreezeLayer.getWidth());

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testHideAllNonFrozenAndOneFrozenNotConsecutive() {
        this.bodyDataLayer.setColumnPercentageSizing(false);

        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide all non frozen and one frozen column
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 4));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 3));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 2));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 0));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(100, this.compositeFreezeLayer.getWidth());

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }

    @Test
    public void testHideAllNonFrozenAndOneFrozenNotConsecutiveMultiShow() {
        this.bodyDataLayer.setColumnPercentageSizing(false);

        // freeze
        this.compositeFreezeLayer.doCommand(
                new FreezeColumnCommand(this.compositeFreezeLayer, 1));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // hide all non frozen and one frozen column
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 4));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 3));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 2));
        this.compositeFreezeLayer.doCommand(
                new ColumnHideCommand(this.compositeFreezeLayer, 0));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(100, this.compositeFreezeLayer.getWidth());

        assertEquals(0, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        // show again
        this.compositeFreezeLayer.doCommand(new MultiColumnShowCommand(Arrays.asList(0, 2, 3, 4, 5)));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(0, this.freezeLayer.getRowCount());
        assertEquals(1, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(-1, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(8, this.viewportLayer.getRowCount());
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(0, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(200, this.viewportLayer.getMinimumOrigin().getX());
        assertEquals(0, this.viewportLayer.getMinimumOrigin().getY());

        assertEquals(500, this.compositeFreezeLayer.getWidth());
    }
}
