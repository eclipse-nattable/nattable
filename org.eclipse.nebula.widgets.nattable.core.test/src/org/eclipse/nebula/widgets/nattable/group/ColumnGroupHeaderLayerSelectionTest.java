/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.NumberValues;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.group.command.ViewportSelectColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ViewportSelectColumnGroupCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.preserve.PreserveSelectionModel;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupHeaderLayerSelectionTest {

    public static final String TEST_GROUP_NAME_3 = "testGroupName3";
    public static final String TEST_GROUP_NAME_2 = "testGroupName2";
    public static final String TEST_GROUP_NAME_1 = "testGroupName";
    public static final String NO_GROUP_NAME = "";
    public ColumnGroupHeaderLayer columnGroupLayer;
    private ColumnGroupModel model;
    private DefaultGridLayer gridLayer;

    private IRowDataProvider<NumberValues> dataProvider;

    @Before
    public void setup() {
        this.dataProvider = new ListDataProvider<>(
                getNumberValues(),
                new ReflectiveColumnPropertyAccessor<>(
                        "columnOneNumber",
                        "columnTwoNumber",
                        "columnThreeNumber",
                        "columnFourNumber",
                        "columnFiveNumber",
                        "columnSixNumber",
                        "columnSevenNumber",
                        "columnEightNumber",
                        "columnNineNumber",
                        "columnTenNumber"));
        this.gridLayer = new GridLayerFixture(this.dataProvider);
        this.model = new ColumnGroupModel();
        // 10 columns in header
        this.columnGroupLayer = new ColumnGroupHeaderLayer(
                this.gridLayer.getColumnHeaderLayer(),
                this.gridLayer.getBodyLayer().getSelectionLayer(),
                this.model,
                false);

        this.columnGroupLayer.addConfiguration(new DefaultColumnGroupHeaderLayerConfiguration(this.model, true));
        this.gridLayer.getBodyLayer().getViewportLayer().registerCommandHandler(
                new ViewportSelectColumnGroupCommandHandler(this.gridLayer.getBodyLayer().getViewportLayer(), this.columnGroupLayer));

        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_1, 0, 1, 2);
        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 5, 6);
        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_3, 8, 9);

        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 200);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

    private List<NumberValues> getNumberValues() {
        List<NumberValues> result = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            NumberValues value = new NumberValues();
            value.setColumnOneNumber(i * 10);
            value.setColumnTwoNumber(i * 10 + 1);
            value.setColumnThreeNumber(i * 10 + 2);
            value.setColumnFourNumber(i * 10 + 3);
            value.setColumnFiveNumber(i * 10 + 4);
            value.setColumnSixNumber(i * 10 + 5);
            value.setColumnSevenNumber(i * 10 + 6);
            value.setColumnEightNumber(i * 10 + 7);
            value.setColumnNineNumber(i * 10 + 8);
            value.setColumnTenNumber(i * 10 + 9);
            result.add(value);
        }
        return result;
    }

    @Test
    public void shouldSelectAllCellsInGroup() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
    }

    @Test
    public void shouldDeselectAndSelectAllCellsInGroup() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, false));

        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));
    }

    @Test
    public void shouldSelectAllCellsInGroupWithCtrl() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, true));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(3));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(4));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));
    }

    @Test
    public void shouldSelectAllCellsInGroupsToRightWithShift() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(3));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(4));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, true, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(3));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(4));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));
    }

    @Test
    public void shouldSelectAllCellsInGroupsToLeftWithShift() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, false));

        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(3));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(4));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, true, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(3));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(4));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        // since the selection with shift is calculated from the anchor
        // position, column 6 is now not selected anymore
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));
    }

    @Test
    public void shouldMoveAnchorOnDeselectWithCtrl() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, true));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));

        PositionCoordinate selectionAnchor = this.gridLayer.getBodyLayer().getSelectionLayer().getSelectionAnchor();
        assertEquals(0, selectionAnchor.getRowPosition());
        assertEquals(5, selectionAnchor.getColumnPosition());

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, true));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));

        selectionAnchor = this.gridLayer.getBodyLayer().getSelectionLayer().getSelectionAnchor();
        assertEquals(0, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());
    }

    @Test
    public void shouldSelectInScrolledState() {
        assertEquals(0, this.gridLayer.getBodyLayer().getViewportLayer().getRowIndexByPosition(0));

        // scroll down
        this.gridLayer.getBodyLayer().getViewportLayer().moveRowPositionIntoViewport(20);

        assertEquals(12, this.gridLayer.getBodyLayer().getViewportLayer().getRowIndexByPosition(0));

        // trigger column group selection
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        // no scrolling expected
        assertEquals(12, this.gridLayer.getBodyLayer().getViewportLayer().getRowIndexByPosition(0));

        PositionCoordinate selectionAnchor = this.gridLayer.getBodyLayer().getSelectionLayer().getSelectionAnchor();
        assertEquals(12, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());
    }

    @Test
    public void shouldSelectAllCellsInGroupsToRightWithShiftInScrolledState() {
        assertEquals(0, this.gridLayer.getBodyLayer().getViewportLayer().getRowIndexByPosition(0));

        // scroll down
        this.gridLayer.getBodyLayer().getViewportLayer().moveRowPositionIntoViewport(20);

        assertEquals(12, this.gridLayer.getBodyLayer().getViewportLayer().getRowIndexByPosition(0));

        // trigger column group selection
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(3));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(4));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));

        // no scrolling expected
        assertEquals(12, this.gridLayer.getBodyLayer().getViewportLayer().getRowIndexByPosition(0));

        PositionCoordinate selectionAnchor = this.gridLayer.getBodyLayer().getSelectionLayer().getSelectionAnchor();
        assertEquals(12, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, true, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(3));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(4));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));

        // no scrolling expected
        assertEquals(12, this.gridLayer.getBodyLayer().getViewportLayer().getRowIndexByPosition(0));

        selectionAnchor = this.gridLayer.getBodyLayer().getSelectionLayer().getSelectionAnchor();
        assertEquals(12, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());
    }

    @Test
    public void shouldMoveAnchorOnDeselectWithCtrlWithPreserve() {
        this.gridLayer.getBodyLayer().getSelectionLayer().setSelectionModel(
                new PreserveSelectionModel<>(this.gridLayer.getBodyLayer().getSelectionLayer(), this.dataProvider, new IRowIdAccessor<NumberValues>() {

                    @Override
                    public Serializable getRowId(NumberValues rowObject) {
                        return rowObject.getColumnOneNumber();
                    }
                }));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, true));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));

        PositionCoordinate selectionAnchor = this.gridLayer.getBodyLayer().getSelectionLayer().getSelectionAnchor();
        assertEquals(0, selectionAnchor.getRowPosition());
        assertEquals(5, selectionAnchor.getColumnPosition());

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, true));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));

        selectionAnchor = this.gridLayer.getBodyLayer().getSelectionLayer().getSelectionAnchor();
        assertEquals(0, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());
    }
}
