/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.group.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.NumberValues;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.command.ViewportSelectRowGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.config.DefaultRowGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.preserve.PreserveSelectionModel;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.DataProviderFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RowGroupHeaderLayerSelectionTest {

    public static final String TEST_GROUP_NAME_3 = "testGroupName3";
    public static final String TEST_GROUP_NAME_2 = "testGroupName2";
    public static final String TEST_GROUP_NAME_1 = "testGroupName";
    public static final String NO_GROUP_NAME = "";
    public RowGroupHeaderLayer rowGroupLayer;
    private GridLayer gridLayer;
    private DefaultBodyLayerStack bodyLayer;

    private IRowDataProvider<NumberValues> dataProvider;

    private LayerListenerFixture layerListener;

    @BeforeEach
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

        this.bodyLayer = new DefaultBodyLayerStack(new DataLayer(this.dataProvider));
        SelectionLayer selectionLayer = this.bodyLayer.getSelectionLayer();

        // Column header
        ILayer columnHeaderLayer = new ColumnHeaderLayer(
                new DefaultColumnHeaderDataLayer(new DataProviderFixture(10, 1)), this.bodyLayer, selectionLayer);

        // Row header
        ILayer rowHeaderLayer = new RowHeaderLayer(
                new DefaultRowHeaderDataLayer(new DataProviderFixture(1, 5)), this.bodyLayer, selectionLayer);

        this.rowGroupLayer = new RowGroupHeaderLayer(
                rowHeaderLayer,
                selectionLayer,
                false);

        this.rowGroupLayer.addConfiguration(new DefaultRowGroupHeaderLayerConfiguration(true));

        this.rowGroupLayer.addGroup(TEST_GROUP_NAME_1, 0, 3);
        this.rowGroupLayer.addGroup(TEST_GROUP_NAME_2, 5, 2);
        this.rowGroupLayer.addGroup(TEST_GROUP_NAME_3, 8, 2);

        // Corner
        ILayer cornerLayer = new CornerLayer(
                new DataLayer(new DataProviderFixture(1, 1)), rowHeaderLayer, columnHeaderLayer);

        this.gridLayer = new GridLayer(this.bodyLayer, columnHeaderLayer, this.rowGroupLayer, cornerLayer);
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 700, 140);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.layerListener = new LayerListenerFixture();
        this.gridLayer.getBodyLayer().addLayerListener(this.layerListener);
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
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, false, false));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        assertEquals(1, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(RowSelectionEvent.class));

        RowSelectionEvent event = (RowSelectionEvent) this.layerListener.getReceivedEvent(RowSelectionEvent.class);
        Collection<Range> columnPositionRanges = event.getRowPositionRanges();
        assertEquals(1, columnPositionRanges.size());
        assertEquals(new Range(0, 3), columnPositionRanges.iterator().next());
    }

    @Test
    public void shouldDeselectAndSelectAllCellsInGroup() {
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, false, false));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, false, false));

        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));
    }

    @Test
    public void shouldSelectAllCellsInGroupWithCtrl() {
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, false, false));

        assertEquals(1, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(RowSelectionEvent.class));

        RowSelectionEvent event = (RowSelectionEvent) this.layerListener.getReceivedEvent(RowSelectionEvent.class);
        Collection<Range> rowPositionRanges = event.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(0, 3), rowPositionRanges.iterator().next());

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, false, true));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(3));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(4));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));
    }

    @Test
    public void shouldSelectAllCellsInGroupsToRightWithShift() {
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, false, false));

        assertEquals(1, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(RowSelectionEvent.class));

        RowSelectionEvent event = (RowSelectionEvent) this.layerListener.getReceivedEvent(RowSelectionEvent.class);
        Collection<Range> rowPositionRanges = event.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(0, 3), rowPositionRanges.iterator().next());

        this.layerListener.clearReceivedEvents();

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(3));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(4));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, true, false));

        assertEquals(1, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(RowSelectionEvent.class));

        event = (RowSelectionEvent) this.layerListener.getReceivedEvent(RowSelectionEvent.class);
        rowPositionRanges = event.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(0, 7), rowPositionRanges.iterator().next());

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(3));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(4));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));
    }

    @Test
    public void shouldSelectAllCellsInGroupsToLeftWithShift() {
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, false, false));

        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(3));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(4));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, true, false));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(3));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(4));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        // since the selection with shift is calculated from the anchor
        // position, column 6 is now not selected anymore
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));
    }

    @Test
    public void shouldMoveAnchorOnDeselectWithCtrl() {
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, false, false));

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, false, true));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));

        PositionCoordinate selectionAnchor = this.bodyLayer.getSelectionLayer().getSelectionAnchor();
        assertEquals(5, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, false, true));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));

        selectionAnchor = this.bodyLayer.getSelectionLayer().getSelectionAnchor();
        assertEquals(2, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());
    }

    @Test
    public void shouldSelectInColumnScrolledState() {
        assertEquals(0, this.bodyLayer.getViewportLayer().getColumnIndexByPosition(0));

        // scroll right
        this.bodyLayer.getViewportLayer().moveColumnPositionIntoViewport(9);

        assertEquals(3, this.bodyLayer.getViewportLayer().getColumnIndexByPosition(0));

        // trigger row group selection
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, false, false));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        // no scrolling expected
        assertEquals(3, this.bodyLayer.getViewportLayer().getColumnIndexByPosition(0));

        PositionCoordinate selectionAnchor = this.bodyLayer.getSelectionLayer().getSelectionAnchor();
        assertEquals(0, selectionAnchor.getRowPosition());
        assertEquals(3, selectionAnchor.getColumnPosition());
    }

    @Test
    public void shouldSelectRowInScrolledState() {
        assertEquals(0, this.bodyLayer.getViewportLayer().getRowIndexByPosition(0));

        // scroll down
        this.bodyLayer.getViewportLayer().moveRowPositionIntoViewport(7);

        assertEquals(2, this.bodyLayer.getViewportLayer().getRowIndexByPosition(0));

        // trigger row group selection
        ILayerCell cell = this.gridLayer.getCellByPosition(0, 1);
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(
                this.gridLayer,
                cell.getRowPosition(),
                cell.getOriginRowPosition(),
                cell.getRowSpan(), false, false));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        // no scrolling expected
        assertEquals(2, this.bodyLayer.getViewportLayer().getRowIndexByPosition(0));

        PositionCoordinate selectionAnchor = this.bodyLayer.getSelectionLayer().getSelectionAnchor();
        assertEquals(2, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());
    }

    @Test
    public void shouldSelectAllCellsInGroupsDownWithShiftInScrolledState() {
        assertEquals(0, this.bodyLayer.getViewportLayer().getColumnIndexByPosition(0));

        // scroll to right
        this.bodyLayer.getViewportLayer().moveColumnPositionIntoViewport(9);

        assertEquals(3, this.bodyLayer.getViewportLayer().getColumnIndexByPosition(0));

        // trigger row group selection
        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, false, false));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(3));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(4));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));

        // no scrolling expected
        assertEquals(3, this.bodyLayer.getViewportLayer().getColumnIndexByPosition(0));

        PositionCoordinate selectionAnchor = this.bodyLayer.getSelectionLayer().getSelectionAnchor();
        assertEquals(0, selectionAnchor.getRowPosition());
        assertEquals(3, selectionAnchor.getColumnPosition());

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, true, false));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(3));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(4));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));

        // no scrolling expected
        assertEquals(3, this.bodyLayer.getViewportLayer().getColumnIndexByPosition(0));

        selectionAnchor = this.bodyLayer.getSelectionLayer().getSelectionAnchor();
        assertEquals(0, selectionAnchor.getRowPosition());
        assertEquals(3, selectionAnchor.getColumnPosition());
    }

    @Test
    public void shouldMoveAnchorOnDeselectWithCtrlWithPreserve() {
        this.bodyLayer.getSelectionLayer().setSelectionModel(
                new PreserveSelectionModel<>(this.bodyLayer.getSelectionLayer(), this.dataProvider, new IRowIdAccessor<NumberValues>() {

                    @Override
                    public Serializable getRowId(NumberValues rowObject) {
                        return rowObject.getColumnOneNumber();
                    }
                }));

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 1, 1, 3, false, false));

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, false, true));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));

        PositionCoordinate selectionAnchor = this.bodyLayer.getSelectionLayer().getSelectionAnchor();
        assertEquals(5, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());

        this.gridLayer.doCommand(new ViewportSelectRowGroupCommand(this.gridLayer, 6, 6, 2, false, true));

        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(0));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(1));
        assertTrue(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(2));

        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(5));
        assertFalse(this.bodyLayer.getSelectionLayer().isRowPositionFullySelected(6));

        selectionAnchor = this.bodyLayer.getSelectionLayer().getSelectionAnchor();
        assertEquals(2, selectionAnchor.getRowPosition());
        assertEquals(0, selectionAnchor.getColumnPosition());
    }
}
